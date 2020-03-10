package com.example.bop;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

	private TextView text_view_sessions_count,
			text_view_total_distance,
			text_view_total_time,
			text_view_longest_distance,
			text_view_longest_time,
			text_view_avg_distance,
			text_view_avg_time;

	private Button button_week, button_month, button_all_time;
	private Button activeButton;

	private String dateSelection = null;
	private String thisWeekString, thisMonthString, allTimeString;

	public void onWeekButtonPressed(View v){
		dateSelection =  thisWeekString;
		selectButton(button_week);
	}

	public void onMonthButtonPressed(View v){
		dateSelection = thisMonthString;
		selectButton(button_month);
	}

	public void onAllTimeButtonPressed(View v){
		dateSelection = allTimeString;
		selectButton(button_all_time);
	}

	private void createDateSelectionStrings(){

		allTimeString = null;

		// get today and clear time of day
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);

		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		thisWeekString = BopProviderContract.ACTIVITY_DATETIME + " > " + cal.getTimeInMillis();

		// get start of the month
		cal.set(Calendar.DAY_OF_MONTH, 1);
		thisMonthString = BopProviderContract.ACTIVITY_DATETIME + " > " + cal.getTimeInMillis();

	}

	private void selectButton(Button selectedButton){
		button_week.setBackgroundColor(Color.WHITE);
		button_week.setTextColor(getResources().getColor(R.color.colorPrimary));

		button_month.setBackgroundColor(Color.WHITE);
		button_month.setTextColor(getResources().getColor(R.color.colorPrimary));

		button_all_time.setBackgroundColor(Color.WHITE);
		button_all_time.setTextColor(getResources().getColor(R.color.colorPrimary));

		activeButton = selectedButton;
		activeButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
		activeButton.setTextColor(Color.WHITE);

		setViewStats();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_profile, container, false);

		//Find buttons
		button_week = v.findViewById(R.id.button_week);
		button_month = v.findViewById(R.id.button_month);
		button_all_time = v.findViewById(R.id.all_time);

		//Set button onclick listeners
		button_week.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onWeekButtonPressed(view);
			}
		});

		button_month.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onMonthButtonPressed(view);
			}
		});

		button_all_time.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onAllTimeButtonPressed(view);
			}
		});

		//Find text views
		text_view_sessions_count = v.findViewById(R.id.text_view_profile_sessions_count);
		text_view_total_distance = v.findViewById(R.id.text_view_profile_total_distance);
		text_view_total_time = v.findViewById(R.id.text_view_profile_total_time);
		text_view_longest_distance = v.findViewById(R.id.text_view_profile_longest_distance);
		text_view_longest_time = v.findViewById(R.id.text_view_profile_longest_time);
		text_view_avg_distance = v.findViewById(R.id.text_view_profile_avg_distance);
		text_view_avg_time = v.findViewById(R.id.text_view_profile_avg_time);

		createDateSelectionStrings();

		//Simulate the week button being pressed
		onWeekButtonPressed(button_week);

		return v;
	}

	public void setViewStats(){
		//Prepare strings
		String sessions_count,
				total_distance,
				total_time,
				longest_distance,
				longest_time,
				average_distance,
				average_time;

		//Cursor to be reused
		Cursor c;

		//Query to get the count of the sessions
		c = getContext().getContentResolver().query(BopProviderContract.SESSIONS_COUNT_URI,
				null, dateSelection, null, null);
		c.moveToFirst();
		sessions_count = c.getString(c.getColumnIndexOrThrow(BopProviderContract.SESSIONS_COUNT));

		//If no sessions came through, stop querying
		if (sessions_count.equals("0")){
			total_distance = "0m";
			total_time = "0s";
			longest_distance = "0m";
			longest_time = "0s";
			average_distance = "0m";
			average_time = "0s";
		}
		else {
			//Query to get the total distance across all sessions
			c = getContext().getContentResolver().query(BopProviderContract.TOTAL_DISTANCE_URI,
					null, dateSelection, null, null);
			c.moveToFirst();
			total_distance = TrackedSession.distanceToString(Float.parseFloat(c.getString(c.getColumnIndexOrThrow(BopProviderContract.TOTAL_DISTANCE))), true);

			//Query to get the total time across all sessions
			c = getContext().getContentResolver().query(BopProviderContract.TOTAL_TIME_URI,
					null, dateSelection, null, null);
			c.moveToFirst();
			total_time = TrackedSession.timeToString(Long.parseLong(c.getString(c.getColumnIndexOrThrow(BopProviderContract.TOTAL_TIME))));

			//Query to get the longest session distance
			c = getContext().getContentResolver().query(BopProviderContract.LONGEST_DISTANCE_URI,
					null, dateSelection, null, null);
			c.moveToFirst();
			longest_distance = TrackedSession.distanceToString(Float.parseFloat(c.getString(c.getColumnIndexOrThrow(BopProviderContract.LONGEST_DISTANCE))), true);

			//Query to get the longest session time across all sessions
			c = getContext().getContentResolver().query(BopProviderContract.LONGEST_TIME_URI,
					null, dateSelection, null, null);
			c.moveToFirst();
			longest_time = TrackedSession.timeToString(Long.parseLong(c.getString(c.getColumnIndexOrThrow(BopProviderContract.LONGEST_TIME))));

			//Query to get the average distance across all sessions
			c = getContext().getContentResolver().query(BopProviderContract.AVERAGE_DISTANCE_URI,
					null, dateSelection, null, null);
			c.moveToFirst();
			average_distance = TrackedSession.distanceToString(Float.parseFloat(c.getString(c.getColumnIndexOrThrow(BopProviderContract.AVERAGE_DISTANCE))), true);

			//Query to get the average time across all sessions
			c = getContext().getContentResolver().query(BopProviderContract.AVERAGE_TIME_URI,
					null, dateSelection, null, null);
			c.moveToFirst();
			average_time = TrackedSession.timeToString((long) Float.parseFloat(c.getString(c.getColumnIndexOrThrow(BopProviderContract.AVERAGE_TIME))));
		}

		//Close and finish with cursor
		c.close();

		//Set text view text values
		text_view_sessions_count.setText(sessions_count);
		text_view_total_distance.setText(total_distance);
		text_view_total_time.setText(total_time);
		text_view_longest_distance.setText(longest_distance);
		text_view_longest_time.setText(longest_time);
		text_view_avg_distance.setText(average_distance);
		text_view_avg_time.setText(average_time);
	}
}
