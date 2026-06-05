package com.driverspa.adapter;

import static com.driverspa.util.Constants.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.driverspa.R;
import com.driverspa.model.Box;

/**
 * Created by Yerzhan Tanatov on 22/12/15.
 */
public class TabBarAdapter extends BaseAdapter {

    Context context;
    protected List<Box> tabBars;
    int selectedTab = 0;

    public TabBarAdapter(Context context, List<Box> tabBars){
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
        View rowView = inflater.inflate(R.layout.tabbar_item, parent,false);
        TextView tabItemTxt = (TextView)rowView.findViewById(R.id.txtTabBarItem);
        TextView tabItemTxtDetail = (TextView)rowView.findViewById(R.id.txtTabBarItemDetail);

        tabItemTxt.setText(tabBars.get(position).getBoxName());
        tabItemTxtDetail.setVisibility(View.VISIBLE);
        if(tabBars.get(position).getBookingType().getValue().equals("online"))
          tabItemTxtDetail.setText("Онлайн/Живая оч.");
        else{
            tabItemTxtDetail.setText("Живая оч.");
        }

        if(position == selectedTab){
            rowView.setBackgroundResource(R.drawable.background_tab_selected);
            tabItemTxt.setTextColor(context.getResources().getColor(R.color.White));
            tabItemTxtDetail.setTextColor(context.getResources().getColor(R.color.White));
        }
        else{
            rowView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            tabItemTxt.setTextColor(context.getResources().getColor(R.color.tab_inactive));
            tabItemTxtDetail.setTextColor(context.getResources().getColor(R.color.tab_inactive));
        }
        return rowView;
    }

    public void setSelectedTab(int selectedTab){
        this.selectedTab = selectedTab;
    }
}
