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
import android.widget.TextView;

import java.text.DecimalFormat;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.WasherPublic;
import com.driverspa.util.LogUtil;

public class WasherListAdapter extends BaseDataAdapter<WasherPublic> {

	boolean isCompany = false;
	boolean showDistance = true;
	Location currentLoc;
	public Location getCurrentLoc() {
		return currentLoc;
	}

	public void setCurrentLoc(Location currentLoc) {
		this.currentLoc = currentLoc;
	}

	public WasherListAdapter(Context context, Location currentLoc, boolean showDistance) {
		super(context);
		this.currentLoc = currentLoc;
		this.showDistance = showDistance;
	}

	public WasherListAdapter(Context context, Location currentLoc) {
		super(context);
		this.currentLoc = currentLoc;
	}

	public WasherListAdapter(Context context, boolean isCompany) {
		super(context);
		this.isCompany = isCompany;
	}

	public WasherListAdapter(Context context) {
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
//		if (view != null) {
//			holder = (ViewHolder) view.getTag();
//		} else {
		view = LayoutInflater.from(context).inflate(R.layout.list_washers_item, parent, false);
		holder = new ViewHolder(view);
		view.setTag(holder);
//		}

		WasherPublic item = getItem(position);
		holder.name.setText(item.getName());

		if(!isCompany){
			holder.imageAvailability.setVisibility(View.VISIBLE);
			holder.price.setVisibility(View.VISIBLE);
			holder.reviewCount.setVisibility(View.VISIBLE);
			holder.price.setVisibility(View.VISIBLE);
			holder.reviewCount.setVisibility(View.VISIBLE);
			holder.distance.setVisibility(View.VISIBLE);
			holder.priceLayout.setVisibility(View.VISIBLE);
			holder.address.setVisibility(View.VISIBLE);

			holder.imageAvailability.setSelected(item.isBookable());
//		holder.price.setText("");
			holder.price.setText(BA.str(R.string.from_pre) + (item.getPrice()!=null?item.getPrice()+"":"500") + " ₸");
			holder.reviewCount.setText(item.getRating().intValue()+"");
			holder.address.setText(item.getAddress());

			DecimalFormat formatter = new DecimalFormat("###.##");
			if(currentLoc != null){
				try{
					Location washerLocation = new Location("A");
					washerLocation.setLatitude(Double.parseDouble(item.getLonLat().get(1).toString()));
					washerLocation.setLongitude(Double.parseDouble(item.getLonLat().get(0).toString()));
					float dist = currentLoc.distanceTo(washerLocation);
					holder.distance.setText(String.format("%s km", formatter.format(dist/1000).replaceAll(",", " ")));
					if(!showDistance){
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

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
