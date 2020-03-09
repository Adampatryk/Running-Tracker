package com.example.bop;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class SessionDetailsActivity extends AppCompatActivity {

	int session_id;
	TextView session_title, session_description, session_distance, session_time;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_session_details);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle("");

		setSupportActionBar(toolbar);
		//getSupportActionBar().setTitle("");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		session_id = Integer.parseInt(getIntent().getExtras().getString("id"));

		session_title = findViewById(R.id.text_view_session_title);
		session_description = findViewById(R.id.text_view_session_description);
		session_distance = findViewById(R.id.text_view_session_distance);
		session_time = findViewById(R.id.text_view_session_time);

		querySession(session_id);
	}

	public void querySession(int id){

		ArrayList<String> recordData = new ArrayList<>();

		Cursor cursor = getContentResolver().query(BopProviderContract.ACTIVITY_URI,
				null, BopProviderContract.ACTIVITY_ID + "=?", new String[]{""+id}, null);

		if (cursor.moveToFirst())
		{
			do
			{
				if (recordData.isEmpty()){
					recordData.add("" + id);
					recordData.add(cursor.getString(1)); // Title
					recordData.add(cursor.getString(2)); // Datetime
					recordData.add(cursor.getString(3)); // Description
					recordData.add(cursor.getString(4)); // Rating
					recordData.add(cursor.getString(5)); // Activity type
					recordData.add(cursor.getString(6)); // Duration
					recordData.add(cursor.getString(7)); // Avg Speed
					recordData.add(cursor.getString(8)); // Rating
					recordData.add(cursor.getString(9)); // Distance
					recordData.add(cursor.getString(10)); // Calories Burned
				}
			} while(cursor.moveToNext());
		}

		cursor.close();

		String title = recordData.get(1);
		String description = recordData.get(3);
		String distance = TrackedSession.distanceToString(Float.parseFloat(recordData.get(9))) + "km";
		String time = TrackedSession.timeToString(Long.parseLong(recordData.get(6)));

		session_title.setText(title);
		session_description.setText(description);
		session_distance.setText(distance);
		session_time.setText(time);
	}

	public void onDiscardTrackedActivity(View v){

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE){

					getContentResolver().delete(ContentUris.withAppendedId(BopProviderContract.ACTIVITY_URI, session_id), null, null);
					setResult(1);
					finish();
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
				.setMessage("Are you sure you want to delete this session?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("Cancel", dialogClickListener)
				.show();
	}
}
