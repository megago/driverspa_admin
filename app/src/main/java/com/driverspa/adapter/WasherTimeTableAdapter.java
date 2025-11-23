package com.driverspa.adapter;

import static com.driverspa.util.Constants.GRID_STATUS;
import static com.driverspa.util.Constants.GRID_TITLE;
import static com.driverspa.util.Constants.SLOT_AVAILABLE;
import static com.driverspa.util.Constants.SLOT_NOT_AVAILABLE;

import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.util.Functions;

public class WasherTimeTableAdapter extends BaseAdapter {
    private Context context;
    List<HashMap<String,String>> gridList;
    private View selectedView;
    private int selectedPosition;
    public WasherTimeTableAdapter(Context c, List<HashMap<String,String>> gridList) {
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
    
    public void setSelectedPosition(int position){
    	this.selectedPosition = position; 
    }
    
    public int getSelectedPosition(){
    	return selectedPosition;
    }
    
    public View getSelectedView(){
    	return selectedView;
    }
    
    public View getView(int position, View view, ViewGroup parent) {
    	view = LayoutInflater.from(context).inflate(R.layout.grid_time_item, parent, false);
		ViewHolder holder = new ViewHolder(view);			 			
		view.setTag(holder);			
		
		if(position == selectedPosition) view.setSelected(true);

		if(gridList.get(position).get(GRID_STATUS)
				          .equals(SLOT_AVAILABLE)){
			holder.imageCar.setVisibility(View.GONE); 
			holder.txtTime.setVisibility(View.VISIBLE);
			holder.txtTime.setText(gridList.get(position).get(GRID_TITLE));
		}
		else if(gridList.get(position).get(GRID_STATUS)
		          .equals(SLOT_NOT_AVAILABLE)){
			view.setEnabled(false);
			holder.gridItem.setEnabled(false);
			holder.imageCar.setVisibility(View.GONE); 
			holder.txtTime.setVisibility(View.GONE);			
		}
		else{
			view.setEnabled(false);
			holder.gridItem.setEnabled(false);
			holder.imageCar.setVisibility(View.VISIBLE); 
			holder.txtTime.setVisibility(View.GONE);			
		}
		
		ViewGroup.LayoutParams layoutParams = holder.gridItem.getLayoutParams();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = ((display.getWidth()-(int)Functions.dipToPixels(context, 40)))/4;
		layoutParams.width = width; //this is in pixels
		layoutParams.height = width; //this is in pixels
		holder.gridItem.setLayoutParams(layoutParams);
				
        return view;
    }

    static class ViewHolder {
			
		@BindView(R.id.imgCar)
		ImageView imageCar;	

		@BindView(R.id.txtTime)
		TextView txtTime;	
		
		@BindView(R.id.layoutGridItem)
		View gridItem;			

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}