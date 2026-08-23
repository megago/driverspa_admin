package com.driverspa.adapter;
import com.driverspa.BA;

import android.content.Context;
import android.location.Location;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import java.text.DecimalFormat;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.WasherPublic;
import com.driverspa.util.LogUtil;

public class WasherListNearByAdapter extends BaseDataAdapter<WasherPublic> {

	DecimalFormat formatter = new DecimalFormat("###.##");
	boolean isCompany = false;
	boolean useNoGPSOption = false;
	Location currentLoc;
	public Location getCurrentLoc() {
		return currentLoc;
	}

	public void setCurrentLoc(Location currentLoc) {
		this.currentLoc = currentLoc;
	}

	public WasherListNearByAdapter(Context context, Location currentLoc, boolean showDistance) {
		super(context);
		this.currentLoc = currentLoc;
		this.useNoGPSOption = showDistance;
	}

	public WasherListNearByAdapter(Context context, boolean isCompany) {
		super(context);
		this.isCompany = isCompany;
	}

	public WasherListNearByAdapter(Context context) {
		super(context);		
	}	
		
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		
		if (isLoad && position == getCount() - 1) {
			LinearLayout ll = new LinearLayout(context);
			ll.setGravity(Gravity.CENTER_HORIZONTAL);
			ProgressBar bar = new ProgressBar(context);
			ll.addView(bar);
			return ll;
		}
				
		ViewHolder holder;
		WasherPublic item = getItem(position);
//		if (view != null) {
//			holder = (ViewHolder) view.getTag();
//		} else {
		  if(item.isBookable() && !item.getStatus().equals("information"))
			 view = LayoutInflater.from(context).inflate(R.layout.list_nearby_washers_online, parent, false);
		  else if(!item.isBookable() && !item.getStatus().equals("information"))
			 view = LayoutInflater.from(context).inflate(R.layout.list_nearby_washers_online_not_working, parent, false);
		  else
			 view = LayoutInflater.from(context).inflate(R.layout.list_nearby_washers_offline, parent, false);

			holder = new ViewHolder(view);		
		    view.setTag(holder);
//		}

		holder.name.setText(item.getName().toLowerCase().contains("автомойка")?item.getName():BA.str(R.string.car_wash_label_sp)+item.getName());
		
	   if(!isCompany){		
//		holder.imageAvailability.setVisibility(View.VISIBLE);
		holder.price.setVisibility(View.VISIBLE);
//		holder.reviewCount.setVisibility(View.VISIBLE);
		holder.price.setVisibility(View.VISIBLE);
//		holder.reviewCount.setVisibility(View.VISIBLE);
		holder.distance.setVisibility(View.VISIBLE);
		holder.priceLayout.setVisibility(View.VISIBLE);
		holder.address.setVisibility(View.VISIBLE);

//		holder.imageAvailability.setSelected(item.isBookable());
//		holder.price.setText("");
		holder.price.setText(BA.str(R.string.body_interior_from_pre) + (item.getPrice()!=null?formatter.format(item.getPrice()).replaceAll(",", " ")+"":"500"));
		holder.review.setRating((float)item.getRating().doubleValue());
		holder.reviewCount.setText(item.getRating().intValue()+"");
		holder.address.setText(item.getAddress());


		if(currentLoc != null){
		  try{	
		  Location washerLocation = new Location("A");
		  washerLocation.setLatitude(Double.parseDouble(item.getLonLat().get(1).toString()));
		  washerLocation.setLongitude(Double.parseDouble(item.getLonLat().get(0).toString()));
	      float dist = currentLoc.distanceTo(washerLocation);
	      holder.distance.setText(String.format("%s km", formatter.format(dist/1000).replaceAll(",", " ")));
	      if(useNoGPSOption){
	    	  holder.distance.setVisibility(View.GONE);
	      }
	      else{
	    	  holder.distance.setVisibility(View.VISIBLE);
	       }
		  }
		  catch(Exception e){
			  LogUtil.d("YERZHAN", "Current location problem");
		  }
		 }	
		}
		else{
			holder.imageAvailability.setVisibility(View.GONE);
			holder.price.setVisibility(View.GONE);
			holder.reviewCount.setVisibility(View.GONE);
			holder.price.setVisibility(View.GONE);
			holder.reviewCount.setVisibility(View.GONE);
			holder.distance.setVisibility(View.GONE);
			holder.priceLayout.setVisibility(View.GONE);
			holder.address.setVisibility(View.GONE);

		}
					
		return view;
	}
	
	static class ViewHolder {
		 		 				
		@BindView(R.id.imgAvailability)
		ImageView imageAvailability;	
		
		@BindView(R.id.txtWasherAddress)
		TextView address;

		@BindView(R.id.txtWasherName)
		TextView name;

		@BindView(R.id.txtPrice)
		TextView price;
		
		@BindView(R.id.txtRevCount)
		TextView reviewCount;
		
		@BindView(R.id.txtDistance)
		TextView distance;
		
		@BindView(R.id.priceLayout)
		View priceLayout;
		@BindView(R.id.review)
		RatingBar review;

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
