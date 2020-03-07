package com.example.bop;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.core.app.NotificationCompat;

import static com.example.bop.App.CHANNEL_ID;

public class LocationService extends Service {
	private static final String TAG = "LocationService";

	private final IBinder binder = new LocationServiceBinder();

	private FusedLocationProviderClient fusedLocationProviderClient;
	private LocationRequest locationRequest;
	private LocationCallback locationCallback;
	private TrackedSession trackedSession = new TrackedSession();


	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate: called");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "Service Started!", Toast.LENGTH_SHORT).show();

		//Start the service as a foreground service with a notification
		startForeground(1, createNotification());
		Log.d(TAG, "onStartCommand: Started service in foreground");

		return super.onStartCommand(intent, flags, startId);
	}

	public class LocationServiceBinder extends Binder {
		void startTracking(){
			startLocationTracking();
		}

		void resumeTracking(){
			resumeLocationTracking();
		}

		void stopTracking(){
			stopLocationTracking();
		}

		ArrayList<Location> getTrkPoints(){
			return trackedSession.getTrkPoints();
		}

		TrackedSession getTrackedActivity(){
			return trackedSession;
		}

		void endTracking(){

			Log.d(TAG, "endTracking: Tracked Activity toString(): \n" + trackedSession.toString());
		}

		//Getters and setters

		void setTitle(String title) { trackedSession.setTitle(title); }

		String getTitle(){return trackedSession.getTitle();}

		void setDescription(String description){ trackedSession.setDescription(description);}

		String getDescription(){return trackedSession.getDescription();}

		Date getTimeCreated(){return trackedSession.getTimeCreated();}

		long getDuration(){return trackedSession.getCurrentTimeMillis();}

		String getTimeString() {return trackedSession.getTimeString();}

		float getDistance() {return trackedSession.getDistance();}

		String getDistanceString() {return trackedSession.getDistanceString();}

		float getAvgSpeed() {return trackedSession.getAvgSpeed();}

		String getAvgSpeedString(){return trackedSession.getAvgSpeedString();}

		float getElevation() {return trackedSession.getElevation();}

		String getElevationString(){return trackedSession.getElevationString(); }
	}

	private void startLocationTracking(){
		if (trackedSession.isCreated()) {
			resumeLocationTracking();
			Log.d(TAG, "startLocationTracking: Tracked Activity has already been started, resuming...");
		} else {
			fusedLocationProviderClient = new FusedLocationProviderClient(this);

			locationRequest = LocationRequest.create();
			locationRequest.setInterval(10000);
			locationRequest.setFastestInterval(5000);
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

			locationCallback = new LocationCallback(){
				@Override
				public void onLocationResult(LocationResult locationResult) {
					if (locationResult == null) {
						return;
					}
					for (Location location : locationResult.getLocations()) {
						Log.d(TAG, "onLocationResult: Location: " + location);
						trackedSession.addTrackPoint(location);
					}
				}
			};

			trackedSession.startTrackingActivity();
			fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

			Log.d(TAG, "startLocationTracking: requested location updates");
		}
		
	}

	private void resumeLocationTracking(){
		if (trackedSession.isCreated()) {
			trackedSession.resumeTrackingActivity();
			fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
			Log.d(TAG, "resumeLocationTracking: resume requested location updates");
		} else {
			startLocationTracking();
			Log.d(TAG, "resumeLocationTracking: No Tracked Activity started, starting a new one...");
		}


	}

	private void stopLocationTracking(){
		trackedSession.stopTrackingActivity();
		fusedLocationProviderClient.removeLocationUpdates(locationCallback);

		Log.d(TAG, "stopLocationTracking: removed location updates");
	}

	private Notification createNotification(){
		//Take the user to the Tracking Activity when clicking on the notification
		Intent notificationIntent = new Intent(this, TrackingActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		//Build the notification
		Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setContentTitle("Bop")
				.setContentText("Tracking Run...")
				.setSmallIcon(R.drawable.ic_runner)
				.setContentIntent(pendingIntent)
				.build();

		return notification;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "Service Stopped!", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
}
