package com.driverspa.admin.fragment;

/**
 * @author Yerzhan
 *
 */

import static com.driverspa.util.Constants.URL_PREFIX;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.driverspa.adapter.GroupServicesGridAdapter;
import com.driverspa.adapter.OfflineBoxesGridAdapter;
import com.driverspa.adapter.ServicesGridAdapter;
import com.driverspa.assist.BaseAssist;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Box;
import com.driverspa.model.CarType;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.ServiceItem;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.util.Functions;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminBookRequestEvent;
import com.driverspa.util.otto.ws.AdminBookResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.ExpandableGridView;

public class AdminAddQueuedBookFragment extends AdminBaseFragment {

	public interface ActivityActions {
	}

	Washer washer;
	String boxName;
	String boxId;
	Long gridTime;
	String gridTitle;
	String bookDay;

	@BindView(R.id.btnSedan)
	View buttonSedan;
	@BindView(R.id.btnCrossover)
	View buttonCrossover;
	@BindView(R.id.btnBigJeep)
	View buttonBigJeep;
	@BindView(R.id.btnMinbus)
	View buttonMinbus;
	@BindView(R.id.btnMini)
	View buttonMini;
	@BindView(R.id.btnUniversal)
	View buttonUniversal;
	@BindView(R.id.btnMiniJeep)
	View buttonMiniJeep;
	@BindView(R.id.btnMinivan)
	View buttonMiniVan;
	@BindView(R.id.btnMoto)
	View buttonMoto;

	@BindView(R.id.carNoWrapper)
	TextInputLayout carNoWrapper;
	@BindView(R.id.carNo)
	EditText carNo;
	@BindView(R.id.carMark)
	EditText carMark;
	@BindView(R.id.phone)
	EditText mobile;
	@BindView(R.id.discount)
	EditText discount;
	@BindView(R.id.comment)
	EditText comment;
	@BindView(R.id.txtBookTime)
	TextView txtBookTime;
	CarType selectedCarType;
	@BindView(R.id.txtPrice)
	TextView txtPrice;
	@BindView(R.id.txtTime)
	TextView txtTime;
	TextView titleView;

	@BindView(R.id.no_service)
	View noService;
	@BindView(R.id.no_offline_box)
	View noOfflineBox;

	@BindView(R.id.detailLayout)
	View detailLayout;
	@BindView(R.id.more)
	TextView moreButton;

	@BindView(R.id.extra)
	TextView extraButton;
	@BindView(R.id.extraLayout)
	View extraLayout;
	@BindView(R.id.paid)
	CheckBox paid;

	@BindView(R.id.btnBook)
	Button buttonBook;
	@BindView(R.id.btnQueue)
	Button buttonQueue;

	@BindView(R.id.toBox)
	View buttonToBox;
	@BindView(R.id.toQueue)
	View buttonToQueue;
	@BindView(R.id.priceLayout)
	View priceLayout;

	BookingRequest request;
	HashMap<Integer,Double> menuServicesPrices = new HashMap<Integer,Double>();

	@BindView(R.id.grid_group_services)
	ExpandableGridView gridGroupServices;
	@BindView(R.id.grid_services)
	ExpandableGridView gridServices;
	@BindView(R.id.grid_boxes)
	ExpandableGridView gridBoxes;
	@BindView(R.id.serviceLayout)
	View serviceLayout;
	@BindView(R.id.groupServiceLayout)
	View groupServiceLayout;
	@BindView(R.id.no_service_group)
	View noServiceGroup;
	@BindView(R.id.groupBtn)
	View groupBtn;
	@BindView(R.id.serviceBtn)
	View serviceBtn;

	private ServicesGridAdapter serviceAdapter;
	private GroupServicesGridAdapter groupServiceAdapter;
	private OfflineBoxesGridAdapter boxAdapter;

	private ActivityActions activityActions;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin_add_queue, container, false);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		ButterKnife.bind(this, view);

		Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
		titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText(BA.str(R.string.add_booking));

		selectedCarType = CarType.Sedan;
		groupBtn.setSelected(true);
		serviceBtn.setSelected(false);
		groupServiceLayout.setVisibility(View.VISIBLE);
		serviceLayout.setVisibility(View.GONE);
		buttonToBox.setSelected(true);
		buttonToQueue.setSelected(false);

		try {
			MaskedWatcher maskedWatcher = new MaskedWatcher("### ###-##-##");
			mobile.addTextChangedListener(maskedWatcher);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		discount.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if(serviceAdapter != null && serviceAdapter.getData() != null)
					calculatePriceAndTime((ArrayList) serviceAdapter.getData(),(ArrayList) groupServiceAdapter.getData());
			}
		});

		titleView.setText(BA.str(R.string.send_to_box));
		request = new BookingRequest();
		buttonSedan.setOnClickListener(buttonSelector);
		buttonCrossover.setOnClickListener(buttonSelector);
		buttonBigJeep.setOnClickListener(buttonSelector);
		buttonMinbus.setOnClickListener(buttonSelector);
		buttonMoto.setOnClickListener(buttonSelector);
		buttonMini.setOnClickListener(buttonSelector);
		buttonMiniJeep.setOnClickListener(buttonSelector);
		buttonMiniVan.setOnClickListener(buttonSelector);
		buttonUniversal.setOnClickListener(buttonSelector);

		buttonSedan.setSelected(true);
		buttonCrossover.setSelected(false);
		buttonBigJeep.setSelected(false);
		buttonMinbus.setSelected(false);
		buttonMoto.setSelected(false);

		WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth()-(int) Functions.dipToPixels(getActivity(), 10);

		ViewGroup.LayoutParams layoutParamsServices = gridServices.getLayoutParams();
		layoutParamsServices.width = width;
		gridServices.setLayoutParams(layoutParamsServices);
		gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(getActivity(), 32)) / 4);
		gridGroupServices.setLayoutParams(layoutParamsServices);
		gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(getActivity(), 32)) / 4);

		//set layout params same as grid services
		gridBoxes.setLayoutParams(layoutParamsServices);
		gridBoxes.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(getActivity(), 32)) / 4);

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


	@OnClick(R.id.toBox)
	public void onTodayButtonClick(){
		buttonToBox.setSelected(true);
		buttonToQueue.setSelected(false);
		priceLayout.setVisibility(View.VISIBLE);
		buttonBook.setVisibility(View.VISIBLE);
		buttonQueue.setVisibility(View.GONE);
		detailLayout.setVisibility(View.VISIBLE);
		titleView.setText(BA.str(R.string.send_to_box));
	}

	@OnClick(R.id.toQueue)
	public void onTomorrowButtonClick(){
		priceLayout.setVisibility(View.GONE);
		buttonToBox.setSelected(false);
		buttonToQueue.setSelected(true);
		buttonBook.setVisibility(View.GONE);
		buttonQueue.setVisibility(View.VISIBLE);
		detailLayout.setVisibility(View.GONE);
		titleView.setText(BA.str(R.string.add_to_queue));
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {
			makeCarTypeButtons(washer.getCarTypes());
			createService(selectedCarType.getValue());
			createBoxGrid(washer);
		}
	}

	private void createService(Integer carType){
		resetPriceAndTime();
		//Services
		ArrayList<ServiceItem> serviceItems = new ArrayList<ServiceItem>();
		final LinkedHashMap<Integer,String> dictionary = BA.getReference().getServices();
		LinkedHashMap<Integer,ArrayList<Washer.Prices>> washerMenu = washer.getMenu();
		if(washerMenu != null && washerMenu.size() > 0){
			serviceItems.clear();
			menuServicesPrices.clear();
			ArrayList<Washer.Prices> pricesMenu = washerMenu.get(carType);
			if (pricesMenu != null) {
				serviceItems.addAll(pricesMenu.stream().map(priceMenu -> {
					ServiceItem newItem = new ServiceItem();
					newItem.setServiceId(priceMenu.getType());
					newItem.setServiceName(dictionary.get(priceMenu.getType()));
					newItem.setPrice(priceMenu.getPrice());
					newItem.setTime(priceMenu.getTime());
					return newItem;
				}).collect(Collectors.toList()));
			}
		}

		//Group services
		List<Washer.GroupMenu> groupServiceItems = new ArrayList<Washer.GroupMenu>();
		List<Washer.GroupMenu> washerGroup = washer.getGroupMenu();
		if(washerGroup != null && washerGroup.size() > 0){
			groupServiceItems.clear();
			groupServiceItems.addAll(washerGroup.stream()
					.filter(g -> g.getPrices() != null && g.getPrices().stream().anyMatch(p -> p.getCarType() == carType))
					.collect(Collectors.toList()));
		}

		//Service adapter
		serviceAdapter = new ServicesGridAdapter(getActivity(),serviceItems);
		gridServices.setAdapter(serviceAdapter);
		gridServices.setExpanded(true);
		gridServices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				closeKeyboard();
				request.setServices(new ArrayList<Integer>());
				final ArrayList<Integer> requestServices = new ArrayList<Integer>();
				if (serviceAdapter.getSelectedPosition().get(position) != null && serviceAdapter.getSelectedPosition().get(position)) {
					serviceAdapter.setSelectedPosition(position, false);
					serviceAdapter.notifyDataSetChanged();
				} else {
					if(serviceAdapter.getData().get(position).getPrice() > 0) {
						if(request != null && request.getGroupServices() != null && request.getGroupServices().size() > 0){
							Washer.GroupMenu selectedGroup = null;
							if(groupServiceAdapter != null && groupServiceAdapter.getData() != null && groupServiceAdapter.getData().size() > 0) {
								selectedGroup = groupServiceAdapter.getData().stream()
										.filter(g -> request.getGroupServices().contains(g.getId()))
										.findFirst()
										.orElse(null);
							}
							if(selectedGroup != null && selectedGroup.getServices()!=null && selectedGroup.getServices().contains(serviceAdapter.getData().get(position).getServiceId())){
								ToastUtil.display(BA.getContext(),BA.str(R.string.service_already_in_complex));
							}
							else{
								serviceAdapter.setSelectedPosition(position, true);
								serviceAdapter.notifyDataSetChanged();
							}

						}
						else {
							serviceAdapter.setSelectedPosition(position, true);
							serviceAdapter.notifyDataSetChanged();
						}
					}
					else{
						ToastUtil.display(BA.getContext(),BA.str(R.string.err_no_price_for_type));
					}
				}

				requestServices.addAll(IntStream.range(0, serviceAdapter.getData().size())
						.filter(i -> serviceAdapter.getSelectedPosition().get(i) != null && serviceAdapter.getSelectedPosition().get(i))
						.mapToObj(i -> serviceAdapter.getData().get(i).getServiceId())
						.collect(Collectors.toList()));
				request.setServices(new ArrayList<Integer>(requestServices));
				requestServices.clear();
				calculatePriceAndTime((ArrayList) serviceAdapter.getData(),(ArrayList) groupServiceAdapter.getData());
			}
		});

		if(serviceItems.size() == 0){
			gridServices.setVisibility(View.GONE);
			noService.setVisibility(View.VISIBLE);
		}
		else{
			gridServices.setVisibility(View.VISIBLE);
			noService.setVisibility(View.GONE);
		}

		//Group Service adapter
		groupServiceAdapter = new GroupServicesGridAdapter(getActivity(),groupServiceItems);
		gridGroupServices.setAdapter(groupServiceAdapter);
		gridGroupServices.setExpanded(true);
		gridGroupServices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				closeKeyboard();
				request.setGroupServices(new ArrayList<String>());
				final ArrayList<String> groupRequestServices = new ArrayList<String>();

				if (groupServiceAdapter.getSelectedPosition().get(position) != null && groupServiceAdapter.getSelectedPosition().get(position)) {
					groupServiceAdapter.setSelectedPosition(position, false);
					groupServiceAdapter.notifyDataSetChanged();
				}
				else{
					Washer.GroupMenu selectedGroup = groupServiceAdapter.getData().get(position);

					if(selectedGroup != null && selectedGroup.getServices() != null && request != null && request.getServices() != null && request.getServices().size() > 0){
						boolean found = request.getServices().stream().anyMatch(s -> selectedGroup.getServices().contains(s));
						if(found) {
							ToastUtil.display(BA.getContext(), BA.str(R.string.service_already_selected_complex));
						}
						else{
							groupServiceAdapter.setSelectedPosition(position, true);
							groupServiceAdapter.notifyDataSetChanged();
						}
					}
					else {
						groupServiceAdapter.setSelectedPosition(position, true);
						groupServiceAdapter.notifyDataSetChanged();
					}

				}

				groupRequestServices.addAll(IntStream.range(0, groupServiceAdapter.getData().size())
						.filter(i -> groupServiceAdapter.getSelectedPosition().get(i) != null && groupServiceAdapter.getSelectedPosition().get(i))
						.mapToObj(i -> groupServiceAdapter.getData().get(i).getId())
						.collect(Collectors.toList()));
				request.setGroupServices(new ArrayList<String>(groupRequestServices));
				groupRequestServices.clear();
				calculatePriceAndTime((ArrayList) serviceAdapter.getData(),(ArrayList) groupServiceAdapter.getData());
			}
		});

		if(serviceItems.size() == 0){
			gridServices.setVisibility(View.GONE);
			noService.setVisibility(View.VISIBLE);
		}
		else{
			gridServices.setVisibility(View.VISIBLE);
			noService.setVisibility(View.GONE);
		}
		if(groupServiceItems.size() == 0){
			gridGroupServices.setVisibility(View.GONE);
			noServiceGroup.setVisibility(View.VISIBLE);
		}
		else{
			gridGroupServices.setVisibility(View.VISIBLE);
			noServiceGroup.setVisibility(View.GONE);
		}
	}



	private void createBoxGrid(Washer washer){
		//Boxes
		ArrayList<Box> boxItems = new ArrayList<Box>();
		if(washer != null && washer.getBoxSettings() != null && washer.getBoxSettings().size() > 0)
			washer.getBoxSettings().forEach(item -> {
//			 if(item.getBookingType().getValue().equals(Constants.OFFLINE)) {
				boxItems.add(item);
//			 }
			});

		//Box adapter
		boxAdapter = new OfflineBoxesGridAdapter(getActivity(),boxItems);
		gridBoxes.setAdapter(boxAdapter);
		gridBoxes.setExpanded(true);
		gridBoxes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				closeKeyboard();
				IntStream.range(0, boxAdapter.getData().size())
						.filter(i -> boxAdapter.getSelectedPosition().get(i) != null && boxAdapter.getSelectedPosition().get(i))
						.forEach(i -> boxAdapter.setSelectedPosition(i, false));
				boxAdapter.notifyDataSetChanged();

				boxAdapter.setSelectedPosition(position, true);
				boxAdapter.notifyDataSetChanged();
				boxId = ((Box)boxAdapter.getItem(position)).getUid();
			}
		});

		if(boxItems.size() == 0){
			gridBoxes.setVisibility(View.GONE);
			noOfflineBox.setVisibility(View.VISIBLE);
		}
		else{
			gridBoxes.setVisibility(View.VISIBLE);
			noOfflineBox.setVisibility(View.GONE);
		}
	}

	private void resetPriceAndTime(){
		txtPrice.setText("0 ₸");
		txtTime.setText(BA.str(R.string.zero_min));
		request.setServices(new ArrayList<Integer>());
		request.setGroupServices(new ArrayList<String>());
	}

	public void calculatePriceAndTime(ArrayList<ServiceItem> data, ArrayList<Washer.GroupMenu> groupData){
		txtPrice.setText("0 ₸");
		txtTime.setText(BA.str(R.string.zero_min));
		Double totalPrice = 0d;
		Integer totalTime = 0;
		for(int i = 0; i < data.size(); i++){
			for(Integer service : request.getServices()){
				if(data.get(i).getServiceId() == service) {
					totalPrice += (data.get(i).getPrice() != null ? data.get(i).getPrice() : 0d);
					totalTime += (data.get(i).getTime() != null ? data.get(i).getTime() : 0);
				}
			}
		}

		if(groupData != null && groupData.size() > 0) {
			for (int i = 0; i < groupData.size(); i++) {
				if (request.getGroupServices() != null && request.getGroupServices().size() > 0) {
					for (String groupId : request.getGroupServices()) {
						if (groupData.get(i).getId().equals(groupId)) {
							for (ReversePrices p : groupData.get(i).getPrices()) {
								if (p.getCarType() == selectedCarType.getValue()) {
									totalPrice += (p.getPrice() != null ? p.getPrice() : 0d);
									totalTime += (p.getTime() != null ? p.getTime() : 0);
								}
							}
						}
					}
				}
			}
		}

		if(totalPrice > 0) {
			int discnt = 0;
			if(!TextUtils.isEmpty(discount.getText().toString())) discnt = Integer.parseInt(discount.getText().toString());
			int totalPriceWithDiscount = (int)(Math.round(totalPrice)-Math.round(totalPrice)*discnt/100);
			txtPrice.setText(totalPriceWithDiscount + " ₸");
			if(request != null)
				request.setServicePrice(totalPrice+"");
		}

		if(totalTime > 0){
			txtTime.setText(Math.round(totalTime)+BA.str(R.string.space_min));
			if(request != null)
				request.setServiceTotalTime(totalTime);

		}
	}

	@OnFocusChange(R.id.carNo)
	public void onFocusChangeCarNo(boolean focus){
		if(focus){
			carNo.setError(null);
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

	@OnClick(R.id.btnQueue)
	public void createQueueRequest() {
		carNo.setError(null);
		closeKeyboard();
		String сarNoStr = carNo.getText().toString();
		if (TextUtils.isEmpty(сarNoStr)){
			ToastUtil.displayAtTop(getActivity(), BA.str(R.string.enter_car_plate));
			if(carNo.hasFocus())
				carNo.setError(null);
			else
				carNo.setError("");
			titleView.requestFocus();
			return;
		}

		String phoneStr = mobile.getText().toString();
		phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");

		request.setCarNo(сarNoStr);
		request.setMobile(TextUtils.isEmpty(phoneStr)?"":"+7"+phoneStr);
		request.setClientKey(сarNoStr);
		request.setTime(Instant.now());
		request.setCarwash(URL_PREFIX+washer.getId());
		request.setOffline(true);
		request.setQueued(true);
		request.setDirectWash(false);
		request.setCarMark(carMark.getText().toString());
		request.setPaid(paid.isChecked());
		request.setComment(comment.getText().toString());
		request.setDiscount(Double.parseDouble(discount.getText().toString()));

		BA.getEventBus().post(new AdminBookRequestEvent(request));
		setWaitScreen(true);
	}


	@OnClick(R.id.btnBook)
	public void createBookingRequest(){
		carNo.setError(null);
		closeKeyboard();
		String сarNoStr = carNo.getText().toString();
		if (TextUtils.isEmpty(сarNoStr)){
			ToastUtil.displayAtTop(getActivity(), BA.str(R.string.enter_car_plate));
			carNo.setError("");
			carNo.requestFocus();
			return;
		}

		String phoneStr = mobile.getText().toString();
		phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");

		if(selectedCarType == null) {
			ToastUtil.displayAtTop(getActivity(), BA.str(R.string.select_car));
			return;
		}

		if(!(request.getServices() != null && request.getServices().size() > 0) && !(request.getGroupServices() != null && request.getGroupServices().size() > 0)){
			ToastUtil.displayAtTop(getActivity(), BA.str(R.string.select_services));
			return;
		}

		if(TextUtils.isEmpty(boxId)){
			ToastUtil.displayAtTop(getActivity(), BA.str(R.string.select_box));
			return;
		}

		request.setCarNo(сarNoStr);
		request.setMobile(TextUtils.isEmpty(phoneStr)?"":"+7"+phoneStr);
		request.setClientKey(сarNoStr);
		request.setCarType(selectedCarType.getValue());
		request.setBoxId(boxId);
		request.setTime(Instant.now());
		request.setCarwash(URL_PREFIX+washer.getId());
		request.setOffline(true);
		request.setQueued(true);
		request.setDirectWash(true);
		request.setCarMark(carMark.getText().toString());
		request.setPaid(paid.isChecked());
		request.setComment(comment.getText().toString());
		try {
			request.setDiscount(Double.parseDouble(discount.getText().toString()));
		}
		catch(NumberFormatException e){
			request.setDiscount(0d);
		}

		BA.getEventBus().post(new AdminBookRequestEvent(request));
		setWaitScreen(true);
	}

	@Subscribe
	public void onAdminBookResponseReceived(AdminBookResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
//				ToastUtil.display(getActivity(), "Успешно забронировали");
				getActivity().finish();
				overridePendingTransitionWithCommonCloseTransition();
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_add_queue));
		}
	}

	@OnClick(R.id.serviceBtn)
	public void onServiceButtonClicked(){
		serviceLayout.setVisibility(View.VISIBLE);
		groupServiceLayout.setVisibility(View.GONE);
		serviceBtn.setSelected(true);
		groupBtn.setSelected(false);
	}
	@OnClick(R.id.groupBtn)
	public void onGroupServiceButtonClicked(){
		serviceLayout.setVisibility(View.GONE);
		groupServiceLayout.setVisibility(View.VISIBLE);
		groupBtn.setSelected(true);
		serviceBtn.setSelected(false);
	}


	@OnClick(R.id.more)
	public void moreFieldsClicked(){

		if(detailLayout.getVisibility() == View.GONE) {
			detailLayout.setVisibility(View.VISIBLE);
			moreButton.setText(BA.str(R.string.additional));
			moreButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_white_24dp, 0);
			buttonBook.setEnabled(true);
		}
		else{
			detailLayout.setVisibility(View.GONE);
			moreButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
			moreButton.setText(BA.str(R.string.additional));
			buttonBook.setEnabled(false);
		}
	}

	@OnClick(R.id.extra)
	public void extraFieldsClicked(){
		if(extraLayout.getVisibility() == View.GONE) {
			extraLayout.setVisibility(View.VISIBLE);
			extraButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_white_24dp, 0);
		}
		else{
			extraLayout.setVisibility(View.GONE);
			extraButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
		}
	}

	private void makeCarTypeButtons(List<Integer> cartypes){
		buttonMini.setVisibility(View.GONE);
		buttonSedan.setVisibility(View.GONE);
		buttonUniversal.setVisibility(View.GONE);
		buttonCrossover.setVisibility(View.GONE);
		buttonMiniJeep.setVisibility(View.GONE);
		buttonBigJeep.setVisibility(View.GONE);
		buttonMiniVan.setVisibility(View.GONE);
		buttonMinbus.setVisibility(View.GONE);
		buttonMoto.setVisibility(View.GONE);

		if(cartypes.contains(CarType.Mini.getValue())){
			buttonMini.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Sedan.getValue())){
			buttonSedan.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Universal.getValue())){
			buttonUniversal.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Crossover.getValue())){
			buttonCrossover.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.MiniJeep.getValue())){
			buttonMiniJeep.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Jeep.getValue())){
			buttonBigJeep.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Minivan.getValue())){
			buttonMiniVan.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Minibus.getValue())){
			buttonMinbus.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Moto.getValue())){
			buttonMoto.setVisibility(View.VISIBLE);
		}
	}

	private View.OnClickListener buttonSelector = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if ( view.getId() == buttonSedan.getId() && !buttonSedan.isSelected()){
				buttonSedan.setSelected(true);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Sedan;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonMini.getId()  && !buttonMini.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(true);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Mini;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonUniversal.getId()  && !buttonUniversal.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(true);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Universal;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonMiniJeep.getId()  && !buttonMiniJeep.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(true);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.MiniJeep;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonMiniVan.getId()  && !buttonMiniVan.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(true);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Minivan;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonCrossover.getId()  && !buttonCrossover.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(true);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Crossover;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonBigJeep.getId()  && !buttonBigJeep.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(true);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Jeep;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonMoto.getId()  && !buttonMoto.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(true);
				selectedCarType = CarType.Moto;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonMinbus.getId()  && !buttonMinbus.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(true);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Minibus;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
		}
	};

	@OnClick(R.id.paid_text)
	public void onPaidTextClicked(){
		paid.setChecked(!paid.isChecked());
	}
}
