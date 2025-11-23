package com.driverspa.admin.fragment;

import static com.driverspa.util.Constants.BOTH;
import static com.driverspa.util.Constants.OFFLINE;
import static com.driverspa.util.Constants.ONLINE;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.listener.DatePickerDateSetListener;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminCampaignAddRequestEvent;
import com.driverspa.util.otto.ws.AdminCampaignAddResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminAddCampaignFragment extends AdminBaseFragment implements DatePickerDateSetListener {
		
        public interface ActivityActions {
			public void openCampaign();
        }

	    private ActivityActions activityActions;

		String boxId;
		@BindView(R.id.txtTitle)
		TextView title;
		@BindView(R.id.description)
		EditText description;
		@BindView(R.id.endDate)
		TextView endDate;
		@BindView(R.id.discount)
		EditText discount;
		@BindView(R.id.offlineImage)
		ImageView offlineImage;
		@BindView(R.id.onlineImage)
		ImageView onlineImage;
		@BindView(R.id.bothImage)
		ImageView bothImage;
		String campaignType;
		Washer washer;
		@BindView(R.id.both)
		View both;
		Date endDateDT;

		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	        return inflater.inflate(R.layout.fragment_admin_add_campaign, container, false);
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
			titleView.setText("Новая акция");
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
						endDateClicked();
						return true;
					}
					return false;
				}
			});

			campaignType = Constants.ONLINE;
			setSelectedCampaignType();
		}


	private void setSelectedCampaignType(){

		onlineImage.setImageResource(android.R.color.transparent);
		offlineImage.setImageResource(android.R.color.transparent);
		bothImage.setImageResource(android.R.color.transparent);
		if(campaignType.equals(ONLINE))
			onlineImage.setImageResource(R.drawable.ic_checkmark);
		else if(campaignType.equals(OFFLINE))
			offlineImage.setImageResource(R.drawable.ic_checkmark);
		else if(campaignType.equals(BOTH))
			bothImage.setImageResource(R.drawable.ic_checkmark);

	}

//	@OnClick(R.id.online)
//	public void onOnlineButtonClicked(){
//		campaignType = ONLINE;
//		setSelectedCampaignType();
//	}
//
//	@OnClick(R.id.offline)
//	public void onOfflineButtonClicked(){
//		campaignType = OFFLINE;
//		setSelectedCampaignType();
//	}
//
//	@OnClick(R.id.both)
//	public void onBothButtonClicked(){
//		campaignType = BOTH;
//		setSelectedCampaignType();
//	}

	public void onAddCampaignButtonClicked(){
		if(TextUtils.isEmpty(description.getText().toString())){
			ToastUtil.display(BA.getContext(),"Введите описание акции");
			description.requestFocus();
			return;
		}

		int length = description.getText().toString().length();
		if(length < 30){
			ToastUtil.display(BA.getContext(),"Описание акции не может быть меньше 30 символов");
			description.requestFocus();
			return;
		}

		if(TextUtils.isEmpty(endDate.getText().toString())){
			ToastUtil.display(BA.getContext(),"Введите дата окончания акции");
			return;
		}

		if(endDateDT != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(endDateDT);
			cal.set(Calendar.HOUR_OF_DAY,23);
			cal.set(Calendar.MINUTE,59);
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MILLISECOND,0);
			endDateDT = cal.getTime();
		}

		if(TextUtils.isEmpty(discount.getText().toString())){
			discount.requestFocus();
			ToastUtil.display(BA.getContext(),"Введите скидку для акции");
			return;
		}

		int discountInt = 0;
		try{
			discountInt = Integer.parseInt(discount.getText().toString());
		}
		catch(Exception e){
			discountInt = 0;
		}
		if(discountInt == 0){
			discount.requestFocus();
			ToastUtil.display(BA.getContext(),"Введите скидку для акции");
			return;
		}

		setWaitScreen(true);
		BA.getEventBus().post(new AdminCampaignAddRequestEvent(washer.getId(),description.getText().toString(), campaignType, endDateDT,discountInt));
	}

	@Subscribe
	public void onCampaignResponseReceived(AdminCampaignAddResponseEvent event){
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

	@OnFocusChange(R.id.discount)
	public void onFocusChangeDiscount(boolean focus){
		if(!focus){
			if(TextUtils.isEmpty(discount.getText().toString()))
				discount.setText("0");
		}
		else{
			if(!TextUtils.isEmpty(discount.getText().toString()) && discount.getText().toString().equals("0")){
				discount.setText("");
			}
		}
	}

	@Override
	public void onDatePickerDataset(Long time, String type) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
		Date dt = new Date(time);
		String dateStr = formatter.format(dt);
		endDate.setText(dateStr);
		endDateDT = dt;
	}

	@OnClick(R.id.endDate)
	public void endDateClicked(){
		if(endDateDT == null)
			endDateDT = new Date();
		DatePickerFragment picker =  DatePickerFragment.newInstance("end_date", (int) endDateDT.getTime());
		picker.setDatePickerDateSetListener(this);
		picker.show(getChildFragmentManager(), "datePicker");
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
