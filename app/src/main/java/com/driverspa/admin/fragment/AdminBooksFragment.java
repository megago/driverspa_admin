package com.driverspa.admin.fragment;

import static com.driverspa.util.Constants.*;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

import com.driverspa.adapter.AdminBooksAdapter;
import com.driverspa.assist.BaseAssist;
import com.google.android.material.appbar.AppBarLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.AdminQueueListAdapter;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.listener.DatePickerDateSetListener;
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.BookInfo;
import com.driverspa.model.LocalBookingList;
import com.driverspa.model.PushData;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.BookOptionClickEvent;
import com.driverspa.util.otto.NewBookingPushRequestEvent;
import com.driverspa.util.otto.NewBookingPushResponseEvent;
import com.driverspa.util.otto.PushUpdateListRequestEvent;
import com.driverspa.util.otto.RemoveBulkBookingPushRequestEvent;
import com.driverspa.util.otto.ws.AdminBookApproveRequestEvent;
import com.driverspa.util.otto.ws.AdminBookApproveResponseEvent;
import com.driverspa.util.otto.ws.AdminBookCancelRequestEvent;
import com.driverspa.util.otto.ws.AdminBookCancelResponseEvent;
import com.driverspa.util.otto.ws.AdminBookNoClientRequestEvent;
import com.driverspa.util.otto.ws.AdminBookNoClientResponseEvent;
import com.driverspa.util.otto.ws.AdminBookStopRequestEvent;
import com.driverspa.util.otto.ws.AdminBookStopResponseEvent;
import com.driverspa.util.otto.ws.AdminBooksRequestEvent;
import com.driverspa.util.otto.ws.AdminBooksResponseEvent;
import com.driverspa.util.otto.ws.AdminFinishQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminFinishQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminMarkAsClientPaidRequestEvent;
import com.driverspa.util.otto.ws.AdminMarkAsClientPaidResponseEvent;
import com.driverspa.util.otto.ws.AdminPostponeQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminPostponeQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminQueuedBooksRequestEvent;
import com.driverspa.util.otto.ws.AdminQueuedBooksResponseEvent;
import com.driverspa.util.otto.ws.AdminRejectQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminRejectQueueResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.DividerItemDecoration;
import com.driverspa.view.NDSpinner;
import com.driverspa.view.ProgressViewHolder;
import com.driverspa.view.TwoWayView;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AdminBooksFragment extends AdminBaseFragment implements View.OnClickListener,DatePickerDateSetListener {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());

	public interface ActivityActions {
    	   public void openBookInfo(String bookId);
    	   public void openAddQuee();
		   public void editEntry(BookInfo bookInfo);
    	   public void openQueueInfo(BookInfo bookInfo);
    	   public void openQueueInfoWithTransition(BookInfo bookInfo,View sharedView);
        }

        public static final String ALL = "all";
        private ActivityActions activityActions;
	    private int numberPage = 0;
	    private AdminBooksAdapter listAdapter;

		@BindView(R.id.recycler_linear)
		RecyclerView mRecyclerLinear;

    	@BindView(R.id.noInternetLayout)
		LinearLayout noInternetLayout;
	    @BindView(R.id.progressBar)
		ProgressBar progressBar;
	    @BindView(R.id.repeatConnect)
		Button repeatConnect;
	    @BindView(R.id.noInternetMessage)
	    TextView noIternetMsg;

		@BindView(R.id.btnPending)
		TextView buttonPending;
		@BindView(R.id.btnToday)
		TextView buttonToday;
		@BindView(R.id.btnTomorrow)
		TextView buttonTomorrow;

	    @BindView(R.id.noRecords)
		TextView noRecords;
		@BindView(R.id.bookBtn)
		View bookButton;

		@BindView(R.id.queue_horizontal_list)
		protected TwoWayView horizontalList;
		@BindView(R.id.queueLayout)
		protected View queueLayout;

	    private AdminQueueListAdapter queueListAdapter;
	    private List<BookInfo> bookInfoAdapterData;
		private boolean loading = false;
		private boolean noData = false;
		private String selectedDay;
	    AdminSearchFilter filter = new AdminSearchFilter();
	    AdminSearchFilter queuedFilter = new AdminSearchFilter();
	    private boolean lazyLoading = false;
	    DividerItemDecoration itemDecoration;
		HashMap<String,PushData> bookingPushData;
		Washer washer;
	    NDSpinner spinner;
	    ProgressBar actionBarProgress;
	    Toolbar toolbar;
		AppBarLayout appBarLayout;
	    View actionBarRefreshButton;
	    boolean visible = false;
		boolean selectionWithoutUpdate = false;
	    View addQueueButton;
		View addBookButton;
	    boolean loadingWithoutLoader = false;
		private Handler handler;
		long lastUpdateTime = System.currentTimeMillis();
		private boolean isSpinnerTouched = false;
	    Long currentTime = -1L;

	    boolean loadWithUpdate = false;
		private Runnable runnable = new Runnable() {
			@Override
			public void run() {
				long currentTime = System.currentTimeMillis();
				long diffMinutes = (currentTime - lastUpdateTime) / (60 * 1000);
				if(diffMinutes > 5 && numberPage == 0){
					loadDataWithoutLoader();
				}
				handler.postDelayed(runnable, 30000);
			}
		};

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_admin_books, container, false);
			return view;
	    }
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}

	    @Override
	    public void onViewCreated(View view, Bundle savedInstanceState) {
			ButterKnife.bind(AdminBooksFragment.this,view);
			handler = new Handler();
			itemDecoration = new DividerItemDecoration(getActivity());
			selectedDay = PENDING;
			filter.setOrderBy("time");
			buttonPending.setSelected(true);

			toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
			appBarLayout = (AppBarLayout)getActivity().findViewById(R.id.appbar);
			spinner = (NDSpinner) toolbar.findViewById(R.id.spinner);
			actionBarProgress = (ProgressBar) toolbar.findViewById(R.id.progress);
			actionBarRefreshButton = toolbar.findViewById(R.id.refresh);

			addQueueButton = getActivity().findViewById(R.id.button_queue_book);
			addBookButton = getActivity().findViewById(R.id.button_add_book);

			addQueueButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
						if(washer != null && (washer.getStatus().equals(APPROVED) || washer.getStatus().equals(TEST))){
						  activityActions.openAddQuee();
						}
						else
						 ToastUtil.display(BA.getContext(),BA.str(R.string.wash_not_activated_short));
				    }
			});
			actionBarRefreshButton.setOnClickListener(this);
			addBookButton.setVisibility(View.GONE);
			createActionBarSpinner();
			queueListAdapter = new AdminQueueListAdapter(getActivity(),getActivity());
			makeList();

	        noInternetLayout.setVisibility(View.GONE);

			spinner.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					isSpinnerTouched = true;
					return false;
				}
			});
		}

	public void onDateClick(){
		DatePickerFragment picker =  DatePickerFragment.newInstance(currentTime*1000);
		picker.setDatePickerDateSetListener(this);
		picker.show(getChildFragmentManager(), "datePicker");
	}

	@OnClick(R.id.repeatConnect)
	   public void onRepeatConnect(){
				if (Functions.checkOnline(getActivity())) {
					loadData(numberPage, false);
				} else {					
					progressBar.setVisibility(View.GONE);
					noInternetLayout.setVisibility(View.VISIBLE);
			}
       }
      
	   private void loadData(int numberPage, boolean lazyLoading) {
		   loadingWithoutLoader = false;
		   lastUpdateTime = System.currentTimeMillis();
			   if (!lazyLoading) {
				   noData = false;
			   }
			   actionBarRefreshButton.setVisibility(View.GONE);
			   actionBarProgress.setVisibility(View.VISIBLE);
			   this.lazyLoading = lazyLoading;
			   this.loading = true;
			   this.numberPage = numberPage;
			   filter.setDay(selectedDay);

			   if (selectedDay.equals(PENDING)) {
				   filter.setLimit("200");
				   filter.setOffset(Integer.toString(numberPage * 200));
			   } else {
				   filter.setLimit("20");
				   filter.setOffset(Integer.toString(numberPage * 20));
			   }

			   java.time.Instant dt = java.time.Instant.ofEpochSecond(currentTime);
			   java.time.Instant utcDate = Functions.getUTCDate(dt);
			   long utcTime = utcDate.getEpochSecond();
			   filter.setDate(Long.toString(utcTime));
			   BA.getEventBus().post(new AdminBooksRequestEvent(filter));

			   //load queued books list
			   BA.getEventBus().post(new AdminQueuedBooksRequestEvent(queuedFilter));
		}

	private void loadDataWithoutLoader() {
    		loadingWithoutLoader = true;
			lastUpdateTime = System.currentTimeMillis();
			noData = false;

			this.numberPage = 0;
			filter.setDay(selectedDay);
			if (selectedDay.equals(PENDING)) {
				filter.setLimit("200");
				filter.setOffset(Integer.toString(numberPage * 200));
			}
			else {
				filter.setLimit("20");
				filter.setOffset(Integer.toString(numberPage * 20));
			}
			java.time.Instant dt = java.time.Instant.ofEpochSecond(currentTime);
			java.time.Instant utcDate = Functions.getUTCDate(dt);
			long utcTime = utcDate.getEpochSecond();
			filter.setDate(Long.toString(utcTime));

			BA.getEventBus().post(new AdminBooksRequestEvent(filter));
			//load queued books list
			BA.getEventBus().post(new AdminQueuedBooksRequestEvent(queuedFilter));
	}

		private void makeList() {
			bookInfoAdapterData = new ArrayList<BookInfo>();
			listAdapter = new AdminBooksAdapter(getActivity(), bookInfoAdapterData, washer, bookingPushData);
			listAdapter.setData(bookInfoAdapterData);
			createListLayout();
		}

	   private void createListLayout(){
			((AdminBooksAdapter) listAdapter).setOnItemClickListener(new AdminBooksAdapter.OnItemClickListener() {
				@Override
				public void onItemClick(View view, int position) {
					final BookInfo book = (BookInfo)((bookInfoAdapterData.get(position)));
					loadWithUpdate = true;
					activityActions.openBookInfo(book.getId());
				}
			});

			LinearLayoutManager manager = new LinearLayoutManager(getActivity());
			manager.setOrientation(LinearLayoutManager.VERTICAL);
			mRecyclerLinear.setLayoutManager(manager);
			mRecyclerLinear.setAdapter(listAdapter);
		    mRecyclerLinear.removeItemDecoration(itemDecoration);
		    mRecyclerLinear.addItemDecoration(itemDecoration);
			createScrollListener(mRecyclerLinear, manager, listAdapter);
	}

	private void createScrollListener(final RecyclerView mRecyclerView, final LinearLayoutManager mLayoutManager, final AdminBooksAdapter adapter) {
		mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				int totalItem = mLayoutManager.getItemCount();
				int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
				if (!noData && !loading && lastVisibleItem == totalItem - 1) {
					if (bookInfoAdapterData.size() > 0 && (numberPage+1)*20 == bookInfoAdapterData.size() ) {
						View view = getActivity().getLayoutInflater().inflate(R.layout.progress_item, mRecyclerView, false);
						ProgressViewHolder holder = new ProgressViewHolder(view);
						adapter.addFooterView(VIEW_FOOTER, holder);
						numberPage++;
						loadData(numberPage,true);
					}
				}
			}
		});
	}
				
		/**
		 * Received location and request people nearby
		 * @param event
		 */
		@Subscribe
		public void onAdminBooksReceived(AdminBooksResponseEvent event) {
			setWaitScreen(false);
			loading = false;
			if(visible) {
				actionBarRefreshButton.setVisibility(View.VISIBLE);
				actionBarProgress.setVisibility(View.GONE);
			}

			if(washer!=null && listAdapter!=null) listAdapter.setWasher(washer);
			noRecords.setVisibility(View.GONE);
			listAdapter.removeFooterView(VIEW_FOOTER);

			if(selectedDay.equals(PENDING) && !lazyLoading) {
				if (bookingPushData != null && bookingPushData.size() > 0)
					BA.getEventBus().post(new RemoveBulkBookingPushRequestEvent());
			}

			if(event.getResult() != null){
			  if(BaseAssist.isSuccess(event.getResult())){
				  noInternetLayout.setVisibility(View.GONE);
				  progressBar.setVisibility(View.GONE);
				  LocalBookingList localList = new LocalBookingList();
				  List<BookInfo> localBooksList = new ArrayList<BookInfo>();

				  removeApprovedTomorrowBooksForPending(event.getBooks());

				  if(!(event.getBooks() != null && event.getBooks().size() > 0)){
					  noData = true;
					  if(!lazyLoading) {
						  bookInfoAdapterData.clear();
						  listAdapter.notifyDataSetChanged();
						  noRecords.setVisibility(View.VISIBLE);
						  if(washer != null && (washer.getStatus().equals(APPROVED) || washer.getStatus().equals(TEST))){
							  if(selectedDay.equals(PENDING)){
								  noRecords.setText(BA.str(R.string.no_orders_to_process));
							  }
							  else if(selectedDay.equals(TODAY)){
								  noRecords.setText(BA.str(R.string.no_wash_today));
							  }
							  else if(selectedDay.equals(TOMORROW)){
								  noRecords.setText(BA.str(R.string.no_orders_tomorrow));
							  }
							  else if(selectedDay.equals(ALL)){
								  noRecords.setText(BA.str(R.string.no_orders_history));
							  }
							  else if(selectedDay.equals(FOR_DATE)){
								  String dateStr = DATE_FORMATTER.format(java.time.Instant.ofEpochSecond(currentTime));
								  noRecords.setText(BA.str(R.string.no_orders_on_date)+dateStr);
							  }
						  }
						  else
							  noRecords.setText(BA.str(R.string.wash_not_activated_full));
					  }
				  }
				  else{
					  if(!lazyLoading){
						  if(!loadingWithoutLoader) {
							  bookInfoAdapterData.clear();
							  bookInfoAdapterData.addAll(event.getBooks());
							  sortWithPendingStatus(bookInfoAdapterData);
							  listAdapter.notifyDataSetChanged();
							  loadWithUpdate = false;
						  }
						  else{
								  bookInfoAdapterData.clear();
								  bookInfoAdapterData.addAll(event.getBooks());
								  sortWithPendingStatus(bookInfoAdapterData);
								  listAdapter.notifyDataSetChanged();
								  loadWithUpdate = false;
						  }
					  }
					  else{ //Lazy loading data insert
						  bookInfoAdapterData.addAll(event.getBooks());
						  sortWithPendingStatus(bookInfoAdapterData);
						  listAdapter.notifyDataSetChanged();
					  }


					  for(BookInfo item : event.getBooks()){
						  if(item.getStatus().equals(PENDING)) {
							  PushData data = new PushData(item.getId());
							  data.setType("booking_"+item.getStatus());
							  data.setText("");
							  data.setObjectId(item.getId());
							  localBooksList.add(item);
							  BA.getEventBus().post(new NewBookingPushRequestEvent(data));
						  }
					  }
				  }
   				  localList.setBooks(localBooksList);
				  UserPreferences.putLocalBookingList(getActivity(),localList.serialize());
			  }
			  else{
	             if(!lazyLoading) {
					 noInternetLayout.setVisibility(View.GONE);
					 progressBar.setVisibility(View.GONE);
					 if(bookInfoAdapterData != null && bookInfoAdapterData.size() > 0) {
						 noRecords.setVisibility(View.GONE);
					 }
					 else
						 noRecords.setVisibility(View.VISIBLE);
					 ToastUtil.display(getActivity(), event.getResult().getMessage());
				 }
				  else{
					 noInternetLayout.setVisibility(View.GONE);
					 progressBar.setVisibility(View.GONE);
					 if(bookInfoAdapterData != null && bookInfoAdapterData.size() > 0){
						 ToastUtil.display(getActivity(), event.getResult().getMessage());
					 }
				 }
			  }
			}
			else{
				if(!lazyLoading) {
					noInternetLayout.setVisibility(View.GONE);
					progressBar.setVisibility(View.GONE);
					noRecords.setVisibility(View.GONE);
					if(bookInfoAdapterData != null && bookInfoAdapterData.size() > 0) {
						noRecords.setVisibility(View.GONE);
					}
					else
						noRecords.setVisibility(View.VISIBLE);
				}
				else{
					noInternetLayout.setVisibility(View.GONE);
					progressBar.setVisibility(View.GONE);
				}
				ToastUtil.display(getActivity(), BA.str(R.string.err_get_data));
			}
		}

		@Subscribe
		public void onAdminQueuedBooksReceived(AdminQueuedBooksResponseEvent event) {
			if(event.getResult() != null){
				if(BaseAssist.isSuccess(event.getResult())){
				   horizontalList.setAdapter(queueListAdapter);
				   if(event.getResult().getBooks() != null && event.getResult().getBooks().size() > 0)
					queueLayout.setVisibility(View.VISIBLE);
				   else queueLayout.setVisibility(View.GONE);
				   queueListAdapter.set(event.getResult().getBooks());
				   queueListAdapter.notifyDataSetChanged();
				}
				else{
					queueLayout.setVisibility(View.GONE);
				}
			}
			else{
				queueLayout.setVisibility(View.GONE);
			}
		}

		@Override
		public void onDestroy() {
			super.onDestroy();		
		}

		@Override
		public void onResume() {
			super.onResume();
			BA.getEventBus().register(this);
			handler.postDelayed(runnable, 30000);
		}
		
		@Override
		public void onPause() {
			super.onPause();
			loading = false;
			if(listAdapter != null)
			   listAdapter.stopTimer();
			handler.removeCallbacks(runnable);
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
		
	    @OnClick(R.id.btnPending)
	    public void onPendingButtonClick(){
	    	selectedDay = PENDING;
			setWaitScreen(true);
			filter.setOrderBy("time");
	     	buttonPending.setSelected(true);
			buttonToday.setSelected(false);
	     	buttonTomorrow.setSelected(false);
	     	loadData(0, false);
	    }

	    @OnClick(R.id.btnToday)
	    public void onTodayButtonClick(){
			setWaitScreen(true);
	    	selectedDay = TODAY;
			filter.setOrderBy("-time");
	     	buttonToday.setSelected(true);
			buttonPending.setSelected(false);
	     	buttonTomorrow.setSelected(false);
	     	loadData(0, false);
	    }

	    @OnClick(R.id.btnTomorrow)
	  public void onTomorrowButtonClick(){
			setWaitScreen(true);
	    	selectedDay = TOMORROW;
			filter.setOrderBy("-time");
			buttonPending.setSelected(false);
	     	buttonToday.setSelected(false);
	     	buttonTomorrow.setSelected(true);
	     	loadData(0, false);
	 }

	 public void onAllButtonClick(){
		setWaitScreen(true);
		selectedDay = ALL;
	    filter.setOrderBy("-time");
		buttonPending.setSelected(false);
		buttonToday.setSelected(false);
		buttonTomorrow.setSelected(true);
		loadData(0, false);
	}

	@Override
	public void onDatePickerDataset(Long time,String type) {
		currentTime = time/1000;
		setWaitScreen(true);
		selectedDay = FOR_DATE;
		createActionBarSpinner();
		loadData(0, false);
	}

	@Subscribe
	public void onBookingPushReceived(NewBookingPushResponseEvent event){
		if(event != null && event.getPush() != null){
			bookingPushData = event.getPush().getData();
		}
	}

	@Subscribe
	public void onPushUpdateListRequested(PushUpdateListRequestEvent event){
			if(washer == null)
				return; // carwash not loaded yet; the initial load will pick it up
			if(Functions.checkOnline(getActivity())){
				numberPage = 0;
				loadDataWithoutLoader();
			}
			else{
				progressBar.setVisibility(View.GONE);
				noInternetLayout.setVisibility(View.VISIBLE);
			}
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {

			filter.setWasherId(washer.getId());
			queuedFilter.setWasherId(washer.getId());
			queuedFilter.setLimit("200");
			filter.setDay(TODAY);
			if (!Functions.checkOnline(getActivity())) {
				progressBar.setVisibility(View.GONE);
				noInternetLayout.setVisibility(View.VISIBLE);
				if (listAdapter != null) {
					listAdapter.notifyDataSetChanged();
				}
			}
			else {
				  loadDataWithoutLoader();
			}
		}
	}

	public class RecycleHederViewHolder extends RecyclerView.ViewHolder {
		View view;
		public RecycleHederViewHolder(View v) {
			super(v);
			this.view = v;
			buttonPending = (TextView) v.findViewById(R.id.btnPending);
			buttonToday = (TextView) v.findViewById(R.id.btnToday);
			buttonTomorrow = (TextView) v.findViewById(R.id.btnTomorrow);
		}
		public View getView() {
			return this.view;
		}
	}

	@Subscribe
	public void onBookApproveReceived(AdminBookApproveResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				loadWithUpdate = true;
				loadDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.booking_accepted));
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_accept_booking));
		}
	}

	@Subscribe
	public void onBookStopReceived(AdminBookStopResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				loadDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.service_finished));
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_stop_service));
		}
	}


	@Subscribe
	public void onBookNoClientReceived(AdminBookNoClientResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				loadDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.order_rejected_noshow));

			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.error_typo));
		}
	}

	@Subscribe
	public void onBookCancelReceived(AdminBookCancelResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				loadDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.order_rejected_typo));
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_cancel_booking));
		}
	}


	@Subscribe
	public void onAdminRejectQueueResponseReceived(AdminRejectQueueResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				loadDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.queue_rejected_typo));
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_add_queue));
		}
	}

	@Subscribe
	public void onAdminFinishQueueResponseReceived(AdminFinishQueueResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				loadDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.service_provided));
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_add_queue));
		}
	}

	@Subscribe
	public void onAdminPostponeQueueResponseReceived(AdminPostponeQueueResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				loadData(0,false);
				ToastUtil.display(BA.getContext(),BA.str(R.string.extended_10min_ok));
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_extend));
		}
	}

	@Subscribe
	public void onAdminMarkAsClientPaidResponseReceived(AdminMarkAsClientPaidResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				loadData(0,false);
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.error_word));
		}
	}

	private void createActionBarSpinner(){
		if(spinner != null) {
		  String[] valuesArray = new String[]{BA.str(R.string.orders_in_progress), BA.str(R.string.all_orders_today), BA.str(R.string.orders_tomorrow), BA.str(R.string.orders_history), BA.str(R.string.orders_on_date)};
		  if (currentTime != -1) {
			  String dateStr = DATE_FORMATTER.format(java.time.Instant.ofEpochSecond(currentTime));
			  valuesArray = new String[]{BA.str(R.string.orders_in_progress), BA.str(R.string.all_orders_today), BA.str(R.string.orders_history),BA.str(R.string.orders_tomorrow), BA.str(R.string.orders_on_date_pre) + dateStr};
		  }
		  ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.navigation_spinner_item, valuesArray);
		  adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);
		  spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				  if (!isSpinnerTouched) return;
//				  if (!selectionWithoutUpdate) {
					  switch (pos) {
						  case 0:
							  onPendingButtonClick();
							  break;
						  case 1:
							  onTodayButtonClick();
							  break;
						  case 2:
							  onTomorrowButtonClick();
							  break;
						  case 3:
							  onAllButtonClick();
							  break;
						  case 4:
							  onDateClick();
							  break;
					  }
				  isSpinnerTouched = false;
			  }

			  public void onNothingSelected(AdapterView<?> parent) {
			  }
		  });
		  spinner.setAdapter(adapter);
		  if (selectedDay.equals(PENDING)) {
			  spinner.setSelection(0);
		  } else if (selectedDay.equals(TODAY)) {
			  spinner.setSelection(1);
		  } else if (selectedDay.equals(TOMORROW)) {
			  spinner.setSelection(2);
		  } else if (selectedDay.equals(ALL)) {
			  spinner.setSelection(3);
		  } else if (selectedDay.equals(FOR_DATE)) {
			  spinner.setSelection(4);
		  }
	    }
	  }

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		visible = isVisibleToUser;
		if(visible && washer != null) {
			selectionWithoutUpdate = true;
			if(appBarLayout != null)
			   appBarLayout.setExpanded(true, true);
			if(spinner != null)
			 createActionBarSpinner();
			if(addQueueButton != null) {
				actionBarRefreshButton.setOnClickListener(this);
				spinner.setVisibility(View.VISIBLE);
				addBookButton.setVisibility(View.GONE);
				loadDataWithoutLoader();
				addQueueButton.setVisibility(View.VISIBLE);
			}
		}
		else
		   if(addQueueButton != null) {
			   addQueueButton.setVisibility(View.GONE);
			   addBookButton.setVisibility(View.GONE);
		}
	}

	private void sortWithPendingStatus(List<BookInfo> books){
		Collections.sort(books, new Comparator<BookInfo>() {
			@Override
			public int compare(BookInfo lhs, BookInfo rhs) {
				if(lhs.getStatus().equals(PENDING) && !rhs.getStatus().equals(PENDING))
					return -1;
				else
					return 1;
			}
		});
	}

	public void removeApprovedTomorrowBooksForPending(List<BookInfo> books){
		//Remove approved tomorrow books for pending
		try {
			if (books != null && books.size() > 0) {
				Iterator<BookInfo> iter = books.iterator();
				while (iter.hasNext()) {
					BookInfo book = iter.next();
					if (selectedDay.equals(PENDING) && !book.getStatus().equals(PENDING)) {
						java.time.Instant bookDateTime = Functions.getUTCDate(book.getTime());
						long bookDateTimeMillis = bookDateTime.toEpochMilli();
						if (!Functions.isToday(bookDateTime, Functions.getCarwashZone()) && System.currentTimeMillis() < bookDateTimeMillis) {
							iter.remove();
						}
					}
				}
			}
		}
		catch(Exception e){}
	}

	@Override
	public void onClick(View v) {
		loadData(0,false);
	}


	@Subscribe
	public void onBookOptionClicked(BookOptionClickEvent event){
	   if(event != null && event.getBook()!= null){
		   final BookInfo book = event.getBook();
		   if(!book.isQueued()) {
			   if(book.getStatus().equals(APPROVED)
//							&& book.getServerTime().getTime() > book.getEndTime().getTime()
					   ) {
				   AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
				   b.setItems(R.array.admin_approved_book_options, new DialogInterface.OnClickListener() {
					   @Override
					   public void onClick(DialogInterface dialogInterface, int i) {
						   switch (i) {
							   case 0:
								   setWaitScreen(true);
								   BA.getEventBus().post(new AdminBookStopRequestEvent(book.getId()));
								   break;
							   case 1:
								   AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(getActivity());
								   dialog.setTitle(BA.str(R.string.client_no_show_q));
								   dialog.setNeutralButton(BA.str(R.string.no_word), null);
								   dialog.setPositiveButton(BA.str(R.string.yes_word), new DialogInterface.OnClickListener() {
									   @Override
									   public void onClick(DialogInterface dialog, int which) {
										   setWaitScreen(true);
										   BA.getEventBus().post(new AdminBookNoClientRequestEvent(book.getId()));
									   }
								   });
								   dialog.show();
								   break;
							   case 2:
								   dialog = new MaterialAlertDialogBuilder(getActivity());
								   dialog.setTitle(BA.str(R.string.cancel_booking_q));
								   dialog.setNeutralButton(BA.str(R.string.no_word), null);
								   dialog.setPositiveButton(BA.str(R.string.yes_word), new DialogInterface.OnClickListener() {
									   @Override
									   public void onClick(DialogInterface dialog, int which) {
										   setWaitScreen(true);
										   BA.getEventBus().post(new AdminBookCancelRequestEvent(book.getId()));
									   }
								   });
								   dialog.show();
								   break;
							   case 3:
								   loadWithUpdate = true;
								   activityActions.editEntry(book);
								   break;
							   case 4:
								   loadWithUpdate = true;
								   activityActions.openBookInfo(book.getId());
								   break;
						   }
					   }
				   });
				   b.setTitle(BA.str(R.string.service_rendered_to)+book.getClientKey()+"?");
				   b.show();
			   }
			   else if(book.getStatus().equals(PENDING)){
				   AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
				   b.setItems(R.array.admin_pending_book_options, new DialogInterface.OnClickListener() {
					   @Override
					   public void onClick(DialogInterface dialogInterface, int i) {
						   switch (i) {
							   case 0:
								   setWaitScreen(true);
								   BA.getEventBus().post(new AdminBookApproveRequestEvent(book.getId()));
								   break;
							   case 1:
								   AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(getActivity());
								   dialog.setTitle(BA.str(R.string.cancel_booking_q));
								   dialog.setNeutralButton(BA.str(R.string.no_word), null);
								   dialog.setPositiveButton(BA.str(R.string.yes_word), new DialogInterface.OnClickListener() {
									   @Override
									   public void onClick(DialogInterface dialog, int which) {
										   setWaitScreen(true);
										   BA.getEventBus().post(new AdminBookCancelRequestEvent(book.getId()));
									   }
								   });
								   dialog.show();
								   break;
							   case 2:
								   loadWithUpdate = true;
								   activityActions.openBookInfo(book.getId());
								   break;
						   }
					   }
				   });
				   b.setTitle(BA.str(R.string.op_booking_client)+book.getClientKey()+"");
				   b.show();
			   }
			   else{
				   activityActions.openBookInfo(book.getId());
			   }
		   }
		   else{
			   //Queued booking
			   if(book.getStatus().equals(QUEUED_APPROVED)){
				   AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
				   if(book.getPaid()) {
					   b.setItems(R.array.admin_approved_queue_options, new DialogInterface.OnClickListener() {
						   @Override
						   public void onClick(DialogInterface dialogInterface, int i) {
							   switch (i) {
								   case 0:
									   setWaitScreen(true);
									   BookingRequest request = new BookingRequest();
									   request.setId(book.getId());
									   request.setDuration(10);
									   BA.getEventBus().post(new AdminPostponeQueueRequestEvent(request));
									   break;
								   case 1:
									   AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(getActivity());
									   dialog.setTitle(BA.str(R.string.finish_order_q));
									   dialog.setNeutralButton(BA.str(R.string.no_word), null);
									   dialog.setPositiveButton(BA.str(R.string.yes_word), new DialogInterface.OnClickListener() {
										   @Override
										   public void onClick(DialogInterface dialog, int which) {
											   setWaitScreen(true);
											   BookingRequest request = new BookingRequest();
											   request.setId(book.getId());
											   BA.getEventBus().post(new AdminFinishQueueRequestEvent(request));
										   }
									   });
									   dialog.show();
									   break;
								   case 2:
									   dialog = new MaterialAlertDialogBuilder(getActivity());
									   dialog.setTitle(BA.str(R.string.cancel_order_q));
									   dialog.setNeutralButton(BA.str(R.string.no_word), null);
									   dialog.setPositiveButton(BA.str(R.string.yes_word), new DialogInterface.OnClickListener() {
										   @Override
										   public void onClick(DialogInterface dialog, int which) {
											   setWaitScreen(true);
											   BookingRequest request = new BookingRequest();
											   request.setId(book.getId());
											   BA.getEventBus().post(new AdminRejectQueueRequestEvent(request));
										   }
									   });
									   dialog.show();
									   break;
								   case 3:
									   loadWithUpdate = true;
									   activityActions.editEntry(book);
									   break;
								   case 4:
									   loadWithUpdate = true;
									   activityActions.openBookInfo(book.getId());
									   break;
							   }
						   }
					   });
				   }
				   else{
					   b.setItems(R.array.admin_approved_queue_not_paid_options, new DialogInterface.OnClickListener() {
						   @Override
						   public void onClick(DialogInterface dialogInterface, int i) {
							   switch (i) {
								   case 0:
									   setWaitScreen(true);
									   BookingRequest request = new BookingRequest();
									   request.setId(book.getId());
									   request.setPaid(true);
									   loadWithUpdate = true;
									   BA.getEventBus().post(new AdminMarkAsClientPaidRequestEvent(request));
									   break;
								   case 1:
									   setWaitScreen(true);
									   request = new BookingRequest();
									   request.setId(book.getId());
									   request.setDuration(10);
									   BA.getEventBus().post(new AdminPostponeQueueRequestEvent(request));
									   break;
								   case 2:
									   AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(getActivity());
									   dialog.setTitle(BA.str(R.string.finish_order_q));
									   dialog.setNeutralButton(BA.str(R.string.no_word), null);
									   dialog.setPositiveButton(BA.str(R.string.yes_word), new DialogInterface.OnClickListener() {
										   @Override
										   public void onClick(DialogInterface dialog, int which) {
											   setWaitScreen(true);
											   BookingRequest request = new BookingRequest();
											   request.setId(book.getId());
											   BA.getEventBus().post(new AdminFinishQueueRequestEvent(request));
										   }
									   });
									   dialog.show();
									   break;
								   case 3:
									   dialog = new MaterialAlertDialogBuilder(getActivity());
									   dialog.setTitle(BA.str(R.string.cancel_order_q));
									   dialog.setNeutralButton(BA.str(R.string.no_word), null);
									   dialog.setPositiveButton(BA.str(R.string.yes_word), new DialogInterface.OnClickListener() {
										   @Override
										   public void onClick(DialogInterface dialog, int which) {
											   setWaitScreen(true);
											   BookingRequest request = new BookingRequest();
											   request.setId(book.getId());
											   BA.getEventBus().post(new AdminRejectQueueRequestEvent(request));
										   }
									   });
									   dialog.show();
									   break;
								   case 4:
									   loadWithUpdate = true;
									   activityActions.editEntry(book);
									   break;
								   case 5:
									   loadWithUpdate = true;
									   activityActions.openBookInfo(book.getId());
									   break;
							   }
						   }
					   });

				   }
				   b.setTitle(BA.str(R.string.op_client)+book.getClientKey().replaceAll("\\(","").replaceAll("\\)","")+"");
				   b.show();
			   }
			   else{
				   if(!book.getPaid()) {
					   AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
					   b.setItems(R.array.admin_not_paid_options, new DialogInterface.OnClickListener() {
						   @Override
						   public void onClick(DialogInterface dialogInterface, int i) {
							   switch (i) {
								   case 0:
									   setWaitScreen(true);
									   BookingRequest request = new BookingRequest();
									   request.setId(book.getId());
									   request.setPaid(true);
									   loadWithUpdate = true;
									   BA.getEventBus().post(new AdminMarkAsClientPaidRequestEvent(request));
									   break;
								   case 1:
									   loadWithUpdate = true;
									   activityActions.openBookInfo(book.getId());
									   break;
							   }
						   }
					   });
					   b.setTitle(BA.str(R.string.op_booking_client)+book.getClientKey()+"");
					   b.show();
				   }
				   else{
					   loadWithUpdate = true;
					   activityActions.openBookInfo(book.getId());
				   }

			   }
		   }
	   }
	}

}

