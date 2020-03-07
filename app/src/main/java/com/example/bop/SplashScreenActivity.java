package com.example.bop;

import androidx.appcompat.app.AppCompatActivity;
import gr.net.maroulis.library.EasySplashScreen;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SplashScreenActivity extends AppCompatActivity {

	private static final String TAG = "SplashScreenActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		//Create the configuration for the splashcreen, icons, text, timeout...
		EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
				.withFullScreen()
				.withTargetActivity(MainActivity.class)
				.withSplashTimeOut(3000)
				.withBackgroundColor(Color.parseColor("#002b54"))
				.withBeforeLogoText("Bop")
				.withLogo(R.drawable.ic_runner);

		//Set design style of the text before logo
		config.getBeforeLogoTextView().setTextColor(Color.WHITE);
		config.getBeforeLogoTextView().setTextSize(30);

		//Create the SplashScreen and set the ContentView to display it
		View splashScreen = config.create();
		setContentView(splashScreen);
		Log.d(TAG, "onCreate: SplashScreen Showing");
	}
}
