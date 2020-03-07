package com.example.bop;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
	public static final String CHANNEL_ID = "locationServiceChannel";

	@Override
	public void onCreate() {
		createNotificationChannel();
		super.onCreate();
	}

	private void createNotificationChannel(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel serviceChannel = new NotificationChannel(
					CHANNEL_ID,
					"Location Service Channel",
					NotificationManager.IMPORTANCE_DEFAULT
			);

			NotificationManager manager = getSystemService(NotificationManager.class);
			manager.createNotificationChannel(serviceChannel);
		}
	}
}
