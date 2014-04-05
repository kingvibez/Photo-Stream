package com.mohan.android.photogallery;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

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
	
	//Override startSearch to have the current query highlighted
	@Override
	public void startSearch(String initialQuery, boolean selectInitialQuery, 
			Bundle appSearchData, boolean globalSearch) {
		
		String currentQuery = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(PhotoFetcher.PREF_SEARCH_QUERY, null);
		
		super.startSearch(currentQuery, true, appSearchData, globalSearch);
	}

}
