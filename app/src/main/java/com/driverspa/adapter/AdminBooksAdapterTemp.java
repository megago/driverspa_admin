package com.driverspa.adapter;

import static com.driverspa.util.Constants.PENDING;
import static com.driverspa.util.Constants.bookStatus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

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
import com.driverspa.util.Functions;

@SuppressLint({ "SimpleDateFormat", "ViewHolder", "NewApi" })
public class AdminBooksAdapterTemp extends BaseDataAdapter<BookInfo> {
    public static final int TIMER_MINUTE = 14;
    HashMap<String,PushData> bookingPushData;
    Context context;
    Washer washer;

    private List<ViewHolder> lstHolders;
    private Handler mHandler = new Handler();
    private Runnable updateRemainingTimeRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (lstHolders) {
                long currentTime = System.currentTimeMillis();
                for (ViewHolder holder : lstHolders) {
                    holder.updateTimeRemaining(currentTime);
                }
            }
        }
    };

    public AdminBooksAdapterTemp(Context context, Washer washer, HashMap<String,PushData> bookingPushData) {
        super(context);
        this.context = context;
        this.bookingPushData = bookingPushData;
        this.washer = washer;
        lstHolders = new ArrayList<>();
        startUpdateTimer();
    }

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

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (isLoad && position == getCount() - 1) {
            LinearLayout ll = new LinearLayout(context);
            ll.setGravity(Gravity.CENTER_HORIZONTAL);
            ProgressBar bar = new ProgressBar(context);
            ll.addView(bar);
            return ll;
        }

        ViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_admin_books_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
            synchronized (lstHolders) {
                lstHolders.add(holder);
            }
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        BookInfo item = getItem(position);
            HashMap<String,String> timeMap = Functions.formatDateTime(item.getTime());
            HashMap<String,String> timeTSMap = Functions.formatDateTime(item.getTs());

        holder.bookTime.setText(""+timeMap.get(Functions.TIME));

        PrettyTime p = new PrettyTime(new Locale("ru"));
              Calendar bookTS = Calendar.getInstance();
              bookTS.setTimeInMillis(item.getTs().toEpochMilli());
              holder.bookTS.setText(p.format(bookTS));

//		holder.bookTS.setText(timeTSMap.get(Functions.DATE)+"\n"+timeTSMap.get(Functions.TIME));
        holder.txtBoxName.setVisibility(View.GONE);

        if(washer != null && washer.getBoxSettings() != null){
            for(Box bs : washer.getBoxSettings()){
                if(bs.getUid().equals(item.getBoxId())){
                    holder.txtBoxName.setVisibility(View.VISIBLE);
                    holder.txtBoxName.setText(bs.getBoxName());
                    break;
                }
            }
        }

        holder.bookKey.setText(""+item.getClientKey());
        holder.bookStatus.setText(""+bookStatus.get(item.getStatus()));

        if(bookingPushData != null &&bookingPushData.get(item.getId()) != null && item.getStatus().equals(PENDING))
            holder.parentView.setBackgroundResource(R.drawable.background_admin_booked_black);
        else {
            //make it transparent and remove this id from push data
            holder.parentView.setBackgroundColor(Color.TRANSPARENT);
//			BA.getEventBus().post(new RemoveBookingPushRequestEvent(new PushData(item.getId())));
        }
        if(!item.getStatus().equals(PENDING)){
            holder.remainingTime.setVisibility(View.GONE);
        }


        holder.setBookInfo(item);
        return view;
    }

    static class ViewHolder {

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
        @BindView(R.id.remainingTime)
        TextView remainingTime;

        public void updateTimeRemaining(long currentTime) {
            long timeDiff = currentTime - bookInfo.getTs().toEpochMilli();
            if (timeDiff > 0) {
                int seconds = (int) (timeDiff / 1000) % 60;
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);
                int hours = (int) ((timeDiff / (1000 * 60 * 60)) % 24);

                if((TIMER_MINUTE - minutes) <= 0) {
                    remainingTime.setText("00:00");
//					remainingTime.setVisibility(View.GONE);
                }
                else if(bookInfo.getStatus().equals(PENDING)){
                    remainingTime.setVisibility(View.VISIBLE);
                    remainingTime.setText(String.format("%02d", (TIMER_MINUTE - minutes)) + ":" + String.format("%02d", (59 - seconds)));
                }
            }
        }

        public void setBookInfo(BookInfo bookInfo){
            this.bookInfo = bookInfo;
        }
        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}


