package com.driverspa.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.Reference;

public class CityChooseAdapter extends BaseDataAdapter<Reference.City> {

	private String selectedCityCode;


	public CityChooseAdapter(Context context) {
		super(context);		
	}	
		
	@Override
	public View getView(final int position, View view, ViewGroup parent) {		
		
		view = LayoutInflater.from(context).inflate(R.layout.list_city_item, parent, false);
		ViewHolder holder = new ViewHolder(view);			 			
		view.setTag(holder);

		Reference.City item = getItem(position);
		holder.description.setText(item.getTitle());
		if(!TextUtils.isEmpty(selectedCityCode) && selectedCityCode.equals(item.getCode())){
		   holder.checkBox.setVisibility(View.VISIBLE);
		}
		else
			holder.checkBox.setVisibility(View.GONE);

		return view;
	}
	
	static class ViewHolder {
		 		 						
		@BindView(R.id.desc)
		TextView description;
		@BindView(R.id.checkbox)
		View checkBox;
						
		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}

	public String getSelectedCityCode() {
		return selectedCityCode;
	}

	public void setSelectedCityCode(String selectedCityCode) {
		this.selectedCityCode = selectedCityCode;
	}
}
