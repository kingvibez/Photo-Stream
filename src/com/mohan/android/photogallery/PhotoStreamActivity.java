package com.mohan.android.photogallery;

import android.app.SearchManager;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

public class PhotoStreamActivity extends SingleFragmentActivity {
	
	private static final String TAG = "PhotoStreamActivity";

	@Override
	public Fragment createFragment() {
		return new PhotoStreamFragment();
	}
	
	//Override onNewIntent b/c launch mode is SingleTop
	@Override
	public void onNewIntent(Intent intent) {
		PhotoStreamFragment fragment = (PhotoStreamFragment)
				getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		
		if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			//Log.i(TAG, "Got search query: "+query);
			
			PreferenceManager.getDefaultSharedPreferences(this)
				.edit()
				.putString(PhotoFetcher.PREF_SEARCH_QUERY, query)
				.commit();
		}
		
		fragment.updateItems();
	}

}
