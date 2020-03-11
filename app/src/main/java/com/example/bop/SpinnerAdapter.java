package com.example.bop;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class SpinnerAdapter extends ArrayAdapter<String> {

	private ArrayList mData;
	public Resources mResources;
	private LayoutInflater mInflater;


	public SpinnerAdapter(
			Activity activitySpinner,
			int textViewResourceId,
			ArrayList objects,
			Resources resLocal
	) {
		super(activitySpinner, textViewResourceId, objects);

		mData = objects;
		mResources = resLocal;
		mInflater = (LayoutInflater) activitySpinner.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	@Override
	public int getCount(){
		return mData.size();
	}

	public View getCustomView(int position, View convertView, ViewGroup parent) {

		View row = mInflater.inflate(R.layout.adapter_spinner, parent, false);
		TextView label = (TextView) row.findViewById(R.id.spinner_item);
		label.setText(mData.get(position).toString());

		return row;
	}
}