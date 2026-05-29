package com.driverspa.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.activity.AdminChangeGroupServicePriceActivity;
import com.driverspa.dialog.MultipleSelectDialog;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;

public class GroupServicePriceAdapter extends BaseExpandableListAdapter {

	private Context _context;
	List<Washer.GroupMenu> servicePriceExpandableList;
	final List<HashMap<String,String>> servicesMapList = new ArrayList<HashMap<String,String>>();
	final HashMap<Integer,String> allServices = BA.getReference().getServices();
	final HashMap<Integer,String> allCarTypes = BA.getReference().getCarType();

	public GroupServicePriceAdapter(Context context) {
		this._context = context;
		for (Map.Entry<Integer,String> entry : allServices.entrySet()) {
			HashMap<String,String> serviceMap = new HashMap<String,String>();
			Integer key = entry.getKey();
			String value = entry.getValue();
			serviceMap.put(MultipleSelectDialog.ID, Integer.toString(key));
			serviceMap.put(MultipleSelectDialog.NAME, value);
			servicesMapList.add(serviceMap);
		}
	}

	public GroupServicePriceAdapter(Context context, List<Washer.GroupMenu> servicePriceExpandableList) {
		this._context = context;
		this.servicePriceExpandableList = servicePriceExpandableList;

		for (Map.Entry<Integer,String> entry : allServices.entrySet()) {
			HashMap<String,String> serviceMap = new HashMap<String,String>();
			Integer key = entry.getKey();
			String value = entry.getValue();
			serviceMap.put(MultipleSelectDialog.ID, Integer.toString(key));
			serviceMap.put(MultipleSelectDialog.NAME, value);
			servicesMapList.add(serviceMap);
		}
	}

	public void setData(List<Washer.GroupMenu> servicePriceExpandableList){
		this.servicePriceExpandableList = servicePriceExpandableList;
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return servicePriceExpandableList.get(groupPosition).getPrices().get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.list_service_price_item, null);
		}

		TextView itemName = (TextView) convertView.findViewById(R.id.item_name);
		TextView itemValue = (TextView) convertView.findViewById(R.id.item_value);
		TextView edit = (TextView) convertView.findViewById(R.id.edit);

		edit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(_context,AdminChangeGroupServicePriceActivity.class);
				intent.putExtra(Constants.GROUP,servicePriceExpandableList.get(groupPosition).getId());
				_context.startActivity(intent);

			}
		});
		ReversePrices item = servicePriceExpandableList.get(groupPosition).getPrices().get(childPosition);

		itemName.setText(allCarTypes.get(item.getCarType()));
		itemValue.setText(item.getPrice()+ " ₸ · "+item.getTime() +" мин");

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.servicePriceExpandableList.get(groupPosition).getPrices().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.servicePriceExpandableList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this.servicePriceExpandableList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.list_service_price_group, null);
		}

		Washer.GroupMenu item = servicePriceExpandableList.get(groupPosition);
		TextView parentTextTitle = (TextView) convertView.findViewById(R.id.desc);
		TextView detail = (TextView) convertView.findViewById(R.id.detail);

		String serviceNames = "";
		if(item.getServices() != null) {
			for (int i = 0; i < item.getServices().size(); i++) {
				serviceNames = ((i == 0) ? "" : (serviceNames + " + ")) + ((BA.getReference().getServices().get(item.getServices().get(i))));
			}
		}

		View checkBox = convertView.findViewById(R.id.checkbox);
		parentTextTitle.setText(item.getName());

		if(item.getPrices() != null && item.getPrices().size() > 0){
			checkBox.setVisibility(View.VISIBLE);
			detail.setVisibility(View.VISIBLE);
            detail.setText(serviceNames);
//			detail.setText("Детали");
//			detail.setPaintFlags(detail.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		}
		else {
			checkBox.setVisibility(View.GONE);
			detail.setVisibility(View.GONE);
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}