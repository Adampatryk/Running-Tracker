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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

//Activity to let the user annotate their tracked session and store it in the database
public class SaveTrackedActivity extends AppCompatActivity {

	private static final String TAG = "SaveTrackedActivity";
	LocationService.LocationServiceBinder locationServiceBinder = null;

	EditText editTextTitle, editTextDescription;
	private ImageView imageView;
	private SeekBar ratingSeekBar;

	//Holds the connection to the service to retrieve and set details
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected: ");
			locationServiceBinder = (LocationService.LocationServiceBinder) service;

			//Set views to data from tracked session
			editTextTitle.setText(locationServiceBinder.getTitle());
			editTextDescription.setText(locationServiceBinder.getDescription());
			ratingSeekBar.setProgress(locationServiceBinder.getRating());
			imageView.setImageBitmap(locationServiceBinder.getImage());
			setSeekBarColour(ratingSeekBar.getProgress());
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

		//Setup the toolbar
		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		//Bind to the location service to retrieve
		Intent intent = new Intent(this, LocationService.class);
		this.bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);

		editTextTitle = findViewById(R.id.edit_text_session_title);
		editTextDescription = findViewById(R.id.edit_text_session_description);
		imageView = findViewById(R.id.session_photo);
		ratingSeekBar = findViewById(R.id.ratingSeekBar);

		ratingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				setSeekBarColour(i);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		//Set initial ratingSeekBar colour and progress
		ratingSeekBar.getProgressDrawable().setColorFilter(Color.rgb(255, 255, 0), PorterDuff.Mode.MULTIPLY);
	}

	//When the Add photo button is clicked
	public void onAddImageClicked(View v) {
		selectImage(this);
	}

	//Allow the user to select an image to store with the session
	// by taking one or choosing one from their library
	private void selectImage(Context context) {
		final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

		//Display optionbox for user to choose how to upload their photo
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Save a photo from the run...");

		builder.setItems(options, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int item) {
				//Start camera capture intent if they chose to take a photo
				if (options[item].equals("Take Photo")) {
					Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(takePicture, 0);

				}
				//Start the media chooser intent if they wanted to choose from their gallery
				else if (options[item].equals("Choose from Gallery")) {
					Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(pickPhoto, 1);

				}
				//Cancel = do nothing
				else if (options[item].equals("Cancel")) {
					dialog.dismiss();
				}
			}
		});
		//Show the dialogbox
		builder.show();
	}

	//Change the colour of the progress seekbar based on the value
	//Transition from red to yellow to green
	private void setSeekBarColour(int progress) {
		//Red 255, 0, 0
		//Yellow 255, 255, 0
		//Green 0, 255, 0
		int red = 0, green = 0, blue = 0;

		if (progress <= 5) {
			red = 255;
			green = (int) (((float) progress / 5f) * 255f);
		} else {
			green = 255;
			red = 255 - (int) ((((float) progress - 5f) / 5f) * 255f);
		}

		//Change colour of progress depending on value
		ratingSeekBar.getProgressDrawable().setColorFilter(Color.rgb(red, green, blue), PorterDuff.Mode.MULTIPLY);
	}

	//Where the user is returned after going out to choose their image
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_CANCELED) {
			switch (requestCode) {
				case 0:
					if (resultCode == RESULT_OK && data != null) {
						Bitmap bitmap = (Bitmap) data.getExtras().get("data");
						//Image is set to the image view and saved to the tracked session for persitence
						imageView.setImageBitmap(bitmap);
						locationServiceBinder.setImage(bitmap);
					}

					break;
				case 1:
					if (resultCode == RESULT_OK && data != null) {
						Uri selectedImage = data.getData();
						Bitmap bitmap = null;
						try {
							bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						//Image is set to the image view and saved to the tracked session for persitence
						imageView.setImageBitmap(bitmap);
						locationServiceBinder.setImage(bitmap);
					}
					break;
			}
		}
	}

	//Changes the title of the user given input to be nicely formatted with only the first
	//character of every word capitalised
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

	//Save the details to the database
	public void onSaveTrackedActivity(View v) {

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

		//Set the details to the tracked session for completeness
		saveDataToServiceBinder();

		//Save the tracked session data
		ContentValues sessionValues = new ContentValues();
		sessionValues.put(BopProviderContract.ACTIVITY_TITLE, locationServiceBinder.getTitle());
		sessionValues.put(BopProviderContract.ACTIVITY_DESCRIPTION, locationServiceBinder.getDescription());
		Toast.makeText(this, "" + locationServiceBinder.getActivityTypeString(), Toast.LENGTH_SHORT).show();
		sessionValues.put(BopProviderContract.ACTIVITY_ACTIVITY_TYPE, locationServiceBinder.getActivityTypeString());
		sessionValues.put(BopProviderContract.ACTIVITY_DATETIME, locationServiceBinder.getTimeCreated().getTime());
		sessionValues.put(BopProviderContract.ACTIVITY_DISTANCE, locationServiceBinder.getDistance());
		sessionValues.put(BopProviderContract.ACTIVITY_DURATION, locationServiceBinder.getDuration());
		sessionValues.put(BopProviderContract.ACTIVITY_AVG_SPEED, locationServiceBinder.getAvgSpeed());
		sessionValues.put(BopProviderContract.ACTIVITY_ELEVATION, locationServiceBinder.getElevation());
		sessionValues.put(BopProviderContract.ACTIVITY_RATING, locationServiceBinder.getRating());
		sessionValues.put(BopProviderContract.ACTIVITY_CALORIES_BURNED, 435); //Ready for implementing BMI calculations
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

	//Button to discard the current tracked session
	public void onDiscardTrackedActivity(View v) {

		//If the user is sure they want to discard this activity, the user is safely taken back to the
		//main menu, stopping the service and losing the tracked session
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					Log.d(TAG, "onSaveTrackedActivity: Stopped LocationService");
					backToMainActivity();
				}
			}
		};

		//Show dialog to ask the user if they are sure they want to discard the current session
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
				.setMessage("Are you sure you want to discard this activity?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("Cancel", dialogClickListener)
				.show();
	}

	//When back is pressed, the data input by the user should be saved
	@Override
	public void onBackPressed() {
		saveDataToServiceBinder();
		super.onBackPressed();
	}

	//When up is pressed, the data input by the user should be saved
	@Override
	public boolean onSupportNavigateUp() {
		saveDataToServiceBinder();
		return super.onSupportNavigateUp();
	}

	//Set the user given strings into the tracked session for persistence
	public void saveDataToServiceBinder() {
		locationServiceBinder.setTitle(toTitleCase(editTextTitle.getText().toString()));
		locationServiceBinder.setDescription(editTextDescription.getText().toString());
		locationServiceBinder.setRating(ratingSeekBar.getProgress());
	}

	//Safely go back to the main activity
	protected void backToMainActivity() {
		//Stop the location service
		stopService(new Intent(this, LocationService.class));

		//Go back to main activity intent
		Intent intent = new Intent(getBaseContext(), MainActivity.class);

		//Flag to clean up all other activities
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}
}
