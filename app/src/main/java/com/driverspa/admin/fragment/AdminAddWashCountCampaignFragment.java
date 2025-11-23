package com.driverspa.admin.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnFocusChange;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Washer;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminWashCountCampaignAddRequestEvent;
import com.driverspa.util.otto.ws.AdminWashCountCampaignAddResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminAddWashCountCampaignFragment extends AdminBaseFragment {
		
        public interface ActivityActions {
			public void openCampaign();
        }

	    private ActivityActions activityActions;

		@BindView(R.id.txtTitle)
		TextView title;
		@BindView(R.id.description)
		EditText description;
		@BindView(R.id.washCount)
		EditText washCount;
		Washer washer;

		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	        return inflater.inflate(R.layout.fragment_admin_add_wash_count_campaign, container, false);
	    }
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);					
		}
		
	    @Override
	    public void onViewCreated(View view, Bundle savedInstanceState) {
			ButterKnife.bind(this, view);
			Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
			TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
			titleView.setText("По количеству мойки");
//			titleView.setCompoundDrawablePadding((int) Functions.dipToPixels(getActivity(),5f));
//			titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_discount_list, 0);
			mToolbar.findViewById(R.id.action_done).setVisibility(View.VISIBLE);
			mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onAddCampaignButtonClicked();
				}
			});
			description.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_NEXT) {
						closeKeyboard();
						return true;
					}
					return false;
				}
			});

		}

	public void onAddCampaignButtonClicked(){
		if(TextUtils.isEmpty(description.getText().toString())){
			ToastUtil.display(BA.getContext(),"Введите описание акции");
			description.requestFocus();
			return;
		}

		int length = description.getText().toString().length();
		if(length < 15){
			ToastUtil.display(BA.getContext(),"Описание акции не может быть меньше 15 символов");
			description.requestFocus();
			return;
		}


		if(TextUtils.isEmpty(washCount.getText().toString())){
			washCount.requestFocus();
			ToastUtil.display(BA.getContext(),"Введите количество мойки");
			return;
		}

		int washCountInt = 0;
		try{
			washCountInt = Integer.parseInt(washCount.getText().toString());
		}
		catch(Exception e){
			washCountInt = 0;
		}
		if(washCountInt == 0){
			washCount.requestFocus();
			ToastUtil.display(BA.getContext(),"Введите количество мойки");
			return;
		}

		setWaitScreen(true);
		BA.getEventBus().post(new AdminWashCountCampaignAddRequestEvent(washer.getId(),description.getText().toString(), washCountInt));
	}

	@Subscribe
	public void onCampaignResponseReceived(AdminWashCountCampaignAddResponseEvent event){
		setWaitScreen(false);
		if(event.getData() != null){
			if(BaseAssist.isSuccess(event.getData())){
				closeKeyboard();
				activityActions.openCampaign();
				ToastUtil.display(BA.getContext(),"Успешно добавлена новая акция");
			}
			else{
				ToastUtil.display(BA.getContext(), event.getData().getMessage());
			}
		}
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {

		}
	}

	@OnFocusChange(R.id.washCount)
	public void onFocusChangeDiscount(boolean focus){
		if(!focus){
			if(TextUtils.isEmpty(washCount.getText().toString()))
				washCount.setText("0");
		}
		else{
			if(!TextUtils.isEmpty(washCount.getText().toString()) && washCount.getText().toString().equals("0")){
				washCount.setText("");
			}
		}
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
  }
