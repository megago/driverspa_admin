package com.driverspa.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.Washer.Review;


public class WasherAdminReviewAdapter extends  RecyclerViewAdapter<WasherAdminReviewAdapter.WasherReviewViewHolder>  {

	private List<Review> mData;
	private Context context;
	OnItemClickListener mItemClickListener;

	public WasherAdminReviewAdapter(Context context, List<Review> data) {
		super(context);
		this.context = context;

		this.mData = data;
	}

	public void setData(List<Review> data){
		this.mData = data;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public WasherReviewViewHolder onCreateView(ViewGroup parent, int viewType) {
		final View view = LayoutInflater.from(context).inflate(R.layout.list_washer_review_item, parent, false);
		return new WasherReviewViewHolder(view);
	}

	@Override
	public void onBindView(final WasherReviewViewHolder holder, int position) {

		Review item = mData.get(position);
		holder.washerReviewText.setText(item.getText());
		holder.authorName.setText(item.getAuthor());

		if(item.getMark() > 0 && item.getMark() < 2){
			holder.star1.setSelected(true);
		}
		else if(item.getMark() > 1 && item.getMark() < 3){
			holder.star1.setSelected(true);
			holder.star2.setSelected(true);
		}
		else if(item.getMark() > 2 && item.getMark() < 4){
			holder.star1.setSelected(true);
			holder.star2.setSelected(true);
			holder.star3.setSelected(true);
		}
		else if(item.getMark() > 3 && item.getMark() < 5){
			holder.star1.setSelected(true);
			holder.star2.setSelected(true);
			holder.star3.setSelected(true);
			holder.star4.setSelected(true);
		}
		else if(item.getMark() == 5){
			holder.star1.setSelected(true);
			holder.star2.setSelected(true);
			holder.star3.setSelected(true);
			holder.star4.setSelected(true);
			holder.star5.setSelected(true);
		}

	}

	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

	public interface OnItemLongClickListener {
		public void onItemLongClick(View view, int position);
	}


	public void add(Review review, int position) {
		position = position == -1 ? getItemCount() : position;
		mData.add(position, review);
		notifyItemInserted(position);
	}

	public void remove(int position) {
		if (position < getItemCount()) {
			mData.remove(position);
			notifyItemRemoved(position);
		}
	}

	public void refreshData(List<Review> data){
		this.mData = data;
		notifyDataSetChanged();
	}

	public class WasherReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

		@BindView(R.id.txtReview)
		TextView washerReviewText;
		@BindView(R.id.authorName)
		TextView authorName;
		@BindView(R.id.star1)
		ImageView star1;
		@BindView(R.id.star2)
		ImageView star2;
		@BindView(R.id.star3)
		ImageView star3;
		@BindView(R.id.star4)
		ImageView star4;
		@BindView(R.id.star5)
		ImageView star5;

		@BindView(R.id.parentView)
		View parentView;


		public WasherReviewViewHolder(View view) {
			super(view);
			ButterKnife.bind(this, view);
			parentView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (mItemClickListener != null) {
				mItemClickListener.onItemClick(view, getPosition());
			}
		}
	}

	public void setOnItemClickListener(final OnItemClickListener listener) {
		this.mItemClickListener = listener;
	}

}
