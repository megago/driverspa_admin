package com.driverspa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.Statistic;

public class AnalyticsListAdapter extends BaseDataAdapter<Statistic.StatisticDetail> {

    public AnalyticsListAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder = null;
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.list_statistics_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        Statistic.StatisticDetail item = getItem(position);
        holder.itemName.setText(item.getItemName());
        if(item.getItemValueString().contains(".0"))
          holder.itemValue.setText(item.getItemValueString().replace(".0",""));
        else
            holder.itemValue.setText(item.getItemValueString());
        return view;
    }

    static class ViewHolder {

        @BindView(R.id.item_name)
        TextView itemName;
        @BindView(R.id.item_value)
        TextView itemValue;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
