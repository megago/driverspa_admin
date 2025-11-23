package com.driverspa.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.UserPhotosAdapter;
import com.driverspa.model.PhotoParcelable;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.UserPhotoChangeRequestEvent;
import com.driverspa.util.otto.ws.UserPhotoChangeResponseEvent;

public class UserPhotoChangeFragment extends BaseFragment {

	public static final String USER_ID = "USER_ID";
	public static final String USER_PHOTOS = "USER_PHOTOS";
	public static final String USER_DATA_JSON = "USER_DATA_JSON";
	
	public interface ActivityActions {
		public void finishAfterUpdate();
	}
	
	@BindView(R.id.gridview)
	protected GridView gridView;

	protected String userId;
	protected UserPhotosAdapter photosAdapter;

	protected ActivityActions activityActions;
	
	public static UserPhotoChangeFragment newInstance(String userId, ArrayList<PhotoParcelable> photos) {
		UserPhotoChangeFragment fragment = new UserPhotoChangeFragment();
		Bundle bundle = new Bundle();
		bundle.putString(USER_ID,  userId);
		bundle.putParcelableArrayList(USER_PHOTOS, photos);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		userId = getArguments().getString(USER_ID);
		ArrayList<PhotoParcelable> photos = getArguments().getParcelableArrayList(USER_PHOTOS);
		
		photosAdapter = new UserPhotosAdapter(getActivity());
		photosAdapter.set(photos);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_user_photo_change, container, false);
		ButterKnife.bind(this, view);

		gridView.setAdapter(photosAdapter);

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PhotoParcelable photo = photosAdapter.getItem(position);
				BA.getEventBus().post(new UserPhotoChangeRequestEvent(userId, photo.getId()));
			}
		});
		return view;
	}
	
	@Subscribe
	public void onUserPhotoChangeResponseEventReceived(UserPhotoChangeResponseEvent event) {
		if(event.getData() != null){			
		  if(BaseAssist.isSuccess(event.getData())){
			 if( activityActions != null )
				activityActions.finishAfterUpdate();
			 }
		  else{
			  ToastUtil.display(getActivity(), event.getData().getMessage());
		  }
		}		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		BA.getEventBus().register(this);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityActions = (ActivityActions) activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		activityActions = null;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
	}

	@Override 
	public void onDestroyView() {
		super.onDestroyView();
//		ButterKnife.reset(this);
	}

}
