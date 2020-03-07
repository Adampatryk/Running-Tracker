package com.example.bop;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class SaveTrackedActivity extends AppCompatActivity {
	private static final String TAG = "SaveTrackedActivity";
	LocationService.LocationServiceBinder locationServiceBinder = null;
	EditText editTextTitle, editTextDescription;

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected: ");
			locationServiceBinder = (LocationService.LocationServiceBinder) service;
			editTextTitle.setText(locationServiceBinder.getTitle());
			editTextDescription.setText(locationServiceBinder.getDescription());
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
		setContentView(R.layout.activity_save_tracked);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle("");

		setSupportActionBar(toolbar);
		//getSupportActionBar().setTitle("");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = new Intent(this, LocationService.class);
		this.bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);

		editTextTitle = findViewById(R.id.edit_text_session_title);
		editTextDescription = findViewById(R.id.edit_text_session_description);

	}

	public void onSaveTrackedActivity(View v){

		Log.d(TAG, "onSaveTrackedActivity: Stopped LocationService");

		locationServiceBinder.setTitle(editTextTitle.getText().toString());
		locationServiceBinder.setDescription(editTextDescription.getText().toString());

		//Save the tracked data
		BopProvider bopProvider = new BopProvider();

		ContentValues sessionValues = new ContentValues();
		sessionValues.put(BopProviderContract.ACTIVITY_TITLE, locationServiceBinder.getTitle());
		sessionValues.put(BopProviderContract.ACTIVITY_DESCRIPTION, locationServiceBinder.getDescription());
		sessionValues.put(BopProviderContract.ACTIVITY_ACTIVITY_TYPE, "Run");
		sessionValues.put(BopProviderContract.ACTIVITY_DATETIME, locationServiceBinder.getTimeCreated().toString());
		sessionValues.put(BopProviderContract.ACTIVITY_DISTANCE, locationServiceBinder.getDistance());
		sessionValues.put(BopProviderContract.ACTIVITY_DURATION, locationServiceBinder.getDuration());
		sessionValues.put(BopProviderContract.ACTIVITY_AVG_SPEED, locationServiceBinder.getAvgSpeed());
		sessionValues.put(BopProviderContract.ACTIVITY_ELEVATION, locationServiceBinder.getElevation());
		sessionValues.put(BopProviderContract.ACTIVITY_RATING, 8);
		sessionValues.put(BopProviderContract.ACTIVITY_CALORIES_BURNED, 435);

		getContentResolver().insert(BopProviderContract.ACTIVITY_URI, sessionValues);

		//Stop the location service
		stopService(new Intent(this, LocationService.class));

		//Go back to the main screen
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	public void onDiscardTrackedActivity(View v){

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE){
					stopService(new Intent(getBaseContext(), LocationService.class));
					Log.d(TAG, "onSaveTrackedActivity: Stopped LocationService");

					Intent intent = new Intent(getBaseContext(), MainActivity.class);
					startActivity(intent);
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
				.setMessage("Are you sure you want to discard this activity?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("Cancel", dialogClickListener)
				.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		locationServiceBinder.setTitle(editTextTitle.getText().toString());
		locationServiceBinder.setDescription(editTextDescription.getText().toString());
	}
}
