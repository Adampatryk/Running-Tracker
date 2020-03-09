package com.example.bop;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RecordFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {
	private static final String TAG = "RecordFragment";

	private Button button_start_tracking;
	private Button button_choose_sport;

	private MapView mapView;
	private GoogleMap googleMap;
	private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
	public static boolean HAS_LOCATION_PERMISSION = false;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		//Inflate the fragment view to the container it belongs to
		View v = inflater.inflate(R.layout.fragment_record, container, false);

		button_choose_sport = v.findViewById(R.id.button_choose_sport);
		button_start_tracking = v.findViewById(R.id.button_start_tracking);

		//When the start tracking activity is pressed, the service is started to track GPS location
		button_start_tracking.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (getContext() != null){

					//Attempt by checking if location setting is turned on
					attemptStartTrackingService();

					Log.d(TAG, "onClick: Started LocationService");
				} 
				else{
					Log.d(TAG, "onClick: Cannot start service, getContext() returned null");
				}
			}
		});

		Bundle mapViewBundle = null;
		if (savedInstanceState != null) {
			mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
		}

		mapView = v.findViewById(R.id.map);
		mapView.onCreate(mapViewBundle);
		mapView.getMapAsync(this); //CAUSES CRASH WHEN LOCATION PERMISSION NOT ON

		return v;
	}

	private void attemptStartTrackingService(){
		LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setInterval(10000);
		locationRequest.setFastestInterval(5000);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(locationRequest);

		SettingsClient client = LocationServices.getSettingsClient(getContext());
		Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

		task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
			@Override
			public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
				// All location settings are satisfied
				startTrackingActivity();
			}
		});

		task.addOnFailureListener(getActivity(), new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				promptToChangeLocationSettings(e);
			}
		});
	}

	public void startTrackingActivity(){
		Toast.makeText(getContext(), "Starting Service", Toast.LENGTH_SHORT).show();
		Intent activityIntent = new Intent(getContext(), TrackingActivity.class);
		startActivity(activityIntent);

		Intent serviceIntent = new Intent(getContext(), LocationService.class);
		getContext().startService(serviceIntent);

	}

	private void promptToChangeLocationSettings(Exception e){
		if (e instanceof ResolvableApiException) {
			// Location settings are not satisfied, but this can be fixed
			// by showing the user a dialog.
			try {
				// Show the dialog by calling startResolutionForResult(),
				// and check the result in onActivityResult().
				ResolvableApiException resolvable = (ResolvableApiException) e;
				resolvable.startResolutionForResult(getActivity(), MainActivity.REQUEST_CHECK_SETTINGS);
			} catch (IntentSender.SendIntentException sendEx) {
				// Ignore the error.
			}
		}
	}

	@Override
	public void onMapReady(final GoogleMap googleMap) {
		this.googleMap = googleMap;
		googleMap.setMyLocationEnabled(true);
		googleMap.setOnMyLocationButtonClickListener(this);
		googleMap.setOnMyLocationClickListener(this);

		FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(getActivity());

		fusedLocationProviderClient.getLastLocation()
				.addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
					@Override
					public void onSuccess(Location location) {
						// Got last known location. In some rare situations this can be null.
						if (location != null) {
							LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
							CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
							googleMap.moveCamera(cameraUpdate);
						}
					}
				});
	}

	@Override
	public boolean onMyLocationButtonClick() {
		return false;
	}

	@Override
	public void onMyLocationClick(@NonNull Location location) {
		Toast.makeText(getActivity(), "Your Location: " + location.toString(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);

		Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
		if (mapViewBundle == null) {
			mapViewBundle = new Bundle();
			outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
		}

		mapView.onSaveInstanceState(mapViewBundle);
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
		mapView.onStart();
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		mapView.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}
}
