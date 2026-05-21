package com.driverspa.adapter;

import static com.driverspa.util.Constants.PENDING;
import static com.driverspa.util.Constants.QUEUED_APPROVED;
import static com.driverspa.util.Constants.QUEUED_FINISHED;
import static com.driverspa.util.Constants.QUEUED_REJECTED;
import static com.driverspa.util.Constants.bookStatus;

import android.annotation.SuppressLint;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.BookInfo;
import com.driverspa.model.Box;
import com.driverspa.model.PushData;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;


@SuppressLint("NewApi")
public class SingleClientBooksAdapter extends  RecyclerViewAdapter<SingleClientBooksAdapter.BookInfoViewHolder>  {

	public static final int ITEM_TYPE_BOOK = 0;
	public static final int ITEM_TYPE_QUEUE = 1;

	public static final int TIMER_MINUTE = 14;
	HashMap<String,PushData> bookingPushData;
	Context context;
	Washer washer;
	private List<BookInfo> mData;
	OnItemClickListener mItemClickListener;
	OnItemLongClickListener  mItemLongClickListener;
	private List<BookInfoViewHolder> lstHolders;
	private Handler mHandler = new Handler();

	private Runnable updateRemainingTimeRunnable = new Runnable() {
		@Override
		public void run() {
			synchronized (lstHolders) {
				long currentTime = System.currentTimeMillis();
				for (BookInfoViewHolder holder : lstHolders) {
					holder.updateTimeRemaining(currentTime);
				}
			}
		}
	};

	private void startUpdateTimer() {
		Timer tmr = new Timer();
		tmr.schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.post(updateRemainingTimeRunnable);
			}
		}, 1000, 1000);
	}

	public void stopTimer(){
		if(mHandler != null && updateRemainingTimeRunnable !=null)
			mHandler.removeCallbacks(updateRemainingTimeRunnable);
	}

	public SingleClientBooksAdapter(Context context, List<BookInfo> data, Washer washer, HashMap<String,PushData> bookingPushData) {
		super(context);
		this.context = context;
		setData(data);
		this.bookingPushData = bookingPushData;
		this.washer = washer;
		lstHolders = new ArrayList<>();
//		startUpdateTimer();
	}

	public void setData(List<BookInfo> data){
		this.mData = data;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public BookInfoViewHolder onCreateView(ViewGroup parent, int viewType) {
		final View view = LayoutInflater.from(context).inflate(R.layout.list_single_client_books_item, parent, false);
		return new BookInfoViewHolder(view);
	}

	@Override
	public void onBindView(final BookInfoViewHolder holder, int position) {

		synchronized (lstHolders) {
			lstHolders.add(holder);
		}
		final BookInfo item = mData.get(position);
		Instant bookDateTime = item.getTime();
		HashMap<String,String> timeMap = Functions.formatDateTime(bookDateTime);
		holder.parentView.setHasTransientState(true);
		if(holder.parentView.getAnimation() != null)
			holder.parentView.getAnimation().cancel();

		holder.txtBoxName.setVisibility(View.GONE);

//		if(!item.getPaid()){
//			holder.notPaid.setVisibility(View.VISIBLE);
//		}
//		else{
//			holder.notPaid.setVisibility(View.GONE);
//		}

		holder.notPaid.setVisibility(View.GONE);
		holder.bookTime.setText(""+timeMap.get(Functions.TIME));
		PrettyTime p = new PrettyTime(new Locale("ru"));
		Calendar bookTS = Calendar.getInstance();
		bookTS.setTimeInMillis(item.getTs().toEpochMilli());
		holder.bookTS.setVisibility(View.VISIBLE);
		if(Functions.isToday(bookDateTime)) {
			holder.bookTS.setText(p.format(bookTS));
		}
		else{
			HashMap<String,String> timeTSMap = Functions.formatDateTime(item.getTs());
			holder.bookTS.setText(timeTSMap.get(Functions.DATE)+"\nв "+timeTSMap.get(Functions.TIME));
		}

		//For tomorrow or different date bookings
		if(!Functions.isToday(bookDateTime) && Instant.now().isBefore(bookDateTime)) {
			holder.bookTS.setText(""+timeMap.get(Functions.TIME));
			holder.bookTime.setText("Завтра");
		}
//		else if(!DateUtils.isToday(Functions.getUTCDate(item.getTs()).getTime()) && !DateUtils.isToday(Functions.getUTCDate(item.getTime()).getTime())){
////		   holder.bookTS.setVisibility(View.GONE);
//		   holder.bookTime.setText(timeMap.get(Functions.DATE)+"\nв "+timeMap.get(Functions.TIME));
//		}

		if (washer != null && washer.getBoxSettings() != null) {
			for (Box bs : washer.getBoxSettings()) {
				if (bs.getUid().equals(item.getBoxId())) {
					holder.txtBoxName.setVisibility(View.VISIBLE);
					holder.txtBoxName.setText(bs.getBoxName());
					break;
				}
			}
		}

		holder.bookStatus.setText("" + bookStatus.get(item.getStatus()));

		if(!item.isQueued()) {
			holder.onlineBook.setVisibility(View.VISIBLE);
			holder.remainingTimeLayout.setVisibility(View.VISIBLE);
			holder.layoutQueue.setVisibility(View.GONE);
			holder.bookStatus.setVisibility(View.VISIBLE);
			holder.bookKey.setText("" + item.getClientKey());

			if (!item.getStatus().equals(PENDING)) {
				holder.remainingTime.setVisibility(View.GONE);
			}

		}else{
			holder.onlineBook.setVisibility(View.GONE);
  			holder.layoutQueue.setVisibility(View.VISIBLE);
			holder.remainingTime.setVisibility(View.GONE);
			holder.remainingTimeLayout.setVisibility(View.GONE);

			holder.order.setText(""+item.getOrder());
			holder.queueCarNo.setText(""+item.getClientKey().replaceAll("\\(","").replaceAll("\\)",""));
			if(item.getClientKey().contains("("))
				holder.queueCarNo.setText(item.getClientKey().toUpperCase().substring(0,item.getClientKey().indexOf("(")));

			if(item.getStatus().equals(QUEUED_APPROVED)){
				holder.bookKey.setText("Автозавершение через ");
			}
			else if(item.getStatus().equals(QUEUED_REJECTED)){
				holder.bookKey.setText("Очередь отменен");
			}
			else if(item.getStatus().equals(QUEUED_FINISHED)){
				holder.bookKey.setText("Очередь завершен");
			}

		}

		holder.setBookInfo(item);

	}

	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

	public interface OnItemLongClickListener {
		public void onItemLongClick(View view, int position);
	}

	public void add(BookInfo book, int position) {
		position = position == -1 ? getItemCount() : position;
		mData.add(position, book);
		notifyItemInserted(position);
	}

	public void remove(int position) {
		if (position < getItemCount()) {
			mData.remove(position);
			notifyItemRemoved(position);
		}
	}

	public void refreshData(List<BookInfo> data){
		this.mData = data;
		notifyDataSetChanged();
	}

	public class BookInfoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

		BookInfo bookInfo;

		@BindView(R.id.txtBookTime)
		TextView bookTime;
		@BindView(R.id.txtBookTS)
		TextView bookTS;
		@BindView(R.id.txtBoxName)
		TextView txtBoxName;
		@BindView(R.id.txtBookKey)
		TextView bookKey;
		@BindView(R.id.txtBookStatus)
		TextView bookStatus;
		@BindView(R.id.parentView)
		View parentView;
		@BindView(R.id.remainingTimeLayout)
		View remainingTimeLayout;

		@BindView(R.id.remainingTime)
		TextView remainingTime;
		@BindView(R.id.layoutQueue)
		View layoutQueue;
		@BindView(R.id.order)
		TextView order;
		@BindView(R.id.queue_car_no)
		TextView queueCarNo;
		@BindView(R.id.imgAvailability)
		View onlineBook;
		@BindView(R.id.notPaid)
		View notPaid;

		public void updateTimeRemaining(long currentTime) {
			if(!bookInfo.isQueued()) {
				long timeDiff = currentTime - bookInfo.getTs().toEpochMilli();

				if (timeDiff > 0) {
					int seconds = (int) (timeDiff / 1000) % 60;
					int minutes = (int) ((timeDiff / (1000 * 60)) % 60);
					int hours = (int) ((timeDiff / (1000 * 60 * 60)) % 24);

					if ((TIMER_MINUTE - minutes) <= 0) {
						remainingTime.setText("00:00");
//					remainingTime.setVisibility(View.GONE);
					} else if (bookInfo.getStatus().equals(PENDING)) {
						remainingTime.setVisibility(View.VISIBLE);
						remainingTime.setText(String.format("%02d", (TIMER_MINUTE - minutes)) + ":" + String.format("%02d", (59 - seconds)));
					}
				}

				if (bookInfo.getStatus().equals(PENDING) || (bookInfo.getStatus().equals(Constants.APPROVED) && bookInfo.getServerTime().isAfter(bookInfo.getEndTime()))) {
					if (parentView.getTag() == null) {
							ObjectAnimator fadeOut = ObjectAnimator.ofFloat(parentView, "alpha", 1f, 0f);
							fadeOut.setDuration(700);
							ObjectAnimator fadeIn = ObjectAnimator.ofFloat(parentView, "alpha", 0f, 1f);
							fadeIn.setDuration(700);
							parentView.setHasTransientState(false);
							final AnimatorSet mAnimationSet = new AnimatorSet();

							mAnimationSet.play(fadeIn).after(fadeOut);
							mAnimationSet.addListener(new AnimatorListenerAdapter() {
								@Override
								public void onAnimationEnd(Animator animation) {
									super.onAnimationEnd(animation);
									if (bookInfo.getStatus().equals(PENDING) || (bookInfo.getStatus().equals(Constants.APPROVED) && bookInfo.getServerTime().isAfter(bookInfo.getEndTime()))) {
										mAnimationSet.start();
										parentView.setHasTransientState(true);
									} else {
										parentView.setHasTransientState(false);
									}
								}
							});
							parentView.setTag(mAnimationSet);
//							mAnimationSet.start();
						  }

						}
						else {
							parentView.setSelected(false);
						}
			}
			else{
				//Calculate remaining time until end time
				Instant endInstant = bookInfo.getEndTime();
				Duration remaining = Duration.between(Instant.ofEpochMilli(currentTime), endInstant);
				if (!remaining.isNegative()) {
					long totalSeconds = remaining.getSeconds();
					int seconds = (int) (totalSeconds % 60);
					int minutes = (int) ((totalSeconds / 60) % 60);
					int hours = (int) ((totalSeconds / 3600) % 24);
					minutes = hours*60 + minutes;
					if (bookInfo.getStatus().equals(QUEUED_APPROVED) //&& minutes > 0
							) {
						bookKey.setText("Автозавершение через "+String.format("%02d", (minutes)) + ":" + String.format("%02d", (seconds)));
						bookStatus.setText(Constants.bookStatus.get(QUEUED_APPROVED));
					}
					else{
						if(bookInfo.getStatus().equals(QUEUED_REJECTED)) bookKey.setText("Очередь отменен");
						else bookKey.setText("Очередь завершен");
					}
				}
				else{
					if(bookInfo.getStatus().equals(QUEUED_REJECTED)) bookKey.setText("Очередь отменен");
					else {
						bookKey.setText("Очередь завершен");
						bookStatus.setText(Constants.bookStatus.get(QUEUED_FINISHED));
					}
				}
			}
		}

		public void setBookInfo(BookInfo bookInfo){
			this.bookInfo = bookInfo;
		}

		public BookInfoViewHolder(View view) {
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



