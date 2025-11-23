package com.driverspa.admin.fragment;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.driverspa.adapter.StatisticsExpandableListAdapter;
import com.driverspa.assist.BaseAssist;
import com.driverspa.db.WashmeOrmLiteSqlHelper;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.activity.AdminTransactionsActivity;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.listener.DatePickerDateSetListener;
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.Statistic;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.otto.ws.AdminStatisticsRequestEvent;
import com.driverspa.util.otto.ws.AdminStatisticsResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminStatisticsFragment extends AdminBaseFragment implements DatePickerDateSetListener {


	    public static AdminStatisticsFragment init(boolean fromWorkshift, long startDate, long endDate){
			AdminStatisticsFragment fragment = new AdminStatisticsFragment();
			Bundle b = new Bundle();
			b.putBoolean(AdminTransactionsActivity.FROM_WORKSHIFT,fromWorkshift);
			b.putLong(AdminTransactionsActivity.START_DATE, startDate);
			b.putLong(AdminTransactionsActivity.END_DATE, endDate);
			fragment.setArguments(b);

			return fragment;
		}
        public interface ActivityActions {
        }

	    private ActivityActions activityActions;

		Washer washer;
		Spinner spinner;
		ProgressBar actionBarProgress;
		Toolbar toolbar;
		View actionBarRefreshButton;
		TextView titleView;
	    @BindView(R.id.date)
		TextView date;
	    @BindView(R.id.dateTS)
		TextView dateTS;
	    @BindView(R.id.onDateLayout)
		View onDateLayout;
	    @BindView(R.id.dateIntervalLayout)
		View dateIntervalLayout;

		@BindView(R.id.expandable_list_view)
		ExpandableListView listView;
		StatisticsExpandableListAdapter adapter;
	    List<Statistic> statisticList;
		@BindView(R.id.startTime)
		TextView startTime;
		Date startTimeDt;
		@BindView(R.id.endTime)
		TextView endTime;
		Date endTimeDt;

		boolean fromWorkshift = false;
		long workShiftStartDate;
		long workShiftEndDate;

	    Long currentTime;
	    AdminSearchFilter filter = new AdminSearchFilter();

		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	        return inflater.inflate(R.layout.fragment_admin_statistics, container, false);
	    }
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);					
		}
		
	    @Override
	    public void onViewCreated(View view, Bundle savedInstanceState) {
			ButterKnife.bind(this, view);
			currentTime = (new Date()).getTime()/1000;

			fromWorkshift = getArguments().getBoolean(AdminTransactionsActivity.FROM_WORKSHIFT,false);
			if(fromWorkshift){
				 dateIntervalLayout.setVisibility(View.VISIBLE);
				 onDateLayout.setVisibility(View.GONE);
				 workShiftStartDate = getArguments().getLong(AdminTransactionsActivity.START_DATE,0);
				 workShiftEndDate = getArguments().getLong(AdminTransactionsActivity.END_DATE,0);
 				 HashMap<String,String> startTimeMap = Functions.formatUTCDate(new Date(workShiftStartDate));
				 startTime.setText(startTimeMap.get(Functions.DATE)+" "+startTimeMap.get(Functions.TIME));
				 HashMap<String,String> endTimeMap = Functions.formatUTCDate(new Date(workShiftEndDate));
				 endTime.setText(endTimeMap.get(Functions.DATE)+" "+endTimeMap.get(Functions.TIME));
				 startTimeDt = Functions.getUTCDate(new Date(workShiftStartDate));
				 endTimeDt = Functions.getUTCDate(new Date(workShiftEndDate));
			}

			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
			Date dt = new Date(currentTime*1000);
			String dateStr = formatter.format(dt);
			date.setText(dateStr);

			date.setPaintFlags(date.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

			toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
			spinner = (Spinner) toolbar.findViewById(R.id.spinner);
			actionBarProgress = (ProgressBar) toolbar.findViewById(R.id.progress);
			actionBarRefreshButton = toolbar.findViewById(R.id.refresh);
			titleView = (TextView)toolbar.findViewById(R.id.action_bar_title);
			titleView.setVisibility(View.VISIBLE);
			if(spinner != null) spinner.setVisibility(View.GONE);
			actionBarProgress.setVisibility(View.GONE);
			titleView.setText("Статистика");
			adapter = new StatisticsExpandableListAdapter(getActivity());
			actionBarRefreshButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
                  loadData(currentTime);
				}
			});
		}


	@OnClick(R.id.date)
	public void onDateClick(){
		DatePickerFragment picker =  DatePickerFragment.newInstance(currentTime*1000);
		picker.setDatePickerDateSetListener(this);
		picker.show(getChildFragmentManager(), "datePicker");
	}

	@Override
	public void onDatePickerDataset(Long time,String type) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
		Date dt = new Date(time);
		String dateStr = formatter.format(dt);
		date.setText(dateStr);
		currentTime = dt.getTime()/1000;
		loadData(currentTime);
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {
			loadData(currentTime);
		}
	}

     private void loadData(Long time){
		 setWaitScreen(true);
		 filter.setWasherId(washer.getId());
		 filter.setLimit("1000");
		 if(!fromWorkshift) {
			 filter.setLimit("1000");
			 Date dt = new Date(time * 1000);
			 Date utcDate = Functions.getUTCDate(dt);
			 Long utcTime = utcDate.getTime() / 1000;
			 filter.setDate(Long.toString(utcTime));
		 }else{
			 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			 filter.setStartDatePure(formatter.format(startTimeDt.getTime()));
			 filter.setEndDatePure(formatter.format(endTimeDt.getTime()));
		 }
		 BA.getEventBus().post(new AdminStatisticsRequestEvent(filter));
	  }

	@Subscribe
	public void onAdminBooksReceived(AdminStatisticsResponseEvent event) {
		setWaitScreen(false);
		if (event.getResult() != null) {
			if (BaseAssist.isSuccess(event.getResult())) {
               if(event.getResult().getBooks() != null && event.getResult().getBooks().size() > 0){
				   WashmeOrmLiteSqlHelper dbHelper = new WashmeOrmLiteSqlHelper(BA.getContext());
				   adapter.setData(dbHelper.getStatisticData(washer,new Date()));
				   listView.setAdapter(adapter);
				   adapter.notifyDataSetChanged();
			   }
				else{
				   WashmeOrmLiteSqlHelper dbHelper = new WashmeOrmLiteSqlHelper(BA.getContext());
				   adapter.setData(dbHelper.getStatisticData(washer,new Date()));
				   listView.setAdapter(adapter);
				   adapter.notifyDataSetChanged();
			   }
			}
			else{
				WashmeOrmLiteSqlHelper dbHelper = new WashmeOrmLiteSqlHelper(BA.getContext());
				adapter.setData(dbHelper.getStatisticData(washer,new Date()));
				listView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		}
		else{
			WashmeOrmLiteSqlHelper dbHelper = new WashmeOrmLiteSqlHelper(BA.getContext());
			adapter.setData(dbHelper.getStatisticData(washer,new Date()));
			listView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}

		for(int i=0; i < adapter.getGroupCount(); i++)
			listView.expandGroup(i);
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
