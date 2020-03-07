package com.example.bop;

import android.net.Uri;

public class BopProviderContract {
	public static final String AUTHORITY = "com.example.bop";

	//Tables
	public static final String ACTIVITY_TABLE = "activity";
	public static final String TRK_POINT_TABLE = "trk_point";

	//activity table fields
	public static final String ACTIVITY_ID = "_id";
	public static final String ACTIVITY_TITLE = "title";
	public static final String ACTIVITY_DATETIME = "datetime";
	public static final String ACTIVITY_DESCRIPTION = "description";
	public static final String ACTIVITY_RATING = "rating";
	public static final String ACTIVITY_ACTIVITY_TYPE = "activity_type";
	public static final String ACTIVITY_DURATION = "duration";
	public static final String ACTIVITY_AVG_SPEED = "avg_speed";
	public static final String ACTIVITY_ELEVATION = "elevation";
	public static final String ACTIVITY_DISTANCE = "distance";
	public static final String ACTIVITY_CALORIES_BURNED = "calories_burned";

	//trk_point table fields
	public static final String TRK_POINT_ID = "_id";
	public static final String TRK_POINT_ACTIVITY_ID = "activity_id";
	public static final String TRK_POINT_LATITUDE = "latitude";
	public static final String TRK_POINT_LONGITUDE = "longitude";
	public static final String TRK_POINT_ELEVATION = "elevation";
	public static final String TRK_POINT_DATETIME = "datetime";

	public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/BopProvider.data.text";
	public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/BopProvider.data.text";

	//Uris
	public static final Uri ACTIVITY_URI = Uri.parse("content://"+AUTHORITY + "/" + ACTIVITY_TABLE);
	public static final Uri TRK_POINT_URI = Uri.parse("content://"+AUTHORITY + "/" + TRK_POINT_TABLE);
	public static final Uri ALL_URI = Uri.parse("content://"+AUTHORITY+"/");
}
