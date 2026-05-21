package com.driverspa.admin.fragment;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;
import com.todddavies.components.progressbar.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.WashCountCounterAdapter;
import com.driverspa.model.Campaign;
import com.driverspa.model.CampaignType;
import com.driverspa.model.WashCountCampaign;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.DeleteWashCountCampaign;
import com.driverspa.util.otto.FragmentVisibilityEvent;
import com.driverspa.util.otto.ws.AdminCampaignDeleteRequestEvent;
import com.driverspa.util.otto.ws.AdminCampaignDeleteResponseEvent;
import com.driverspa.util.otto.ws.AdminCampaignListRequestEvent;
import com.driverspa.util.otto.ws.AdminCampaignListResponseEvent;
import com.driverspa.util.otto.ws.AdminWashCountCampaignDeleteRequestEvent;
import com.driverspa.util.otto.ws.AdminWashCountCampaignDeleteResponseEvent;
import com.driverspa.util.otto.ws.AdminWashCountCampaignListRequestEvent;
import com.driverspa.util.otto.ws.AdminWashCountCampaignListResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.ExpandableListView;

public class AdminCampaignFragment extends AdminBaseFragment{

	public static final int MAX_SECS_TO_REFRESH = 10;

    public interface ActivityActions {
		public void openAddCampaign();
		public void openAddWashCountCampaign();
	}

	private ActivityActions activityActions;

    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.endDate)
    TextView endDate;
    @BindView(R.id.discount)
    TextView discount;
    @BindView(R.id.campaignType)
    TextView campaignType;
    Campaign campaign;
    Washer washer;

	boolean loading = false;
	private Handler customHandler = new Handler();

	@BindView(R.id.months)
	TextView months;
	@BindView(R.id.months_label)
	TextView monthsLabel;
	@BindView(R.id.days)
	TextView days;
	@BindView(R.id.days_label)
	TextView daysLabel;
	@BindView(R.id.hours)
	TextView hours;
	@BindView(R.id.hours_label)
	TextView hoursLabel;
	@BindView(R.id.minutes)
	TextView minutes;
	@BindView(R.id.minutes_label)
	TextView minutesLabel;
	@BindView(R.id.seconds)
	TextView seconds;
	@BindView(R.id.seconds_label)
	TextView secondsLabel;
	Long lastRemainingSeconds;
	@BindView(R.id.remaining_time_unknown)
	TextView timeUnknown;
	@BindView(R.id.times_layout)
	View timesLayout;
	@BindView(R.id.mon_days_layout)
	View monthsDaysLayout;
	@BindView(R.id.campaignLayout)
	View campaignLayout;
	@BindView(R.id.washCountCampaignLayout)
	View washCountCampaignLayout;
	@BindView(R.id.btnAddCampaign)
	View buttonAddCampaign;
	@BindView(R.id.btnDeleteCampaign)
	View buttonDeleteCampaign;
	@BindView(R.id.noWashCountCampaign)
	View noWashCountCampaign;
	@BindView(R.id.noCampaign)
	View noCampaign;

	@BindView(R.id.list_view)
	ExpandableListView listView;
	WashCountCounterAdapter adapter;
    List<WashCountCampaign> washCountCampaignList;

	String[] daysArr;
	String[] hoursArr;
	String[] minutesArr;
	String[] secondsArr;
	boolean runned;
	int progress = 0;
	int currentProgress;
	@BindView(R.id.progress_wheel)
	ProgressWheel progressWheel;
	TextView titleView;
	Thread wheelInitialAnimationThread;
	Toolbar mToolbar;

	private Runnable updateTimerThread = new Runnable() {
		public void run() {
			customHandler.postDelayed(this, 1000);
			if(lastRemainingSeconds!=null) {
				lastRemainingSeconds = lastRemainingSeconds - 1;
				//Refresh data from server every MAX_SECS_TO_REFRESH
				calculateRemainingTime(lastRemainingSeconds);
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_admin_campaign, container, false);
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
		
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		ButterKnife.bind(this, view);
		mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
		titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("Акция");
//		titleView.setCompoundDrawablePadding((int)Functions.dipToPixels(getActivity(),5f));
//		titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_discount_list, 0);

		mToolbar.findViewById(R.id.action_done).setVisibility(View.GONE);
		campaignLayout.setVisibility(View.GONE);
		buttonAddCampaign.setVisibility(View.GONE);

		daysArr = getResources().getStringArray(R.array.day);
		hoursArr = getResources().getStringArray(R.array.hour);
		minutesArr = getResources().getStringArray(R.array.minute);
		secondsArr = getResources().getStringArray(R.array.second);
		wheelInitialAnimationThread = new Thread(r);
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {
			setWaitScreen(true);
			BA.getEventBus().post(new AdminCampaignListRequestEvent(washer.getId()));
			BA.getEventBus().post(new AdminWashCountCampaignListRequestEvent(washer.getId()));
		}
	}

	@Subscribe
	public void onAdminWashCountCampaignListReceived(AdminWashCountCampaignListResponseEvent event) {
		setWaitScreen(false);
		if(event.getData() != null) {
			if (BaseAssist.isSuccess(event.getData())) {
				washCountCampaignList = event.getData().getResponse().getCampaigns();
				washer.setWashCountCampaigns(washCountCampaignList);
				UserPreferences.putWasherData(BA.getContext(),washer.serialize());
				if(washCountCampaignList!=null && washCountCampaignList.size() > 0) {
					noWashCountCampaign.setVisibility(View.GONE);
					adapter = new WashCountCounterAdapter(getActivity());
					adapter.set(washCountCampaignList);
					listView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
				}
				else{
					noWashCountCampaign.setVisibility(View.VISIBLE);
					adapter = new WashCountCounterAdapter(getActivity());
					adapter.set(washCountCampaignList);
					listView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
//					addEmptyTextView(listView, "Нет акции по количеству мойки");
				}
			}
			else{
				noWashCountCampaign.setVisibility(View.VISIBLE);
//				addEmptyTextView(listView, "Нет акции по количеству мойки");
			}
		}
		else{
			noWashCountCampaign.setVisibility(View.VISIBLE);
//			addEmptyTextView(listView, "Нет акции по количеству мойки");
		}
	}

	@Subscribe
	public void onAdminCampaignListReceived(AdminCampaignListResponseEvent event){
		if(event.getData() != null){
			if(BaseAssist.isSuccess(event.getData())) {
				ArrayList<Campaign> campaigns = event.getData().getResponse().getCampaigns();
				if(campaigns!= null && campaigns.size() > 0) {
					campaign = campaigns.get(0);

					if(campaign.getEndTime() != null) {
						noCampaign.setVisibility(View.GONE);
						buttonDeleteCampaign.setVisibility(View.VISIBLE);

									long utcDate = campaign.getTs().toEpochMilli();
									long utcEndDate = campaign.getEndTime().toEpochMilli();

									lastRemainingSeconds = (utcEndDate - System.currentTimeMillis())/1000;
						if(lastRemainingSeconds < 0) {
							campaignLayout.setVisibility(View.GONE);
							buttonAddCampaign.setVisibility(View.VISIBLE);
						}
						campaignLayout.setVisibility(View.VISIBLE);
						buttonAddCampaign.setVisibility(View.GONE);

						setData(campaign);
									long totalSecs = (utcEndDate - utcDate)/1000;
						double doubleProgress = ((double)(totalSecs-lastRemainingSeconds)/(double)totalSecs)*360;
						currentProgress = (int)doubleProgress;
						if (!runned)
							wheelInitialAnimationThread.start();
						else
							incrementProgressWheel(currentProgress);
						timeUnknown.setVisibility(View.GONE);
						timesLayout.setVisibility(View.VISIBLE);
						monthsDaysLayout.setVisibility(View.VISIBLE);
					}
					else{
						timeUnknown.setVisibility(View.VISIBLE);
						timesLayout.setVisibility(View.GONE);
						monthsDaysLayout.setVisibility(View.GONE);
					}
				}
				else{
					buttonDeleteCampaign.setVisibility(View.GONE);
					noCampaign.setVisibility(View.VISIBLE);
					campaignLayout.setVisibility(View.GONE);
					buttonAddCampaign.setVisibility(View.VISIBLE);
//					activityActions.openAddCampaign();
				}
			}
			else{
				buttonDeleteCampaign.setVisibility(View.GONE);
				noCampaign.setVisibility(View.VISIBLE);
				ToastUtil.display(BA.getContext(),event.getData().getMessage());
			}
		}
		else {
			buttonDeleteCampaign.setVisibility(View.GONE);
			noCampaign.setVisibility(View.VISIBLE);
			campaignLayout.setVisibility(View.GONE);
			buttonAddCampaign.setVisibility(View.GONE);
		}
	}

	private void setData(Campaign campaign){
		description.setText(campaign.getDescription());
		discount.setText("-"+campaign.getCampaignDiscount()+"%");
		if(campaign.getCampaignType() == CampaignType.Both){
			campaignType.setText("Распространяется на все виды мойки (Онлайн бронирование, живая очередь)");
		}
		else if(campaign.getCampaignType() == CampaignType.Online){
			campaignType.setText("Распространяется только на онлайн бронирования");
		}
		else if(campaign.getCampaignType() == CampaignType.Offline){
			campaignType.setText("Распространяется только на живую очередь");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if(titleView != null)
		   titleView.setText("Акция");
		if(customHandler != null && updateTimerThread != null)
			customHandler.postDelayed(updateTimerThread, 0);
		BA.getEventBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		customHandler.removeCallbacks(updateTimerThread);
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

	private void calculateRemainingTime(Long remainingSeconds) {
		if (monthsDaysLayout.getVisibility() == View.VISIBLE && remainingSeconds > 0) {
			long diff = remainingSeconds;
			long diffSeconds = diff % 60;
			long diffMinutes = (diff / (60)) % 60;
			long diffHours = (diff / (60 * 60)) % 24;
			long diffDays = diff / (24 * 60 * 60);
			long diffMonths = diff / (30 * 24 * 60 * 60);

			months.setText(Long.toString(diffMonths));
			days.setText(String.format("%01d", diffDays));
			hours.setText(String.format("%02d", diffHours));
			minutes.setText(String.format("%02d", diffMinutes));
			seconds.setText(String.format("%02d", diffSeconds));

			daysLabel.setText(Functions.getRussianFineLabel(Long.toString(diffDays), daysArr));
			hoursLabel.setText(Functions.getRussianFineLabel(Long.toString(diffHours), hoursArr));
			minutesLabel.setText(Functions.getRussianFineLabel(Long.toString(diffMinutes), minutesArr));
			secondsLabel.setText(Functions.getRussianFineLabel(Long.toString(diffSeconds), secondsArr));
		}
		else{
			activityActions.openAddCampaign();
		}
	 }


	private void incrementProgressWheel(int currentProgress) {
		progress = 0;
		progressWheel.resetCount();
		while (progress < currentProgress) {
			progressWheel.incrementProgress();
			progress++;
		}
	}

	final Runnable r = new Runnable() {
		public void run() {
			runned = true;
			while (progress < currentProgress) {
				progressWheel.incrementProgress();
				progress++;
				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		}
	};

	public void showOptions(){
				AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
				b.setItems(R.array.campaign_options, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						switch (i) {
							case 0:
								setWaitScreen(true);
								BA.getEventBus().post(new AdminCampaignDeleteRequestEvent(washer.getId(),campaign.getId()));
								break;
						}
					}
				});
//				b.setTitle("Услуга оказана клиенту "+book.getClientKey()+"?");
//						b.setNeutralButton("Отменить", null);
				b.show();
	}

	@Subscribe
	public void setFragmentVisibility(FragmentVisibilityEvent event) {
		setWaitScreen(true);
		if (washer != null) {
			setWaitScreen(true);
			mToolbar.findViewById(R.id.action_done).setVisibility(View.GONE);
			titleView.setText("Акция");
			BA.getEventBus().post(new AdminCampaignListRequestEvent(washer.getId()));
			BA.getEventBus().post(new AdminWashCountCampaignListRequestEvent(washer.getId()));
		}
	}

	@Subscribe
	public void onAdminCampaignDeleteReceived(AdminCampaignDeleteResponseEvent event) {
		setWaitScreen(false);
		if (event.getData() != null) {
			if (BaseAssist.isSuccess(event.getData())) {
				buttonDeleteCampaign.setVisibility(View.GONE);
				campaignLayout.setVisibility(View.GONE);
				buttonAddCampaign.setVisibility(View.VISIBLE);
				noCampaign.setVisibility(View.VISIBLE);
			}
			else{
				ToastUtil.display(getActivity(),event.getData().getMessage());
			}
		}
	}

	@Subscribe
	public void onAdminWashCountCampaignDeleteReceived(AdminWashCountCampaignDeleteResponseEvent event) {
		setWaitScreen(false);
		if (event.getData() != null) {
			if (BaseAssist.isSuccess(event.getData())) {
				BA.getEventBus().post(new AdminWashCountCampaignListRequestEvent(washer.getId()));
			}
			else{
				ToastUtil.display(getActivity(),event.getData().getMessage());
			}
		}
	}

	@Subscribe
	public void onAdminWashCountDeleteRequest(final DeleteWashCountCampaign event) {
		setWaitScreen(false);
		if (event.getCampaign() != null) {
			AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
			b.setItems(R.array.campaign_options, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					switch (i) {
						case 0:
							setWaitScreen(true);
							BA.getEventBus().post(new AdminWashCountCampaignDeleteRequestEvent(washer.getId(),event.getCampaign().getId()));
							break;
					}
				}
			});
			b.show();
		}
	}

	@OnClick(R.id.btnAddCampaign)
	public void onAddCampaignButtonClicked(){
		activityActions.openAddCampaign();
	}

	@OnClick(R.id.btnAddWashCountCampaign)
	public void onAddWashCountCampaignButtonClicked(){
		activityActions.openAddWashCountCampaign();
	}

	@OnClick(R.id.btnDeleteCampaign)
	public void onDeleteCampaignButtonClicked(){
		AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
		b.setItems(R.array.campaign_options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				switch (i) {
					case 0:
						setWaitScreen(true);
						BA.getEventBus().post(new AdminCampaignDeleteRequestEvent(washer.getId(),campaign.getId()));
						break;
				}
			}
		});
		b.show();
	}
}
