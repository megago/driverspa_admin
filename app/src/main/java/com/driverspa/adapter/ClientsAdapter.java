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
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.HttpClient;
import com.driverspa.util.RetrofitClient;
import com.driverspa.util.TransformationCircle;


public class ClientsAdapter extends  RecyclerViewAdapter<ClientsAdapter.ClientInfoViewHolder>  {
	DecimalFormat formatter = new DecimalFormat("#,###.##");
	Context context;
	Washer washer;
	private List<ClientInfo> mData;
	OnItemClickListener mItemClickListener;
	OnItemLongClickListener  mItemLongClickListener;
	Filter filter;

	public ClientsAdapter(Context context, List<ClientInfo> data, Washer washer) {
		super(context);
		this.context = context;
		setData(data);
		this.washer = washer;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public void setData(List<ClientInfo> data){
		this.mData = data;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public ClientInfoViewHolder onCreateView(ViewGroup parent, int viewType) {
		final View view = LayoutInflater.from(context).inflate(R.layout.list_admin_clients_item, parent, false);
		return new ClientInfoViewHolder(view);
	}

	@Override
	public void onBindView(final ClientInfoViewHolder holder, int position) {

		final ClientInfo item = mData.get(position);

//		if(!item.getRegistered())
//			holder.arrowRight.setVisibility(View.GONE);
//		else
//			holder.arrowRight.setVisibility(View.VISIBLE);

		if(!TextUtils.isEmpty(item.getProfileImageUrl())){
			HttpClient.getPicasso()
					.load(RetrofitClient.API_URL_IMAGES+item.getProfileImageUrl())
					.fit()
					.centerCrop()
					.transform(new TransformationCircle(context))
					.into(holder.imgClientAvatar, new Callback() {
						@Override
						public void onSuccess() {
						}

						@Override
						public void onError() {
						}
					});
		}
		else{
			holder.imgClientAvatar.setVisibility(View.VISIBLE);
			holder.imgClientAvatar.setImageResource(R.drawable.ic_no_image);
		}

		holder.clientName.setText("");
		if(!TextUtils.isEmpty(item.getName()))
           holder.clientName.setText(item.getName());
		if(!TextUtils.isEmpty(item.getPhone()))
           holder.mobile.setText(Functions.formatPhoneNumber("+7 ### ### ## ##",item.getPhone()));
        else
			holder.mobile.setText("");

		String carNo = item.getCarNo();

		if(!TextUtils.isEmpty(item.getName()) && !TextUtils.isEmpty(item.getCarNo()) && carNo.contains(item.getName())){
			carNo = carNo.replaceAll(item.getName(),"").replaceAll("\\(","").replaceAll("\\)","");
		}

		if(!TextUtils.isEmpty(item.getCarNo()))
			holder.clientName.append(" "+carNo.replaceAll("\\(","").replaceAll("\\)",""));
//		if(!TextUtils.isEmpty(item.getCarNo()))

		holder.finishedBooksCount.setVisibility(View.VISIBLE);
		if(item.getDepositAmount()!=null)
			holder.finishedBooksCount.setText("Депозит "+formatter.format(item.getDepositAmount())+" ₸");
		if(item.getAccruedBonuses()!=null){
			holder.finishedBooksCount.append(" Начислено бонусов "+formatter.format(item.getAccruedBonuses())+" ₸");
		}

//		if(item.getFinishedBooksCount()!=null) {
//			holder.finishedBooksCount.setVisibility(View.VISIBLE);
//			holder.finishedBooksCount.setText("");
//			holder.finishedBooksCount.setText("Помыта " + item.getFinishedBooksCount() + " раз(а).");
//			if (item.getLastFinishedBookDate() != null) {
//				HashMap<String, String> timeMap = Functions.formatUTCDate(item.getLastFinishedBookDate());
//				holder.finishedBooksCount.append(" Последняя мойка " + timeMap.get(Functions.DATE));
//			}
//		}
//		else{
//			holder.finishedBooksCount.setVisibility(View.GONE);
//		}

		if(item.getCars() != null && item.getCars().size() > 1){
			holder.clientName.append("(еще "+(item.getCars().size()-1)+" машин)");
		}
	}


	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

	public interface OnItemLongClickListener {
		public void onItemLongClick(View view, int position);
	}

	public void add(ClientInfo client, int position) {
		position = position == -1 ? getItemCount() : position;
		mData.add(position, client);
		notifyItemInserted(position);
	}

	public void remove(int position) {
		if (position < getItemCount()) {
			mData.remove(position);
			notifyItemRemoved(position);
		}
	}

	public void refreshData(List<ClientInfo> data){
		this.mData = data;
		notifyDataSetChanged();
	}

	public class ClientInfoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

		ClientInfo bookInfo;
		@BindView(R.id.parentView)
		View parentView;
		@BindView(R.id.txtClientName)
		TextView clientName;
		@BindView(R.id.txtFinishedBooksCount)
		TextView finishedBooksCount;
		@BindView(R.id.mobile)
		TextView mobile;
		@BindView(R.id.imgClientAvatar)
		ImageView imgClientAvatar;
		@BindView(R.id.arrowRight)
		View arrowRight;


		public ClientInfoViewHolder(View view) {
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



