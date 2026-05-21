package com.driverspa.adapter;

import static com.driverspa.util.Constants.APPROVED;
import static com.driverspa.util.Constants.PENDING;
import static com.driverspa.util.Constants.bookStatus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.model.BookInfo;
import com.driverspa.model.PushData;
import com.driverspa.util.Functions;

@SuppressLint({ "SimpleDateFormat", "ViewHolder", "NewApi" })
public class BooksAdapter extends BaseDataAdapter<BookInfo> {

	HashMap<String,PushData> bookingPushData;
	Context context;

	public BooksAdapter(Context context) {
		super(context);		
	}

	public BooksAdapter(Context context, HashMap<String,PushData> bookingPushData) {
		super(context);
		this.context = context;
		this.bookingPushData = bookingPushData;
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

		view = LayoutInflater.from(context).inflate(R.layout.list_books_item, parent, false);
		ViewHolder holder = new ViewHolder(view);
		view.setTag(holder);
		BookInfo item = getItem(position);
		HashMap<String,String> timeMap = Functions.formatDateTime(item.getTime());
		HashMap<String,String> timeTSMap = Functions.formatDateTime(item.getTs());

		if(item.getStatus().equals(PENDING) || item.getStatus().equals(APPROVED)) {
			holder.parentView.setBackgroundResource(R.drawable.background_client_booked_black);
			PushData data = new PushData(item.getId());
			data.setType("booking_"+item.getStatus());
			data.setText("");
			data.setObjectId(item.getId());
		}
		else {
			//make it transparent and remove this id from push data
			holder.parentView.setBackgroundColor(Color.TRANSPARENT);
		}

		holder.bookTime.setText(""+timeMap.get(Functions.TIME));
		PrettyTime p = new PrettyTime(new Locale("ru"));
		Calendar bookTs = Calendar.getInstance();
		bookTs.setTimeInMillis(item.getTs().toEpochMilli());
		holder.bookTS.setText(p.format(bookTs));

//		holder.bookTS.setText(timeTSMap.get(Functions.DATE)+"\n"+timeTSMap.get(Functions.TIME));
		holder.bookKey.setText(""+item.getClientKey());
		holder.bookStatus.setText(""+bookStatus.get(item.getStatus()));
		holder.txtWasherName.setText(item.getCarwashName());

		return view;
	}

	static class ViewHolder {

		@BindView(R.id.txtBookTime)
		TextView bookTime;
		@BindView(R.id.txtBookTS)
		TextView bookTS;
		@BindView(R.id.txtBookKey)
		TextView bookKey;
		@BindView(R.id.txtWasherName)
		TextView txtWasherName;
		@BindView(R.id.txtBookStatus)
		TextView bookStatus;
		@BindView(R.id.parentView)
		View parentView;

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
