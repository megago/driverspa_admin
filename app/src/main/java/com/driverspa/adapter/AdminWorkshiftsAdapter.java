package com.driverspa.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.Workshift;
import com.driverspa.util.Functions;

public class AdminWorkshiftsAdapter extends BaseDataAdapter<Workshift> {

	DecimalFormat formatter = new DecimalFormat("###.##");

	public AdminWorkshiftsAdapter(Context context) {
		super(context);
	}
		
	@Override
	public View getView(int position, View view, ViewGroup parent) {
     ViewHolder holder = null;	
		 if(view == null){
			view = LayoutInflater.from(context).inflate(R.layout.list_admin_workshifts_item, parent, false);
			holder = new ViewHolder(view);
			view.setTag(holder);
		 }
		 else{
			 holder = (ViewHolder) view.getTag();
		 }

		Workshift item = getItem(position);

		String openedName = "";
		if(item.getOpenedUser() != null){
			if(!TextUtils.isEmpty(item.getOpenedUser().getFirstName()))
				openedName = item.getOpenedUser().getFirstName();
			else
				openedName = item.getOpenedUser().getPhone();
		}
		if(item.getStartTime()!=null){
			HashMap<String,String> startTime = Functions.formatUTCDate(item.getStartTime());
			holder.openedDate.setText(startTime.get(Functions.DATE)+" в "+startTime.get(Functions.TIME)+ (TextUtils.isEmpty(openedName)?"":(" ("+openedName+")")));
		}

		String closedName = "";
		if(item.getClosedUser() != null){
			if(!TextUtils.isEmpty(item.getClosedUser().getFirstName()))
				closedName = item.getClosedUser().getFirstName();
			else
				closedName = item.getClosedUser().getPhone();
		}

		if(item.getEndTime()!=null){
			HashMap<String,String> endTime = Functions.formatUTCDate(item.getEndTime());
			holder.closedDate.setText(endTime.get(Functions.DATE)+" в "+endTime.get(Functions.TIME)+ (TextUtils.isEmpty(closedName)?"":(" ("+closedName+")")));
			holder.closeView.setVisibility(View.VISIBLE);
			holder.status.setText("Закрыт");
			holder.arrowRight.setVisibility(View.VISIBLE);
		}
		else{
			holder.arrowRight.setVisibility(View.GONE);
			holder.status.setText("Открыт - нажмите чтобы закрыть");
			holder.closeView.setVisibility(View.GONE);
		}

//		holder.staffName.setText(item.getFirstName()+" "+(TextUtils.isEmpty(item.getLastName())?"":item.getLastName()));
//		holder.staffRole.setText(Constants.staffRole.get(item.getRole()));
//		holder.staffStatus.setText(Constants.staffStatus.get(item.getStatus()));
//		holder.amount.setText("");
//		if(item.getAmount() > 0)
//			holder.amount.setText(formatter.format(item.getAmount())+" ₸");

	  return view;
	}
	
	static class ViewHolder {
		 		 						
		@BindView(R.id.openedDate)
		TextView openedDate;
		@BindView(R.id.closedDate)
		TextView closedDate;
		@BindView(R.id.status)
		TextView status;
		@BindView(R.id.txtLayout)
		View openView;
		@BindView(R.id.txtLayout2)
		View closeView;
		@BindView(R.id.arrowRight)
		View arrowRight;

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}


}
