package com.driverspa.admin.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.activity.BaseActivity;
import com.driverspa.activity.GalleryPhotosActivity;
import com.driverspa.admin.activity.AdditionalInformationActivity;
import com.driverspa.admin.activity.AddressActivity;
import com.driverspa.admin.activity.AddressMapActivity;
import com.driverspa.admin.activity.AdminBoxesActivity;
import com.driverspa.admin.activity.AdminGroupServicePriceActivity;
import com.driverspa.admin.activity.AdminPricesActivity;
import com.driverspa.admin.activity.AdminServicePriceActivity;
import com.driverspa.admin.activity.ExtraSettingsActivity;
import com.driverspa.admin.activity.TimeShortDefinitionActivity;
import com.driverspa.dialog.MultipleSelectDialog;
import com.driverspa.dialog.SingleSelectDialog;
import com.driverspa.model.BookingType;
import com.driverspa.model.Box;
import com.driverspa.model.Image;
import com.driverspa.model.NotificationType;
import com.driverspa.model.PhotoParcelable;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.ActivityForResult;
import com.driverspa.util.Constants;
import com.driverspa.util.HttpClient;
import com.driverspa.util.JsonUtil;
import com.driverspa.util.L;
import com.driverspa.util.RetrofitClient;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.TransformationCircle;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.PhotoPostRequestEvent;
import com.driverspa.util.otto.ws.AdminPhotoPostRequestEvent;
import com.driverspa.util.otto.ws.AdminPhotoPostResponseEvent;
import com.driverspa.util.otto.ws.ClientPhotoPostRequestEvent;
import com.driverspa.util.otto.ws.EditWasherRequestEvent;
import com.driverspa.util.otto.ws.EditWasherResponseEvent;
import com.driverspa.util.otto.ws.UserGetSelfRequestEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminProfileFragment extends AdminBaseFragment{

	private ActivityActions activityActions;
	String userId = UserPreferences.getUserId(BA.getContext());
	String phone = UserPreferences.getUserPhone(BA.getContext());

	public interface ActivityActions{
		public void showProgressBar(boolean set);
		public void profileUserPhotoChange(String userId, ArrayList<PhotoParcelable> photos);
	}

	User user;

	private ArrayList<PhotoParcelable> profilePhotos;
	MultipleSelectDialog carTypesDialog;
	MultipleSelectDialog servicesDialog;
	SingleSelectDialog boxDialog;

	@BindView (R.id.companyName)
	EditText companyName;
	@BindView (R.id.company_avatar)
	ImageView companyImage;
	@BindView(R.id.txtAddress)
	TextView txtAddress;
	@BindView(R.id.txtCarType)
	TextView txtCarType;
	@BindView(R.id.txtServices)
	TextView txtServices;
	@BindView(R.id.txtBoxes)
	TextView txtBoxes;
	@BindView(R.id.public_washer)
	CheckBox publicWasher;
	@BindView(R.id.images_count)
	TextView imagesCount;
	@BindView(R.id.washerStatus)
	TextView washerStatus;
	@BindView(R.id.images_count_rect)
	View imagesCountRect;
	@BindView(R.id.images_circle)
	View imagesCircle;
    @BindView(R.id.receive_notification)
	CheckBox receiveNotification;
    @BindView(R.id.checkbox)
	View receiveNotificationCheckBox;
	@BindView(R.id.profile_avatar_invisible)
	ImageView avatarBackground;
	@BindView(R.id.fade_for_avatar)
	View fadeAvatar;

	boolean informationSaved = false;
	HashMap<String,Double> menuServicesPrices = new HashMap<String,Double>();
	Washer washer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_admin_profile, container,false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
//		TmpMyWash.createInfoWashers("semey");
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		ButterKnife.bind(this, view);
		Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("Профиль");

		companyName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			  if(washer != null)
				 washer.setName(s.toString());
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				saveData();
				return true;
			case R.id.menu_add:
				profileViewAddMedia();
				return true;
			default:
				return super.onOptionsItemSelected(item);
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
		BA.setNotificationType(NotificationType.PROFILE);
	}

	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
		if(washer != null) {
			if(!TextUtils.isEmpty(companyName.getText().toString()))
				washer.setName(companyName.getText().toString());
//			    saveData();
//			    BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
		}
		BA.setNotificationType(NotificationType.NOTHING);
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

	@Subscribe
	public void onUserSelfReceived(UserGetSelfResponseEvent event){
		setWaitScreen(false);
		user = event.getUser();
		profilePhotos = new ArrayList<PhotoParcelable>();
		if(user != null){
			for(Image photo: user.getImages()){
				PhotoParcelable parcelablePhoto = PhotoParcelable.newPhoto(photo.getPhotoOrm(user.getId()));
				profilePhotos.add(parcelablePhoto);
			}

			imagesCircle.setVisibility(View.GONE);
			imagesCount.setVisibility(View.GONE);
			imagesCountRect.setVisibility(View.GONE);

			if(!TextUtils.isEmpty(user.getAvatar())){
				HttpClient.getPicasso()
						.load(RetrofitClient.API_URL_IMAGES+user.getAvatar())
						.fit()
						.centerCrop()
						.transform(new TransformationCircle(getActivity()))
						.into(companyImage, new Callback() {
							@Override
							public void onSuccess() {
								int cnt = user.getImages().size();
								if(cnt > 1){
									imagesCircle.setVisibility(View.VISIBLE);
									imagesCount.setVisibility(View.VISIBLE);
									imagesCount.setText(cnt+"");
									imagesCountRect.setVisibility(View.VISIBLE);
								}
							}

							@Override
							public void onError() {
								imagesCircle.setVisibility(View.GONE);
								imagesCount.setVisibility(View.GONE);
								imagesCountRect.setVisibility(View.GONE);
							}
						});

				fadeAvatar.setVisibility(View.GONE);
				avatarBackground.setVisibility(View.GONE);

//				HttpClient.getPicasso()
//						.load(RetrofitClient.API_URL_IMAGES + user.getAvatar())
//						.fit()
//						.centerCrop()
//						.into(avatarBackground, new Callback() {
//							@Override
//							public void onSuccess() {
//								fadeAvatar.setVisibility(View.VISIBLE);
//								avatarBackground.setVisibility(View.VISIBLE);
//							}
//
//							@Override
//							public void onError() {
//								fadeAvatar.setVisibility(View.GONE);
//								avatarBackground.setVisibility(View.GONE);
//							}
//						});
//				fadeAvatar.setVisibility(View.VISIBLE);

			}
			else{
				fadeAvatar.setVisibility(View.GONE);
				avatarBackground.setVisibility(View.GONE);
				companyImage.setVisibility(View.VISIBLE);
				companyImage.setImageResource(R.drawable.ic_no_image);
			}
		}
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event){
		L.d("profile washer received");
		washer = event.getWasher();
		setData();
	}

	public void setData(){

		receiveNotification.setChecked(UserPreferences.isUserNotificationEnabled(BA.getContext()));
		receiveNotificationCheckBox.setVisibility(UserPreferences.isUserNotificationEnabled(BA.getContext())?View.VISIBLE:View.GONE);

		if(washer != null){

			//Company name set
			if(washer.getName()!=null && washer.getName().equals("NA")) companyName.setText("");
			else companyName.setText(washer.getName());

			publicWasher.setChecked(washer.isWasherPublic());

			if(washer.getStatus() != null && washer.getStatus().equals(Constants.PENDING)){
				washerStatus.setText("Ваш запрос находится в обработке...");
			}
			else if(washer.getStatus() != null && washer.getStatus().equals(Constants.TEST)){
				washerStatus.setText("Тестовая мойка");
			}
			else if(washer.getStatus() != null && washer.getStatus().equals(Constants.APPROVED)){
				washerStatus.setText("Мойка активна");
			}
			else if(washer.getStatus() != null && washer.getStatus().equals("information")){
				washerStatus.setText("Вы не принимаете брони");
			}

			if(!TextUtils.isEmpty(washer.getAddress()))
				txtAddress.setText(washer.getAddress());
			else
				txtAddress.setText("Адрес");

			//Car types initilize
			List<HashMap<String,String>> carTypeMapList = new ArrayList<HashMap<String,String>>();
			final HashMap<Integer,String> allCarTypes = BA.getReference().getCarType();
			List<HashMap<String,String>> selectedCarTypeMapList = new ArrayList<HashMap<String,String>>();

			for (Map.Entry<Integer,String> entry : allCarTypes.entrySet()) {
				HashMap<String,String> carMap = new HashMap<String,String>();
				Integer key = entry.getKey();
				String value = entry.getValue();
				carMap.put(MultipleSelectDialog.ID, Integer.toString(key));
				carMap.put(MultipleSelectDialog.NAME, value);
				carTypeMapList.add(carMap);
			}

			for(Integer carType : washer.getCarTypes()){
				HashMap<String,String> carMap = new HashMap<String,String>();
				carMap.put(MultipleSelectDialog.ID, Integer.toString(carType));
				carMap.put(MultipleSelectDialog.NAME, allCarTypes.get(carType));
				selectedCarTypeMapList.add(carMap);
			}

			if(selectedCarTypeMapList.size() == 1){
				txtCarType.setText(allCarTypes.get(Integer.parseInt(selectedCarTypeMapList.get(0).get(MultipleSelectDialog.ID))));
			}
			else{
				txtCarType.setText("Выбрано ("+selectedCarTypeMapList.size()+") типа машин");
			}

			final ArrayList<Integer> requestCarTypes = new ArrayList<Integer>();
			carTypesDialog = new MultipleSelectDialog(getActivity(), carTypeMapList, "Выберите услуги", selectedCarTypeMapList) {
				@Override
				public void onDismiss(HashMap<Integer, String> namesSelectedItm) {

					if(namesSelectedItm.size() == 0){
						txtCarType.setText("Выберите типы машин");
					}
					else if(namesSelectedItm.size() == 1){
						requestCarTypes.add(Integer.parseInt(namesSelectedItm.get(0)));
						txtCarType.setText(allCarTypes.get(Integer.parseInt(namesSelectedItm.get(0))));
					}
					else{
						txtCarType.setText("Выбрано ("+namesSelectedItm.size()+") типа машин");
						for(int i = 0; i < namesSelectedItm.size(); i++)
							requestCarTypes.add(Integer.parseInt(namesSelectedItm.get(i)));
					}
					washer.setCarTypes(new ArrayList<Integer>(requestCarTypes));
					requestCarTypes.clear();
				}
			};

			//Services initilize
			List<HashMap<String,String>> servicesMapList = new ArrayList<HashMap<String,String>>();
			final HashMap<Integer,String> allServices = BA.getReference().getServices();
			List<HashMap<String,String>> selectedServicesMapList = new ArrayList<HashMap<String,String>>();

			for (Map.Entry<Integer,String> entry : allServices.entrySet()) {
				HashMap<String,String> serviceMap = new HashMap<String,String>();
				Integer key = entry.getKey();
				String value = entry.getValue();
				serviceMap.put(MultipleSelectDialog.ID, Integer.toString(key));
				serviceMap.put(MultipleSelectDialog.NAME, value);
				servicesMapList.add(serviceMap);
			}

			for(Integer service : washer.getServices()){
				HashMap<String,String> serviceMap = new HashMap<String,String>();
				serviceMap.put(MultipleSelectDialog.ID, Integer.toString(service));
				serviceMap.put(MultipleSelectDialog.NAME, allServices.get(service));
				selectedServicesMapList.add(serviceMap);
			}

			if(selectedServicesMapList.size() == 1){
				txtServices.setText(allServices.get(Integer.parseInt(selectedServicesMapList.get(0).get(MultipleSelectDialog.ID))));
			}
			else{
				txtServices.setText("Выбрано ("+selectedServicesMapList.size()+") услуг");
			}

			final ArrayList<Integer> requestServices = new ArrayList<Integer>();
			servicesDialog = new MultipleSelectDialog(getActivity(), servicesMapList, "Выберите услуги", selectedServicesMapList) {
				@Override
				public void onDismiss(HashMap<Integer, String> namesSelectedItm) {

					if(namesSelectedItm.size() == 0){
						txtServices.setText("Выберите услуги");
					}
					else if(namesSelectedItm.size() == 1){
						requestServices.add(Integer.parseInt(namesSelectedItm.get(0)));
						txtServices.setText(allServices.get(Integer.parseInt(namesSelectedItm.get(0))));
					}
					else{
						txtServices.setText("Выбрано ("+namesSelectedItm.size()+") услуг");
						for(int i = 0; i < namesSelectedItm.size(); i++){
							requestServices.add(Integer.parseInt(namesSelectedItm.get(i)));
						}
					}
					washer.setServices(new ArrayList<Integer>(requestServices));
					requestServices.clear();
				}
			};

			//Box count
			List<HashMap<String,String>> boxMapList = new ArrayList<HashMap<String,String>>();
			List<HashMap<String,String>> selectedBoxMapList = new ArrayList<HashMap<String,String>>();
			if(washer.getBoxSettings() != null && washer.getBoxSettings().size() > 0){
				String size = Integer.toString(washer.getBoxSettings().size());
				HashMap<String,String> selectedBox = new HashMap<String,String>();
				selectedBox.put(size, size);
				selectedBoxMapList.add(selectedBox);
				txtBoxes.setText("Количество боксов: "+washer.getBoxSettings().size());
			}

			for(int i = 1; i <= 15; i++){
				HashMap<String,String> boxMap = new HashMap<String,String>();
				boxMap.put(SingleSelectDialog.ID, Integer.toString(i));
				boxMap.put(SingleSelectDialog.NAME, Integer.toString(i));
				boxMapList.add(boxMap);
			}

			final ArrayList<Box> boxSettings = new ArrayList<Box>();
			boxDialog = new SingleSelectDialog(getActivity(), boxMapList, "Количество боксов", selectedBoxMapList) {
				@Override
				public void onDismiss(HashMap<Integer, String> namesSelectedItm) {
					if(namesSelectedItm.get(0) != null){
						txtBoxes.setText("Количество боксов: "+namesSelectedItm.get(0));
						for(int i = 0; i < Integer.parseInt(namesSelectedItm.get(0).toString()); i++){
							Box boxSetting = new Box();
							boxSetting.setBookingType(BookingType.Hybrid);
							boxSettings.add(boxSetting);
						}
					}
					else
						txtBoxes.setText("Боксы");
	//				  washer.setBoxSettings(boxSettings);
//					washer.setBoxSettings(null);
					boxSettings.clear();
				}
			};

		}
	}

	private boolean validateProfileInfo(String str, View view, String warningMessage){
		if(TextUtils.isEmpty(str) || str.length() <= 2){
			ToastUtil.display(getActivity(), warningMessage);
			view.requestFocus();
			return false;
		}
		return true;
	}

	@Subscribe
	public void onPhotoPostRequested(PhotoPostRequestEvent event){
		setWaitScreen(true);
		BA.getEventBus().post(new ClientPhotoPostRequestEvent(event.getFile()));
	}

	public void profileViewAddMedia() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		intent.putExtra(BaseActivity.OPENING_ANIMATION, false);
		startActivityForResult(intent, ActivityForResult.ADMIN_PHOTO_UPLOAD);
	}

	@Override
	public void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ActivityForResult.ADMIN_PHOTO_UPLOAD) {
			if (resultCode == Activity.RESULT_OK) {
				String selectedImagePath = null;
				Uri selectedImageUri = data.getData();
				Cursor cursor = getActivity().getContentResolver().query(selectedImageUri, null, null, null, null);
				if (cursor == null) {
					selectedImagePath = selectedImageUri.getPath();
				} else {
					cursor.moveToFirst();
					int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
					selectedImagePath = cursor.getString(idx);
				}
				setWaitScreen(true);
				BA.getEventBus().post(new AdminPhotoPostRequestEvent(new File(selectedImagePath),washer.getId()));
			}
		}
		else if ( requestCode == ActivityForResult.ACTIVITY_PHOTO_CHANGE ) {
			// check if user has changed photo or not
			if( resultCode == ActivityForResult.PHOTO_CHANGED_YES) {
				// update view as user info has changed
//				setWaitScreen(true);
				BA.getEventBus().post(new UserGetSelfRequestEvent(userId));
			}
		}
		else if ( requestCode == ActivityForResult.ACTIVITY_ADDRESS ) {
			// check if user has changed photo or not
			if( resultCode == ActivityForResult.ACTIVITY_ADDRESS_CHANGED_YES) {
//				washer.setAddress(data.getStringExtra(AddressActivity.STREET));
				if(!TextUtils.isEmpty(washer.getAddress()))
					txtAddress.setText(washer.getAddress());
				else
					txtAddress.setText("Адрес");
//
//				ArrayList<Double> lonLat = new ArrayList<Double>();
//				lonLat.add(data.getDoubleExtra(AddressMapActivity.LOC_LON, AddressMapActivity.LON));
//				lonLat.add(data.getDoubleExtra(AddressMapActivity.LOC_LAT, AddressMapActivity.LAT));
//				washer.setLonLat(lonLat);
			}
		}
		else if ( requestCode == ActivityForResult.ACTIVITY_PRICE ) {
//			if( resultCode == ActivityForResult.ACTIVITY_PRICE_CHANGED_YES) {
////				washer = JsonUtil.deserializeToWasherBase(data.getStringExtra(Constants.WASHER_BASE));
//			}
		}
		else if ( requestCode == ActivityForResult.ACTIVITY_ADDITIONAL_INFO ) {
			if( resultCode == ActivityForResult.ACTIVITY_ADDITIONAL_INFO_CHANGED_YES) {
//				washer = JsonUtil.deserializeToWasherBase(data.getStringExtra(Constants.WASHER_BASE));
			}
		}
		else if ( requestCode == ActivityForResult.ACTIVITY_GALLERY_PHOTOS ) {
			// check if user has changed photo or not
			if( resultCode == ActivityForResult.GALLERY_PHOTO_CHANGED_YES) {
				// update view as user info has changed
				setWaitScreen(true);
				BA.getEventBus().post(new UserGetSelfRequestEvent(userId));
			}
		}
	}

	@Subscribe
	public void onPhotoPostReceived(AdminPhotoPostResponseEvent event){
		setWaitScreen(false);
		if(event.getResponse() != null){
			if(BaseAssist.isSuccess(event.getResponse())){
				ToastUtil.display(getActivity(), "Успешно загрузили фото");
				BA.getEventBus().post(new UserGetSelfRequestEvent(userId));
			}
			else{
				ToastUtil.display(getActivity(), event.getResponse().getMessage());
			}
		}
		else{
		}
	}

	@OnClick (R.id.company_avatar)
	public void onPhotoChangeClick(){
		Builder b = new Builder(getActivity(),AlertDialog.THEME_HOLO_LIGHT);
		b.setItems(R.array.photo_options, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int i)
			{
				switch (i)
				{
					case 0:
						profileViewAddMedia();
						break;
//					case 1:
//						if(profilePhotos.size() > 1){
//
//							startActivityForResult(UserPhotoChangeActivity.newIntent(getActivity(), userId, profilePhotos), ActivityForResult.ACTIVITY_PHOTO_CHANGE);
//						}
//						else{
//							ToastUtil.display(getActivity(), "Загрузите 1 и более фото для выбора в качестве аватара!");
//						}
//						break;
					case 1:
						if(profilePhotos.size() > 0){
							startActivityForResult(GalleryPhotosActivity.newIntent(getActivity(), profilePhotos, 0, true), ActivityForResult.ACTIVITY_GALLERY_PHOTOS);
						}
						else{
							ToastUtil.display(getActivity(), "Загрузите 1 и более фото для просмотра!");
						}
						break;
				}
			}
		});
//		b.setTitle("Фото");
//		b.setNeutralButton("Отменить", null);
		b.show();
	}

	@OnClick(R.id.activate_washer)
	public void onActivateWasherClicked(){
		washer.setWasherPublic(!publicWasher.isChecked());
		publicWasher.setChecked(!publicWasher.isChecked());
	}

	@OnClick(R.id.address)
	public void onAddressClicked(){
//		Intent intent = new Intent(this,AddressMapActivity.class);
//		intent.putExtra(AddressMapActivity.LOC_LAT, lat);
//		intent.putExtra(AddressMapActivity.LOC_LON, lon);
//		startActivityForResult(intent, ActivityForResult.ACTIVITY_ADDRESS_MAP);

		Intent intent = new Intent(getActivity(),AddressMapActivity.class);
		String address;
		try{
			address = washer.getAddress();
		}
		catch(Exception e){
			address = "";
		}

		intent.putExtra(AddressActivity.STREET, address);
		double lat = washer.getLonLat().get(1);
		double lon = washer.getLonLat().get(0);


		intent.putExtra(AddressMapActivity.LOC_LAT, lat);
		intent.putExtra(AddressMapActivity.LOC_LON, lon);
		startActivityForResult(intent, ActivityForResult.ACTIVITY_ADDRESS_MAP);
		overridePendingTransitionWithCommoOpeningTransition(getActivity());
	}

	@OnClick (R.id.carType)
	public void onCarTypesClicked(){
//		carTypesDialog.show();
		Intent intent = new Intent(getActivity(),AdminServicePriceActivity.class);
		startActivityForResult(intent, ActivityForResult.ACTIVITY_PRICE);
		overridePendingTransitionWithCommoOpeningTransition(getActivity());
	}

	@OnClick (R.id.services)
	public void onServicesClicked(){
		servicesDialog.show();
	}

	@OnClick (R.id.prices)
	public void onPriceDefinitionClicked() {
		Intent intent = new Intent(getActivity(), AdminPricesActivity.class);
//		Intent intent = new Intent(getActivity(), AdminServicePriceActivity.class);
		startActivityForResult(intent, ActivityForResult.ACTIVITY_PRICE);
		overridePendingTransitionWithCommoOpeningTransition(getActivity());
	}

	@OnClick (R.id.extran_settings)
	public void onExtraSettingsClicked() {
		Intent intent = new Intent(getActivity(), ExtraSettingsActivity.class);
		startActivityForResult(intent, ActivityForResult.ACTIVITY_PRICE);
		overridePendingTransitionWithCommoOpeningTransition(getActivity());
	}

	@OnClick (R.id.group_prices)
	public void onGroupPriceDefinitionClicked(){
		Intent intent = new Intent(getActivity(),AdminGroupServicePriceActivity.class);
		startActivityForResult(intent, ActivityForResult.ACTIVITY_PRICE);
		overridePendingTransitionWithCommoOpeningTransition(getActivity());

//		Intent intent = new Intent(getActivity(),PriceDefinitionActivity.class);
//		LinkedHashMap<Integer, ArrayList<Prices>> carTypeMenuAll = new LinkedHashMap<Integer, ArrayList<Prices>>();
//		LinkedHashMap<Integer, ArrayList<Prices>> carTypeMenu = new LinkedHashMap<Integer, ArrayList<Prices>>();
//		if(washer.getCarTypes() != null && washer.getCarTypes().size() > 0 && washer.getServices() != null && washer.getServices().size() > 0){
//			try{
//				carTypeMenuAll = washer.getMenu();
////					   if(carTypeMenu == null) carTypeMenu = new HashMap<Integer, ArrayList<Prices>>();
//					for(Integer carType : washer.getCarTypes()){
//						//Check if there is no menu for this car type
//						if(carTypeMenuAll.get(carType) == null) {
//							ArrayList<Prices> prices = new ArrayList<Prices>();
//							for (Integer service : washer.getServices()) {
//								Prices price = new Prices();
//								price.setType(service);
//								price.setTime(0);
//								price.setPrice(0d);
//								prices.add(price);
//							}
//							carTypeMenu.put(carType, prices);
//						}
//						else{
//							//Check if there is services of prices not equal to selected services
//							ArrayList<Prices> prices = new ArrayList<Prices>();
//							for (Integer service : washer.getServices()) {
//								Prices price = new Prices();
//								price.setType(service);
//								price.setTime(0);
//								price.setPrice(0d);
//								for(Prices p : carTypeMenuAll.get(carType)){
//									if(p.getType().intValue() == service.intValue()){
//										price = p;
//										break;
//									}
//								}
//								prices.add(price);
//							}
//						  carTypeMenu.put(carType, prices);
//						}
//				}
//
//
//			}
//			catch(Exception e){
//			}
//			washer.setMenu(carTypeMenu);
//			intent.putExtra(Constants.WASHER_BASE, JsonUtil.serialize(washer));
//			startActivityForResult(intent, ActivityForResult.ACTIVITY_PRICE);
//			overridePendingTransitionWithCommoOpeningTransition(getActivity());
//		}
//		else{
//			ToastUtil.display(getActivity(), "Небходимо определить тип машин и услуги");
//		}
	}

	@OnClick (R.id.boxes)
	public void onBoxButtonClicked(){
		boxDialog.show();
	}

	@OnClick (R.id.additional)
	public void onAdditionalInformationClicked(){
		Intent intent = new Intent(getActivity(),AdditionalInformationActivity.class);
		intent.putExtra(Constants.WASHER_BASE, JsonUtil.serialize(washer));
		startActivityForResult(intent, ActivityForResult.ACTIVITY_ADDITIONAL_INFO);
		overridePendingTransitionWithCommoOpeningTransition(getActivity());
	}

	@OnClick (R.id.btnSave)
	public void onSaveButtonClicked(){
		saveData();
	}

	public void saveData(){
        if(TextUtils.isEmpty(companyName.getText().toString())){
			companyName.setError("Введите название мойки");
			companyImage.requestFocus();
			return;
		}

		if(!TextUtils.isEmpty(companyName.getText().toString()))
		   washer.setName(companyName.getText().toString());
		washer.setWasherPublic(true);
//		WasherBase washerForEdit = (WasherBase)washer;
//		WasherBase washerForEdit2 = washerForEdit.clone();
		BA.getEventBus().post(new EditWasherRequestEvent(washer.getId(), washer));
	}

	public boolean saveExitData(){
		if(TextUtils.isEmpty(companyName.getText().toString())){
//			ToastUtil.displayAtTop(getActivity(),"Введите название мойки");
			companyName.setError("Введите название мойки");
			companyImage.requestFocus();
			return false;
		}

		if(!TextUtils.isEmpty(companyName.getText().toString()))
			washer.setName(companyName.getText().toString());
		washer.setWasherPublic(true);
//		WasherBase washerForEdit = (WasherBase)washer;
//		WasherBase washerForEdit2 = washerForEdit.clone();
		BA.getEventBus().post(new EditWasherRequestEvent(washer.getId(), washer));
		return true;
	}


	@Subscribe
	public void onWasherEditedReceived(EditWasherResponseEvent event){
		setWaitScreen(false);
		if(event.getData() != null){
			if(BaseAssist.isSuccess(event.getData())){
				informationSaved = true;
				if(washer.getStatus() != null && washer.getStatus().equals(Constants.PENDING)) {
					ToastUtil.displayAtTop(getActivity(), "Успешно сохранено");
				}
				else
					ToastUtil.displayAtTop(getActivity(), "Успешно сохранено");
			}
			else{
				ToastUtil.displayAtTop(getActivity(), event.getData().getMessage());
			}
		}
		else{
			ToastUtil.displayAtTop(getActivity(), "Неизвестная ошибка при сохранении профиля пользователя");
		}
	}

	    @OnClick(R.id.time)
    public void onTimeDefinitionClicked(){
		Intent intent = new Intent(getActivity(),TimeShortDefinitionActivity.class);
		startActivityForResult(intent, ActivityForResult.ACTIVITY_TIME_DEFINITION);
		overridePendingTransitionWithCommoOpeningTransition(getActivity());
    }


	@OnClick (R.id.boxSettings)
	public void onBoxSettingsButtonClicked(){
		Intent intent = new Intent(getActivity(), AdminBoxesActivity.class);
		intent.putExtra(Constants.EXTRA_WASHER_ID, washer.getId());
		startActivity(intent);
		overridePendingTransitionWithCommoOpeningTransition(getActivity());
	}

	@OnClick(R.id.notification)
	public void onNotificationCheckChanged(){
		UserPreferences.setUserNotification(BA.getContext(),!receiveNotification.isChecked());
		receiveNotification.setChecked(!receiveNotification.isChecked());
		receiveNotificationCheckBox.setVisibility(receiveNotification.isChecked()?View.VISIBLE:View.GONE);
	}
}
