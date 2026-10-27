package com.driverspa.adapter;

/**
 * Created by Yerzhan Tanatov on 21/09/16.
 */

import static com.driverspa.util.Constants.EXTRA_BOOK;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.R;
import com.driverspa.activity.BaseActivity;
import com.driverspa.admin.activity.AdminAddQueuedBookActivity;
import com.driverspa.model.BookInfo;


public class AdminQueueListAdapter extends BaseDataAdapter<BookInfo> {

    Context context;
    Activity activity;
    public AdminQueueListAdapter(Context context) {
        super(context);
        this.context = context;
    }
    public AdminQueueListAdapter(Context context, Activity activity) {
        super(context);
        this.context = context;
        this.activity = activity;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        final BookInfo item = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_admin_queue_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.order.setText(item.getOrder()+"");
        holder.carNo.setText(item.getClientKey());
        if(item.getClientKey().contains("("))
         holder.carNo.setText(item.getClientKey().toUpperCase().substring(0,item.getClientKey().indexOf("(")));
        holder.liveOrder.setText((position+1)+"");
        final ViewHolder copyHolder= holder;
        holder.order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String transitionName = "title_transition";
                ActivityOptions transitionActivityOptions = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(activity, copyHolder.order, transitionName);
                }
                Intent intent = new Intent(context,AdminAddQueuedBookActivity.class);
                intent.putExtra(EXTRA_BOOK, item.serialize() );
                intent.putExtra(BaseActivity.OPENING_ANIMATION,false);
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                    context.startActivity(intent, transitionActivityOptions.toBundle());
//                }
//                else {
                    context.startActivity(intent);
//                }
            }
        });
        return view;
    }

    static class ViewHolder {
        @BindView(R.id.order)
        TextView order;
        @BindView(R.id.queue_car_no)
        TextView carNo;
        @BindView(R.id.queue_live_order)
        TextView liveOrder;

        public ViewHolder(View v){
            ButterKnife.bind(this,v);
        }
    }
}
