package com.example.bop;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.core.app.NotificationCompat;

import static com.example.bop.App.CHANNEL_ID;

//This service deals with the tracking of a session, activities bind to this to control what happens
//such as pausing and unpausing. This is done with the additional helper class of a TrackedSession
//which abstracts all the details about a session into a class which this class manipulates and controls
public class LocationService extends Service {
	private static final String TAG = "LocationService";

	//Allows activities to control the service
	private final IBinder binder = new LocationServiceBinder();

	private FusedLocationProviderClient fusedLocationProviderClient;
	private LocationRequest locationRequest;
	private LocationCallback locationCallback;
	//Holds all the details about the tracked session
	private TrackedSession trackedSession = new TrackedSession();


	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate: called");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		//Start the service as a foreground service with a notification
		startForeground(1, createNotification());
		Log.d(TAG, "onStartCommand: Started service in foreground");

		return super.onStartCommand(intent, flags, startId);
	}

	//Defines the communication functions
	public class LocationServiceBinder extends Binder {

		//Check if the session is currently paused
		boolean isPaused() {
			return trackedSession.isPaused();
		}

		//Continue / or start tracking
		void resumeTracking() {
			resumeLocationTracking();
		}

		//Pause
		void stopTracking() {
			stopLocationTracking();
		}

		//Getters and setters

		//Get the tracked session
		TrackedSession getTrackedActivity() {
			return trackedSession;
		}

		//Get all the track points
		ArrayList<Location> getTrkPoints() {
			return trackedSession.getTrkPoints();
		}

		//Set the tracked session rating for persistence
		void setRating(int rating) {
			trackedSession.setRating(rating);
		}

		//Get the tracked session rating
		int getRating() {
			return trackedSession.getRating();
		}

		//Set the bitmap image to the tracked session for persistence
		void setImage(Bitmap bitmap) {
			trackedSession.setImage(bitmap);
		}

		//Get the bitmap image of the tracked session
		Bitmap getImage() {
			return trackedSession.getImage();
		}

		//Set the title of the tracked session for persistence
		void setTitle(String title) {
			trackedSession.setTitle(title);
		}

		//Get the title of the tracked session
		String getTitle() {
			return trackedSession.getTitle();
		}

		//Set description for the tracked session for persistence
		void setDescription(String description) {
			trackedSession.setDescription(description);
		}

		//Get the description of the tracked session
		String getDescription() {
			return trackedSession.getDescription();
		}

		//Get the millisecond time that this was created
		Date getTimeCreated() {
			return trackedSession.getTimeCreated();
		}

		//Get the duration of the tracked session
		long getDuration() {
			return trackedSession.getCurrentTimeMillis();
		}

		//Get the duration as a string ready for printing
		String getTimeString() {
			return trackedSession.getTimeString();
		}

		//Get the distance of the tracked session
		float getDistance() {
			return trackedSession.getDistance();
		}

		//Get the distance as a string ready for printing
		String getDistanceString() {
			return trackedSession.getDistanceString();
		}

		//Get the average speed of the tracked session
		float getAvgSpeed() {
			return trackedSession.getAvgSpeed();
		}

		//Get the average speed as a string
		String getAvgSpeedString() {
			return trackedSession.getAvgSpeedString();
		}

		//Get the elevation
		float getElevation() {
			return trackedSession.getElevation();
		}

		//Get the elevation as a string ready for priting
		String getElevationString() {
			return trackedSession.getElevationString();
		}
	}

	//Start the location requests
	private void startLocationTracking() {
		//If a tracked session already exists just resume it
		if (trackedSession.isCreated()) {
			resumeLocationTracking();
			Log.d(TAG, "startLocationTracking: Tracked Activity has already been started, resuming...");
		} else {
			//Set up location requests
			fusedLocationProviderClient = new FusedLocationProviderClient(this);

			locationRequest = LocationRequest.create();
			locationRequest.setInterval(10000);
			locationRequest.setFastestInterval(5000);
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

			locationCallback = new LocationCallback() {
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

			//Set the tracked session to start
			trackedSession.startTrackingActivity();
			fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

			Log.d(TAG, "startLocationTracking: requested location updates");
		}

	}

	private void resumeLocationTracking() {
		if (trackedSession.isCreated()) {
			//If the tracked session is paused, unpause it, otherwise do nothing
			if (trackedSession.isPaused()) {
				trackedSession.resumeTrackingActivity();
				fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
			}
			Log.d(TAG, "resumeLocationTracking: resume requested location updates");
		}
		//If a tracked session didn't already exist with location tracking, a new one is started
		else {
			startLocationTracking();
			Log.d(TAG, "resumeLocationTracking: No Tracked Activity started, starting a new one...");
		}
	}

	//Pauses the tracking and removes the location updates
	private void stopLocationTracking() {
		trackedSession.stopTrackingActivity();
		fusedLocationProviderClient.removeLocationUpdates(locationCallback);

		Log.d(TAG, "stopLocationTracking: removed location updates");
	}

	//Creates the notification that allows the user to come back to the tracking screen
	private Notification createNotification() {
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
		//Toast.makeText(this, "Service Stopped!", Toast.LENGTH_SHORT).show();
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
