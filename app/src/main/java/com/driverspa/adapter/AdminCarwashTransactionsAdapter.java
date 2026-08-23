package com.driverspa.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.CarwashTransaction;
import com.driverspa.util.Functions;

public class AdminCarwashTransactionsAdapter extends BaseDataAdapter<CarwashTransaction> {

	DecimalFormat formatter = new DecimalFormat("#,###.##");

	LinkedHashMap<Integer,String> operationTypes;
	public AdminCarwashTransactionsAdapter(Context context) {
		super(context);
		operationTypes = BA.getReference().getOperationTypes();
	}
		
	@Override
	public View getView(int position, View view, ViewGroup parent) {
     ViewHolder holder = null;	
		 if(view == null){
			view = LayoutInflater.from(context).inflate(R.layout.list_admin_carwash_transaction_item, parent, false);
			holder = new ViewHolder(view);
			view.setTag(holder);
		 }
		 else{
			 holder = (ViewHolder) view.getTag();
		 }

		CarwashTransaction item = getItem(position);
		String userName = "";
		if(item.getUser() != null){
			if(!TextUtils.isEmpty(item.getUser().getFirstName()))
				userName = item.getUser().getFirstName();
			else
				userName = item.getUser().getPhone();
		}

		holder.operationType.setText(operationTypes.get(item.getOperationType()));
		holder.description.setText(item.getDescription());
		holder.user.setText("("+userName+")");
		holder.amount.setText(formatter.format(item.getAmount())+" ₸");
		if(item.getTranDate()!=null){
			HashMap<String,String> startTime = Functions.formatUTCDate(item.getTranDate());
			holder.tranDate.setText(startTime.get(Functions.DATE)+BA.str(R.string.space_at_space)+startTime.get(Functions.TIME));
		}
		else{
			holder.tranDate.setText("");
		}


		return view;
	}
	
	static class ViewHolder {
		 		 						
		@BindView(R.id.operationType)
		TextView operationType;
		@BindView(R.id.description)
		TextView description;
		@BindView(R.id.user)
		TextView user;
		@BindView(R.id.amount)
		TextView amount;
		@BindView(R.id.tranDate)
		TextView tranDate;

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}


}
