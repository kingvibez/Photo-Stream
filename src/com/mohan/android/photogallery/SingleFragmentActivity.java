package com.mohan.android.photogallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public abstract class SingleFragmentActivity extends FragmentActivity {
	
	//Abstract method createFragment() that the subclass will implement
	protected abstract Fragment createFragment();
	
	//Method that allows different layouts to be called
	protected int getLayoutResId() {
		return R.layout.activity_fragment;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());
		
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragment_container);
		
		if (fragment == null) {
			fragment = createFragment();
			fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
		}
		
	}

}
