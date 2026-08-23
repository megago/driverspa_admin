package com.driverspa.activity;
import com.driverspa.BA;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.splunk.mint.Mint;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.fragment.UserPhotoChangeFragment;
import com.driverspa.model.PhotoParcelable;
import com.driverspa.util.ActivityForResult;

public class UserPhotoChangeActivity extends BaseActivity implements UserPhotoChangeFragment.ActivityActions {

	TextView title;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Mint.initAndStartSession(this.getApplication(), "b054ddc0");
		ButterKnife.bind(this);
		setContentView(R.layout.activity_user_photo);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		title = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		title.setText(BA.str(R.string.select_photo));
		if( savedInstanceState == null ) {
			String userId = getIntent().getStringExtra(UserPhotoChangeFragment.USER_ID);
			ArrayList<PhotoParcelable> photos = getIntent().getParcelableArrayListExtra(UserPhotoChangeFragment.USER_PHOTOS);
			Fragment fragment = UserPhotoChangeFragment.newInstance(userId, photos);
        	getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
		}
		
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	setResult(RESULT_OK, new Intent());
                finish();	                
                overridePendingTransitionWithCommonCloseTransition();
                break;          
        }
        return true;
    }
	
	public static Intent newIntent(Context context, String userId, ArrayList<PhotoParcelable> photos) {
		Intent intent = new Intent(context, UserPhotoChangeActivity.class);
		intent.putExtra(UserPhotoChangeFragment.USER_ID, userId);
		intent.putParcelableArrayListExtra(UserPhotoChangeFragment.USER_PHOTOS, photos);
		return intent;
	}

//
//	/**
//	 * User has changed photo. Use user info in result and finish activity
//	 */
	@Override
	public void finishAfterUpdate() {
		Intent intent = new Intent();
		setResult(ActivityForResult.PHOTO_CHANGED_YES, intent);
		finish();
	}
	
}
