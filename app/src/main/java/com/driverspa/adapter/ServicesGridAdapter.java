package com.driverspa.adapter;

import android.content.Context;
import android.graphics.Typeface;
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
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.ServiceItem;
import com.driverspa.util.Functions;
import com.driverspa.util.otto.ServiceUnselectClicked;

public class ServicesGridAdapter extends BaseAdapter {
    private Context context;
    List<ServiceItem> gridList;
    private View selectedView;
    private HashMap<Integer,Boolean> selectedPosition = new HashMap<Integer,Boolean>();;
    private HashMap<Integer,Integer> selectedPositionCount = new HashMap<Integer,Integer>();

    public ServicesGridAdapter(Context c, List<ServiceItem> gridList) {
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
        //Commented to handle multiple services
//        selectedPosition.clear();
        selectedPosition.put(position, selected);
        selectedPositionCount.put(position, (selectedPositionCount.get(position)!=null?selectedPositionCount.get(position):0)+1);
    }

    public void unSelectSelectedPosition(Integer position){
        if((selectedPositionCount.get(position)!=null?selectedPositionCount.get(position):0)-1 >= 0)
            selectedPositionCount.put(position, (selectedPositionCount.get(position)!=null?selectedPositionCount.get(position):0)-1);
        if((selectedPositionCount.get(position)!=null?selectedPositionCount.get(position):0) == 0){
            selectedPosition.put(position, false);
        }
    }

    public HashMap<Integer,Integer> getSelectedPositionCount(){
        return selectedPositionCount;
    }

    public List<ServiceItem> getData(){
        return gridList;
    }
    
    public HashMap<Integer,Boolean> getSelectedPosition(){
    	return selectedPosition;
    }
    
    public View getSelectedView(){
    	return selectedView;
    }
    
    public View getView(final int position, View view, ViewGroup parent) {
    	view = LayoutInflater.from(context).inflate(R.layout.grid_service_item, parent, false);
		ViewHolder holder = new ViewHolder(view);			 			
		view.setTag(holder);

        final View gridLayoutView = view.findViewById(R.id.layoutGridItem);
        if(selectedPosition.get(position) != null && selectedPosition.get(position)) {
            holder.unSelectService.setVisibility(View.VISIBLE);
            holder.serviceCountView.setVisibility(View.VISIBLE);
            holder.serviceCountText.setText(selectedPositionCount.get(position)+"");
            holder.gridItem.setBackgroundResource(R.drawable.button_grid_item_drawable_cars_pressed);
        }
        else{
            holder.unSelectService.setVisibility(View.GONE);
            holder.serviceCountView.setVisibility(View.GONE);
            holder.serviceCountText.setText("0");
            holder.gridItem.setBackgroundResource(R.drawable.background_button_grid_item_cars);
        }

        ServiceItem item = gridList.get(position);
        holder.serviceName.setText(item.getServiceName());
        holder.serviceName.setTypeface(null, Typeface.NORMAL);

        if(item.getPrice() == 0){
            holder.serviceName.setTextColor(context.getResources().getColor(R.color.BlueLight));
            holder.serviceName.setTypeface(null, Typeface.ITALIC);
            holder.serviceName.setText(item.getServiceName()+" - 0тг");
        }
        holder.unSelectService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BA.getEventBus().post(new ServiceUnselectClicked(position));
            }
        });

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
			
		@BindView(R.id.service_name)
		TextView serviceName;
		
		@BindView(R.id.layoutGridItem)
		View gridItem;
        @BindView(R.id.unselect_service)
        View unSelectService;
        @BindView(R.id.service_count)
        View serviceCountView;
        @BindView(R.id.service_count_text)
        TextView serviceCountText;

        public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}