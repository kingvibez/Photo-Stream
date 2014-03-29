package com.mohan.android.photogallery;

import android.support.v4.app.Fragment;

public class PhotoStreamActivity extends SingleFragmentActivity {

	@Override
	public Fragment createFragment() {
		return new PhotoStreamFragment();
	}

}
