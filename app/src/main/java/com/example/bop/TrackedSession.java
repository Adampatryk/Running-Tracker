package com.example.bop;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;

public class TrackedSession {

	private static final String TAG = "TrackedSession";

	//Tracked session details
	private String title = "";
	private String description = "";
	private float distance = 0;
	private float avgSpeed = 0;
	private float avgSpeedFromSUVAT = 0;
	private long timeElapsedBetweenStartStops = 0;
	private float timeElapsedBetweenTrkPoints = 0;
	private float elevation = 0;
	private Date timeCreated = null; //The time at which the session was first started
	private Date timeStarted = null; //The time at which, most recently, the session was started/resumed
	private Date timeStopped = null;
	private ArrayList<Location> trkPoints = new ArrayList<>();
	private Bitmap image;
	private int rating = 5;

	private float totalSpeedForRunningAverage = 0;
	private int totalTrkPointsWithSpeedForRunningAverage = 0;

	//Boolean for whether the session is paused or not
	private boolean paused = false;

	public TrackedSession() {
	}

	//Logic for adding a track point and updating the maths for calculations
	public void addTrackPoint(Location trkPoint) {
		if (!paused) {
			if (trkPoints.isEmpty()) {
				avgSpeed = trkPoint.getSpeed();
			} else {
				Location lastTrkPoint = trkPoints.get(trkPoints.size() - 1);
				distance += trkPoint.distanceTo(lastTrkPoint);
				Log.d(TAG, "addTrackPoint Distance: " + trkPoint.distanceTo(lastTrkPoint));
				elevation += Math.abs(lastTrkPoint.getAltitude() - trkPoint.getAltitude());
				timeElapsedBetweenTrkPoints += Math.abs(lastTrkPoint.getTime() - trkPoint.getTime());
				avgSpeedFromSUVAT = ((distance / (float) 1000) / (getCurrentTimeMillis() / (float) 3600000));
			}

			if (trkPoint.getSpeed() != 0) {
				totalSpeedForRunningAverage += trkPoint.getSpeed();
				Log.d(TAG, "addTrackPoint Speed: " + trkPoint.getSpeed());
				totalTrkPointsWithSpeedForRunningAverage += 1;
				avgSpeed = totalSpeedForRunningAverage / totalTrkPointsWithSpeedForRunningAverage;
			}
		} else {
			paused = false;
			Log.d(TAG, "addTrackPoint: Unpaused");
		}

		trkPoints.add(trkPoint);
	}

	//Gets all the track points for the tracked session
	public ArrayList<Location> getTrkPoints() {
		return trkPoints;
	}

	public boolean isCreated() {
		return !(timeCreated == null);
	}

	//When a tracked session is first started, give it a created and started date
	public void startTrackingActivity() {
		timeCreated = new Date(System.currentTimeMillis());
		timeStarted = new Date(System.currentTimeMillis());
	}

	//Resume tracking, if the session has been created then update the timestarted
	public void resumeTrackingActivity() {
		if (timeCreated == null) {
			Log.d(TAG, "resumeTrackingActivity: No tracking activity to resume: " +
					"must call startTrackingActivity first.");
		} else {
			timeStarted = new Date(System.currentTimeMillis());
		}
	}

	//Called when the user hits the STOP button, this calculates the duration of the activity
	//calculates the average speed based on distance and time, and prevents the future track
	//point from changing the statistics on distance, time, speed etc.
	public void stopTrackingActivity() {


		//Get a time for when this tracked activity was stopped
		timeStopped = new Date(System.currentTimeMillis());

		//Find the difference in time between starting and stopping the activity
		timeElapsedBetweenStartStops += timeStopped.getTime() - timeStarted.getTime();

		//Calculate the average speed based on the distance and the time between stopping and starting
		//the session
		avgSpeedFromSUVAT = distance / (timeElapsedBetweenStartStops / 1000);

		//Pause to prevent stats being calculated on the next track point
		paused = true;
	}

	//Get all the trackpoints as a string for debugging
	public String getTrkPointsAsString() {
		String trkPointStr = "";

		int id = 0;
		for (Location location : trkPoints) {
			trkPointStr = trkPointStr.concat("Location id=" + (id++) + location.toString() + "\n");
		}

		return trkPointStr;
	}

	//Get the current time of the session
	public long getCurrentTimeMillis() {
		//If paused it is the latest added up time between stops and starts
		if (paused) {
			return timeElapsedBetweenStartStops;
		}
		//If not paused then must add the current time to the latest started time
		else {
			return timeElapsedBetweenStartStops + (System.currentTimeMillis() - timeStarted.getTime());
		}
	}

	//Getters and setters

	int getRating() {
		return rating;
	}

	void setRating(int rating) {
		this.rating = rating;
	}

	boolean isPaused() {
		return this.paused;
	}

	void setImage(Bitmap bitmap) {
		this.image = bitmap;
	}

	Bitmap getImage() {
		return this.image;
	}

	void setTitle(String title) {
		this.title = title;
	}

	String getTitle() {
		return title;
	}

	void setDescription(String description) {
		this.description = description;
	}

	String getDescription() {
		return description;
	}

	String getTimeString() {
		long timeInMilliseconds = getCurrentTimeMillis();
		return timeToString(timeInMilliseconds);
	}

	//Static helper method to format the string nicely depending on what the time is
	static String timeToString(long timeInMilliseconds) {
		long timeSwapBuff = 0L;
		long updateTime = timeSwapBuff + timeInMilliseconds;
		int secs = (int) (updateTime / 1000);
		int mins = secs / 60;
		secs %= 60;
		int hrs = mins / 60;
		mins %= 60;
		int milliseconds = (int) timeInMilliseconds % 1000;
		int centisecs = milliseconds / 10;

		//The following conditionals mean that the values that are there are printed
		if (hrs == 0) {
			if (mins == 0) {
				return String.format(Locale.UK, "%d.%02ds", secs, centisecs);
			} else {
				return String.format(Locale.UK, "%d:%02d.%02ds", mins, secs, centisecs);
			}
		}

		return String.format(Locale.UK, "%d:%02d:%02d.%02ds", hrs, mins, secs, centisecs);

	}

	public float getDistance() {
		return distance;
	}

	String getDistanceString() {
		return distanceToString(distance, true);
	}

	//Static helper method to format the distance nicely based on what the distance is
	// e.g. only print metres until the value is above a km
	static String distanceToString(float distance, boolean labelled) {
		int metres = Math.round(distance);
		int km = metres / 1000;
		metres = metres % 1000;
		String format = "%d";

		if (km == 0) {
			if (labelled) {
				format += "m";
			}
			return String.format(Locale.UK, format, metres);
		}

		format = "%d.%03d";
		if (labelled) {
			format += "km";
		}
		return String.format(Locale.UK, format, km, metres);
	}

	public float getAvgSpeed() {
		return avgSpeedFromSUVAT;
	}

	String getAvgSpeedString() {
		return String.format(Locale.UK, "%.2f", avgSpeed);
	}

	public float getElevation() {
		return elevation;
	}

	String getElevationString() {
		return String.format(Locale.UK, "%.1f", elevation);
	}

	Date getTimeCreated() {
		return timeCreated;
	}

	//To string method for debugging the session
	@NonNull
	public String toString() {
		String distanceStr = "Distance: " + distance + "m";
		String avgSpeedStr = "Average Speed (Measured): " + avgSpeed + "m/s";
		String avgSpeedFromSUVATStr = "Average Speed (Calculated): " + avgSpeedFromSUVAT + "m/s";
		String elevationStr = "Elevation: " + elevation + "m";
		String durationStr = "Duration Between Track Points: " + timeElapsedBetweenTrkPoints / 1000 + "s";
		String timeCreatedStr = "Start Time: " + timeCreated.toString();
		String timeStoppedStr = "Stop Time: " + timeStopped.toString();
		String durationElapsedStr = "Duration Between Start and Stop: " + timeElapsedBetweenStartStops / 1000 + "s";
		String trkPointsSizeStr = "Track Points Stored: " + trkPoints.size();
		String trkPointStr = getTrkPointsAsString();

		return distanceStr + "\n" +
				avgSpeedStr + "\n" +
				avgSpeedFromSUVATStr + "\n" +
				elevationStr + "\n" +
				durationStr + "\n" +
				timeCreatedStr + "\n" +
				timeStoppedStr + "\n" +
				durationElapsedStr + "\n" +
				trkPointsSizeStr + "\n\n" +
				"TRACK POINTS: \n" +
				trkPointStr;
	}
}
