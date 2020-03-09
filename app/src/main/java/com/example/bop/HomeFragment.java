package com.example.bop;

import android.content.Intent;
import android.database.Cursor;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

	SimpleCursorAdapter simpleCursorAdapter;
	ListView sessionsListView;
	final static int DELETE_REQUEST_CODE = 0;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_home, container, false);
		sessionsListView = v.findViewById(R.id.list_view_sessions);
		setUpListView();

		return v;
	}

	private void setUpListView() {
		Cursor c;

		String[] columns = new String[]{
				BopProviderContract.ACTIVITY_TITLE,
				BopProviderContract.ACTIVITY_DISTANCE,
				BopProviderContract.ACTIVITY_DATETIME,
				BopProviderContract.ACTIVITY_DURATION,
				BopProviderContract.ACTIVITY_ID
		};

		int[] to = new int[]{
				R.id.text_view_row_title,
				R.id.text_view_distance,
				R.id.text_view_date_label,
				R.id.text_view_row_time,
				R.id.text_view_row_id
		};

		c = getContext().getContentResolver().query(BopProviderContract.ACTIVITY_URI, columns, null, null, null);

		simpleCursorAdapter = new SimpleCursorAdapter(getContext(), R.layout.row_session, c, columns, to, 0);
		sessionsListView.setAdapter(simpleCursorAdapter);

		final Intent goToSessionDetailsActivity = new Intent(getContext(), SessionDetailsActivity.class);
		sessionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				int id = Integer.parseInt(((TextView) view.findViewById(R.id.text_view_row_id)).getText().toString());

				goToSessionDetailsActivity.putExtra("id", ""+id);
				startActivityForResult(goToSessionDetailsActivity, 0);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == DELETE_REQUEST_CODE){
			if (resultCode == 1) {
				setUpListView();
			}
		}
	}
}
