package com.driverspa.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;

/**
 * @author Yerzhan
 * 
 */

public class MainFragment extends BaseFragment {

	public interface ActivityActions {
		public void openAdminRegistration();
		public void openAdminHomeActivity();		
	}

	private final String TAG = "MainFragment";

	private ActivityActions activityActions;

	@BindView(R.id.btnUserStart)
	Button userStartButton;
	@BindView(R.id.btnWasherStart)
	Button userWasherButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		ButterKnife.bind(this, view);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
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

	@OnClick(R.id.btnWasherStart)
	protected void processWasherStartClicked() {
		activityActions.openAdminRegistration();
	}

	@Override
	public void onResume() {
		super.onResume();
		BA.getEventBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
	}
}
