package com.example.bop;

import com.example.bop.R;
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

//This is the activity that is the user sees when the session is being tracked
public class TrackingActivity extends AppCompatActivity {

	private static final String TAG = "TrackingActivity";
	LocationService.LocationServiceBinder locationServiceBinder = null;
	TextView timeTextView, distanceTextView, avgSpeedTextView, elevationTextView;
	int activityTypeInd;
	Handler updateTimeHandler = new Handler();
	Handler updateStatsHandler = new Handler();
	boolean stopped = false, isBound = false;

	//This thread updates the timer as often as it can to give smooth counting effect
	Runnable updateTimerThread = new Runnable() {
		@Override
		public void run() {
			String timeString = locationServiceBinder.getTimeString();
			timeTextView.setText(timeString);

			if (!stopped) updateTimeHandler.postDelayed(this, 0);
		}
	};

	//This thread checks and updates the distance and average speed every second
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
				if (!stopped) {
					stopTracking();
					Toast.makeText(context, "Location must be on to track session", Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	//Holds the connection to the service
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected: ");
			isBound = true;
			locationServiceBinder = (LocationService.LocationServiceBinder) service;
			locationServiceBinder.resumeTracking();

			//Set the activity type
			locationServiceBinder.setActivityType(activityTypeInd);

			//When the service is connected, make sure the receiver is listening for location setting changes
			registerReceiver(locationSettingStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

			//Start the update threads
			updateTimeHandler.postDelayed(updateTimerThread, 0);
			updateStatsHandler.postDelayed(updateStatsThread, 0);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected: ");
			locationServiceBinder = null;
			isBound = false;
			//Broadcast receiver not needed when location service is not getting location updates
			unregisterReceiver(locationSettingStateReceiver);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracking);

		//If the activity type was sent, assign it
		Bundle bundle = getIntent().getExtras();

		if (bundle != null && !bundle.isEmpty()){
			activityTypeInd = bundle.getInt(BopProviderContract.ACTIVITY_ACTIVITY_TYPE);
		}

		//Start the location tracking service
		Intent intent = new Intent(this, LocationService.class);
		getApplicationContext().bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);

		//Find text views
		timeTextView = findViewById(R.id.text_view_time);
		distanceTextView = findViewById(R.id.text_view_distance);
		avgSpeedTextView = findViewById(R.id.text_view_speed);
		elevationTextView = findViewById(R.id.text_view_elevation);
	}

	//When the user clicks the stop button
	public void onStopTrackingPressed(View v) {
		stopTracking();
	}

	//To resume the tracking
	@Override
	protected void onResume() {
		super.onResume();
		stopped = false;
	}

	//To stop the tracking safely must unbind to prevent leaks
	public void stopTracking() {
		stopped = true;
		if (isBound) {
			locationServiceBinder.stopTracking();
			getApplicationContext().unbindService(serviceConnection);
			isBound = false;
		}

		Intent intent = new Intent(this, EndTrackingActivity.class);
		startActivity(intent);
	}

	//When back button is pressed, interpret that as a pause
	@Override
	public void onBackPressed() {
		stopTracking();
	}

	//Unbind also on destroy to prevent leaks
	@Override
	protected void onDestroy() {
		if (isBound) {
			getApplicationContext().unbindService(serviceConnection);
			isBound = false;
		}
		super.onDestroy();
	}
}
