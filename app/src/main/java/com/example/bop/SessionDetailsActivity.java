package com.example.bop;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.sql.Blob;
import java.util.ArrayList;

public class SessionDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

	int session_id;
	TextView session_title, session_description, session_distance, session_time, session_rating;
	GoogleMap map;
	ImageView imageView;

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
		imageView = findViewById(R.id.image_view_session_image);
		session_rating = findViewById(R.id.text_view_session_rating);

		querySession(session_id);

		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	public void querySession(int id){

		ArrayList<String> recordData = new ArrayList<>();
		Bitmap bitmap = null;

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
					recordData.add(cursor.getString(8)); //Elevation
					recordData.add(cursor.getString(9)); // Distance
					recordData.add(cursor.getString(10)); // Calories Burned
					bitmap = ImageDBHelper.getImage(cursor.getBlob(11)); //Image
				}
			} while(cursor.moveToNext());
		}

		cursor.close();

		String title = recordData.get(1);
		String description = recordData.get(3);
		String distance = TrackedSession.distanceToString(Float.parseFloat(recordData.get(9)), true);
		String time = TrackedSession.timeToString(Long.parseLong(recordData.get(6)));
		String rating = recordData.get(4);

		//If an image exists in the database set the image
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		}

		session_title.setText(title);
		session_description.setText(description);
		session_distance.setText(distance);
		session_time.setText(time);
		session_rating.setText(rating);
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

	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
		plotTrackPoints();
	}

	public void plotTrackPoints(){
		//Get track points
		ArrayList<Location> trkPoints = getTrackPointsFromDB();

		PolylineOptions polylineOptions = new PolylineOptions();

		//Add track points to polyline
		for (Location trkPoint : trkPoints){
			LatLng latLng = new LatLng(trkPoint.getLatitude(), trkPoint.getLongitude());
			polylineOptions.add(latLng);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
		}

		//Add polyline to map
		map.addPolyline(polylineOptions);

	}

	public ArrayList<Location> getTrackPointsFromDB(){
		ArrayList<Location> trkPoints = new ArrayList<Location>();


		Cursor c = getContentResolver().query(BopProviderContract.TRK_POINT_URI,
				null, BopProviderContract.TRK_POINT_ACTIVITY_ID + "=?", new String[]{""+session_id}, null);

		if (c.moveToFirst())
		{
			do
			{
				Location trkPoint = new Location("");
				trkPoint.setLatitude(Double.parseDouble(c.getString(2)));
				trkPoint.setLongitude(Double.parseDouble(c.getString(3)));
				trkPoint.setAltitude(Float.parseFloat(c.getString(4)));
				trkPoint.setTime(Long.parseLong(c.getString(5)));

				trkPoints.add(trkPoint);
			} while(c.moveToNext());
		}

		c.close();

		return trkPoints;
	}
}
