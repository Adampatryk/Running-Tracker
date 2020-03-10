package com.example.bop;

import android.net.Uri;

//Provides the strings and URIs that are used to communicate with the ContentProvider without having to
//know the field names. Theoretically a value could be changed here and as long as it didnt go out of sync with the database
//eveything would still work
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
	public static final String ACTIVITY_IMAGE = "image";

	//trk_point table fields
	public static final String TRK_POINT_ID = "_id";
	public static final String TRK_POINT_ACTIVITY_ID = "activity_id";
	public static final String TRK_POINT_LATITUDE = "latitude";
	public static final String TRK_POINT_LONGITUDE = "longitude";
	public static final String TRK_POINT_ELEVATION = "elevation";
	public static final String TRK_POINT_DATETIME = "datetime";

	public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/BopProvider.data.text";
	public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/BopProvider.data.text";

	//URIs
	public static final Uri ACTIVITY_URI = Uri.parse("content://" + AUTHORITY + "/" + ACTIVITY_TABLE);
	public static final Uri TRK_POINT_URI = Uri.parse("content://" + AUTHORITY + "/" + TRK_POINT_TABLE);

	//VALUES
	public static final String SESSIONS_COUNT = "sessions_count";
	public static final String TOTAL_TIME = "total_time";
	public static final String LONGEST_TIME = "longest_time";
	public static final String AVERAGE_TIME = "avg_time";
	public static final String TOTAL_DISTANCE = "total_distance";
	public static final String LONGEST_DISTANCE = "longest_distance";
	public static final String AVERAGE_DISTANCE = "avg_distance";

	//Value URIs
	public static final Uri SESSIONS_COUNT_URI = Uri.parse("content://" + AUTHORITY + "/" + SESSIONS_COUNT);
	public static final Uri TOTAL_DISTANCE_URI = Uri.parse("content://" + AUTHORITY + "/" + TOTAL_DISTANCE);
	public static final Uri TOTAL_TIME_URI = Uri.parse("content://" + AUTHORITY + "/" + TOTAL_TIME);
	public static final Uri LONGEST_TIME_URI = Uri.parse("content://" + AUTHORITY + "/" + LONGEST_TIME);
	public static final Uri AVERAGE_TIME_URI = Uri.parse("content://" + AUTHORITY + "/" + AVERAGE_TIME);
	public static final Uri LONGEST_DISTANCE_URI = Uri.parse("content://" + AUTHORITY + "/" + LONGEST_DISTANCE);
	public static final Uri AVERAGE_DISTANCE_URI = Uri.parse("content://" + AUTHORITY + "/" + AVERAGE_DISTANCE);
}
