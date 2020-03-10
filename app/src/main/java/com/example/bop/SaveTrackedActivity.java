package com.example.bop;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class SaveTrackedActivity extends AppCompatActivity {
	private static final String TAG = "SaveTrackedActivity";
	LocationService.LocationServiceBinder locationServiceBinder = null;
	EditText editTextTitle, editTextDescription;
	private ImageView imageView;

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

	public void onAddImageClicked(View v){
		selectImage(this);
	}

	private void selectImage(Context context) {
		final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Save a photo from the run...");

		builder.setItems(options, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int item) {

				if (options[item].equals("Take Photo")) {
					Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(takePicture, 0);

				} else if (options[item].equals("Choose from Gallery")) {
					Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(pickPhoto , 1);

				} else if (options[item].equals("Cancel")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save_tracked);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle("");

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = new Intent(this, LocationService.class);
		this.bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);

		editTextTitle = findViewById(R.id.edit_text_session_title);
		editTextDescription = findViewById(R.id.edit_text_session_description);
		imageView = findViewById(R.id.session_photo);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode != RESULT_CANCELED) {
			switch (requestCode) {
				case 0:
					if (resultCode == RESULT_OK && data != null) {
						Bitmap bitmap = (Bitmap) data.getExtras().get("data");
						imageView.setImageBitmap(bitmap);
						locationServiceBinder.setImage(bitmap);
					}

					break;
				case 1:
					if (resultCode == RESULT_OK && data != null) {
						Uri selectedImage =  data.getData();
						Bitmap bitmap = null;
						try {
							bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						imageView.setImageBitmap(bitmap);
						locationServiceBinder.setImage(bitmap);
					}
					break;
			}
		}
	}

	public String toTitleCase(String str) {

		if (str == null) {
			return null;
		}

		boolean space = true;
		StringBuilder builder = new StringBuilder(str);
		final int len = builder.length();

		for (int i = 0; i < len; ++i) {
			char c = builder.charAt(i);
			if (space) {
				if (!Character.isWhitespace(c)) {
					// Convert to title case and switch out of whitespace mode.
					builder.setCharAt(i, Character.toTitleCase(c));
					space = false;
				}
			} else if (Character.isWhitespace(c)) {
				space = true;
			} else {
				builder.setCharAt(i, Character.toLowerCase(c));
			}
		}

		return builder.toString();
	}

	public void onSaveTrackedActivity(View v){

		//Check if the user has set a title
		if (editTextTitle.getText().toString().trim().equals("")) {

			//Alert the user to add a title
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("You can't leave the title of your run empty!")
					.setTitle("Empty title")
					.setPositiveButton("OK", null);
			AlertDialog dialog = builder.create();
			dialog.show();

			return;
		}

		locationServiceBinder.setTitle(toTitleCase(editTextTitle.getText().toString()));
		locationServiceBinder.setDescription(editTextDescription.getText().toString());

		//Save the tracked session data
		ContentValues sessionValues = new ContentValues();
		sessionValues.put(BopProviderContract.ACTIVITY_TITLE, locationServiceBinder.getTitle());
		sessionValues.put(BopProviderContract.ACTIVITY_DESCRIPTION, locationServiceBinder.getDescription());
		sessionValues.put(BopProviderContract.ACTIVITY_ACTIVITY_TYPE, "Run");
		sessionValues.put(BopProviderContract.ACTIVITY_DATETIME, locationServiceBinder.getTimeCreated().getTime());
		sessionValues.put(BopProviderContract.ACTIVITY_DISTANCE, locationServiceBinder.getDistance());
		sessionValues.put(BopProviderContract.ACTIVITY_DURATION, locationServiceBinder.getDuration());
		sessionValues.put(BopProviderContract.ACTIVITY_AVG_SPEED, locationServiceBinder.getAvgSpeed());
		sessionValues.put(BopProviderContract.ACTIVITY_ELEVATION, locationServiceBinder.getElevation());
		sessionValues.put(BopProviderContract.ACTIVITY_RATING, 8);
		sessionValues.put(BopProviderContract.ACTIVITY_CALORIES_BURNED, 435);
		sessionValues.put(BopProviderContract.ACTIVITY_IMAGE, ImageDBHelper.getBytes(locationServiceBinder.getImage()));

		getContentResolver().insert(BopProviderContract.ACTIVITY_URI, sessionValues);

		//Save the track points
		ArrayList<Location> trackPoints = locationServiceBinder.getTrkPoints();

		for (Location trkPoint : trackPoints) {
			ContentValues trackPointValues = new ContentValues();
			trackPointValues.put(BopProviderContract.TRK_POINT_DATETIME, trkPoint.getTime());
			trackPointValues.put(BopProviderContract.TRK_POINT_ELEVATION, trkPoint.getAltitude());
			trackPointValues.put(BopProviderContract.TRK_POINT_LONGITUDE, trkPoint.getLongitude());
			trackPointValues.put(BopProviderContract.TRK_POINT_LATITUDE, trkPoint.getLatitude());
			getContentResolver().insert(BopProviderContract.TRK_POINT_URI, trackPointValues);
		}

		//Clean up and go back to the main activity
		backToMainActivity();
	}

	public void onDiscardTrackedActivity(View v){

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE){
					Log.d(TAG, "onSaveTrackedActivity: Stopped LocationService");
					backToMainActivity();
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
	public void onBackPressed() {
		locationServiceBinder.setTitle(editTextTitle.getText().toString());
		locationServiceBinder.setDescription(editTextDescription.getText().toString());
		super.onBackPressed();
	}


	@Override
	public boolean onSupportNavigateUp() {
		locationServiceBinder.setTitle(editTextTitle.getText().toString());
		locationServiceBinder.setDescription(editTextDescription.getText().toString());
		return super.onSupportNavigateUp();
	}

	protected void backToMainActivity(){
		//Stop the location service
		stopService(new Intent(this, LocationService.class));

		//Go back
		Intent intent = new Intent(getBaseContext(), MainActivity.class);

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}
}
