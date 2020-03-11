package com.example.bop;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//This class is the definition for how data should be taken out of cursor and into a row of data for
//a session in the listview
public class SessionCursorAdapter extends CursorAdapter {
	public SessionCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor, 0);
	}

	// Inflates the view
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.row_session, parent, false);
	}

	// The bindView method is used to bind all data from the cursor to the views of the row
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// Find fields to populate in inflated template
		TextView titleTextView = view.findViewById(R.id.text_view_row_title);
		TextView distanceTextView = view.findViewById(R.id.text_view_row_distance);
		TextView datetimeTextView = view.findViewById(R.id.text_view_row_date);
		TextView durationTextView = view.findViewById(R.id.text_view_row_time);
		TextView idTextView = view.findViewById(R.id.text_view_row_id);
		TextView activityTypeTextView = view.findViewById(R.id.text_view_activity_type);
		TextView descriptionTextView = view.findViewById(R.id.text_view_row_description);
		ImageView rowImageView = view.findViewById(R.id.row_image);

		// Extract properties from cursor
		String title = cursor.getString(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_TITLE));

		//Format distance
		String distance = cursor.getString(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_DISTANCE));
		distance = TrackedSession.distanceToString(Float.parseFloat(distance), true);

		//Format datetime
		long unix_datetime = cursor.getLong(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_DATETIME));
		String datetime = new SimpleDateFormat("d MMM ha", Locale.UK).format(new Date(unix_datetime));

		//Format the duration
		String duration = cursor.getString(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_DURATION));
		duration = TrackedSession.timeToString(Long.parseLong(duration));

		String id = cursor.getString(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_ID));

		//Cut off the description if it is too long and add ellipsis
		String description = cursor.getString(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_DESCRIPTION));
		if (description.length() > 45) {
			description = description.substring(0, 45) + "...";
		}
		//Remove the description textview if there is no description
		if (description.trim().equals("")){
			descriptionTextView.setVisibility(View.GONE);
		}

		String activityType = cursor.getString(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_ACTIVITY_TYPE));

		Bitmap imageBitmap = ImageDBHelper.getImage(cursor.getBlob(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_IMAGE)));

		// Populate fields with extracted properties
		titleTextView.setText(title);
		distanceTextView.setText(distance);
		datetimeTextView.setText(datetime);
		durationTextView.setText(duration);
		idTextView.setText(id);
		descriptionTextView.setText(description);
		activityTypeTextView.setText(activityType);
		rowImageView.setImageBitmap(imageBitmap);
	}
}