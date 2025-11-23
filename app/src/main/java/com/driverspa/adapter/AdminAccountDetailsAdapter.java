package com.driverspa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.CarwashAccountDetail;

public class AdminAccountDetailsAdapter extends BaseDataAdapter<CarwashAccountDetail> {

	DecimalFormat formatter = new DecimalFormat("#,###.##");

	LinkedHashMap<Integer,String> accountNumbers;
	public AdminAccountDetailsAdapter(Context context) {
		super(context);
		accountNumbers = BA.getReference().getAccountNumbers();
	}
		
	@Override
	public View getView(int position, View view, ViewGroup parent) {
     ViewHolder holder = null;	
		 if(view == null){
			view = LayoutInflater.from(context).inflate(R.layout.list_admin_account_detail_item, parent, false);
			holder = new ViewHolder(view);
			view.setTag(holder);
		 }
		 else{
			 holder = (ViewHolder) view.getTag();
		 }

		CarwashAccountDetail item = getItem(position);
		holder.accountName.setText(accountNumbers.get(item.getAccountCode()));
		holder.current.setText(formatter.format(item.getAmount())+"");
		holder.start.setText(formatter.format(item.getOpeningAmount())+"");
		holder.end.setText(formatter.format(item.getEndingAmount())+"");

	  return view;
	}
	
	static class ViewHolder {
		 		 						
		@BindView(R.id.accountName)
		TextView accountName;
		@BindView(R.id.current)
		TextView current;
		@BindView(R.id.start)
		TextView start;
		@BindView(R.id.end)
		TextView end;

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}


}
