package com.example.bop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class TrackingActivity extends AppCompatActivity {

	private static final String TAG = "TrackingActivity";
	LocationService.LocationServiceBinder locationServiceBinder = null;
	TextView timeTextView, distanceTextView, avgSpeedTextView, elevationTextView;
	Handler updateTimeHandler = new Handler();
	Handler updateStatsHandler = new Handler();
	boolean stopped;

	Runnable updateTimerThread = new Runnable() {
		@Override
		public void run() {

			timeTextView.setText(locationServiceBinder.getTimeString());

			if (!stopped) updateTimeHandler.postDelayed(this, 0);
		}
	};

	Runnable updateStatsThread = new Runnable() {
		@Override
		public void run() {
			//Set the distance
			distanceTextView.setText(locationServiceBinder.getDistanceString());

			//Set the average speed
			avgSpeedTextView.setText(locationServiceBinder.getAvgSpeedString());

			//Set the elevation
			elevationTextView.setText(locationServiceBinder.getElevationString());

			if (!stopped) updateStatsHandler.postDelayed(this, 1000);
		}
	};

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected: ");
			locationServiceBinder = (LocationService.LocationServiceBinder) service;
			locationServiceBinder.startTracking();

			//startTime = locationServiceBinder.getCurrentTimeMillis();

			updateTimeHandler.postDelayed(updateTimerThread, 0);
			updateStatsHandler.postDelayed(updateStatsThread, 0);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected: ");
			locationServiceBinder = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracking);

		Intent intent = new Intent(this, LocationService.class);
		this.bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);

		timeTextView = findViewById(R.id.text_view_time);
		distanceTextView = findViewById(R.id.text_view_distance);
		avgSpeedTextView = findViewById(R.id.text_view_speed);
		elevationTextView = findViewById(R.id.text_view_elevation);

		stopped = false;
	}

	public void onStopTrackingPressed(View v){
		locationServiceBinder.stopTracking();
		Intent intent = new Intent(this, EndTrackingActivity.class);
		startActivity(intent);
		//stopTracking();
		stopped = true;
	}

	public void stopTracking(){
		locationServiceBinder.stopTracking();

		Intent intent = new Intent(this, EndTrackingActivity.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		stopTracking();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
