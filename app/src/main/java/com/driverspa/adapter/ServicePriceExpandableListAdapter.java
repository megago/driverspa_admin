package com.driverspa.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

import com.driverspa.R;
import com.driverspa.admin.activity.AdminServicePriceDetailActivity;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.ServicePriceExpandable;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;

public class ServicePriceExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    List<ServicePriceExpandable> servicePriceExpandableList;
    private Washer washer;

    public ServicePriceExpandableListAdapter(Context context) {
        this._context = context;
        this.washer = washer;
    }

    public ServicePriceExpandableListAdapter(Context context, List<ServicePriceExpandable> servicePriceExpandableList) {
        this._context = context;
        this.servicePriceExpandableList = servicePriceExpandableList;
        this.washer = washer;
    }

    public void setData(List<ServicePriceExpandable> servicePriceExpandableList){
        this.servicePriceExpandableList = servicePriceExpandableList;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
    	return servicePriceExpandableList.get(groupPosition).getPricesWithCarType().get(childPosititon);
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
        View mainLayout = convertView.findViewById(R.id.mainLayout);
//        if(groupPosition%2 == 0)
//            mainLayout.setBackgroundResource(R.color.BlackTransparent);
//        else
//            mainLayout.setBackgroundColor(Color.TRANSPARENT);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(_context,AdminServicePriceDetailActivity.class);
                intent.putExtra(Constants.SERVICE,servicePriceExpandableList.get(groupPosition).getServiceId());
                _context.startActivity(intent);

            }
        });
        edit.setPaintFlags(edit.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        ReversePrices item = servicePriceExpandableList.get(groupPosition).getPricesWithCarType().get(childPosition);

        itemName.setText(" - "+item.getCarTypeName());
//        itemValue.setText("Цена: "+item.getPrice()+ " ₸ Время: "+item.getTime() +" мин.");
        itemValue.setText(item.getPrice()+ " ₸    "+item.getTime() +" мин.");

        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
    	return this.servicePriceExpandableList.get(groupPosition).getPricesWithCarType().size();
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

        ServicePriceExpandable item = servicePriceExpandableList.get(groupPosition);
        TextView parentTextTitle = (TextView) convertView.findViewById(R.id.desc);
        TextView detail = (TextView) convertView.findViewById(R.id.detail);
        View mainLayout = convertView.findViewById(R.id.mainLayout);

//        if(groupPosition%2 == 0)
//         mainLayout.setBackgroundResource(R.color.BlackTransparent);
//        else
//         mainLayout.setBackgroundColor(Color.TRANSPARENT);

        View checkBox = convertView.findViewById(R.id.checkbox);
        parentTextTitle.setText(item.getServiceName());

        if(item.getPricesWithCarType() != null && item.getPricesWithCarType().size() > 0){
            checkBox.setVisibility(View.VISIBLE);
            detail.setVisibility(View.GONE);
//            detail.setText(item.getPricesWithCarType().size() + " типа машин");
            detail.setText("Детали");
            detail.setPaintFlags(detail.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
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