package com.mohan.android.photogallery;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

public class PhotoStreamFragment extends Fragment {
	private static final String TAG = "PhotoStreamFragment";
	
	private GridView mGridView;
	private ArrayList<GalleryItem> mItems;
	private int mPage = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
		new FetchItemsTask().execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_photo_stream, container, false);
		
		mGridView = (GridView)v.findViewById(R.id.grid_view);
		
		setupAdapter();
		
		return v;
	}
	
	void setupAdapter() {
		if (getActivity() == null || mGridView == null) return;
		
		if (mItems != null) {
			mGridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(),
					android.R.layout.simple_gallery_item, mItems));
		} else {
			mGridView.setAdapter(null);
		}
	}
	
	private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>> {
		
		@Override
		protected ArrayList<GalleryItem> doInBackground(Void...params) {
			return new PhotoFetcher().fetchItems(mPage);
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
	
}
