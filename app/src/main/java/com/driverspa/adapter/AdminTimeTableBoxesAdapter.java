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
import com.driverspa.model.api.response.WasherTimeTableResponse.BoxItem;

public class AdminTimeTableBoxesAdapter extends BaseDataAdapter {

	HashMap<String, BoxItem> boxes;
	private String[] boxIds;
	public AdminTimeTableBoxesAdapter(Context context) {
		super(context);
	}	
	
    @Override
    public int getCount() {
        return boxes.size();
    }

    @Override
    public Object getItem(int position) {
        return boxes.get(boxIds[position]);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    public String getBoxId(int position){
    	return boxIds[position];
    }
    
     public void setBoxes(HashMap<String, BoxItem> boxes) {
      this.boxIds = boxes.keySet().toArray(new String[boxes.size()]);
	  this.boxes = boxes;
    }
	
    
	@Override
	public View getView(int position, View view, ViewGroup parent) {
     ViewHolder holder = null;	
	 if(view == null){	
		view = LayoutInflater.from(context).inflate(R.layout.list_timetable_boxes_item, parent, false);
		holder = new ViewHolder(view);
		view.setTag(holder);
	 }
	 else{
		 holder = (ViewHolder) view.getTag();
	 }					
				
      String boxId = boxIds[position];
	  BoxItem boxItem = boxes.get(boxId);
		
	  holder.boxNumber.setText(BA.str(R.string.box_sp)+(position+1)); 
	  return view;
	}
	
	static class ViewHolder {
		 		 						
		@BindView(R.id.txtBox)
		TextView boxNumber;
				
		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
