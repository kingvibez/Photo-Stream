package com.mohan.android.photogallery;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PhotoStreamFragment extends Fragment {
	private static final String TAG = "PhotoStreamFragment";
	
	private GridView mGridView;
	private ArrayList<GalleryItem> mItems;
	ThumbnailDownloader<ImageView> mThumbnailThread;
	private int mPage = 1;
	private LruCache<String, Bitmap> mCache;
	private String mQuery = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
		
		//Create a cache to store bitmaps generated in the future
		int maxMemory = (int)(Runtime.getRuntime().maxMemory()/1024);
		int cacheSize = maxMemory/4;
		mCache = new LruCache<String, Bitmap>(cacheSize) {
			@TargetApi(12)
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				//Get cache size in kB instead of number of items
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
					return bitmap.getByteCount()/1024;
				} else {
					//Didn't have getByteCount before Api Level 12
					return bitmap.getRowBytes()*bitmap.getHeight()/1024;
				}
			}
		};
		
		updateItems();
		
		mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
		mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
			//Implement the inherited abstract method onThumbnailDownloaded
			public void onThumbnailDownloaded(ImageView imageView, String url, Bitmap thumbnail) {
				if (isVisible()) {
					addToCache(url, thumbnail);
					imageView.setImageBitmap(thumbnail);
				}
			}
		});
		mThumbnailThread.start();
		mThumbnailThread.getLooper();
		Log.i(TAG, "Background thread started.");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_photo_stream, container, false);
		
		mGridView = (GridView)v.findViewById(R.id.grid_view);
		
		setupAdapter();
		
		return v;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mThumbnailThread.clearQueue();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mThumbnailThread.quit();
		Log.i(TAG, "Background thread ended.");
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_photo_stream, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_search:
				getActivity().onSearchRequested();
				return true;
			case R.id.menu_item_clear:
				if (mQuery == null) return true;
				PreferenceManager.getDefaultSharedPreferences(getActivity())
					.edit()
					//Put in null value for searchQuery
					.putString(PhotoFetcher.PREF_SEARCH_QUERY, null)
					.commit();
				updateItems();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void updateItems() {
		new FetchItemsTask().execute();
	}
	
	void setupAdapter() {
		if (getActivity() == null || mGridView == null) return;
		
		if (mItems != null) {
			mGridView.setAdapter(new GalleryItemAdapter(mItems));
		} else {
			mGridView.setAdapter(null);
		}
	}
	
	private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>> {
		
		@Override
		protected ArrayList<GalleryItem> doInBackground(Void...params) {
			
			//Make sure there is an activity
			Activity activity = getActivity();
			if (activity == null) return new ArrayList<GalleryItem>();
			
			mQuery = PreferenceManager.getDefaultSharedPreferences(activity)
				.getString(PhotoFetcher.PREF_SEARCH_QUERY, null);
			
			if (mQuery != null) {
				return new PhotoFetcher().searchFlickr(mQuery);
			} else {
				return new PhotoFetcher().fetchFlickrItems(""+mPage);
			}
		}
		
		//Use AsyncTask inherited method onPostExecute to run some methods after
		//doInBackground is completed. This way, we can run methods again in the
		//main thread. It is not advisable to update UI or even update the model
		//layer from the background thread.
		@Override
		protected void onPostExecute(ArrayList<GalleryItem> items) {
			mItems = items;
			setupAdapter();
		}
	}
	
	private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
		public GalleryItemAdapter(ArrayList<GalleryItem> items) {
			super(getActivity(), 0, items);
		}
		
		//GridView calls this getView method on its adapter for every individual view it needs
		//That's why we override it to do what load our desired imageviews
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater()
						.inflate(R.layout.gallery_item, parent, false);
			}
			
			ImageView imageView = (ImageView)convertView
					.findViewById(R.id.gallery_item_image_view);
			imageView.setImageResource(R.drawable.defaultimage);
			
			GalleryItem item = getItem(position);
			String url = item.getUrl();
			Bitmap bitmap = getFromCache(url);
			if (bitmap == null) {
				mThumbnailThread.queueThumbnail(imageView, url);
			} else {
				imageView.setImageBitmap(bitmap);
				Log.i(TAG, "Loaded bitmap from cache!");
			}
			
			return convertView;
		}
	}
	
	//Create some cache methods to put in and get from LruCache
	public Bitmap getFromCache(String url) {
		return mCache.get(url);
	}
	
	public void addToCache(String url, Bitmap bitmap) {
		if (mCache == null) return;
		if (getFromCache(url) == null) {
			mCache.put(url, bitmap);
		}
	}
	//End of cache methods
	
}
