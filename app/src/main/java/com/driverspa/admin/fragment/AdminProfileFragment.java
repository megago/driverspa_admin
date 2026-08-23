package com.driverspa.admin.fragment;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AlertDialog.Builder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import com.driverspa.dialog.LanguageDialog;
import com.driverspa.util.LocaleManager;
import com.driverspa.admin.activity.TimeShortDefinitionActivity;
import com.driverspa.dialog.MultipleSelectDialog;
import com.driverspa.dialog.SingleSelectDialog;
import com.driverspa.model.BookingType;
import com.driverspa.model.Box;
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
	@BindView(R.id.txtCurrentLanguage)
	TextView txtCurrentLanguage;
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
	@BindView(R.id.txtEmail)
	TextView txtEmail;
	@BindView(R.id.emailDivider)
	View emailDivider;

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
		titleView.setText(getString(R.string.profile_title));

		txtCurrentLanguage.setText(LocaleManager.displayName(LocaleManager.current()));

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
		// Refresh the account from the server so fields like email are current
		// (the bus otherwise delivers a possibly-stale cached user).
		if (!TextUtils.isEmpty(userId)) {
			BA.getEventBus().post(new UserGetSelfRequestEvent(userId));
		}
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
			// Show the account email (read-only) on the profile.
			if (!TextUtils.isEmpty(user.getEmail())) {
				txtEmail.setText(user.getEmail());
				txtEmail.setVisibility(View.VISIBLE);
				emailDivider.setVisibility(View.VISIBLE);
			} else {
				txtEmail.setVisibility(View.GONE);
				emailDivider.setVisibility(View.GONE);
			}
			if (user.getImages() != null) {
				profilePhotos = user.getImages().stream()
						.map(photo -> PhotoParcelable.newPhoto(photo.getPhotoOrm(user.getId())))
						.collect(Collectors.toCollection(ArrayList::new));
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
				washerStatus.setText(BA.str(R.string.request_in_progress));
			}
			else if(washer.getStatus() != null && washer.getStatus().equals(Constants.TEST)){
				washerStatus.setText(BA.str(R.string.test_wash));
			}
			else if(washer.getStatus() != null && washer.getStatus().equals(Constants.APPROVED)){
				washerStatus.setText(BA.str(R.string.washer_active));
			}
			else if(washer.getStatus() != null && washer.getStatus().equals("information")){
				washerStatus.setText(BA.str(R.string.not_accepting_bookings));
			}

			if(!TextUtils.isEmpty(washer.getAddress()))
				txtAddress.setText(washer.getAddress());
			else
				txtAddress.setText(BA.str(R.string.address));

			//Car types initilize
			List<HashMap<String,String>> carTypeMapList = new ArrayList<HashMap<String,String>>();
			final HashMap<Integer,String> allCarTypes = BA.getReference().getCarType();
			List<HashMap<String,String>> selectedCarTypeMapList = new ArrayList<HashMap<String,String>>();

			carTypeMapList = allCarTypes.entrySet().stream()
					.map(entry -> toDialogItem(entry.getKey(), entry.getValue()))
					.collect(Collectors.toList());

			selectedCarTypeMapList = washer.getCarTypes().stream()
					.map(carType -> toDialogItem(carType, allCarTypes.get(carType)))
					.collect(Collectors.toList());

			if(selectedCarTypeMapList.size() == 1){
				txtCarType.setText(allCarTypes.get(Integer.parseInt(selectedCarTypeMapList.get(0).get(MultipleSelectDialog.ID))));
			}
			else{
				txtCarType.setText(BA.str(R.string.selected_paren)+selectedCarTypeMapList.size()+BA.str(R.string.paren_car_types));
			}

			final ArrayList<Integer> requestCarTypes = new ArrayList<Integer>();
			carTypesDialog = new MultipleSelectDialog(getActivity(), carTypeMapList, BA.str(R.string.select_services), selectedCarTypeMapList) {
				@Override
				public void onDismiss(HashMap<Integer, String> namesSelectedItm) {
					requestCarTypes.clear();
					requestCarTypes.addAll(toSelectedIds(namesSelectedItm));

					if(namesSelectedItm.size() == 0){
						txtCarType.setText(BA.str(R.string.select_car_types));
					}
					else if(namesSelectedItm.size() == 1){
						txtCarType.setText(allCarTypes.get(Integer.parseInt(namesSelectedItm.get(0))));
					}
					else{
						txtCarType.setText(BA.str(R.string.selected_paren)+namesSelectedItm.size()+BA.str(R.string.paren_car_types));
					}
					washer.setCarTypes(new ArrayList<Integer>(requestCarTypes));
				}
			};

			//Services initilize
			List<HashMap<String,String>> servicesMapList = new ArrayList<HashMap<String,String>>();
			final HashMap<Integer,String> allServices = BA.getReference().getServices();
			List<HashMap<String,String>> selectedServicesMapList = new ArrayList<HashMap<String,String>>();

			servicesMapList = allServices.entrySet().stream()
					.map(entry -> toDialogItem(entry.getKey(), entry.getValue()))
					.collect(Collectors.toList());

			selectedServicesMapList = washer.getServices().stream()
					.map(service -> toDialogItem(service, allServices.get(service)))
					.collect(Collectors.toList());

			if(selectedServicesMapList.size() == 1){
				txtServices.setText(allServices.get(Integer.parseInt(selectedServicesMapList.get(0).get(MultipleSelectDialog.ID))));
			}
			else{
				txtServices.setText(BA.str(R.string.selected_paren)+selectedServicesMapList.size()+BA.str(R.string.paren_services));
			}

			final ArrayList<Integer> requestServices = new ArrayList<Integer>();
			servicesDialog = new MultipleSelectDialog(getActivity(), servicesMapList, BA.str(R.string.select_services), selectedServicesMapList) {
				@Override
				public void onDismiss(HashMap<Integer, String> namesSelectedItm) {
					requestServices.clear();
					requestServices.addAll(toSelectedIds(namesSelectedItm));

					if(namesSelectedItm.size() == 0){
						txtServices.setText(BA.str(R.string.select_services));
					}
					else if(namesSelectedItm.size() == 1){
						txtServices.setText(allServices.get(Integer.parseInt(namesSelectedItm.get(0))));
					}
					else{
						txtServices.setText(BA.str(R.string.selected_paren)+namesSelectedItm.size()+BA.str(R.string.paren_services));
					}
					washer.setServices(new ArrayList<Integer>(requestServices));
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
				txtBoxes.setText(BA.str(R.string.boxes_count_pre)+washer.getBoxSettings().size());
			}

			boxMapList = IntStream.rangeClosed(1, 15)
					.mapToObj(i -> toSingleSelectDialogItem(i))
					.collect(Collectors.toList());

			final ArrayList<Box> boxSettings = new ArrayList<Box>();
			boxDialog = new SingleSelectDialog(getActivity(), boxMapList, BA.str(R.string.boxes_count_label), selectedBoxMapList) {
				@Override
				public void onDismiss(HashMap<Integer, String> namesSelectedItm) {
					if(namesSelectedItm.get(0) != null){
						txtBoxes.setText(BA.str(R.string.boxes_count_pre)+namesSelectedItm.get(0));
						boxSettings.clear();
						boxSettings.addAll(IntStream.range(0, Integer.parseInt(namesSelectedItm.get(0).toString()))
								.mapToObj(i -> {
									Box boxSetting = new Box();
									boxSetting.setBookingType(BookingType.Hybrid);
									return boxSetting;
								})
								.collect(Collectors.toList()));
					}
					else
						txtBoxes.setText(BA.str(R.string.boxes));
	//				  washer.setBoxSettings(boxSettings);
//					washer.setBoxSettings(null);
					boxSettings.clear();
				}
			};

		}
	}

	private HashMap<String, String> toDialogItem(Integer id, String name) {
		HashMap<String, String> item = new HashMap<String, String>();
		item.put(MultipleSelectDialog.ID, Integer.toString(id));
		item.put(MultipleSelectDialog.NAME, name);
		return item;
	}

	private HashMap<String, String> toSingleSelectDialogItem(int value) {
		HashMap<String, String> item = new HashMap<String, String>();
		item.put(SingleSelectDialog.ID, Integer.toString(value));
		item.put(SingleSelectDialog.NAME, Integer.toString(value));
		return item;
	}

	private ArrayList<Integer> toSelectedIds(HashMap<Integer, String> namesSelectedItm) {
		return IntStream.range(0, namesSelectedItm.size())
				.mapToObj(namesSelectedItm::get)
				.filter(Objects::nonNull)
				.map(Integer::parseInt)
				.collect(Collectors.toCollection(ArrayList::new));
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

	/**
	 * Streams the content of a picked image Uri into an app-private cache file.
	 * Avoids the EACCES that direct raw-path access to gallery/DCIM media hits on
	 * Android 10+ (scoped storage). Returns the cache file, or null on failure.
	 */
	private File copyUriToCache(Uri uri) {
		if (getActivity() == null || uri == null) return null;
		InputStream in = null;
		OutputStream out = null;
		try {
			in = getActivity().getContentResolver().openInputStream(uri);
			if (in == null) return null;
			File outFile = new File(getActivity().getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
			out = new FileOutputStream(outFile);
			byte[] buffer = new byte[8192];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			out.flush();
			return outFile;
		} catch (Exception e) {
			return null;
		} finally {
			try { if (in != null) in.close(); } catch (Exception ignored) {}
			try { if (out != null) out.close(); } catch (Exception ignored) {}
		}
	}

	@Override
	public void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ActivityForResult.ADMIN_PHOTO_UPLOAD) {
			if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
				// Copy the picked image into our private cache. On Android 10+
				// the raw MediaStore path can't be opened directly (EACCES), so we
				// stream it via the ContentResolver and upload the cache copy.
				File file = copyUriToCache(data.getData());
				if (file == null) {
					ToastUtil.display(getActivity(), BA.str(R.string.cant_open_image));
				} else {
					setWaitScreen(true);
					BA.getEventBus().post(new AdminPhotoPostRequestEvent(file, washer.getId()));
				}
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
					txtAddress.setText(BA.str(R.string.address));
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
				ToastUtil.display(getActivity(), BA.str(R.string.photo_uploaded_ok));
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
		closeKeyboard();
		Builder b = new MaterialAlertDialogBuilder(getActivity());
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
							ToastUtil.display(getActivity(), BA.str(R.string.upload_photo_hint));
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
		closeKeyboard();
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
		closeKeyboard();
//		carTypesDialog.show();
		Intent intent = new Intent(getActivity(),AdminServicePriceActivity.class);
		startActivityForResult(intent, ActivityForResult.ACTIVITY_PRICE);
		overridePendingTransitionWithCommoOpeningTransition(getActivity());
	}

	@OnClick (R.id.services)
	public void onServicesClicked(){
		closeKeyboard();
		servicesDialog.show();
	}

	@OnClick (R.id.prices)
	public void onPriceDefinitionClicked() {
		closeKeyboard();
		Intent intent = new Intent(getActivity(), AdminPricesActivity.class);
//		Intent intent = new Intent(getActivity(), AdminServicePriceActivity.class);
		startActivityForResult(intent, ActivityForResult.ACTIVITY_PRICE);
		overridePendingTransitionWithCommoOpeningTransition(getActivity());
	}

	@OnClick (R.id.extran_settings)
	public void onExtraSettingsClicked() {
		closeKeyboard();
		Intent intent = new Intent(getActivity(), ExtraSettingsActivity.class);
		startActivityForResult(intent, ActivityForResult.ACTIVITY_PRICE);
		overridePendingTransitionWithCommoOpeningTransition(getActivity());
	}

	@OnClick (R.id.language_settings)
	public void onLanguageClicked() {
		closeKeyboard();
		LanguageDialog.show(getActivity());
	}

	@OnClick (R.id.group_prices)
	public void onGroupPriceDefinitionClicked(){
		closeKeyboard();
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
		closeKeyboard();
		boxDialog.show();
	}

	@OnClick (R.id.additional)
	public void onAdditionalInformationClicked(){
		closeKeyboard();
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
			companyName.setError(BA.str(R.string.enter_washer_name));
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
			companyName.setError(BA.str(R.string.enter_washer_name));
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
					ToastUtil.displayAtTop(getActivity(), BA.str(R.string.saved_ok));
				}
				else
					ToastUtil.displayAtTop(getActivity(), BA.str(R.string.saved_ok));
			}
			else{
				ToastUtil.displayAtTop(getActivity(), event.getData().getMessage());
			}
		}
		else{
			ToastUtil.displayAtTop(getActivity(), BA.str(R.string.err_save_profile));
		}
	}

	    @OnClick(R.id.time)
    public void onTimeDefinitionClicked(){
		closeKeyboard();
		Intent intent = new Intent(getActivity(),TimeShortDefinitionActivity.class);
		startActivityForResult(intent, ActivityForResult.ACTIVITY_TIME_DEFINITION);
		overridePendingTransitionWithCommoOpeningTransition(getActivity());
    }


	@OnClick (R.id.boxSettings)
	public void onBoxSettingsButtonClicked(){
		closeKeyboard();
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
