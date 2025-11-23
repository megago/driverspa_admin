package com.driverspa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import com.driverspa.R;
import com.driverspa.model.ServiceMenuModel;
import com.driverspa.model.Washer.Prices;

public class PricesExpandableListAdapter extends BaseExpandableListAdapter {
 
    private Context _context;
    private List<ServiceMenuModel> carItems;
    private HashMap<Integer,String> cars;
    private HashMap<Integer,String> services;
    
    public PricesExpandableListAdapter(Context context, List<ServiceMenuModel> carItems, HashMap<Integer,String> cars, HashMap<Integer,String> services) {
        this._context = context;        
        this.carItems = carItems;
        this.cars = cars;
        this.services = services;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
    	return carItems.get(groupPosition).getPrices().get(childPosititon);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_cars_item, null);
        }
          
        TextView childTextTitle = (TextView) convertView.findViewById(R.id.text_service);
        TextView childTextPrice = (TextView) convertView.findViewById(R.id.text_price);
                           
        Prices item = carItems.get(groupPosition).getPrices().get(childPosition);
        childTextTitle.setText(services.get(item.getType()));
        childTextPrice.setText(Math.round(item.getPrice())+" ₸");
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
    	return this.carItems.get(groupPosition).getPrices().size();        
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this.carItems.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this.carItems.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_cars_group, null);
        }
        
        ExpandableListView eLV = (ExpandableListView) parent;
        eLV.expandGroup(groupPosition);
                
        TextView parentTextTitle = (TextView) convertView.findViewById(R.id.text_car_type);
        parentTextTitle.setText(cars.get(carItems.get(groupPosition).getCarType()));
         
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