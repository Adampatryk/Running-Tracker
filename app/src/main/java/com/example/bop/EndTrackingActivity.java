package com.example.bop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class EndTrackingActivity  extends FragmentActivity implements OnMapReadyCallback {
	private static final String TAG = "EndTrackingActivity";

	TextView timeTextView, distanceTextView, avgSpeedTextView, elevationTextView;

	GoogleMap map;
	Boolean mapReady = false, bindReady = false;

	LocationService.LocationServiceBinder locationServiceBinder = null;

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected: ");
			locationServiceBinder = (LocationService.LocationServiceBinder) service;
			bindReady = true;
			updateStats();
			plotTrackPoints();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected: ");
			locationServiceBinder = null;
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

		if (! (mapReady & bindReady)) {return;}

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

		this.bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);

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
		resumeTracking();
	}

	public void resumeTracking(){
		Intent intent = new Intent(this, TrackingActivity.class);
		startActivity(intent);

//		finish();
		Log.d(TAG, "onResumeTracking: ");
	}

	public void onEndTrackingPressed(View v){
		endTracking();
	}

	public void endTracking(){
		locationServiceBinder.endTracking();

		Intent intent = new Intent(this, SaveTrackedActivity.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		endTracking();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
