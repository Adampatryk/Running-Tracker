package com.example.bop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class ImageDBHelper {
	// convert from bitmap to byte array
	public static byte[] getBytes(Bitmap bitmap) {
		if (bitmap == null){ return null; }
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap = getResizedBitmap(bitmap, 1000);
		bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);

		return stream.toByteArray();
	}

	// convert from byte array to bitmap
	public static Bitmap getImage(byte[] image) {
		if (image == null) { return null; }
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}

	public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
		int width = image.getWidth();
		int height = image.getHeight();

		float bitmapRatio = (float)width / (float) height;
		if (bitmapRatio > 1) {
			width = maxSize;
			height = (int) (width / bitmapRatio);
		} else {
			height = maxSize;
			width = (int) (height * bitmapRatio);
		}
		return Bitmap.createScaledBitmap(image, width, height, true);
	}
}

