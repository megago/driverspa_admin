package com.driverspa.adapter;

import static com.driverspa.util.Constants.*;

import android.content.Context;
import android.graphics.Color;
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
import com.driverspa.model.BookInfo;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;

public class AdminWasherTimeTableAdapter extends BaseAdapter {
    private Context context;
    List<HashMap<String,String>> gridList;
    private View selectedView;
    private int selectedPosition;
	HashMap<String,BookInfo> bookingObjects;
	private Washer washer;

    public AdminWasherTimeTableAdapter(Context c, List<HashMap<String, String>> gridList,HashMap<String,BookInfo> bookingObjects, Washer washer) {
    	context = c;
        this.gridList = gridList;
		this.bookingObjects = bookingObjects;
		this.washer = washer;
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
    	view = LayoutInflater.from(context).inflate(R.layout.grid_admin_time_item, parent, false);
		ViewHolder holder = new ViewHolder(view);			 			
		view.setTag(holder);			
		
		if(position == selectedPosition) view.setSelected(true);
		holder.txtKey.setVisibility(View.GONE);
		holder.txtFree.setVisibility(View.GONE);
		holder.txtStatus.setVisibility(View.GONE);

		if(gridList.get(position).get(GRID_STATUS).equals(SLOT_AVAILABLE)){
			holder.imageCar.setVisibility(View.GONE); 
			holder.txtTime.setVisibility(View.VISIBLE);
			holder.txtTime.setText(gridList.get(position).get(GRID_TITLE));
			if(Boolean.parseBoolean(gridList.get(position).get(GRID_EXPIRED))){
				view.setEnabled(false);
			}
		}
		else if(gridList.get(position).get(GRID_STATUS).equals(SLOT_NOT_AVAILABLE)){
			view.setEnabled(false);
			holder.gridItem.setEnabled(false);
			holder.imageCar.setVisibility(View.GONE);
			holder.txtFree.setVisibility(View.GONE);
			if(Boolean.parseBoolean(gridList.get(position).get(GRID_EXPIRED))){
				view.setEnabled(false);
			}
		}
		else{
			holder.txtTime.setText(gridList.get(position).get(GRID_TITLE));
			if(bookingObjects.get(gridList.get(position).get(GRID_BOOK_ID)) != null){
				holder.txtKey.setVisibility(View.VISIBLE);
				String clientKey = bookingObjects.get(gridList.get(position).get(GRID_BOOK_ID)).getClientKey();

				holder.txtKey.setText(clientKey.replace("(", "\n("));
			}
			else
				holder.txtKey.setVisibility(View.GONE);

			view.setEnabled(false);
			holder.gridItem.setEnabled(false);
			holder.imageCar.setVisibility(View.GONE);

			BookInfo item = bookingObjects.get(gridList.get(position).get(GRID_BOOK_ID));
			if(!item.getPaid()){
				holder.notPaid.setVisibility(View.VISIBLE);
			}
			else{
				holder.notPaid.setVisibility(View.GONE);
			}

			if(gridList.get(position).get(GRID_BOOK_ID) != null && bookingObjects.get(gridList.get(position).get(GRID_BOOK_ID)) != null) {
				BookInfo bookItem = bookingObjects.get(gridList.get(position).get(GRID_BOOK_ID));
				if(!bookItem.isQueued()) {
					if(!item.isOffline())
						holder.online.setVisibility(View.VISIBLE);
					else
						holder.online.setVisibility(View.GONE);
				}
				else holder.online.setVisibility(View.GONE);
				holder.txtFree.setText(bookStatus.get(bookingObjects.get(gridList.get(position).get(GRID_BOOK_ID)).getStatus()));
				holder.txtStatus.setText(bookStatus.get(bookingObjects.get(gridList.get(position).get(GRID_BOOK_ID)).getStatus()));
				if (bookingObjects.get(gridList.get(position).get(GRID_BOOK_ID)).getStatus().equals(PENDING))
					holder.txtStatus.setTextColor(Color.RED);
				else
					holder.txtStatus.setTextColor(context.getResources().getColor(R.color.BlueLight));
			}

//			holder.txtStatus.setTextColor(context.getResources().getColor(R.color.BlueLight));
			holder.txtStatus.setBackground(Functions.getBookStatusBGColor(context,item.getStatus()));
			holder.txtStatus.setTextColor(Functions.getBookStatusTextColor(context,item.getStatus()));

			holder.innerLayout.setBackgroundColor(context.getResources().getColor(R.color.admin_grid_item_busy));

			holder.txtStatus.setVisibility(View.VISIBLE);
		}
		
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
		layoutParams.height = width; //this is in pixels
		holder.gridItem.setLayoutParams(layoutParams);


        return view;
    }

    static class ViewHolder {
		@BindView(R.id.imgCar)
		ImageView imageCar;
		@BindView(R.id.txtTime)
		TextView txtTime;	
		@BindView(R.id.txtFree)
		TextView txtFree;
		@BindView(R.id.txtKey)
		TextView txtKey;
		@BindView(R.id.txtStatus)
		TextView txtStatus;
		@BindView(R.id.layoutGridItem)
		View gridItem;
		@BindView(R.id.innerLayout)
		View innerLayout;
		@BindView(R.id.online)
		View online;
		@BindView(R.id.notPaid)
		View notPaid;


		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}