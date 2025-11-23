package com.driverspa.adapter;

import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.CarItem;
import com.driverspa.util.Functions;

public class CarsGridAdapter extends BaseAdapter {
    private Context context;
    List<CarItem> gridList;
    private View selectedView;
    private int selectedPosition;
    private boolean withoutAddButton;
    View views [];


    public CarsGridAdapter(Context c, List<CarItem> gridList) {
    	context = c;
        this.gridList = gridList;
        views = new View[gridList.size()];
    }

    public CarsGridAdapter(Context c, List<CarItem> gridList, boolean withoutAddButton) {
    	context = c;
        this.gridList = gridList;
        this.withoutAddButton = withoutAddButton;
        views = new View[gridList.size()];
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

        ViewHolder holder = null;
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.grid_cars_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

		CarItem item = gridList.get(position);

        ViewGroup.LayoutParams layoutParams = holder.gridItem.getLayoutParams();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = ((display.getWidth()-(int)Functions.dipToPixels(context, 45)))/4;
		layoutParams.width = width; //this is in pixels
		layoutParams.height = width; //this is in pixels
        if(position == 0 && !withoutAddButton){
            holder.carNumber.setVisibility(View.GONE);
            holder.car.setBackgroundResource(R.drawable.ic_add);
        }
        else {
            holder.carNumber.setText(item.getCarNumber()+"\n"+item.getCarModel());
            holder.gridItem.setLayoutParams(layoutParams);

            switch ((item.getCarType()!=null?item.getCarType():1)){
                case 1:
                    holder.car.setBackgroundResource(R.drawable.background_button_sedan);
                    if(position == selectedPosition)
                        holder.car.setBackgroundResource(R.drawable.ic_sedan_selected);
                    break;
                case 10:
                    holder.car.setBackgroundResource(R.drawable.background_button_sedan);
                    if(position == selectedPosition)
                        holder.car.setBackgroundResource(R.drawable.ic_sedan_selected);
                    break;
                case 11:
                    holder.car.setBackgroundResource(R.drawable.background_button_sedan);
                    if(position == selectedPosition)
                        holder.car.setBackgroundResource(R.drawable.ic_sedan_selected);
                    break;
                case 2:
                    holder.car.setBackgroundResource(R.drawable.background_button_crossover);
                    if(position == selectedPosition)
                        holder.car.setBackgroundResource(R.drawable.ic_crossover_selected);
                    break;
                case 30:
                    holder.car.setBackgroundResource(R.drawable.background_button_jeep);
                    if(position == selectedPosition)
                        holder.car.setBackgroundResource(R.drawable.ic_jeep_selected);
                    break;
                case 3:
                    holder.car.setBackgroundResource(R.drawable.background_button_jeep);
                    if(position == selectedPosition)
                        holder.car.setBackgroundResource(R.drawable.ic_jeep_selected);
                    break;
                case 4:
                    holder.car.setBackgroundResource(R.drawable.background_button_minbus);
                    if(position == selectedPosition)
                        holder.car.setBackgroundResource(R.drawable.ic_minbus_selected);
                case 41:
                    holder.car.setBackgroundResource(R.drawable.background_button_minbus);
                    if(position == selectedPosition)
                        holder.car.setBackgroundResource(R.drawable.ic_minbus_selected);
                case 5:
                    holder.car.setBackgroundResource(R.drawable.background_button_moto);
                    if(position == selectedPosition)
                        holder.car.setBackgroundResource(R.drawable.ic_motorbike_selected);
                    break;
            }
        }
        views[position] = view;
        view.setSelected(true);
        holder.gridItem.setSelected(true);
        holder.car.setSelected(true);
        if(position == selectedPosition) {
            holder.gridItem.setBackgroundResource(R.drawable.button_grid_item_drawable_cars_pressed);
        }
        else{
            holder.gridItem.setBackgroundResource(R.drawable.background_button_grid_item_cars);
        }
        return view;
    }

    public  List<CarItem> getCarList(){
        return gridList;
    }

    static class ViewHolder {
		@BindView(R.id.car_number)
		TextView carNumber;
		@BindView(R.id.layoutGridItem)
		View gridItem;
		@BindView(R.id.car)
        ImageView car;
		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}

    public View [] getViews(){
        return this.views;
    }
}