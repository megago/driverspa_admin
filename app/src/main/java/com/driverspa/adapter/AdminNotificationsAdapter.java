package com.driverspa.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.AdminNotificationNews;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;


public class AdminNotificationsAdapter extends  RecyclerViewAdapter<AdminNotificationsAdapter.AdminNotificationNewsViewHolder>  {

	Context context;
	Washer washer;
	private List<AdminNotificationNews> mData;
	OnItemClickListener mItemClickListener;
	OnItemLongClickListener  mItemLongClickListener;
	Filter filter;

	public AdminNotificationsAdapter(Context context, List<AdminNotificationNews> data, Washer washer) {
		super(context);
		this.context = context;
		setData(data);
		this.washer = washer;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public void setData(List<AdminNotificationNews> data){
		this.mData = data;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public AdminNotificationNewsViewHolder onCreateView(ViewGroup parent, int viewType) {
		final View view = LayoutInflater.from(context).inflate(R.layout.list_admin_notification_news_item, parent, false);
		return new AdminNotificationNewsViewHolder(view);
	}

	@Override
	public void onBindView(final AdminNotificationNewsViewHolder holder, int position) {

		final AdminNotificationNews item = mData.get(position);
        holder.newsTarget.setText(Constants.newsTarget.get(item.getNewsTarget()));
		holder.newsType.setText(Constants.newsStatus.get(item.getStatus()));
		holder.newsText.setText(item.getText());

		HashMap<String,String> timeTSMap = Functions.formatUTCDate(item.getTS());
		PrettyTime p = new PrettyTime(new Locale("ru"));
		if(DateUtils.isToday(Functions.getUTCDate(item.getTS()).getTime()))
			holder.newsDate.setText(p.format(Functions.getUTCDate(item.getTS())));
		else
			holder.newsDate.setText(p.format(Functions.getUTCDate(item.getTS()))+" ("+timeTSMap.get(Functions.DATE)+" в "+timeTSMap.get(Functions.TIME)+")");
	}


	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

	public interface OnItemLongClickListener {
		public void onItemLongClick(View view, int position);
	}

	public void add(AdminNotificationNews client, int position) {
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

	public void refreshData(List<AdminNotificationNews> data){
		this.mData = data;
		notifyDataSetChanged();
	}

	public class AdminNotificationNewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

		AdminNotificationNews news;
		@BindView(R.id.parentView)
		View parentView;
		@BindView(R.id.newsTarget)
		TextView newsTarget;
		@BindView(R.id.newsType)
		TextView newsType;
		@BindView(R.id.newsText)
		TextView newsText;
		@BindView(R.id.date)
		TextView newsDate;

		public AdminNotificationNewsViewHolder(View view) {
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



