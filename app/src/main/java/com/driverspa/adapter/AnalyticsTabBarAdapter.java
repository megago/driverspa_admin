package com.driverspa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.driverspa.R;
import com.driverspa.model.Statistic;
import com.driverspa.util.StatisticLabels;

/**
 * Created by Yerzhan Tanatov on 22/12/15.
 */
public class AnalyticsTabBarAdapter extends BaseAdapter {

    Context context;
    protected List<Statistic> tabBars;
    int selectedTab = 0;

    public AnalyticsTabBarAdapter(Context context, List<Statistic> tabBars){
        this.context = context;
        this.tabBars = tabBars;
    }

    @Override
    public int getCount() {
        return tabBars.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.tabbar_analytics_item, parent,false);
        TextView tabItemTxt = (TextView)rowView.findViewById(R.id.txtTabBarItem);

        tabItemTxt.setText(StatisticLabels.localize(tabBars.get(position).getTitle()));

        if(position == selectedTab){
            tabItemTxt.setTextColor(context.getResources().getColor(R.color.White));
        }
        else{
            tabItemTxt.setTextColor(context.getResources().getColor(R.color.tab_inactive));
        }
        return rowView;
    }

    public void setSelectedTab(int selectedTab){
        this.selectedTab = selectedTab;
    }
}
