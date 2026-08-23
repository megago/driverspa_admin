package com.driverspa.adapter;
import com.driverspa.BA;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.Box;
import com.driverspa.util.Constants;

public class AdminBoxesAdapter extends BaseDataAdapter<Box> {

	public AdminBoxesAdapter(Context context) {
		super(context);

	}
		
	@Override
	public View getView(int position, View view, ViewGroup parent) {
     ViewHolder holder = null;	
	 if(view == null){	
		view = LayoutInflater.from(context).inflate(R.layout.list_admin_boxes_item, parent, false);
		holder = new ViewHolder(view);
		view.setTag(holder);
	 }
	 else{
		 holder = (ViewHolder) view.getTag();
	 }

	  Box item = getItem(position);
	  holder.boxNumber.setText(item.getBoxName());
	  holder.boxType.setText("");

 	  if(item.getBookingType() != null)
	    holder.boxType.setText(Constants.boxTypeMap.get(item.getBookingType().getValue()));

	  holder.washerPerson.setVisibility(View.GONE);

	  if(!TextUtils.isEmpty(item.getWasherPerson())){
		  holder.washerPerson.setText(item.getWasherPerson());
	  }
		else
		  holder.washerPerson.setText(BA.str(R.string.washer_not_set));
	  return view;
	}
	
	static class ViewHolder {
		 		 						
		@BindView(R.id.txtBox)
		TextView boxNumber;
		@BindView(R.id.txtBoxType)
		TextView boxType;
		@BindView(R.id.washerPerson)
		TextView washerPerson;

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}


}
