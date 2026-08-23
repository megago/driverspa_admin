package com.driverspa.admin.fragment;

import static com.driverspa.util.Constants.APPROVED;
import static com.driverspa.util.Constants.BOOKING_INFO_TYPE;
import static com.driverspa.util.Constants.EXTRA_BOOKING_ID;
import static com.driverspa.util.Constants.FIRST_BOOK;
import static com.driverspa.util.Constants.PENDING;
import static com.driverspa.util.Constants.QUEUED_APPROVED;
import static com.driverspa.util.Constants.STARTED;
import static com.driverspa.util.Constants.bookStatus;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.driverspa.assist.UserSelfAssist;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.BookInfo;
import com.driverspa.model.Box;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.PushData;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.NewBookingPushResponseEvent;
import com.driverspa.util.otto.RemoveBookingPushRequestEvent;
import com.driverspa.util.otto.StopPlayRingtoneRequestEvent;
import com.driverspa.util.otto.ws.AdminBookApproveRequestEvent;
import com.driverspa.util.otto.ws.AdminBookApproveResponseEvent;
import com.driverspa.util.otto.ws.AdminBookCancelRequestEvent;
import com.driverspa.util.otto.ws.AdminBookCancelResponseEvent;
import com.driverspa.util.otto.ws.AdminBookInfoRequestEvent;
import com.driverspa.util.otto.ws.AdminBookInfoResponseEvent;
import com.driverspa.util.otto.ws.AdminBookNoClientRequestEvent;
import com.driverspa.util.otto.ws.AdminBookNoClientResponseEvent;
import com.driverspa.util.otto.ws.AdminBookStopRequestEvent;
import com.driverspa.util.otto.ws.AdminBookStopResponseEvent;
import com.driverspa.util.otto.ws.AdminClientResponseEvent;
import com.driverspa.util.otto.ws.AdminFinishQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminFinishQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminMarkAsClientPaidRequestEvent;
import com.driverspa.util.otto.ws.AdminMarkAsClientPaidResponseEvent;
import com.driverspa.util.otto.ws.AdminPostponeQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminPostponeQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminRejectQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminRejectQueueResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminBookingInfoFragment extends AdminBaseFragment {

	DecimalFormat decimalFormatter = new DecimalFormat("#,###.##");
    public static final int TIMER_MINUTE = 14;
    String userId = UserPreferences.getUserId(BA.getContext());
    String phone = UserPreferences.getUserPhone(BA.getContext());
	HashMap<String,PushData> bookingPushData;

	public interface ActivityActions{
		public void showProgressBar(boolean set);
		public void goToHome();
		public void editEntry(BookInfo bookInfo);
		public void openClientInfo(ClientInfo client);
	}		
    List<HashMap<String,String>> gridList;
    List<String> joinSlots;            
    
	public static AdminBookingInfoFragment newInstance(String bookId, String bookInfoType) {
		AdminBookingInfoFragment fragment = new AdminBookingInfoFragment();
		Bundle bundle = new Bundle();
		bundle.putString(EXTRA_BOOKING_ID, bookId );		
		bundle.putString(BOOKING_INFO_TYPE, bookInfoType );	
		fragment.setArguments(bundle);
		return fragment;
	}

	private Handler mHandler = new Handler();
	@BindView(R.id.txtWasherNameTitle)
	TextView washerTitleName;
	@BindView(R.id.txtClientName)
	TextView clientName2;
	@BindView(R.id.mobileLayout)
	View mobileLayout;
	@BindView(R.id.txtClientPhoneNumber)
	TextView txtClientPhoneNumber;
	@BindView(R.id.txtClientPhone)
	TextView clientPhone;
	@BindView(R.id.txtClientCarNo)
	TextView clientCarNo;
	@BindView(R.id.txtClientCarMark)
	TextView clientCarMark;
	@BindView(R.id.txtClientCarType)
	TextView clientCarType;
	@BindView(R.id.txtClientServiceType)
	TextView clientServiceType;
	@BindView(R.id.txtClientServiceDate)
	TextView clientServiceDate;
	@BindView(R.id.txtClientServiceTime)
	TextView clientServiceTime;
	@BindView(R.id.txtClientServicePrice)
	TextView clientServicePrice;	
	@BindView(R.id.txtBookingStatus)
	TextView txtBookingStatus;
	@BindView(R.id.txtClientBoxName)
	TextView txtClientBoxName;
	@BindView(R.id.txtWasherStaffName)
	TextView txtWasherStaffName;
	@BindView(R.id.remainingTime)
	TextView remainingTime;
	@BindView(R.id.txtDuration)
	TextView txtDuration;
	@BindView(R.id.discount)
	TextView txtDiscount;
	@BindView(R.id.comment)
	TextView txtComment;
	@BindView(R.id.subTitle)
	TextView subTitle;
	@BindView(R.id.txtClientGroupName)
	TextView txtClientGroupName;
	@BindView(R.id.clientName)
	TextView clientName;
	@BindView(R.id.remainingTimeLayout)
	View remainingTimeLayout;
	@BindView(R.id.nocashLayout)
	View nocashLayout;

	@BindView(R.id.layoutFirstTimeBooked)
	View firstBookLayout;
	@BindView(R.id.layoutPending)
	View pendingLayout;		
	@BindView(R.id.layoutClientBooking)
	View clientBookingOperationLayout;		
	@BindView(R.id.btnOk)
	Button buttonOk;
	@BindView(R.id.btnBookStartStop)
	Button btnBookStartStop;
	@BindView(R.id.btnNoClient)
	Button btnNoClient;	
	@BindView(R.id.btnBookFail)
	Button btnBookFail;
	@BindView(R.id.btnCall)
	View btnCall;
	@BindView(R.id.commentLayout)
	View commentLayout;

	@BindView(R.id.countsLayout)
	View countsLayout;
	@BindView(R.id.countsDateLayout)
	View countsDateLayout;

	@BindView(R.id.txtFinishedBooksDate)
	TextView finishedBooksDate;
	@BindView(R.id.txtFinishedBooksCount)
	TextView finishedBooksCount;

	@BindView(R.id.noPaid)
	View noPaid;
	@BindView(R.id.staffLayout)
	View staffLayout;
	@BindView(R.id.boxLayout)
	View boxLayout;
	@BindView(R.id.errorOutline)
	View errorOutline;

	@BindView(R.id.txtPriceDetail)
	TextView txtPriceDetail;


	private ActivityActions activityActions;	
	private String bookId;
	private String bookInfoType;
	private BookInfo book;
	private User user;
	Washer washer;
	String date;
	ClientInfo client;

	@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_admin_booking_info, container,false);
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
			user = UserSelfAssist.getUserFromDb(userId);
			bookId = getArguments().getString(EXTRA_BOOKING_ID);
			bookInfoType = getArguments().getString(BOOKING_INFO_TYPE);
			bookInfoType = !TextUtils.isEmpty(bookInfoType)?bookInfoType:"";									
			activityActions.showProgressBar(true);
		}
		
	    @Override
	    public void onViewCreated(View view, Bundle savedInstanceState) {	       
	        ButterKnife.bind(this, view);
			remainingTimeLayout.setVisibility(View.GONE);
	    }
	
	    @Subscribe
	    public void onBookingInfoReceived(AdminBookInfoResponseEvent event){
	    	activityActions.showProgressBar(false);
			remainingTimeLayout.setVisibility(View.GONE);
	    	setWaitScreen(false);
	    	if(event.getResult() != null){	
	    	  if(BaseAssist.isSuccess(event.getResult())){
				  //If data is received, just remove from new push items
	    		firstBookLayout.setVisibility(bookInfoType.equals(FIRST_BOOK)?View.VISIBLE:View.GONE);	    			    			    		
	    		book = event.getResult().getResponse();

				if(book.getCompanyClient() != null){
					client = book.getCompanyClient();
					setClientInfo(client);
				}

	   		    Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
				TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
				subTitle.setText(bookStatus.get(book.getStatus()));
				subTitle.setBackground(Functions.getBookStatusBGColor(getActivity(),book.getStatus()));
				subTitle.setTextColor(Functions.getBookStatusTextColor(getActivity(),book.getStatus()));

				noPaid.setVisibility(View.GONE);
				if(!book.isQueued()) {
					titleView.setText(BA.str(R.string.booking_word));
				}
				  else{
					if(!book.getPaid()) {
						noPaid.setVisibility(View.VISIBLE);
					}
					titleView.setText(BA.str(R.string.wash));
				}

				if(book.getNocash()){
				   nocashLayout.setVisibility(View.VISIBLE);
				}

				txtBookingStatus.setText(bookStatus.get(book.getStatus()));
//				txtBookingStatus.setBackground(Functions.getBookStatusBGColor(getActivity(),book.getStatus()));
//				txtBookingStatus.setTextColor(Functions.getBookStatusTextColor(getActivity(),book.getStatus()));

				HashMap<String,String> timeMap = Functions.formatUTCDate(book.getTime());
//			    titleView.setText(timeMap.get(Functions.DATE)+" в "+timeMap.get(Functions.TIME));

				if(book.getStatus().equals(APPROVED) && book.getServerTime().isAfter(book.getEndTime()) ) {
					AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
					b.setItems(R.array.admin_approved_book_detail_options, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							switch (i) {
								case 0:
									setWaitScreen(true);
									BA.getEventBus().post(new AdminBookStopRequestEvent(book.getId()));
									break;
								case 1:
									setWaitScreen(true);
									BA.getEventBus().post(new AdminBookNoClientRequestEvent(book.getId()));
									break;
								case 2:
									setWaitScreen(true);
									BA.getEventBus().post(new AdminBookCancelRequestEvent(book.getId()));
									break;
							}
						}
					});
					b.setTitle(BA.str(R.string.service_rendered_to)+book.getClientKey()+"?");
//					b.setNeutralButton("Отменить", null);
					b.show();
				}

				  if(!TextUtils.isEmpty(book.getMobile()) && !book.getMobile().equals(UserPreferences.getUserPhone(BA.getContext()))) {
					  mobileLayout.setVisibility(View.VISIBLE);
					  txtClientPhoneNumber.setText(Functions.formatPhoneNumber("+7 ### ### ## ##",book.getMobile()));
				  }
				  else {
					  mobileLayout.setVisibility(View.GONE);
					  btnCall.setVisibility(View.GONE);
				  }


				if(bookingPushData!=null && bookingPushData.size() > 0 && bookingPushData.get(bookId) != null && !book.getStatus().equals(PENDING)){
					  BA.getEventBus().post(new RemoveBookingPushRequestEvent(new PushData(bookId)));
				}
				else if(book.getStatus().equals(PENDING)) {
					 startUpdateTimer();
				}

				txtDuration.setText(book.getDuration()+BA.str(R.string.space_minutes));
	    		pendingLayout.setVisibility(View.GONE);

	    		if(book.getStatus().equals(APPROVED)) btnBookStartStop.setText(BA.str(R.string.finish_services));
	    		btnBookStartStop.setVisibility(book.getStatus().equals(APPROVED)||book.getStatus().equals(STARTED)?View.VISIBLE:View.GONE);
	    		btnNoClient.setVisibility(book.getStatus().equals(APPROVED)?View.VISIBLE:View.GONE);
	    		btnBookFail.setVisibility(View.GONE);
				clientCarNo.setText(book.getClientKey());
	    		clientCarType.setText(BA.getReference().getCarType().get(Integer.parseInt(book.getCartype())));

				String groupServices = "";
				 if(book.getGroupServices() != null && book.getGroupServices().size() > 0) {
					  LinkedHashMap <String,Integer> groupedGroupServices = Functions.getGroupedGroupServices(book.getGroupServices());
					  for (LinkedHashMap.Entry<String, Integer> entry : groupedGroupServices.entrySet()) {
						  String serviceId = entry.getKey();
						  Integer count = entry.getValue();
						  Washer.GroupMenu g = getGroupServiceName(washer,serviceId);
						  groupServices = groupServices + (g != null?g.getName()+""+(count>1?" x "+count:""):"") + "/";
					  }
					  groupServices = groupServices.substring(0, groupServices.length() - 1);
				  }

				  String services = "";
				  if(book.getServices() != null && book.getServices().size() > 0) {
					  LinkedHashMap <Integer,Integer> groupedServices = Functions.getGroupedServices(book.getServices());
					  for (LinkedHashMap.Entry<Integer, Integer> entry : groupedServices.entrySet()) {
						  Integer serviceId = entry.getKey();
						  Integer count = entry.getValue();
						  services = services + (BA.getReference().getServices().get(serviceId)+""+(count>1?" x "+count:"")) + "/";
					  }
					  services = services.substring(0, services.length() - 1);
				  }

				clientServiceType.setText("");
			    if(!TextUtils.isEmpty(groupServices)) {
					if(!TextUtils.isEmpty(services))
					clientServiceType.append(groupServices + "+" + services);
					else
					clientServiceType.append(groupServices);
				}
				else
					clientServiceType.append(services);

				if(TextUtils.isEmpty(book.getComment())){
					  commentLayout.setVisibility(View.GONE);
				}
				else{
					commentLayout.setVisibility(View.VISIBLE);
					txtComment.setText(book.getComment());
				}
				txtDiscount.setText((book.getDiscount()!=null?book.getDiscount():"0")+" %");

				if(DateUtils.isToday(book.getTime().toEpochMilli()))
	    		  clientServiceDate.setText(BA.str(R.string.today_paren)+timeMap.get(Functions.DATE)+")");
				else clientServiceDate.setText(timeMap.get(Functions.DATE));

	    		clientServiceTime.setText(timeMap.get(Functions.TIME));
	    		clientServicePrice.setText(decimalFormatter.format(Double.parseDouble(book.getPrice()))+" ₸");
				txtPriceDetail.setVisibility(View.VISIBLE);
				txtPriceDetail.setText(Functions.formatPriceDetail(book.getPriceDetails(),Double.parseDouble(book.getPrice())));

				if(washer != null && washer.getBoxSettings() != null && !TextUtils.isEmpty(book.getBoxId())){
					  boxLayout.setVisibility(View.VISIBLE);
					  for(Box bs : washer.getBoxSettings()){
						  if(book.getBoxId().equals(bs.getUid())){
							  txtClientBoxName.setVisibility(View.VISIBLE);
							  txtClientBoxName.setText(bs.getBoxName());
							  break;
						  }
					  }
				   }
				  else{
					txtClientBoxName.setText("");
					txtClientBoxName.setVisibility(View.GONE);
					boxLayout.setVisibility(View.GONE);
			   }

				String washerStaffNames = "";
				if(washer != null && washer.getStaffs() != null && book.getStaffs()!= null && book.getStaffs().size()>0){
					  for(String staffId : book.getStaffs()){
						  washerStaffNames = washerStaffNames + Functions.getStaffName(washer.getStaffs(),staffId).trim() + "/";
					  }
					washerStaffNames = washerStaffNames.substring(0, washerStaffNames.length() - 1);
					staffLayout.setVisibility(View.VISIBLE);
					errorOutline.setVisibility(View.GONE);
					txtWasherStaffName.setText(washerStaffNames);
				  }
				  else{
					  staffLayout.setVisibility(View.GONE);
					  if(washer.getStaffs() != null && washer.getStaffs().size() > 0)
					  	errorOutline.setVisibility(View.VISIBLE);
					  txtWasherStaffName.setText(BA.str(R.string.employees_not_set));
				  }
			  }
	    	  else{
	    		  ToastUtil.display(getActivity(), event.getResult().getMessage());
	    	  }
	    	}
			else{
				ToastUtil.display(getActivity(), BA.str(R.string.err_get_data));
			}
		}

	    private Washer.GroupMenu getGroupServiceName(Washer washer, String groupServiceId){
			for(Washer.GroupMenu groupMenu : washer.getGroupMenu()){
				if(groupMenu.getId().equals(groupServiceId)){
					return groupMenu;
				}
			}
			return null;
		}



	    public void loadData(){
			setWaitScreen(true);
	    	BA.getEventBus().post(new AdminBookInfoRequestEvent(bookId));
			AdminSearchFilter filter = new AdminSearchFilter();
//			if(washer != null) {
//				filter.setCarwash(washer.getId());
//				filter.setBookId(bookId);
//				BA.getEventBus().post(new AdminClientRequestEvent(filter));
//			}
	    }
	    
	    @OnClick(R.id.btnOk)
	    public void onButtonOkClicked(){
	        activityActions.goToHome();
	    }

		@Override
		public void onDestroy() {
			super.onDestroy();		
		}

		@Override
		public void onResume() {
			super.onResume();
			BA.getEventBus().register(this);
			BA.getEventBus().post(new StopPlayRingtoneRequestEvent());
		}
		
		@Override
		public void onPause() {
			super.onPause();
			if(mHandler != null && updateRemainingTimeRunnable !=null)
			   mHandler.removeCallbacks(updateRemainingTimeRunnable);
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
		
		@OnClick(R.id.btnCancelBooking)
		public void onCancelBooking(){
			AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(getActivity());
			dialog.setTitle(BA.str(R.string.cancel_booking_q));
			dialog.setNeutralButton(BA.str(R.string.no_word), null);
			dialog.setPositiveButton(BA.str(R.string.yes_word), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setWaitScreen(true);
					BA.getEventBus().post(new AdminBookCancelRequestEvent(book.getId()));
				}
			});
			dialog.show();
		}

	@OnClick(R.id.btnCancelBooking2)
	public void onCancelBooking2(){
		onCancelBooking();
	}

		@OnClick(R.id.btnApproveBooking)
		public void onApproveBooking(){
			AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(getActivity());
			dialog.setTitle(BA.str(R.string.accept_booking_q));
			dialog.setNeutralButton(BA.str(R.string.no_word), null);
			dialog.setPositiveButton(BA.str(R.string.yes_word), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setWaitScreen(true);
					BA.getEventBus().post(new AdminBookApproveRequestEvent(book.getId()));
				}
			});
			dialog.show();
		}

		@OnClick(R.id.btnBookStartStop)
		public void onStartStopBooking(){
			AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(getActivity());
//			dialog.setTitle(book.getStatus().equals(APPROVED)?"Начать выполнение услуг?":"Завершить выполнение услуг?");
			dialog.setTitle(BA.str(R.string.finish_services_q));
			dialog.setNeutralButton(BA.str(R.string.no_word), null);
			dialog.setPositiveButton(BA.str(R.string.yes_word), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setWaitScreen(true);
//					if(book.getStatus().equals(APPROVED))
//					  BA.getEventBus().post(new AdminBookStartRequestEvent(book.getId()));
//					else
					  BA.getEventBus().post(new AdminBookStopRequestEvent(book.getId()));
				}
			});
			dialog.show();
		}

		@OnClick(R.id.btnNoClient)
		public void onNoClientButtonClicked(){
			AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(getActivity());
			dialog.setTitle(BA.str(R.string.client_no_show_q));
			dialog.setNeutralButton(BA.str(R.string.no_word), null);
			dialog.setPositiveButton(BA.str(R.string.yes_word), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setWaitScreen(true);
					BA.getEventBus().post(new AdminBookNoClientRequestEvent(book.getId()));
				}
			});
			dialog.show();
		}

	@Subscribe
	public void onBookApproveReceived(AdminBookApproveResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				loadData();
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
				loadData();
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
				loadData();
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
				loadData();
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
//				ToastUtil.display(this, "Успешно забронировали");
				loadData();
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
				loadData();
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
				loadData();
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
				loadData();
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.error_word));
		}
	}

	@Subscribe
	public void onBookingPushReceived(NewBookingPushResponseEvent event){
		if(event != null && event.getPush() != null){
			bookingPushData = event.getPush().getData();
		}
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		loadData();
	}

	@OnClick(R.id.btnCall)
	public void onCallButtonClicked(){
	  if(book != null) {
		  Intent intent = new Intent(Intent.ACTION_DIAL);
		  intent.setData(Uri.parse("tel:" + book.getMobile().toString()));
		  startActivity(intent);
	  }
	}

	public void updateTimeRemaining(long currentTime) {
	  if(book != null && book.getTs() != null) {
		  long timeDiff = currentTime - book.getTs().toEpochMilli();
		  if (timeDiff > 0) {
			  int seconds = (int) (timeDiff / 1000) % 60;
			  int minutes = (int) ((timeDiff / (1000 * 60)) % 60);
			  int hours = (int) ((timeDiff / (1000 * 60 * 60)) % 24);

			  if ((TIMER_MINUTE - minutes) <= 0){
//				  remainingTime.setVisibility(View.GONE);
			     remainingTime.setText("00:00");
		      }
			  else if(book.getStatus().equals(PENDING)){
				  remainingTimeLayout.setVisibility(View.VISIBLE);
				  remainingTime.setText(String.format("%02d", (TIMER_MINUTE - minutes)) + ":" + String.format("%02d", (59 - seconds)));
//				  remainingTime.setText(Html.fromHtml("Необходимо подтвердить бронь в течении " + "<font color=\"#DC143C\">" + String.format("%02d", (TIMER_MINUTE - minutes)) + ":" + String.format("%02d", (59 - seconds)) + "</font>"));
			  }
		  }
	  }
	}

	private Runnable updateRemainingTimeRunnable = new Runnable() {
		@Override
		public void run() {
			long currentTime = System.currentTimeMillis();
			updateTimeRemaining(currentTime);
		}
	};

	private void startUpdateTimer() {
		Timer tmr = new Timer();
		tmr.schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.post(updateRemainingTimeRunnable);
			}
		}, 1000, 1000);
	}

	public void showOptions(){
		if(book!=null && !book.isQueued()) {
			//Bookings
			if(book.getStatus().equals(APPROVED)
//							&& book.getServerTime().getTime() > book.getEndTime().getTime()
					) {
				AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
				b.setItems(R.array.admin_approved_book_options, new OnClickListener() {
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
								dialog.setPositiveButton(BA.str(R.string.yes_word), new OnClickListener() {
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
								dialog.setPositiveButton(BA.str(R.string.yes_word), new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										setWaitScreen(true);
										BA.getEventBus().post(new AdminBookCancelRequestEvent(book.getId()));
									}
								});
								dialog.show();
								break;
							case 3:
								activityActions.editEntry(book);
								break;
							case 4:
//								activityActions.openBookInfo(book.getId());
								break;
						}
					}
				});
				b.setTitle(BA.str(R.string.service_rendered_to)+book.getClientKey()+"?");
//						b.setNeutralButton("Отменить", null);
				b.show();
			}
			else if(book.getStatus().equals(PENDING)){
				AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
				b.setItems(R.array.admin_pending_book_options, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						switch (i) {
							case 0:
								setWaitScreen(true);
								BA.getEventBus().post(new AdminBookApproveRequestEvent(book.getId()));
								break;
							case 1:
								setWaitScreen(true);
								BA.getEventBus().post(new AdminBookCancelRequestEvent(book.getId()));
								break;
							case 2:
//								activityActions.openBookInfo(book.getId());
								break;
						}
					}
				});
				b.setTitle(BA.str(R.string.op_booking_client)+book.getClientKey()+"");
//						b.setNeutralButton("Отменить", null);
				b.show();
			}
			else{
//				activityActions.openBookInfo(book.getId());
			}
		}
		else if(book!=null && book.isQueued()) {
			//Queued booking
			if(book.getStatus().equals(QUEUED_APPROVED)){
				AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
				if(book.getPaid()) {
					b.setItems(R.array.admin_approved_queue_options, new OnClickListener() {
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
									dialog.setPositiveButton(BA.str(R.string.yes_word), new OnClickListener() {
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
									dialog.setPositiveButton(BA.str(R.string.yes_word), new OnClickListener() {
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
									activityActions.editEntry(book);
									break;
								case 4:
//									activityActions.openBookInfo(book.getId());
									break;
							}
						}
					});
				}
				else{
					b.setItems(R.array.admin_approved_queue_not_paid_options, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							switch (i) {
								case 0:
									setWaitScreen(true);
									BookingRequest request = new BookingRequest();
									request.setId(book.getId());
									request.setPaid(true);
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
									dialog.setPositiveButton(BA.str(R.string.yes_word), new OnClickListener() {
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
									dialog.setPositiveButton(BA.str(R.string.yes_word), new OnClickListener() {
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

									activityActions.editEntry(book);
									break;
								case 5:
//									activityActions.openBookInfo(book.getId());
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
					b.setItems(R.array.admin_not_paid_options, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							switch (i) {
								case 0:
									setWaitScreen(true);
									BookingRequest request = new BookingRequest();
									request.setId(book.getId());
									request.setPaid(true);
									BA.getEventBus().post(new AdminMarkAsClientPaidRequestEvent(request));
									break;
								case 1:
//									activityActions.openBookInfo(book.getId());
									break;
							}
						}
					});
					b.setTitle(BA.str(R.string.op_booking_client)+book.getClientKey()+"");
					b.show();
				}
				else{
//					activityActions.openBookInfo(book.getId());
				}

			}
		}
	}


	public void setClientInfo(ClientInfo client){
		if(client!=null){
			if(client.getFinishedBooksCount() != null){
				countsLayout.setVisibility(View.VISIBLE);
				finishedBooksCount.setText(client.getFinishedBooksCount()+BA.str(R.string.times_no_dot));
//						if(client.getRegistered())
				finishedBooksCount.setPaintFlags(finishedBooksCount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
				if(client.getLastFinishedBookDate() != null){
					countsDateLayout.setVisibility(View.VISIBLE);
					finishedBooksDate.setPaintFlags(finishedBooksDate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
					HashMap<String,String> timeMap = Functions.formatUTCDate(client.getLastFinishedBookDate());
					finishedBooksDate.setText(timeMap.get(Functions.DATE));
				}
			}
		}
		else{
			countsLayout.setVisibility(View.GONE);
			countsDateLayout.setVisibility(View.GONE);
		}
	}

	@Subscribe
	public void onAdminClientResponseReceived(AdminClientResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				client = event.getResult().getClientInfo();

			}
			else{
				countsLayout.setVisibility(View.GONE);
				countsDateLayout.setVisibility(View.GONE);
			}
		}
		else{
			countsLayout.setVisibility(View.GONE);
			countsDateLayout.setVisibility(View.GONE);
		}
	}

    @OnClick(R.id.countsLayout)
    public void onCountLayoutClicked(){
		if(client!=null && client.getFinishedBooksCount() != null)
		   activityActions.openClientInfo(client);
	}

	@OnClick(R.id.countsDateLayout)
	public void onCountDateLayoutClicked(){
		onCountLayoutClicked();
	}
}
