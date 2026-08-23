package com.driverspa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.WashCountCampaign;
import com.driverspa.util.otto.DeleteWashCountCampaign;

public class WashCountCounterAdapter extends BaseDataAdapter<WashCountCampaign> {

	public WashCountCounterAdapter(Context context) {
		super(context);

	}
		
	@Override
	public View getView(int position, View view, ViewGroup parent) {
     ViewHolder holder = null;	
	 if(view == null){	
		view = LayoutInflater.from(context).inflate(R.layout.list_wash_count_counter_item, parent, false);
		holder = new ViewHolder(view);
		view.setTag(holder);
	 }
	 else{
		 holder = (ViewHolder) view.getTag();
	  }

	  final WashCountCampaign item = getItem(position);
	  holder.description.setText(item.getDescription());
	  holder.washCount.setText(BA.str(R.string.wash_count_pre)+item.getWashCount());
	  holder.options.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BA.getEventBus().post(new DeleteWashCountCampaign(item));
			}
		});
	  return view;
	}
	
	static class ViewHolder {
		@BindView(R.id.description)
		TextView description;
		@BindView(R.id.washCount)
		TextView washCount;
		@BindView(R.id.options)
		View options;

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
