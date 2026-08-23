package com.driverspa.adapter;
import com.driverspa.BA;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.Campaign;
import com.driverspa.util.Functions;

public class AdminCampaignAdapter extends BaseDataAdapter<Campaign> {

	public AdminCampaignAdapter(Context context) {
		super(context);
	}
		
	@Override
	public View getView(int position, View view, ViewGroup parent) {
     ViewHolder holder = null;	
	 if(view == null){	
		view = LayoutInflater.from(context).inflate(R.layout.list_admin_campaign_item, parent, false);
		holder = new ViewHolder(view);
		view.setTag(holder);
	 }
	 else{
		 holder = (ViewHolder) view.getTag();
	 }

	  Campaign item = getItem(position);
	  holder.txtCompaign.setText(item.getDescription());
	  HashMap<String,String> timeMap = Functions.formatUTCDate(item.getEndTime());
	  HashMap<String,String> tsMap = Functions.formatUTCDate(item.getTs());
	  holder.txtEndTime.setText(BA.str(R.string.ending_pre)+timeMap.get(Functions.DATE)+BA.str(R.string.space_at_space)+timeMap.get(Functions.TIME));
	  return view;
	}
	
	static class ViewHolder {

		@BindView(R.id.txtCompaign)
		TextView txtCompaign;
		@BindView(R.id.txtEndTime)
		TextView txtEndTime;

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}


}
