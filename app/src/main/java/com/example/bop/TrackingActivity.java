package com.example.bop;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class TrackingActivity extends AppCompatActivity {

	private static final String TAG = "TrackingActivity";
	LocationService.LocationServiceBinder locationServiceBinder = null;
	TextView timeTextView, distanceTextView, avgSpeedTextView, elevationTextView;
	Handler updateTimeHandler = new Handler();
	Handler updateStatsHandler = new Handler();
	boolean stopped = false, isBound = false;

	Runnable updateTimerThread = new Runnable() {
		@Override
		public void run() {
			String timeString = locationServiceBinder.getTimeString();
			timeTextView.setText(timeString);

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

			if (!stopped) updateStatsHandler.postDelayed(this, 1000);
		}
	};

	//Following broadcast receiver is to listen to the location setting toggle state
	private BroadcastReceiver locationSettingStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
				Toast.makeText(context, "YOU HAVE SWITCHED YOUR SETTING", Toast.LENGTH_SHORT).show();
				if (!stopped) {
					stopTracking();
				}
			}
		}
	};

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected: ");
			isBound = true;
			locationServiceBinder = (LocationService.LocationServiceBinder) service;
//			if (locationServiceBinder.isPaused()) {
//
//			}
			locationServiceBinder.resumeTracking();
			registerReceiver(locationSettingStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
			updateTimeHandler.postDelayed(updateTimerThread, 0);
			updateStatsHandler.postDelayed(updateStatsThread, 0);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected: ");
			locationServiceBinder = null;
			isBound = false;
			unregisterReceiver(locationSettingStateReceiver);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracking);

		Intent intent = new Intent(this, LocationService.class);
		getApplicationContext().bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);

		timeTextView = findViewById(R.id.text_view_time);
		distanceTextView = findViewById(R.id.text_view_distance);
		avgSpeedTextView = findViewById(R.id.text_view_speed);
		elevationTextView = findViewById(R.id.text_view_elevation);
	}

	public void onStopTrackingPressed(View v){
		stopTracking();
	}

	@Override
	protected void onResume() {
		super.onResume();
		stopped = false;
	}

	public void stopTracking(){
		stopped = true;
		if (isBound) {
			locationServiceBinder.stopTracking();
			getApplicationContext().unbindService(serviceConnection);
			isBound = false;
		}

		Intent intent = new Intent(this, EndTrackingActivity.class);
		startActivity(intent);
	}


	@Override
	public void onBackPressed() {
		stopTracking();
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
