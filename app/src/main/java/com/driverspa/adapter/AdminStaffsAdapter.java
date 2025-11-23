package com.driverspa.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.Staff;
import com.driverspa.util.Constants;

public class AdminStaffsAdapter extends BaseDataAdapter<Staff> implements Filterable{

	DecimalFormat formatter = new DecimalFormat("#,###.##");
	List<Staff> allStaffs;
	List<Staff> filteredStaffs;

	public AdminStaffsAdapter(Context context,List<Staff> allStaffs) {
		super(context);
		this.filteredStaffs = allStaffs;
	}

	@Override
	public ArrayList<Staff> getList() {
		return (ArrayList)filteredStaffs;
	}

	@Override
	public int getCount() {
		return filteredStaffs.size();
	}

	@Override
	public Staff getItem(int position) {
		return filteredStaffs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public View getView(int position, View view, ViewGroup parent) {
     ViewHolder holder = null;	
		 if(view == null){
			view = LayoutInflater.from(context).inflate(R.layout.list_admin_staffs_item, parent, false);
			holder = new ViewHolder(view);
			view.setTag(holder);
		 }
		 else{
			 holder = (ViewHolder) view.getTag();
		 }

		Staff item = getItem(position);
		holder.staffName.setText(item.getFirstName()+" "+(TextUtils.isEmpty(item.getLastName())?"":item.getLastName()));
		holder.staffRole.setText(Constants.staffRole.get(item.getRole()));
		holder.staffStatus.setText(Constants.staffStatus.get(item.getStatus()));
		holder.amount.setText("0 ₸");
		if(item.getAmount() > 0)
			holder.amount.setText(formatter.format(item.getAmount())+" ₸ ("+item.getSalaryPercentage()+"%)");
		holder.salaryPercentage.setText("");
//		if(item.getSalaryPercentage() != null && item.getSalaryPercentage() > 0)
//			holder.salaryPercentage.setText("Начисление "+item.getSalaryPercentage()+"% от услуги");
	  return view;
	}

	@Override
	public Filter getFilter() {

		Filter filter = new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,FilterResults results) {
				filteredStaffs = (ArrayList<Staff>) results.values; // has the filtered values
				notifyDataSetChanged();  // notifies the data with new filtered values
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
				List<Staff> FilteredArrList = new ArrayList<Staff>();

				if (allStaffs == null) {
					allStaffs = new ArrayList<Staff>(filteredStaffs); // saves the original data in mOriginalValues
				}

				if (constraint == null || constraint.length() == 0) {
					// set the Original result to return
					results.count = allStaffs.size();
					results.values = allStaffs;
				} else {
					constraint = constraint.toString().toLowerCase();
					for (int i = 0; i < allStaffs.size(); i++) {
						Staff data = allStaffs.get(i);

						if(constraint != null && ( (Constants.WORKS.equals(constraint) && Constants.WORKS.equals(data.getStatus())) || (Constants.WORKS.equals(constraint) && Constants.VACATION.equals(data.getStatus()))) ){
							FilteredArrList.add(data);
						}
						else if(constraint != null && Constants.RESIGNED.equals(constraint) && Constants.RESIGNED.equals(data.getStatus())){
							FilteredArrList.add(data);
						}
					}
					// set the Filtered result to return
					results.count = FilteredArrList.size();
					results.values = FilteredArrList;
				}
				return results;
			}
		};
		return filter;
	}

	static class ViewHolder {
		 		 						
		@BindView(R.id.staffName)
		TextView staffName;
		@BindView(R.id.staffRole)
		TextView staffRole;
		@BindView(R.id.staffStatus)
		TextView staffStatus;
		@BindView(R.id.salaryPercentage)
		TextView salaryPercentage;
		@BindView(R.id.amount)
		TextView amount;

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}


}
