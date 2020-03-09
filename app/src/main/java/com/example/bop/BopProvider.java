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

public class BopProvider extends ContentProvider {
	private static final String TAG = "BopProvider";

	private static final int ACTIVITY_ALL_INFO_CODE = 1;
	private static final int ACTIVITY_ID_CODE = 2;
	private static final int ACTIVITY_ID_TRK_POINTS_CODE = 3;

	DBHelper dbHelper = null;

	private static final UriMatcher uriMatcher;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.ACTIVITY_TABLE, ACTIVITY_ALL_INFO_CODE);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.ACTIVITY_TABLE + "/#", ACTIVITY_ID_CODE);
		uriMatcher.addURI(BopProviderContract.AUTHORITY, BopProviderContract.TRK_POINT_TABLE, ACTIVITY_ID_TRK_POINTS_CODE);
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

		switch(uriMatcher.match(uri)){
			//Case for any specific activity being queried, the ID is appended to the selection query string
			case ACTIVITY_ID_TRK_POINTS_CODE:
				return db.query(BopProviderContract.TRK_POINT_TABLE, projection, selection, selectionArgs, null, null, sortOrder);

			//This gets the info for all (or one when specified) activity
			case ACTIVITY_ALL_INFO_CODE:
				return db.query(BopProviderContract.ACTIVITY_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
			default:
				return null;
		}
	}

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		switch(uriMatcher.match(uri)){

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

		switch(uriMatcher.match(uri)){

			//Case for any specific activity being deleted, the ID is appended to the whereClause query string
			case ACTIVITY_ID_CODE:
				String id = uri.getLastPathSegment();
				String activity_selection = BopProviderContract.ACTIVITY_ID + "=" + id;
				String trk_points_selection = BopProviderContract.TRK_POINT_ACTIVITY_ID + "=" + id;

				Log.d(TAG, "delete: Deleting activity with _id: " + id);
				rowsAffected += db.delete(BopProviderContract.ACTIVITY_TABLE, activity_selection, selectionArgs);

				Log.d(TAG, "delete: Deleting trk_points with activity_id: " + id);
				//rowsAffected += db.delete(BopProviderContract.TRK_POINT_TABLE, trk_points_selection, null);
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

		switch(uriMatcher.match(uri)){

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

		if (uri.getLastPathSegment()==null) {
			contentType = BopProviderContract.CONTENT_TYPE_MULTIPLE;
		} else {
			contentType = BopProviderContract.CONTENT_TYPE_SINGLE;
		}

		return contentType;
	}

	public int getMaxSessionId(){
		String query = "SELECT MAX(" + BopProviderContract.ACTIVITY_ID + ") AS max_id FROM " + BopProviderContract.ACTIVITY_TABLE;
		Cursor cursor = dbHelper.getWritableDatabase().rawQuery(query, null);

		int max_id = 0;
		if (cursor.moveToFirst())
		{
			do
			{
				max_id = cursor.getInt(0);
			} while(cursor.moveToNext());
		}

		cursor.close();
		return max_id;
	}
}
