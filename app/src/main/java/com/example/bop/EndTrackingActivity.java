package com.example.bop;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class EndTrackingActivity  extends FragmentActivity implements OnMapReadyCallback {
	private static final String TAG = "EndTrackingActivity";

	TextView timeTextView, distanceTextView, avgSpeedTextView, elevationTextView;

	GoogleMap map;
	Boolean mapReady = false, isBound = false;

	LocationService.LocationServiceBinder locationServiceBinder = null;

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected: ");
			locationServiceBinder = (LocationService.LocationServiceBinder) service;
			isBound = true;
			updateStats();
			plotTrackPoints();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected: ");
			locationServiceBinder = null;
			isBound = false;
		}
	};

	void updateStats(){
		//Set Text Field Values
		timeTextView.setText(locationServiceBinder.getTimeString());
		distanceTextView.setText(locationServiceBinder.getDistanceString());
		avgSpeedTextView.setText(locationServiceBinder.getAvgSpeedString());
		elevationTextView.setText(locationServiceBinder.getElevationString());
	}

	void plotTrackPoints(){
		//Plot Track Points If Bound and MapReady

		if (! (mapReady & isBound)) {return;}

		Log.d(TAG, "plotTrackPoints: plotting points");
		ArrayList<Location> trkPoints = locationServiceBinder.getTrkPoints();

		PolylineOptions polylineOptions = new PolylineOptions();

		//Add track points to polyline
		for (Location trkPoint : trkPoints){
			LatLng latLng = new LatLng(trkPoint.getLatitude(), trkPoint.getLongitude());
			polylineOptions.add(latLng);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
		}


		//Add polyline to map
		map.addPolyline(polylineOptions);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_end_tracking);

		Intent intent = new Intent(this, LocationService.class);

		timeTextView = findViewById(R.id.text_view_time);
		distanceTextView = findViewById(R.id.text_view_distance);
		avgSpeedTextView = findViewById(R.id.text_view_speed);
		elevationTextView = findViewById(R.id.text_view_elevation);

		getApplicationContext().bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);

		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mapReady = true;
		map = googleMap;
		plotTrackPoints();
	}

	public void onResumeTrackingPressed(View v){
		LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setInterval(10000);
		locationRequest.setFastestInterval(5000);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(locationRequest);

		SettingsClient client = LocationServices.getSettingsClient(this);
		Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

		task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
			@Override
			public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
				// All location settings are satisfied. The client can initialize
				// location requests here.
				// ...
				resumeTracking();
			}
		});

		task.addOnFailureListener(this, new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				promptToChangeLocationSettings(e);
			}
		});

	}

	private void promptToChangeLocationSettings(Exception e){
		if (e instanceof ResolvableApiException) {
			// Location settings are not satisfied, but this can be fixed by showing the user a dialog.
			try {
				// Show the dialog by calling startResolutionForResult() and check the result in onActivityResult().
				ResolvableApiException resolvable = (ResolvableApiException) e;
				resolvable.startResolutionForResult(this, MainActivity.REQUEST_CHECK_SETTINGS);
			} catch (IntentSender.SendIntentException sendEx) {
				// Ignore the error.
			}
		}
	}

	public void resumeTracking(){
		Intent intent = new Intent(this, TrackingActivity.class);
		startActivity(intent);

		Log.d(TAG, "onResumeTracking: ");
	}

	public void onEndTrackingPressed(View v){
		endTracking();
	}

	public void endTracking(){
		if (isBound) {
			locationServiceBinder.endTracking();
			getApplicationContext().unbindService(serviceConnection);
			isBound = false;
		}

		Intent intent = new Intent(this, SaveTrackedActivity.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		endTracking();
	}

	@Override
	protected void onDestroy() {
		if (isBound) {
			getApplicationContext().unbindService(serviceConnection);
			isBound = false;
		}
		super.onDestroy();
	}
}
