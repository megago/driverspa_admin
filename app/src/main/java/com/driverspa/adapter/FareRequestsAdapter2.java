package com.driverspa.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.FareRequest;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.HttpClient;
import com.driverspa.util.RetrofitClient;
import com.driverspa.util.TransformationCircle;


public class FareRequestsAdapter2 extends  RecyclerViewAdapter<FareRequestsAdapter2.ClientFareReqeustViewHolder>  {
	DecimalFormat formatter = new DecimalFormat("#,###.##");
	Context context;
	Washer washer;
	private List<FareRequest> mData;

	OnItemClickListener mItemClickListener;
	OnItemLongClickListener  mItemLongClickListener;
	Filter filter;

	public FareRequestsAdapter2(Context context, List<FareRequest> data, Washer washer) {
		super(context);
		this.context = context;
		setData(data);
		this.washer = washer;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public void setData(List<FareRequest> data){
		this.mData = data;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public ClientFareReqeustViewHolder onCreateView(ViewGroup parent, int viewType) {
		final View view = LayoutInflater.from(context).inflate(R.layout.list_admin_clients_item, parent, false);
		return new ClientFareReqeustViewHolder(view);
	}

	@Override
	public void onBindView(final ClientFareReqeustViewHolder holder, int position) {

		final FareRequest item = mData.get(position);

		if(!TextUtils.isEmpty(item.getClientAvatar())){
			HttpClient.getPicasso()
					.load(RetrofitClient.API_URL_IMAGES+item.getClientAvatar())
					.fit()
					.centerCrop()
					.transform(new TransformationCircle(context))
					.into(holder.clientAvatar, new Callback() {
						@Override
						public void onSuccess() {
						}

						@Override
						public void onError() {
						}
					});
		}
		else{
			holder.clientAvatar.setVisibility(View.VISIBLE);
			holder.clientAvatar.setImageResource(R.drawable.ic_no_image);
		}

		holder.name.setText("");
		if(!TextUtils.isEmpty(item.getClientName()))
           holder.name.setText(item.getClientName());
		if(!TextUtils.isEmpty(item.getClientKey()))
			holder.carName.append(item.getClientKey());

		String services = "";
		if(item.getServices() != null && item.getServices().size() > 0) {
			LinkedHashMap<Integer,Integer> groupedServices = Functions.getGroupedServices(item.getServices());
			for (LinkedHashMap.Entry<Integer, Integer> entry : groupedServices.entrySet()) {
				Integer serviceId = entry.getKey();
				Integer count = entry.getValue();
				services = services + (BA.getReference().getServices().get(serviceId)+""+(count>1?" x "+count:"")) + "/";
			}
			services = services.substring(0, services.length() - 1);
		}
		holder.services.setText(services);
		holder.fare.setText(formatter.format(item.getFare()));
		if(!TextUtils.isEmpty(item.getComment()))
		   holder.comment.setText(item.getComment());
		else{
			holder.comment.setVisibility(View.GONE);
		}
	}


	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

	public interface OnItemLongClickListener {
		public void onItemLongClick(View view, int position);
	}

	public void add(FareRequest fareRequest, int position) {
		position = position == -1 ? getItemCount() : position;
		mData.add(position, fareRequest);
		notifyItemInserted(position);
	}

	public void remove(int position) {
		if (position < getItemCount()) {
			mData.remove(position);
			notifyItemRemoved(position);
		}
	}

	public void refreshData(List<FareRequest> data){
		this.mData = data;
		notifyDataSetChanged();
	}

	public class ClientFareReqeustViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

		FareRequest fareRequest;

		@BindView (R.id.car)
		TextView carName;
		@BindView (R.id.services)
		TextView services;
		@BindView (R.id.price)
		TextView fare;
		@BindView (R.id.comment)
		TextView comment;
		@BindView (R.id.name)
		TextView name;
		@BindView (R.id.time)
		TextView time;
		@BindView (R.id.imgClientAvatar)
		ImageView clientAvatar;
		@BindView (R.id.parentView)
		View parentView;


		public ClientFareReqeustViewHolder(View view) {
			super(view);
			ButterKnife.bind(this, view);
			parentView.setOnClickListener(this);
			parentView.setOnLongClickListener(this);
		}

			@Override
			public void onClick(View view) {
				if (mItemClickListener != null) {
					mItemClickListener.onItemClick(view, getPosition());
				}
			}

		@Override
		public boolean onLongClick(View view) {
			if (mItemLongClickListener != null) {
				mItemLongClickListener.onItemLongClick(view, getPosition());
			}
			return false;
		}
	}

	public void setWasher(Washer washer){
		this.washer = washer;
	}

    public void setOnItemClickListener(final OnItemClickListener listener) {
			this.mItemClickListener = listener;
		}

	   public void setOnItemLongClickListener(final OnItemLongClickListener listener) {
			this.mItemLongClickListener = listener;
		}

}



