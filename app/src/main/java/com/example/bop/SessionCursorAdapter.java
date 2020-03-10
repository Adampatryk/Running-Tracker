package com.example.bop;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SessionCursorAdapter extends CursorAdapter {
	public SessionCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor, 0);
	}

	// The newView method is used to inflate a new view and return it,
	// you don't bind any data to the view at this point.
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.row_session, parent, false);
	}

	// The bindView method is used to bind all data to a given view
	// such as setting the text on a TextView.
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// Find fields to populate in inflated template
		TextView titleTextView = view.findViewById(R.id.text_view_row_title);
		TextView distanceTextView = view.findViewById(R.id.text_view_row_distance);
		TextView datetimeTextView = view.findViewById(R.id.text_view_row_date);
		TextView durationTextView = view.findViewById(R.id.text_view_row_time);
		TextView idTextView = view.findViewById(R.id.text_view_row_id);


		// Extract properties from cursor
		String title = cursor.getString(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_TITLE));

		//Format distance
		String distance = cursor.getString(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_DISTANCE));
		distance = TrackedSession.distanceToString(Float.parseFloat(distance), true);

		//Format datetime
		long unix_datetime = cursor.getLong(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_DATETIME));
		String datetime = new SimpleDateFormat("dd MMM yyyy hh:mma", Locale.UK).format(new Date(unix_datetime));

		//Format the duration
		String duration = cursor.getString(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_DURATION));
		duration = TrackedSession.timeToString(Long.parseLong(duration));

		String id = cursor.getString(cursor.getColumnIndexOrThrow(BopProviderContract.ACTIVITY_ID));

		// Populate fields with extracted properties
		titleTextView.setText(title);
		distanceTextView.setText(distance);
		datetimeTextView.setText(datetime);
		durationTextView.setText(duration);
		idTextView.setText(id);

	}
}