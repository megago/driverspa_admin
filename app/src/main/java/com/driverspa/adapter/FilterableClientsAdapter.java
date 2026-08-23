package com.driverspa.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.otto.SearchEvent;
import com.driverspa.util.otto.ws.AdminClientsResponseEvent;


public class FilterableClientsAdapter extends BaseAdapter implements Filterable {
	DecimalFormat formatter = new DecimalFormat("#,###.##");
	Context context;
	Washer washer;
	private List<ClientInfo> mData;
	Filter filter;
	String searchedPossibleCarNo;

	EditText searchField;
	EditText carNo;

	public FilterableClientsAdapter(Context context, List<ClientInfo> data, Washer washer) {
		this.context = context;
		setData(data);
		this.washer = washer;
	}

	public FilterableClientsAdapter(Context context, List<ClientInfo> data, Washer washer, String searchedPossibleCarNo) {
		this.context = context;
		setData(data);
		this.washer = washer;
		this.searchedPossibleCarNo = searchedPossibleCarNo;
	}

	public EditText getCarNo() {
		return carNo;
	}

	public void setCarNo(EditText carNo) {
		this.carNo = carNo;
	}

	public EditText getSearchField() {
		return searchField;
	}

	public void setSearchField(EditText searchField) {
		this.searchField = searchField;
	}

	public void setSearchedPossibleCarNo(String searchedPossibleCarNo){
		this.searchedPossibleCarNo = searchedPossibleCarNo;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public void setData(List<ClientInfo> data){
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
		ClientInfoViewHolder holder = null;
		LayoutInflater inflater = LayoutInflater.from(context);
		convertView = inflater.inflate(R.layout.list_admin_filterable_clients_item, parent, false);
		holder = new ClientInfoViewHolder(convertView);
		final ClientInfo item = mData.get(position);

		holder.clientName.setText("");
		if(!TextUtils.isEmpty(item.getName()))
			holder.clientName.setText(item.getName());
		if(!TextUtils.isEmpty(item.getPhone())) {
//			holder.mobile.setVisibility(View.VISIBLE);
			holder.mobile.setText(Functions.formatPhoneNumber("+7 ### ### ## ##",item.getPhone()));
		}
		else {
//			holder.mobile.setVisibility(View.GONE);
			holder.mobile.setText(BA.str(R.string.phone_not_specified));
		}

		String carNo = item.getCarNo(this.searchedPossibleCarNo);

		if(!TextUtils.isEmpty(item.getName()) && !TextUtils.isEmpty(carNo) && carNo.contains(item.getName())){
			carNo = carNo.replaceAll(item.getName(),"").replaceAll("\\(","").replaceAll("\\)","");
		}
		holder.finishedBooksCount.setText("");
		if(!TextUtils.isEmpty(carNo))
			holder.clientName.append(" "+carNo.replaceAll("\\(","").replaceAll("\\)",""));

		if(item.getDepositAmount()!=null)
			holder.finishedBooksCount.setText(BA.str(R.string.deposit_pre)+formatter.format(item.getDepositAmount())+" ₸");
		if(item.getAccruedBonuses()!=null){
			holder.finishedBooksCount.append(BA.str(R.string.bonuses_accrued_pre)+formatter.format(item.getAccruedBonuses())+" ₸");
		}

		if(item.getFinishedBooksCount()!=null)
		  holder.finishedBooksCount.setText(BA.str(R.string.washed_pre)+item.getFinishedBooksCount()+BA.str(R.string.times_dot));
		if(item.getLastFinishedBookDate()!=null){
			HashMap<String,String> timeMap = Functions.formatUTCDate(item.getLastFinishedBookDate());
			holder.finishedBooksCount.append(BA.str(R.string.last_wash_pre)+timeMap.get(Functions.DATE));
		}

		try {
			if (item.getCarType() != null) {
				holder.carType.setText(BA.getReference().getCarType().get(item.getCarType(this.searchedPossibleCarNo)));
			}
		}
		catch(Exception e){}

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
					if (constraint != null && constraint.length() > 2) {
						filter = null;
						BA.getEventBus().post(new SearchEvent(constraint.toString()));
						filterResults.values = null;
						filterResults.count = 0;
					}
					else{
						BA.getEventBus().post(new AdminClientsResponseEvent(null));
					}

					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					if (results != null && results.count > 0) {
					}
				}
			};
			return newFilter;
		}
		return filter;
	}


	public void refreshData(List<ClientInfo> data){
		this.mData = data;
		notifyDataSetChanged();
	}

	public class ClientInfoViewHolder {

		ClientInfo bookInfo;
		@BindView(R.id.parentView)
		View parentView;
		@BindView(R.id.txtClientName)
		TextView clientName;
		@BindView(R.id.txtFinishedBooksCount)
		TextView finishedBooksCount;
		@BindView(R.id.mobile)
		TextView mobile;
		@BindView(R.id.carType)
		TextView carType;

		public ClientInfoViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}

	public void setWasher(Washer washer){
		this.washer = washer;
	}
}



