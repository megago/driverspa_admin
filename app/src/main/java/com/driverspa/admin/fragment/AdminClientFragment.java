package com.driverspa.admin.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.driverspa.adapter.CarsGridAdapter;
import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.activity.GalleryPhotosActivity;
import com.driverspa.adapter.SingleClientBooksAdapter;
import com.driverspa.admin.activity.AdminClientActivity;
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.BookInfo;
import com.driverspa.model.CarItem;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.Image;
import com.driverspa.model.ImageDetail;
import com.driverspa.model.NotificationType;
import com.driverspa.model.PhotoParcelable;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.ActivityForResult;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.HttpClient;
import com.driverspa.util.RetrofitClient;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.TransformationCircle;
import com.driverspa.util.otto.ws.AdminCarwashTransactionAddRequestEvent;
import com.driverspa.util.otto.ws.AdminCarwashTransactionAddResponseEvent;
import com.driverspa.util.otto.ws.AdminClientRequestEvent;
import com.driverspa.util.otto.ws.AdminClientResponseEvent;
import com.driverspa.util.otto.ws.AdminSingleClientBooksRequestEvent;
import com.driverspa.util.otto.ws.AdminSingleClientBooksResponseEvent;
import com.driverspa.util.otto.ws.ClientRemoveRequestEvent;
import com.driverspa.util.otto.ws.ClientRemoveResponseEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.DividerItemDecoration;
import com.driverspa.view.ExpandableGridView;

public class AdminClientFragment extends AdminBaseFragment{

	DecimalFormat formatter = new DecimalFormat("#,###.##");
	public static AdminClientFragment newInstance(String clientInfo){
		AdminClientFragment fragment = new AdminClientFragment();
		Bundle b = new Bundle();
		b.putString(AdminClientActivity.CLIENT_INFO,clientInfo);
		fragment.setArguments(b);
		return fragment;
	}

	private ActivityActions activityActions;

	public interface ActivityActions{
		public void openBookInfo(String bookId);
		public void editClient(ClientInfo client);
	}

	ImageView clientAvatar;
	TextView txtClientName;
	TextView clientOptions;
	TextView mobile;
	TextView txtDiscount;
	TextView disableBonusAccrual;
	TextView bonusPercentage;
	TextView accruedBonus;
	TextView depositAmount;
	View finishedBooksLabel;

	@BindView(R.id.recycler_linear)
	RecyclerView mRecyclerLinear;

	private SingleClientBooksAdapter listAdapter;
	DividerItemDecoration itemDecoration;
	RecycleHederViewHolder headerHolder;
	Washer washer;
    ClientInfo client;

	private List<BookInfo> bookInfoAdapterData;
	AdminSearchFilter filter = new AdminSearchFilter();
	private ArrayList<PhotoParcelable> profilePhotos;

	ExpandableGridView gridCars;
	private CarsGridAdapter carAdapter;

	List<CarItem> carItemList = new ArrayList<CarItem>();

	int selectedOperationType = 6; //prepayment with cash
	Dialog paymentDialog;
	boolean paymentDialogOpen = true;
	LinkedHashMap<Integer,String> operationTypes =  BA.getReference().getOperationTypes();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View headerView = getActivity().getLayoutInflater().inflate(R.layout.header_single_client_books, container, false);
		headerHolder = new RecycleHederViewHolder(headerView);
		return inflater.inflate(R.layout.fragment_admin_client, container,false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if(!TextUtils.isEmpty(getArguments().getString(AdminClientActivity.CLIENT_INFO,"")))
		  client = ClientInfo.deserialize(getArguments().getString(AdminClientActivity.CLIENT_INFO));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				getActivity().finish();
				overridePendingTransitionWithCommonCloseTransition();
				break;
			case R.id.menu_options:
				if(client != null) {
					AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
					b.setItems(R.array.company_client_options, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							switch (i) {
								case 0:
									if(!Functions.isUserHasAccess(washer,user,context)) return;
									activityActions.editClient(client);
									break;
								case 1:
									if(!Functions.isUserHasAccess(washer,user,context)) return;
									selectedOperationType = 5;
									paymentDialogCreate();
									break;
								case 2:
									if(!Functions.isUserHasAccess(washer,user,context)) return;
									selectedOperationType = 8;
									paymentDialogCreate();
									break;
								case 3:
									if(!Functions.isUserHasAccess(washer,user,context)) return;
									setWaitScreen(true);
									BA.getEventBus().post(new ClientRemoveRequestEvent(client.getId()));
									break;
							}
						}
					});
//					b.setTitle("Услуга оказана клиенту ");
//					b.setNeutralButton("Отменить", null);
					b.show();
//					AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(getActivity());
////					dialog.setTitle("Изменить данные клиента?");
////					dialog.setNegativeButton("Нет",null);
//					dialog.setPositiveButton("Изменить данные клиента", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							if(!Functions.isUserHasAccess(washer,user,context)) return;
//							activityActions.editClient(client);
//						}
//					});
//					dialog.show();
				}
				break;
		}
		return true;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		ButterKnife.bind(this, view);
		itemDecoration = new DividerItemDecoration(getActivity());
		profilePhotos = new ArrayList<PhotoParcelable>();
		Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("Клиент");
		makeList();
		finishedBooksLabel.setVisibility(View.GONE);

		//Car adapter
		ViewGroup.LayoutParams layoutParamsCar = gridCars.getLayoutParams();
		WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth()-(int) Functions.dipToPixels(getActivity(), 20);
		layoutParamsCar.width = width; //this is in pixels
		gridCars.setLayoutParams(layoutParamsCar);
		gridCars.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(getActivity(), 45)) / 4);

		carAdapter = new CarsGridAdapter(getActivity(),carItemList,true);
		carAdapter.setSelectedPosition(-1);
		gridCars.setAdapter(carAdapter);
		gridCars.setExpanded(true);
//		carAdapter.notifyDataSetChanged();

//		carAdapter.setSelectedView(null);
//		if (carAdapter.getSelectedView() != null)
//			carAdapter.getSelectedView().setSelected(false);

		gridCars.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//				if (position == 0) {
//					openCarDefinition();
//				} else {
//					carAdapter.setSelectedPosition(position);
//					if (carAdapter.getSelectedView() != null)
//						carAdapter.getSelectedView().setSelected(false);
//					carAdapter.setSelectedView(v);
//					v.setSelected(true);
//				}
			}
		});

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	private void makeList() {
		bookInfoAdapterData = new ArrayList<BookInfo>();
		listAdapter = new SingleClientBooksAdapter(getActivity(), bookInfoAdapterData, washer, null);
		listAdapter.setData(bookInfoAdapterData);
		createListLayout();
		listAdapter.addHeaderView(Constants.VIEW_HEADER, headerHolder);
	}

	private void createListLayout(){
		((SingleClientBooksAdapter) listAdapter).setOnItemClickListener(new SingleClientBooksAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				final BookInfo book = (BookInfo)((bookInfoAdapterData.get(position-1)));
				activityActions.openBookInfo(book.getId());
			}
		});

		LinearLayoutManager manager = new LinearLayoutManager(getActivity());
		manager.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerLinear.setLayoutManager(manager);
		mRecyclerLinear.setAdapter(listAdapter);
		mRecyclerLinear.removeItemDecoration(itemDecoration);
		mRecyclerLinear.addItemDecoration(itemDecoration);
//		createScrollListener(mRecyclerLinear, manager, listAdapter);
	}

	private void loadData() {
		if(client != null) {
			filter.setId(client.getId());
			filter.setWasherId(washer.getId());
			filter.setLimit("200");
			filter.setCompanyClient(client.getId());
			BA.getEventBus().post(new AdminSingleClientBooksRequestEvent(filter));
			BA.getEventBus().post(new AdminClientRequestEvent(filter));
		}
	}

	@Subscribe
	public void onSingleClientBooksReceived(AdminSingleClientBooksResponseEvent event) {
		setWaitScreen(false);
//		listAdapter.removeFooterView(VIEW_FOOTER);
		if(event.getResult() != null) {
			if (BaseAssist.isSuccess(event.getResult())) {
				if ((event.getResult() != null && event.getResult().getBooks() != null && event.getResult().getBooks().size() > 0)) {
//					listAdapter.removeHeaderView(Constants.VIEW_HEADER);
					finishedBooksLabel.setVisibility(View.VISIBLE);
					bookInfoAdapterData.clear();
					bookInfoAdapterData.addAll(event.getResult().getBooks());
					listAdapter.notifyDataSetChanged();
//					listAdapter.addHeaderView(Constants.VIEW_HEADER, headerHolder);
				}
				else{
					finishedBooksLabel.setVisibility(View.GONE);
				}
			}
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
	}

	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
		BA.setNotificationType(NotificationType.NOTHING);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = getActivity();
		activityActions = (ActivityActions) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		context = null;
		activityActions = null;
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event){
		washer = event.getWasher();
		setData();
		loadData();
	}

	@SuppressLint("SuspiciousIndentation")
    private void setData(){

      if(client != null){
		  if(!TextUtils.isEmpty(client.getProfileImageUrl())){
			  Image photo = new Image();
			  ImageDetail imageDetail = new ImageDetail();
			  imageDetail.setUrl(client.getProfileImageUrl());
			  photo.setOriginal(imageDetail);
			  photo.setThumb1(imageDetail);
			  photo.setThumb2(imageDetail);
			  PhotoParcelable parcelablePhoto = PhotoParcelable.newPhoto(photo.getPhotoOrm(""));
			  profilePhotos.add(parcelablePhoto);

			  HttpClient.getPicasso()
					  .load(RetrofitClient.API_URL_IMAGES+client.getProfileImageUrl())
					  .fit()
					  .centerCrop()
					  .transform(new TransformationCircle(getActivity()))
					  .into(clientAvatar, new Callback() {
						  @Override
						  public void onSuccess() {
						  }

						  @Override
						  public void onError() {
						  }
					  });
		  }
		  else{
			  clientAvatar.setVisibility(View.VISIBLE);
			  clientAvatar.setImageResource(R.drawable.ic_no_image);
		  }

		  if(client.getCars() != null && client.getCars().size() > 0){
			  carItemList = client.getCars();
			  carAdapter = new CarsGridAdapter(getActivity(),carItemList,true);
			  carAdapter.setSelectedPosition(-1);
			  gridCars.setAdapter(carAdapter);
			  gridCars.setExpanded(true);
		  }

		  if(!TextUtils.isEmpty(client.getPhone()))
		     mobile.setText(Functions.formatPhoneNumber("+7 ### ### ## ##",client.getPhone()));

		  txtDiscount.setText("Скидка "+client.getDiscount()+"%");
		  bonusPercentage.setText("Процент начисление бонуса "+client.getBonusAccrualPercentage()+"%");

		  if(client.isDisableBonusAccrual())
		     disableBonusAccrual.setText("Начисление бонусов запрешено");
 		  else
			 disableBonusAccrual.setText("Начисление бонусов разрешено");

		  accruedBonus.setText("Начислено бонусов "+formatter.format(client.getAccruedBonuses())+" ₸");
		  depositAmount.setText("Депозит клиента "+formatter.format(client.getDepositAmount())+" ₸");

		  txtClientName.setText("");
		  if(!TextUtils.isEmpty(client.getName()))
			  txtClientName.setText(client.getName());
		  String carNo = client.getCarNo();

		  if(!TextUtils.isEmpty(client.getName()) && !TextUtils.isEmpty(client.getCarNo()) && carNo.contains(client.getName())){
			  carNo = carNo.replaceAll(client.getName(),"").replaceAll("\\(","").replaceAll("\\)","");
		  }

		  if(!TextUtils.isEmpty(client.getCarNo()))
			  txtClientName.append(" "+carNo.replaceAll("\\(","").replaceAll("\\)",""));

		  clientOptions.setText("");
		  if(client.getRegistered()) {
			  filter.setUsername(client.getPhone());
			  clientOptions.setText("Зарегистрированный пользователь");
		  }
		  else{
			  filter.setClientKey(client.getCarNo());
			  clientOptions.setText("Незарегистрированный пользователь");
		  }

		  if(client.getFinishedBooksCount() != null && client.getFinishedBooksCount() > 0){
			  clientOptions.append("\n\nпомыл "+client.getFinishedBooksCount()+" раз(а).");
		  }

		  if(client.getLastFinishedBookDate()!=null){
			  HashMap<String,String> timeMap = Functions.formatUTCDate(client.getLastFinishedBookDate());
			  clientOptions.append("\nпоследняя мойка "+timeMap.get(Functions.DATE));
		  }
          if(client.getNocomeBooksCount() != null && client.getNocomeBooksCount() > 0){
			 clientOptions.append("\nне пришел "+client.getNocomeBooksCount()+" раз(а).");
		  }
          if(client.getRejectedByClientBooksCount() != null && client.getRejectedByClientBooksCount() > 0){
			 clientOptions.append("\nотменил мойку "+client.getRejectedByClientBooksCount()+" раз(а).");
		  }
	  }
	}

	public void onPhotoDetailClick(){
		if(profilePhotos.size() > 0){
			startActivityForResult(GalleryPhotosActivity.newIntent(getActivity(), profilePhotos, 0), ActivityForResult.ACTIVITY_GALLERY_PHOTOS);
		}
	}

	public class RecycleHederViewHolder extends RecyclerView.ViewHolder {
		View view;
		public RecycleHederViewHolder(View v) {
			super(v);
			this.view = v;
			txtClientName = (TextView) v.findViewById(R.id.txtClientName);
			clientOptions = (TextView) v.findViewById(R.id.txtClientOptions);

			txtDiscount = (TextView) v.findViewById(R.id.txtDiscount);
			disableBonusAccrual = (TextView) v.findViewById(R.id.disableBonusAccrual);
			bonusPercentage = (TextView) v.findViewById(R.id.bonusPercentage);
			accruedBonus = (TextView) v.findViewById(R.id.accruedBonus);
			depositAmount = (TextView) v.findViewById(R.id.depositAmount);

			clientAvatar = (ImageView) v.findViewById(R.id.clientAvatar);
			gridCars = (ExpandableGridView) v.findViewById(R.id.grid_cars);
			clientAvatar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onPhotoDetailClick();
				}
			});

			mobile = (TextView) v.findViewById(R.id.txtPhone);
			finishedBooksLabel = v.findViewById(R.id.finishedBooksLabel);
		}

		public View getView() {
			return this.view;
		}
	}

	@Subscribe
	public void onAdminClientResponseReceived(AdminClientResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				client = event.getResult().getClientInfo();
				if(client!=null){
					setData();
				}
			}
			else{
			}
		}
		else{
		}
	}

	@Subscribe
	public void onClientRemoveResponseReceived(ClientRemoveResponseEvent event) {
		setWaitScreen(false);
		if(event.getData() != null){
			if(BaseAssist.isSuccess(event.getData())){
				getActivity().finish();
				overridePendingTransitionWithCommonCloseTransition();
			}
			else
				ToastUtil.display(getActivity(),event.getData().getMessage());
		}
		else{
			ToastUtil.display(getActivity(),"Ошибка при удалении клиента");
		}
	}


	public void paymentDialogCreate() {
		paymentDialogOpen = true;
		this.paymentDialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
		Window window = paymentDialog.getWindow();
		window.setGravity(Gravity.CENTER);
		paymentDialog.setContentView(R.layout.dialog_staff_payment);
		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

		final View rootView = (FrameLayout) paymentDialog.findViewById(R.id.root_view);
		final EditText amountEditText = (EditText) paymentDialog.findViewById(R.id.amount);
		if(selectedOperationType == 5){
			((Button) paymentDialog.findViewById(R.id.btnPay)).setText("Оплата");
		}
		else{
			((Button) paymentDialog.findViewById(R.id.btnPay)).setText("Списать");
		}

		amountEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					amountEditText.setText("");
				}
				else
					amountEditText.setText("0");
			}
		});
		paymentDialog.findViewById(R.id.btnPay).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				makeTransaction(selectedOperationType,operationTypes.get(selectedOperationType),amountEditText);
			}
		});

		paymentDialog.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				paymentDialog.dismiss();
			}
		});

		Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_fade_in);
		final Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_fade_out);
		rootView.startAnimation(fadeIn);
		this.paymentDialog.show();
		paymentDialog.setCancelable(true);
	}


	private void makeTransaction(Integer operationType,String description, EditText amount){
		AdminCarwashTransactionAddRequestEvent request = new AdminCarwashTransactionAddRequestEvent();
		closeKeyboard();

		if(TextUtils.isEmpty(amount.getText().toString())){
			ToastUtil.display(getActivity(),"Введите сумму");
			amount.requestFocus();
			return;
		}

		if(Double.parseDouble(amount.getText().toString()) <= 0){
			ToastUtil.display(getActivity(),"Введите сумму выше нуля");
			amount.requestFocus();
			return;
		}

		request.setWasherId(washer.getId());
		request.setAmount(Double.parseDouble(amount.getText().toString()));
		request.setDescription(description);
		request.setOperationType(operationType);
		request.setPaymentType("C");
		request.setClientId(client.getId());
		request.setCarwash(Constants.URL_PREFIX+washer.getId());
		setWaitScreen(true);
		BA.getEventBus().post(request);
	}

	@Subscribe
	public void onAdminAddOperationReceived(AdminCarwashTransactionAddResponseEvent event){
		setWaitScreen(false);
		if(paymentDialog != null)
			paymentDialog.dismiss();
		if(event.getData() != null){
			if(BaseAssist.isSuccess(event.getData())){
				if (washer != null) {
					if(client!=null){
						loadData();
					}
				}
				ToastUtil.display(getActivity(), "Успешно провели транзакцию");
//				finish();
			}
			else{
				ToastUtil.display(getActivity(), event.getData().getMessage());
			}
		}
		else{
			ToastUtil.display(getActivity(), "Ошибка при добавлении операции");
		}
	}

	@Subscribe
	public void onUserReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
	}
	User user;
	Activity context;
}
