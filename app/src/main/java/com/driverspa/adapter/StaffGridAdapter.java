package com.driverspa.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.Staff;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;

public class StaffGridAdapter extends BaseAdapter {
    private Context context;
    List<Staff> gridList;
    private View selectedView;
    private HashMap<Integer,Boolean> selectedPosition = new HashMap<Integer,Boolean>();;

    public StaffGridAdapter(Context c, List<Staff> gridList) {
    	context = c;
        this.gridList = gridList;
    }

    public int getCount() {
        return gridList.size();
    }

    public Object getItem(int position) {
        return gridList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }
    
    public void setSelectedView(View selectedView){
    	this.selectedView = selectedView; 
    }
    
    public void setSelectedPosition(Integer position, boolean selected){
        selectedPosition.put(position, selected);

    }

    public List<Staff> getData(){
        return gridList;
    }
    
    public HashMap<Integer,Boolean> getSelectedPosition(){
    	return selectedPosition;
    }
    
    public View getSelectedView(){
    	return selectedView;
    }
    
    public View getView(int position, View view, ViewGroup parent) {
    	view = LayoutInflater.from(context).inflate(R.layout.grid_staff_item, parent, false);
		ViewHolder holder = new ViewHolder(view);			 			
		view.setTag(holder);

        final View gridLayoutView = view.findViewById(R.id.layoutGridItem);
        if(selectedPosition.get(position) != null && selectedPosition.get(position)) {
            holder.gridItem.setBackgroundResource(R.drawable.button_grid_item_drawable_cars_pressed);
        }
        else{
            holder.gridItem.setBackgroundResource(R.drawable.background_button_grid_item_cars);
        }

        Staff item = gridList.get(position);
        holder.itemName.setText(item.getFirstName());
        if(!TextUtils.isEmpty(item.getRole())) {
            holder.itemDesc.setVisibility(View.VISIBLE);
            holder.itemDesc.setText(Constants.staffRole.get(item.getRole()));//+" \n("+Constants.boxTypeMap.get(item.getBookingType().getValue())+")");
        }
        else{
            holder.itemDesc.setText("");// \n("+Constants.boxTypeMap.get(item.getBookingType().getValue())+")");
            holder.itemDesc.setVisibility(View.GONE);
        }

//        holder.itemBookingType.setText(Constants.boxTypeMap.get(item.getBookingType().getValue()));

		ViewGroup.LayoutParams layoutParams = holder.gridItem.getLayoutParams();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
        boolean tabletSize = context.getResources().getBoolean(R.bool.isTablet);
        boolean tabletBigSize = context.getResources().getBoolean(R.bool.isBigTablet);
        int width = ((display.getWidth()-(int)Functions.dipToPixels(context, 40)))/4;
        if(tabletSize && !tabletBigSize)
            width = ((display.getWidth()-(int)Functions.dipToPixels(context, 40)))/6;
        if(tabletSize && tabletBigSize)
            width = ((display.getWidth()-(int)Functions.dipToPixels(context, 40)))/8;

        layoutParams.width = width; //this is in pixels
		layoutParams.height = width*2/3; //this is in pixels
		holder.gridItem.setLayoutParams(layoutParams);

        return view;
    }

    static class ViewHolder {
			
		@BindView(R.id.item_name)
		TextView itemName;
		@BindView(R.id.item_desc)
		TextView itemDesc;

		@BindView(R.id.layoutGridItem)
		View gridItem;			

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}