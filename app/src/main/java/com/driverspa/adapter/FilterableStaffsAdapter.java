package com.driverspa.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.view.DelayAutoCompleteTextView;


public class FilterableStaffsAdapter extends BaseAdapter implements Filterable {
	DecimalFormat formatter = new DecimalFormat("#,###.##");
	Context context;
	Washer washer;
	private List<Staff> mData;
	Filter filter;
	DelayAutoCompleteTextView staffComplete;

	public FilterableStaffsAdapter(Context context, List<Staff> data, Washer washer, DelayAutoCompleteTextView staffComplete) {
		this.context = context;
		setData(data);
		this.washer = washer;
		this.staffComplete = staffComplete;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public void setData(List<Staff> data){
		this.mData = data;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mData.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		StaffViewHolder holder = null;
		LayoutInflater inflater = LayoutInflater.from(context);
		convertView = inflater.inflate(R.layout.list_admin_filterable_staffs_item, parent, false);
		holder = new StaffViewHolder(convertView);
		final Staff item = mData.get(position);

		holder.staffName.setText("");
		if(!TextUtils.isEmpty(item.getFirstName()))
			holder.staffName.setText(item.getFirstName()+" "+(TextUtils.isEmpty(item.getLastName())?"":item.getLastName()));
		if(!TextUtils.isEmpty(item.getMobile())) {
			holder.mobile.setVisibility(View.VISIBLE);
			holder.mobile.setText(Functions.formatPhoneNumber("+7 ### ### ## ##",item.getMobile()));
		}
		else {
			holder.mobile.setVisibility(View.GONE);
			holder.mobile.setText("телефон не указан");
		}

		if(item.getAmount()!=null)
			holder.amount.setText(" ("+formatter.format(item.getAmount())+" ₸)");

		holder.status.setText(Constants.staffRole.get(item.getRole())+" "+Constants.staffStatus.get(item.getStatus()));
		return convertView;
	}

	@Override
	public Filter getFilter() {
//		return this.filter;
		if(this.filter == null) {
			Filter newFilter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults filterResults = new FilterResults();
					List<Staff> foundStaffs = new ArrayList<Staff>();
					staffComplete.stopLoading();
					if (!TextUtils.isEmpty(constraint.toString())) {
						for(Staff st : washer.getStaffs()){
							if(st.getFirstName().toLowerCase().contains(constraint.toString().toLowerCase())){
								foundStaffs.add(st);
							}
						}

						filter = null;
						filterResults.values = foundStaffs;
						filterResults.count = foundStaffs.size();
					}
					else {
						for (Staff st : washer.getStaffs()) {
							foundStaffs.add(st);
						}
						filter = null;
						filterResults.values = foundStaffs;
						filterResults.count = foundStaffs.size();
					}
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					if (results != null && results.count > 0) {
						mData = (ArrayList<Staff>) results.values;
						notifyDataSetChanged();
					}
				}
			};
			return newFilter;
		}
		return filter;
	}


	public void refreshData(List<Staff> data){
		this.mData = data;
		notifyDataSetChanged();
	}

	public class StaffViewHolder {

		StaffViewHolder staffViewHolder;
		@BindView(R.id.parentView)
		View parentView;
		@BindView(R.id.staffName)
		TextView staffName;
		@BindView(R.id.amount)
		TextView amount;
		@BindView(R.id.mobile)
		TextView mobile;
		@BindView(R.id.status)
		TextView status;

		public StaffViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}

	public void setWasher(Washer washer){
		this.washer = washer;
	}
}



