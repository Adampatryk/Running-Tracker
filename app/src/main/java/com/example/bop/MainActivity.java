package com.example.bop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

//The activity that holds the 3 main fragments of the application and deals with the logic of safely
//switching between them
public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity";
	private int ACCESS_FINE_LOCATION = 1;
	BottomNavigationView bottomNavigationView;

	//Values to determine which fragment to display
	private static final int HOME_FRAGMENT = 0;
	private static final int RECORD_FRAGMENT = 1;
	private static final int PROFILE_FRAGMENT = 2;
	private int currentFragment = 0;
	Fragment selectedFragment;
	
	public static final int REQUEST_CHECK_SETTINGS = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "onCreate: Showing MainActivity");

		//Get bottom_nav View
		bottomNavigationView = findViewById(R.id.bottom_nav);
		bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

		//Initially display HomeFragment
		selectedFragment = new HomeFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
	}

	//Nav listener to listen to navigation bar item selections
	private BottomNavigationView.OnNavigationItemSelectedListener navListener =
			new BottomNavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

					//Figure out which fragment to display based on the id of the menuItem clicked
					switch(menuItem.getItemId()){
						case (R.id.nav_home):
							//Set the current fragment to be a new Home Fragment
							selectedFragment = new HomeFragment();
							currentFragment = HOME_FRAGMENT;
							break;
						case (R.id.nav_record):
							//Check if the location permission is granted before switching to fragment with a map on it
							if (checkLocationPermission()){
								selectedFragment = new RecordFragment();
							}
							break;
						case (R.id.nav_profile):
							//Set the current fragment to be a new Profile Fragment
							selectedFragment = new ProfileFragment();
							currentFragment = PROFILE_FRAGMENT;
							break;
					}

					//Switch the View to the new selected fragment
					if (selectedFragment != null){
						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
					}

					//Select the selected nav button
					return true;
				}
			};

	//Checks if the user has granted location permissions and if not asks for them
	private boolean checkLocationPermission(){
		if (ContextCompat.checkSelfPermission(MainActivity.this,
				Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

			//Request the user for the location permission
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
			return false;
		}
		return true;
	}

	//On the result of asking for location permission
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == ACCESS_FINE_LOCATION)  {
			//If the permission was not granted switch the fragment back to what was there previously
			if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
				switch(currentFragment){
					case HOME_FRAGMENT:
						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
						break;
					case PROFILE_FRAGMENT:
						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
						break;
				}
				bottomNavigationView.getMenu().getItem(currentFragment).setChecked(true);
			}
			else {
				//The permission was allowed and the current fragment becomes the RECORD_FRAGMENT
				currentFragment = RECORD_FRAGMENT;
				selectedFragment = new RecordFragment();

				//Switch to the RecordFragment
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
			}
		}
	}

	//Where the user comes back to after being asked to turn on location setting
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CHECK_SETTINGS){
			//If the user turned on their setting, start tracking the activity
			if (resultCode == RESULT_OK){
				((RecordFragment)selectedFragment).startTrackingActivity();
			} else {
				//If the user didn't turn their setting on, the activity cannot be started to track
				Toast.makeText(this, "Location must be turned on to track a session", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
