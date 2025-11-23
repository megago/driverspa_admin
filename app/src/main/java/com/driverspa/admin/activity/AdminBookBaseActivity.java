package com.driverspa.admin.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.StaffGridAdapter;
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.BookInfo;
import com.driverspa.model.Box;
import com.driverspa.model.CarType;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.ServiceItem;
import com.driverspa.model.Staff;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.ocr.OcrCaptureActivity;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.GroupServiceUnselectClicked;
import com.driverspa.util.otto.ServiceUnselectClicked;
import com.driverspa.util.otto.ws.AdminClientResponseEvent;
import com.driverspa.view.ExpandableGridView;
import com.driverspa.view.NDSpinner;

public class AdminBookBaseActivity extends AdminBaseActivity {

	Washer washer;
	String boxId;

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

	@BindView(R.id.phone)
	EditText mobile;
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

	int selectedItem = 0;
	@BindView(R.id.discount)
	EditText discount;
	@BindView(R.id.discountAmount)
	EditText discountAmount;
	@BindView(R.id.discountSpinner)
	NDSpinner spinner;
	@BindView(R.id.grid_group_services)
	ExpandableGridView gridGroupServices;
	@BindView(R.id.grid_services)
	ExpandableGridView gridServices;
	@BindView(R.id.grid_boxes)
	ExpandableGridView gridBoxes;
	MaskedWatcher maskedWatcher;
	@BindView(R.id.clients)
	TextView clients;

	@BindView(R.id.no_staffs)
	View noStaffs;
	@BindView(R.id.grid_staffs)
	ExpandableGridView gridStaffs;
	@BindView(R.id.cashAmount)
	EditText cashAmount;
	@BindView(R.id.cardAmount)
	EditText cardAmount;
	@BindView(R.id.bonusAmount)
	EditText bonusAmount;
	@BindView(R.id.depositAmount)
	EditText depositAmount;
	@BindView(R.id.clientBonusAmount)
	TextView clientBonusAmount;
	@BindView(R.id.clientDepositAmount)
	TextView clientDepositAmount;
	@BindView(R.id.txtPriceDifference)
	TextView txtPriceDifference;

	StaffGridAdapter staffGridAdapter;
	BookingRequest request;

	protected ServicesGridAdapter serviceAdapter;
	protected GroupServicesGridAdapter groupServiceAdapter;
	protected OfflineBoxesGridAdapter boxAdapter;
	TextWatcher discountAmountWatcher;

	HashMap<Integer, Double> menuServicesPrices = new HashMap<Integer, Double>();
	int RC_OCR_CAPTURE = 9999;

	AdminSearchFilter filter = new AdminSearchFilter();
	String textPrediction;

	ClientInfo selectedClient;
	Double finalPrice;
	Double finalPriceDifference;
	List<BookInfo.PriceDetail> priceDetails = new ArrayList<>();
	boolean isSpinnerTouched = false;

	protected void selectDiscountType(){
		if(selectedItem == 0){
			discount.setText("0");
			discountAmount.setText("0");
			discount.setVisibility(View.VISIBLE);
			discountAmount.setVisibility(View.GONE);
		}
		else{
			discount.setText("0");
			discountAmount.setText("0");
			discount.setVisibility(View.GONE);
			discountAmount.setVisibility(View.VISIBLE);
		}
		if (serviceAdapter != null && serviceAdapter.getData() != null && groupServiceAdapter != null && groupServiceAdapter.getData() != null)
			calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
	}

	protected void createActionBarSpinner(){
		if(spinner != null) {
			String[] valuesArray = new String[]{"%     ", "₸     "};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valuesArray);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if (!isSpinnerTouched) return;
					selectedItem = pos;
					selectDiscountType();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});
			spinner.setAdapter(adapter);
			spinner.setSelection(selectedItem);
		}
	}

	protected void createService(Integer carType) {
		resetPriceAndTime();
		//Services
		ArrayList<ServiceItem> serviceItems = new ArrayList<ServiceItem>();
		final LinkedHashMap<Integer, String> dictionary = BA.getReference().getServices();
		LinkedHashMap<Integer, ArrayList<Washer.Prices>> washerMenu = washer.getMenu();
		if (washerMenu != null && washerMenu.size() > 0) {
			serviceItems.clear();
			menuServicesPrices.clear();
			for (Map.Entry<Integer, ArrayList<Washer.Prices>> entry : washerMenu.entrySet()) {
				Integer key = entry.getKey();
				if (key == carType) {
					ArrayList<Washer.Prices> pricesMenu = entry.getValue();
					for (Washer.Prices priceMenu : pricesMenu) {
						ServiceItem newItem = new ServiceItem();
						newItem.setServiceId(priceMenu.getType());
						newItem.setServiceName(dictionary.get(priceMenu.getType()));
						newItem.setPrice(priceMenu.getPrice());
						newItem.setTime(priceMenu.getTime());
						serviceItems.add(newItem);
					}
					break;
				}
			}
		}

		//Group services
		List<Washer.GroupMenu> groupServiceItems = new ArrayList<Washer.GroupMenu>();
		List<Washer.GroupMenu> washerGroup = washer.getGroupMenu();
		if (washerGroup != null && washerGroup.size() > 0) {
			groupServiceItems.clear();
			for (Washer.GroupMenu g : washerGroup) {
				for (ReversePrices p : g.getPrices()) {
					if (p.getCarType() == carType) {
						groupServiceItems.add(g);
					}
				}
			}
		}

		//Service adapter
		serviceAdapter = new ServicesGridAdapter(this, serviceItems);
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
					if (serviceAdapter.getData().get(position).getPrice() > 0) {
						if (request != null && request.getGroupServices() != null && request.getGroupServices().size() > 0) {
							serviceAdapter.setSelectedPosition(position, true);
							serviceAdapter.notifyDataSetChanged();

						} else {
							serviceAdapter.setSelectedPosition(position, true);
							serviceAdapter.notifyDataSetChanged();
						}
					} else {
						ToastUtil.display(BA.getContext(), "Для данного типа авто и услуги - не указана цена");
					}
				}

				for (int i = 0; i < serviceAdapter.getData().size(); i++) {
					if (serviceAdapter.getSelectedPosition().get(i) != null && serviceAdapter.getSelectedPosition().get(i)) {
						requestServices.add(serviceAdapter.getData().get(i).getServiceId());
					}
				}
				request.setServices(new ArrayList<Integer>(requestServices));
				requestServices.clear();
				calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
			}
		});

		//Group Service adapter
		groupServiceAdapter = new GroupServicesGridAdapter(this, groupServiceItems);
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
				} else {
					Washer.GroupMenu selectedGroup = groupServiceAdapter.getData().get(position);
					if (selectedGroup != null && selectedGroup.getServices() != null && request != null && request.getServices() != null && request.getServices().size() > 0) {

						groupServiceAdapter.setSelectedPosition(position, true);
						groupServiceAdapter.notifyDataSetChanged();
					} else {
						groupServiceAdapter.setSelectedPosition(position, true);
						groupServiceAdapter.notifyDataSetChanged();
					}

				}

				for (int i = 0; i < groupServiceAdapter.getData().size(); i++) {
					if (groupServiceAdapter.getSelectedPosition().get(i) != null && groupServiceAdapter.getSelectedPosition().get(i)) {
						groupRequestServices.add(groupServiceAdapter.getData().get(i).getId());
					}
				}
				request.setGroupServices(new ArrayList<String>(groupRequestServices));
				groupRequestServices.clear();
				calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
			}
		});

		if (serviceItems.size() == 0) {
			gridServices.setVisibility(View.GONE);
		} else {
			gridServices.setVisibility(View.VISIBLE);
		}
		if (groupServiceItems.size() == 0) {
			gridGroupServices.setVisibility(View.GONE);
		} else {
			gridGroupServices.setVisibility(View.VISIBLE);
		}

		if (serviceItems.size() == 0 && groupServiceItems.size() == 0) {
			noService.setVisibility(View.VISIBLE);
		}
		else{
			noService.setVisibility(View.GONE);
		}
	}


	protected void createBoxGrid(Washer washer) {
		//Boxes
		ArrayList<Box> boxItems = new ArrayList<Box>();
		if (washer != null && washer.getBoxSettings() != null && washer.getBoxSettings().size() > 0) {
			boxItems = washer.getBoxSettings();
			boxItems = Functions.filterDisableddBoxes(boxItems);
		}

		//Box adapter
		boxAdapter = new OfflineBoxesGridAdapter(this, boxItems);
		gridBoxes.setAdapter(boxAdapter);
		gridBoxes.setExpanded(true);
		gridBoxes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				closeKeyboard();
				for (int i = 0; i < boxAdapter.getData().size(); i++) {
					if (boxAdapter.getSelectedPosition().get(i) != null && boxAdapter.getSelectedPosition().get(i)) {
						boxAdapter.setSelectedPosition(i, false);
					}
				}
				boxAdapter.notifyDataSetChanged();

				boxAdapter.setSelectedPosition(position, true);
				boxAdapter.notifyDataSetChanged();
				boxId = ((Box) boxAdapter.getItem(position)).getUid();
			}
		});

		if (boxItems.size() == 0) {
			gridBoxes.setVisibility(View.GONE);
			noOfflineBox.setVisibility(View.VISIBLE);
		} else {
			gridBoxes.setVisibility(View.VISIBLE);
			noOfflineBox.setVisibility(View.GONE);
		}
	}

	protected void createStaffGrid(Washer washer) {
		//Staffs
		ArrayList<Staff> items = new ArrayList<Staff>();
		if (washer != null && washer.getStaffs() != null && washer.getStaffs().size() > 0)
			for (Staff item : washer.getStaffs()) {
				if (Constants.WORKS.equals(item.getStatus())) {
					items.add(item);
				}
			}

		//Box adapter
		staffGridAdapter = new StaffGridAdapter(this, items);
		gridStaffs.setAdapter(staffGridAdapter);
		gridStaffs.setExpanded(true);
		gridStaffs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				closeKeyboard();
				final ArrayList<String> requestStaffs = new ArrayList<String>();

				if (staffGridAdapter.getSelectedPosition().get(position) != null && staffGridAdapter.getSelectedPosition().get(position)) {
					staffGridAdapter.setSelectedPosition(position, false);
					staffGridAdapter.notifyDataSetChanged();
				} else {
					staffGridAdapter.setSelectedPosition(position, true);
					staffGridAdapter.notifyDataSetChanged();
					staffGridAdapter.notifyDataSetChanged();
				}

				for (int i = 0; i < staffGridAdapter.getData().size(); i++) {
					if (staffGridAdapter.getSelectedPosition().get(i) != null && staffGridAdapter.getSelectedPosition().get(i)) {
						requestStaffs.add(staffGridAdapter.getData().get(i).getUid());
					}
				}
				request.setStaffs(new ArrayList<String>(requestStaffs));
				requestStaffs.clear();
			}
		});

		if (items.size() == 0) {
			gridStaffs.setVisibility(View.GONE);
			noStaffs.setVisibility(View.VISIBLE);
		} else {
			gridStaffs.setVisibility(View.VISIBLE);
			noStaffs.setVisibility(View.GONE);
		}
	}

	protected void resetPriceAndTime() {
		txtPrice.setText("0 ₸");
		txtTime.setText("0 мин");
		request.setServices(new ArrayList<Integer>());
		request.setGroupServices(new ArrayList<String>());
	}

	@Subscribe
	public void onServiceUnselectClicked(ServiceUnselectClicked event) {
		int position = event.getPosition();
		closeKeyboard();
		request.setServices(new ArrayList<Integer>());
		final ArrayList<Integer> requestServices = new ArrayList<Integer>();
		if (serviceAdapter.getSelectedPosition().get(position) != null && serviceAdapter.getSelectedPosition().get(position)) {
			serviceAdapter.unSelectSelectedPosition(position);
			serviceAdapter.notifyDataSetChanged();
		}

		for (int i = 0; i < serviceAdapter.getData().size(); i++) {
			if (serviceAdapter.getSelectedPosition().get(i) != null && serviceAdapter.getSelectedPosition().get(i)) {
				for (int j = 0; j < serviceAdapter.getSelectedPositionCount().get(i); j++)
					requestServices.add(serviceAdapter.getData().get(i).getServiceId());
			}
		}
		request.setServices(new ArrayList<Integer>(requestServices));
		requestServices.clear();
		calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
	}

	@Subscribe
	public void onGroupServiceUnselectClicked(GroupServiceUnselectClicked event) {
		int position = event.getPosition();
		closeKeyboard();
		request.setGroupServices(new ArrayList<String>());
		final ArrayList<String> groupRequestServices = new ArrayList<String>();
		if (groupServiceAdapter.getSelectedPosition().get(position) != null && groupServiceAdapter.getSelectedPosition().get(position)) {
			groupServiceAdapter.unSelectSelectedPosition(position);
			groupServiceAdapter.notifyDataSetChanged();
		}

		for (int i = 0; i < groupServiceAdapter.getData().size(); i++) {
			if (groupServiceAdapter.getSelectedPosition().get(i) != null && groupServiceAdapter.getSelectedPosition().get(i)) {
				for (int j = 0; j < groupServiceAdapter.getSelectedPositionCount().get(i); j++)
					groupRequestServices.add(groupServiceAdapter.getData().get(i).getId());
			}
		}
		request.setGroupServices(new ArrayList<String>(groupRequestServices));
		groupRequestServices.clear();
		calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
	}

	public void calculatePriceAndTime(ArrayList<ServiceItem> data, ArrayList<Washer.GroupMenu> groupData) {
		cashAmount.setText("0");
		cashAmount.setSelection(cashAmount.getText().toString().length());
		cardAmount.setText("0");
		bonusAmount.setText("0");
		depositAmount.setText("0");
		txtPrice.setText("0 ₸");
		finalPrice = 0d;
		txtTime.setText("0 мин");
		double totalPrice = 0d;
		int totalTime = 0;
		for (int i = 0; i < data.size(); i++) {
			for (Integer service : request.getServices()) {
				if (data.get(i).getServiceId() == service) {
					totalPrice += (data.get(i).getPrice() != null ? data.get(i).getPrice() : 0d);
					totalTime += (data.get(i).getTime() != null ? data.get(i).getTime() : 0);
				}
			}
		}

		if (groupData != null && groupData.size() > 0) {
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

		if (totalPrice > 0) {
			if (selectedItem == 0) { //discount type is %
				int discnt = 0;
				if (!TextUtils.isEmpty(discount.getText().toString()))
					discnt = Integer.parseInt(discount.getText().toString());
				double totalPriceWithDiscount = (int) (Math.round(totalPrice) - Math.round(totalPrice) * discnt / 100);
				txtPrice.setText(totalPriceWithDiscount + " ₸");
				finalPrice = totalPriceWithDiscount;
				cashAmount.setText(finalPrice.intValue() + "");
				cashAmount.setSelection(cashAmount.getText().toString().length());
				if (request != null)
					request.setServicePrice(totalPrice + "");
			} else {
				int discnt = 0;
				if (!TextUtils.isEmpty(discountAmount.getText().toString())) {
					try {
						discnt = Integer.parseInt(discountAmount.getText().toString());
					} catch (Exception e) {
					}
				}
				double totalPriceWithDiscount = (int) (Math.round(totalPrice) - discnt);
				if (totalPriceWithDiscount < 0) {
					discountAmount.removeTextChangedListener(discountAmountWatcher);
					discountAmount.setText((int) totalPrice + "");
					discountAmount.setSelection(discountAmount.getText().toString().length());
					discountAmount.addTextChangedListener(discountAmountWatcher);
					totalPriceWithDiscount = 0;
				}
				finalPrice = totalPriceWithDiscount;
				cashAmount.setText(finalPrice.intValue() + "");
				cashAmount.setSelection(cashAmount.getText().toString().length());
				txtPrice.setText(totalPriceWithDiscount + " ₸");
				if (request != null)
					request.setServicePrice(totalPrice + "");

			}
		}

		if (totalTime > 0) {
			txtTime.setText(Math.round(totalTime) + " мин");
			if (request != null)
				request.setServiceTotalTime(totalTime);

		}

		calculatePaymentTypesTransactionAmount();
	}




	protected void calculatePaymentTypesTransactionAmount() {
	}


	protected View.OnClickListener buttonSelector = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (view.getId() == buttonSedan.getId() && !buttonSedan.isSelected()) {
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
			} else if (view.getId() == buttonMini.getId() && !buttonMini.isSelected()) {
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
			} else if (view.getId() == buttonUniversal.getId() && !buttonUniversal.isSelected()) {
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
			} else if (view.getId() == buttonMiniJeep.getId() && !buttonMiniJeep.isSelected()) {
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
			} else if (view.getId() == buttonMiniVan.getId() && !buttonMiniVan.isSelected()) {
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
			} else if (view.getId() == buttonCrossover.getId() && !buttonCrossover.isSelected()) {
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
			} else if (view.getId() == buttonBigJeep.getId() && !buttonBigJeep.isSelected()) {
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
			} else if (view.getId() == buttonMoto.getId() && !buttonMoto.isSelected()) {
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
			} else if (view.getId() == buttonMinbus.getId() && !buttonMinbus.isSelected()) {
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


	protected void makeCarTypeButtons(List<Integer> cartypes) {
		buttonMini.setVisibility(View.GONE);
		buttonSedan.setVisibility(View.GONE);
		buttonUniversal.setVisibility(View.GONE);
		buttonCrossover.setVisibility(View.GONE);
		buttonMiniJeep.setVisibility(View.GONE);
		buttonBigJeep.setVisibility(View.GONE);
		buttonMiniVan.setVisibility(View.GONE);
		buttonMinbus.setVisibility(View.GONE);
		buttonMoto.setVisibility(View.GONE);

		if (cartypes.contains(CarType.Mini.getValue())) {
			buttonMini.setVisibility(View.VISIBLE);
		}
		if (cartypes.contains(CarType.Sedan.getValue())) {
			buttonSedan.setVisibility(View.VISIBLE);
		}
		if (cartypes.contains(CarType.Universal.getValue())) {
			buttonUniversal.setVisibility(View.VISIBLE);
		}
		if (cartypes.contains(CarType.Crossover.getValue())) {
			buttonCrossover.setVisibility(View.VISIBLE);
		}
		if (cartypes.contains(CarType.MiniJeep.getValue())) {
			buttonMiniJeep.setVisibility(View.VISIBLE);
		}
		if (cartypes.contains(CarType.Jeep.getValue())) {
			buttonBigJeep.setVisibility(View.VISIBLE);
		}
		if (cartypes.contains(CarType.Minivan.getValue())) {
			buttonMiniVan.setVisibility(View.VISIBLE);
		}
		if (cartypes.contains(CarType.Minibus.getValue())) {
			buttonMinbus.setVisibility(View.VISIBLE);
		}
		if (cartypes.contains(CarType.Moto.getValue())) {
			buttonMoto.setVisibility(View.VISIBLE);
		}
	}


	@OnClick(R.id.plateDetection)
	public void plateDetectionClicked() {
		Intent intent = new Intent(this, OcrCaptureActivity.class);
		intent.putExtra(OcrCaptureActivity.AutoFocus, true);
		intent.putExtra(OcrCaptureActivity.UseFlash, false);
		startActivityForResult(intent, RC_OCR_CAPTURE);
	}

	@Subscribe
	public void onAdminClientResponseReceived(AdminClientResponseEvent event) {
		setWaitScreen(false);
		if (event.getResult() != null) {
			if (BaseAssist.isSuccess(event.getResult())) {
				ClientInfo client = event.getResult().getClientInfo();
				if (!TextUtils.isEmpty(textPrediction) && !TextUtils.isEmpty(client.getCarNo())) {
					ToastUtil.display(this, "Клиент найден в базе");
					setData(client);
				}
			} else {
				ToastUtil.display(this, "Клиент не найден в базе");
			}
		} else {
		}
	}

	protected void setData(ClientInfo client){

	}



}

