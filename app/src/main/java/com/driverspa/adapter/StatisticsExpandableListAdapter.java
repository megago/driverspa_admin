package com.driverspa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.List;

import com.driverspa.R;
import com.driverspa.model.Statistic;

public class StatisticsExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    List<Statistic> statistics;

    public StatisticsExpandableListAdapter(Context context) {
        this._context = context;
    }

    public StatisticsExpandableListAdapter(Context context, List<Statistic> statistics) {
        this._context = context;
        this.statistics = statistics;
    }

    public void setData(List<Statistic> statistics){
        this.statistics = statistics;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
    	return statistics.get(groupPosition).getStatisticsList().get(childPosititon);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_statistics_item, null);
        }
          
        TextView itemName = (TextView) convertView.findViewById(R.id.item_name);
        TextView itemValue = (TextView) convertView.findViewById(R.id.item_value);

        Statistic.StatisticDetail item = statistics.get(groupPosition).getStatisticsList().get(childPosition);
        itemName.setText(item.getItemName());
        itemValue.setText(item.getItemValue());

        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
    	return this.statistics.get(groupPosition).getStatisticsList().size();
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this.statistics.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this.statistics.size();
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
            convertView = infalInflater.inflate(R.layout.list_statistics_group, null);
        }

        ExpandableListView eLV = (ExpandableListView) parent;
        eLV.expandGroup(groupPosition);

//        if(groupPosition == 0) {
//        }
//
        TextView parentTextTitle = (TextView) convertView.findViewById(R.id.item_title);
        parentTextTitle.setText(statistics.get(groupPosition).getTitle());
        parentTextTitle.setVisibility(View.GONE);
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