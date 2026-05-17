package com.driverspa.activity;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.fragment.GalleryFragment;
import com.driverspa.model.PhotoParcelable;
import com.driverspa.util.ActivityForResult;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.PhotoDeleteRequestEvent;
import com.driverspa.util.otto.ws.PhotoDeleteResponseEvent;
import com.driverspa.view.HackyViewPager;

public class GalleryPhotosActivity extends BaseActivity implements GalleryFragment.ActivityActions {

	public static final String PHOTOS = "PHOTOS";
	public static final String START_INDEX = "START_INDEX";
	public static final String FROM_PROFILE = "FROM_PROFILE";
	public static final String GALLERY_PHOTO_CHANGED_STRING = "GALLERY_PHOTO_CHANGED";
	
	
	protected ArrayList<PhotoParcelable> photos;
	private HackyViewPager viewPager;
	private String userId; // logged in user id to check if user is viewing own or others' photos
	
	private int GALLERY_PHOTO_CHANGED = ActivityForResult.GALLERY_PHOTO_CHANGED_NO;

	String itemDeleteId;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Mint.initAndStartSession(this.getApplication(), "b054ddc0");
		setContentView(R.layout.activity_gallery);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		TextView delete = (TextView) mToolbar.findViewById(R.id.action_done);
		delete.setText("Удалить фото");
		delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(GalleryPhotosActivity.this);
				dialog.setTitle("Хотите удалить данное фото?");
				dialog.setNeutralButton("Нет", null);
				dialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setWaitScreen(true);
						PhotoParcelable photo = photos.get(viewPager.getCurrentItem());
						itemDeleteId = photo.getId();
						BA.getEventBus().post(new PhotoDeleteRequestEvent(photo.getId()));
					}
				});
				dialog.show();

			}
		});

		userId = UserPreferences.getUserId(this);
		viewPager = (HackyViewPager) findViewById(R.id.view_pager);
		
		photos = new ArrayList<PhotoParcelable>();
		int startIndex = 0;
		if( getIntent() != null ) {
			photos = getIntent().getParcelableArrayListExtra(PHOTOS);
			startIndex = getIntent().getIntExtra(START_INDEX, 0);
			boolean fromProfile = getIntent().getBooleanExtra(FROM_PROFILE,false);
			if(fromProfile)
				delete.setVisibility(View.VISIBLE);
			else
				delete.setVisibility(View.GONE);
		}
		
		viewPager.setAdapter(new GalleryPagerAdapter(getSupportFragmentManager()));
		viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.horizontal_margin));
		viewPager.setOffscreenPageLimit(3);
		viewPager.setCurrentItem(startIndex);
		
		if( savedInstanceState != null ) {
			int changed = savedInstanceState.getInt(GALLERY_PHOTO_CHANGED_STRING, 0);
			if( changed != 0 ) {
				GALLERY_PHOTO_CHANGED = changed;
			}
		}
		setResult(GALLERY_PHOTO_CHANGED);
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
				if(GALLERY_PHOTO_CHANGED == ActivityForResult.GALLERY_PHOTO_CHANGED_YES)
					setResultChanged();
				else
            	    setResult(RESULT_CANCELED, new Intent());
                finish();
                overridePendingTransitionWithCommonCloseTransition();
                break;          
        }
        return true;
    }

	@Override
	protected void onResume() {
		setWaitScreen(false);
		BA.getEventBus().register(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
	}
    
	public static Intent newIntent(Context context, ArrayList<PhotoParcelable> photos, int startIndex) {
		Intent intent = new Intent(context, GalleryPhotosActivity.class);
		intent.putParcelableArrayListExtra(PHOTOS, photos);
		intent.putExtra(START_INDEX, startIndex);
		return intent;
	}

	public static Intent newIntent(Context context, ArrayList<PhotoParcelable> photos, int startIndex, boolean profile) {
		Intent intent = new Intent(context, GalleryPhotosActivity.class);
		intent.putParcelableArrayListExtra(PHOTOS, photos);
		intent.putExtra(START_INDEX, startIndex);
		intent.putExtra(FROM_PROFILE, profile);
		return intent;
	}


	private class GalleryPagerAdapter extends FragmentStatePagerAdapter {

		public GalleryPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return GalleryFragment.init(photos.get(position), position);
		}

		@Override
		public int getCount() {
			return photos.size();
		}
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt(GALLERY_PHOTO_CHANGED_STRING, GALLERY_PHOTO_CHANGED);
	}
	
	
	protected void setResultChanged() {
		GALLERY_PHOTO_CHANGED = ActivityForResult.GALLERY_PHOTO_CHANGED_YES;
		setResult(GALLERY_PHOTO_CHANGED);
	}

	@Subscribe
	public void onPhotoDeleteResponseReceived(PhotoDeleteResponseEvent event){
	   setWaitScreen(false);
       if(event!=null && BaseAssist.isSuccess(event.getResponse())){
		   if(photos!= null && photos.size() > 0) {
			   GALLERY_PHOTO_CHANGED = ActivityForResult.GALLERY_PHOTO_CHANGED_YES;
			   Iterator<PhotoParcelable> itr = photos.iterator();
			   int pos = 0;
			   int index = 0;
			   while (itr.hasNext()) {
				   if (itr.next().getId().equals(itemDeleteId)) {
					   itr.remove();
				   }
			   }
			   if (photos.size() == 0) {
				   setResult(GALLERY_PHOTO_CHANGED);
				   finish();
				   overridePendingTransitionWithCommonCloseTransition();
			   }
			   else{
				   viewPager.setAdapter(new GalleryPagerAdapter(getSupportFragmentManager()));
				   viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.horizontal_margin));
				   viewPager.setOffscreenPageLimit(3);
				   viewPager.setCurrentItem(0);
			   }
		   }
		   else{
			   setResult(GALLERY_PHOTO_CHANGED);
			   finish();
			   overridePendingTransitionWithCommonCloseTransition();
		   }
	   }
	}
}
