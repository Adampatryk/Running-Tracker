package com.example.bop;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//Class that provides data on sessions and track points to classes asking for it using specified URIs
public class BopProvider extends ContentProvider {
	private static final String TAG = "BopProvider";

	//Codes that will dictate different data to be returned
	private static final int ACTIVITY_ALL_INFO_CODE = 1;
	private static final int ACTIVITY_ID_CODE = 2;
	private static final int ACTIVITY_ID_TRK_POINTS_CODE = 3;
	public static final int SESSIONS_COUNT_CODE = 4;
	public static final int TOTAL_TIME_CODE = 5;
	public static final int LONGEST_TIME_CODE = 6;
	public static final int AVERAGE_TIME_CODE = 7;
	public static final int TOTAL_DISTANCE_CODE = 8;
	public static final int LONGEST_DISTANCE_CODE = 9;
	public static final int AVERAGE_DISTANCE_CODE = 10;

	DBHelper dbHelper = null;

	private static final UriMatcher uriMatcher;

	//Creating the URI matcher to map codes
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.ACTIVITY_TABLE, ACTIVITY_ALL_INFO_CODE);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.ACTIVITY_TABLE + "/#", ACTIVITY_ID_CODE);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.TRK_POINT_TABLE, ACTIVITY_ID_TRK_POINTS_CODE);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.SESSIONS_COUNT, SESSIONS_COUNT_CODE);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.TOTAL_TIME, TOTAL_TIME_CODE);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.LONGEST_TIME, LONGEST_TIME_CODE);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.AVERAGE_TIME, AVERAGE_TIME_CODE);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.TOTAL_DISTANCE, TOTAL_DISTANCE_CODE);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.LONGEST_DISTANCE, LONGEST_DISTANCE_CODE);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.AVERAGE_DISTANCE, AVERAGE_DISTANCE_CODE);
	}


	@Override
	public boolean onCreate() {
		dbHelper = new DBHelper(this.getContext());
		Log.d(TAG, "onCreate: dbHelper initialised");
		return true;
	}

	@Nullable
	@Override
	public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		switch (uriMatcher.match(uri)) {
			//Case for any specific activity being queried, the ID is appended to the selection query string
			case ACTIVITY_ID_TRK_POINTS_CODE:
				return db.query(BopProviderContract.TRK_POINT_TABLE, projection, selection, selectionArgs, null, null, sortOrder);

			//This gets the info for all (or one when specified) activity
			case ACTIVITY_ALL_INFO_CODE:
				return db.query(BopProviderContract.ACTIVITY_TABLE, projection, selection, selectionArgs, null, null, sortOrder);

			//Gets the count of the sessions
			case SESSIONS_COUNT_CODE:
				return db.query(BopProviderContract.ACTIVITY_TABLE, new String[]{"COUNT(*) AS " + BopProviderContract.SESSIONS_COUNT},
						selection, selectionArgs, null, null, null);

			//Gets the total time across the queried sessions
			case TOTAL_TIME_CODE:
				return db.query(BopProviderContract.ACTIVITY_TABLE, new String[]{"SUM(" + BopProviderContract.ACTIVITY_DURATION + ") AS " + BopProviderContract.TOTAL_TIME},
						selection, selectionArgs, null, null, null);

			//Gets the longest time across the queried sessions
			case LONGEST_TIME_CODE:
				return db.query(BopProviderContract.ACTIVITY_TABLE, new String[]{"MAX(" + BopProviderContract.ACTIVITY_DURATION + ") AS " + BopProviderContract.LONGEST_TIME},
						selection, selectionArgs, null, null, null);

			//Gets the average time across the queried sessions
			case AVERAGE_TIME_CODE:
				return db.query(BopProviderContract.ACTIVITY_TABLE, new String[]{"AVG(" + BopProviderContract.ACTIVITY_DURATION + ") AS " + BopProviderContract.AVERAGE_TIME},
						selection, selectionArgs, null, null, null);

			//Gets the total distance across the queried sessions
			case TOTAL_DISTANCE_CODE:
				return db.query(BopProviderContract.ACTIVITY_TABLE, new String[]{"SUM(" + BopProviderContract.ACTIVITY_DISTANCE + ") AS " + BopProviderContract.TOTAL_DISTANCE},
						selection, selectionArgs, null, null, null);

			//Gets the longest distance across the queried sessions
			case LONGEST_DISTANCE_CODE:
				return db.query(BopProviderContract.ACTIVITY_TABLE, new String[]{"MAX(" + BopProviderContract.ACTIVITY_DISTANCE + ") AS " + BopProviderContract.LONGEST_DISTANCE},
						selection, selectionArgs, null, null, null);

			//Gets the average distance across the queried sessions
			case AVERAGE_DISTANCE_CODE:
				return db.query(BopProviderContract.ACTIVITY_TABLE, new String[]{"AVG(" + BopProviderContract.ACTIVITY_DISTANCE + ") AS " + BopProviderContract.AVERAGE_DISTANCE},
						selection, selectionArgs, null, null, null);

			default:
				return null;
		}
	}

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		switch (uriMatcher.match(uri)) {

			//Case inserting trk_points for a specific activity
			case ACTIVITY_ID_TRK_POINTS_CODE:
				contentValues.put(BopProviderContract.TRK_POINT_ACTIVITY_ID, getMaxSessionId());
				db.insert(BopProviderContract.TRK_POINT_TABLE, null, contentValues);
				return null;
			//Case for inserting all the activity info into the activity table
			case ACTIVITY_ALL_INFO_CODE:
				db.insert(BopProviderContract.ACTIVITY_TABLE, null, contentValues);
			default:
				return null;
		}
	}

	@Override
	public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int rowsAffected = 0;

		switch (uriMatcher.match(uri)) {

			//Case for any specific activity being deleted, the ID is appended to the whereClause query string
			case ACTIVITY_ID_CODE:
				String id = uri.getLastPathSegment();
				String activity_selection = BopProviderContract.ACTIVITY_ID + "=" + id;

				Log.d(TAG, "delete: Deleting activity with _id: " + id);
				rowsAffected += db.delete(BopProviderContract.ACTIVITY_TABLE, activity_selection, selectionArgs);

				Log.d(TAG, "delete: Deleting trk_points with activity_id: " + id);
				return rowsAffected;
			//Catching the case that someone tries to delete an activity without providing an ID
			case ACTIVITY_ALL_INFO_CODE:
				Log.d(TAG, "delete: Must provide an ID of an activity to delete");
			default:
				return rowsAffected;
		}
	}

	@Override
	public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int rowsAffected = 0;

		switch (uriMatcher.match(uri)) {

			//Case for any specific activity being update, the ID is appended to the whereClause query string
			case ACTIVITY_ID_CODE:
				String id = uri.getLastPathSegment();
				selection = selection + BopProviderContract.ACTIVITY_ID + "=" + id;

				Log.d(TAG, "delete: Deleting activity with _id: " + id);
				rowsAffected += db.update(BopProviderContract.ACTIVITY_TABLE, contentValues, selection, selectionArgs);


				//Catching the case that someone tries to update an activity without providing an ID
			case ACTIVITY_ALL_INFO_CODE:
				Log.d(TAG, "delete: Must provide an ID of an activity to update");
			default:
				return rowsAffected;
		}
	}

	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		Log.d(TAG, "getType: called");

		String contentType;

		if (uri.getLastPathSegment() == null) {
			contentType = BopProviderContract.CONTENT_TYPE_MULTIPLE;
		} else {
			contentType = BopProviderContract.CONTENT_TYPE_SINGLE;
		}

		return contentType;
	}

	//Used to determine the ID of a session that was just added
	public int getMaxSessionId() {
		String query = "SELECT MAX(" + BopProviderContract.ACTIVITY_ID + ") AS max_id FROM " + BopProviderContract.ACTIVITY_TABLE;
		Cursor cursor = dbHelper.getWritableDatabase().rawQuery(query, null);

		int max_id = 0;
		if (cursor.moveToFirst()) {
			do {
				max_id = cursor.getInt(0);
			} while (cursor.moveToNext());
		}

		cursor.close();
		return max_id;
	}
}
