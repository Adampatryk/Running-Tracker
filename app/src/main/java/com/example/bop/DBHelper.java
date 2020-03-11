package com.example.bop;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//Deals with the creating of the schema and updating correctly when there is a change
public class DBHelper extends SQLiteOpenHelper {

	private static final String TAG = "DBHelper";
	private static final String DATABASE_NAME = "bop_db";

	private SQLiteDatabase db;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, 4);
		Log.d(TAG, "DBHelper: Constructor called");
		getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		Log.d(TAG, "onCreate: Creating database");
		db = sqLiteDatabase;
		createDBSchema();
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
		db = sqLiteDatabase;

		//Drop old tables
		db.execSQL("DROP TABLE activity");
		db.execSQL("DROP TABLE trk_point");

		//Recreate schema
		createDBSchema();
	}

	//Function to create the Database schema
	private void createDBSchema() {

		//Create activity table to store tracked sessions/activities by the user
		db.execSQL("CREATE TABLE activity (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
				"title VARCHAR(128) NOT NULL," +
				"datetime INTEGER," +
				"description VARCHAR(512) NOT NULL," +
				"rating INTEGER," +
				"activity_type VARCHAR(64)," + //Activity type ready to be implemented in future
				"duration INTEGER," +
				"avg_speed DOUBLE," +
				"elevation INTEGER," +
				"distance INTEGER," +
				"calories_burned INTEGER," + //Calories burned field for future BMI calculations
				"image BLOB);");

		Log.d(TAG, "createDBSchema: Created activity table");

		//Create trk_point table that holds all the points associated with an activity
		db.execSQL("CREATE TABLE trk_point (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
				"activity_id INTEGER NOT NULL," +
				"latitude DOUBLE," +
				"longitude DOUBLE," +
				"elevation DOUBLE," +
				"datetime DATETIME," +
				"CONSTRAINT fk1 FOREIGN KEY (activity_id) REFERENCES activity (_id) " +
				"ON DELETE CASCADE);");

		Log.d(TAG, "createDBSchema: Created trk_point table");

	}
}
