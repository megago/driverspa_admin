package com.driverspa.admin.fragment;

import static com.driverspa.util.Constants.VIEW_FOOTER;

import android.app.Activity;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.AdminNotificationsAdapter;
import com.driverspa.model.AdminNotificationNews;
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.BookInfo;
import com.driverspa.model.LocalBookingList;
import com.driverspa.model.Washer;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.NotificationNewsListRequestEvent;
import com.driverspa.util.otto.ws.NotificationNewsListResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.DividerItemDecoration;
import com.driverspa.view.ProgressViewHolder;

public class AdminNotificationsFragment extends AdminBaseFragment {

    public interface ActivityActions {
        public void addNews();
    }

    private ActivityActions activityActions;
    private int numberPage = 0;
    private AdminNotificationsAdapter listAdapter;
    Washer washer;

    @BindView(R.id.recycler_linear)
    RecyclerView mRecyclerLinear;

    @BindView(R.id.noInternetLayout)
    LinearLayout noInternetLayout;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.repeatConnect)
    Button repeatConnect;
    @BindView(R.id.noInternetMessage)
    TextView noIternetMsg;
    @BindView(R.id.noRecords)
    TextView noRecords;
    @BindView(R.id.recordsCount)
    TextView recordsCount;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private boolean loading = false;
    private boolean noData = false;

    List<AdminNotificationNews> adminNotificationNewsList;
    private boolean lazyLoading = false;
    DividerItemDecoration itemDecoration;
    AdminSearchFilter filter = new AdminSearchFilter();
    boolean fragmentVisible = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_notifications, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        itemDecoration = new DividerItemDecoration(getActivity());
        makeList();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(0,false);
            }
        });
    }

    private void makeList() {
        adminNotificationNewsList = new ArrayList<AdminNotificationNews>();
        listAdapter = new AdminNotificationsAdapter(getActivity(),adminNotificationNewsList,washer);
        listAdapter.setData(adminNotificationNewsList);
        createListLayout();
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        loadData(0,false);
    }

    private void createListLayout(){
        ((AdminNotificationsAdapter) listAdapter).setOnItemClickListener(new AdminNotificationsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                final AdminNotificationNews news = (AdminNotificationNews)((adminNotificationNewsList.get(position)));
//                showOptions(client);
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerLinear.setLayoutManager(manager);
        mRecyclerLinear.setAdapter(listAdapter);
        mRecyclerLinear.removeItemDecoration(itemDecoration);
        mRecyclerLinear.addItemDecoration(itemDecoration);
        createScrollListener(mRecyclerLinear, manager, listAdapter);
    }

    private void createScrollListener(final RecyclerView mRecyclerView, final LinearLayoutManager mLayoutManager, final AdminNotificationsAdapter adapter) {
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItem = mLayoutManager.getItemCount();
                int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                if (!noData && !loading && lastVisibleItem == totalItem - 1) {
                    if (adminNotificationNewsList.size() > 0 && (numberPage+1)*20 == adminNotificationNewsList.size() ) {
                        View view = getActivity().getLayoutInflater().inflate(R.layout.progress_item, mRecyclerView, false);
                        ProgressViewHolder holder = new ProgressViewHolder(view);
                        adapter.addFooterView(VIEW_FOOTER, holder);
                        numberPage++;
                        loadData(numberPage,true);
                    }
                }
            }
        });
    }

    private void loadData(int numberPage, boolean lazyLoading) {
        if (!lazyLoading) {
            noData = false;
            recordsCount.setVisibility(View.GONE);
        }
        this.lazyLoading = lazyLoading;
        this.loading = true;
        this.numberPage = numberPage;
        filter.setOffset(Integer.toString(numberPage * 20));
        BA.getEventBus().post(new NotificationNewsListRequestEvent(filter));
    }

    @Subscribe
    public void onAdminNotificationNewsResponseReceived(NotificationNewsListResponseEvent event) {
        setWaitScreen(false);
        loading = false;
        swipeRefreshLayout.setRefreshing(false);
        if(washer!=null && listAdapter!=null) listAdapter.setWasher(washer);
        noRecords.setVisibility(View.GONE);
        listAdapter.removeFooterView(VIEW_FOOTER);

        if(event.getData().getResponse() != null){
            if(BaseAssist.isSuccess(event.getData())){
                noInternetLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);

                LocalBookingList localList = new LocalBookingList();
                List<BookInfo> localBooksList = new ArrayList<BookInfo>();

                if(!(event.getData().getResponse().getResult() != null && event.getData().getResponse().getResult().size() > 0)){
                    noData = true;
                    if(!lazyLoading) {
                        adminNotificationNewsList.clear();
                        listAdapter.notifyDataSetChanged();
                        noRecords.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    if(!lazyLoading){
//                       if(event.getResult().getResponse().getMeta() != null) {
//                           recordsCount.setVisibility(View.VISIBLE);
//                           recordsCount.setText(event.getResult().getResponse().getMeta().getTotalCount()+" клиент(ов)");
//                        }
                        adminNotificationNewsList.clear();
                        adminNotificationNewsList.addAll(event.getData().getResponse().getResult());
                        listAdapter.notifyDataSetChanged();

                    }
                    else{ //Lazy loading data insert
                        adminNotificationNewsList.addAll(event.getData().getResponse().getResult());
                        listAdapter.notifyDataSetChanged();
                        listAdapter.notifyDataSetChanged();
                    }

                }
            }
            else{
                if(!lazyLoading) {
                    noInternetLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    if(adminNotificationNewsList != null && adminNotificationNewsList.size() > 0) {
                        noRecords.setVisibility(View.GONE);
                    }
                    else
                        noRecords.setVisibility(View.VISIBLE);
                    ToastUtil.display(getActivity(), event.getData().getMessage());
                }
                else{
                    noInternetLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    if(adminNotificationNewsList != null && adminNotificationNewsList.size() > 0){
                        ToastUtil.display(getActivity(), event.getData().getMessage());
                    }
                }
            }
        }
        else{
            if(!lazyLoading) {
                noInternetLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                noRecords.setVisibility(View.GONE);
                if(adminNotificationNewsList != null && adminNotificationNewsList.size() > 0) {
                    noRecords.setVisibility(View.GONE);
                }
                else
                    noRecords.setVisibility(View.VISIBLE);

            }
            else{
                noInternetLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
            ToastUtil.display(getActivity(), BA.str(R.string.err_get_data));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BA.getEventBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BA.getEventBus().unregister(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activityActions = (ActivityActions) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activityActions = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        fragmentVisible = isVisibleToUser;
        if(fragmentVisible == false && filter != null && washer != null){
            filter.setSearchText("");
            loadData(0,false);
        }
    }

    @OnClick(R.id.addNews)
    public void addNewsButtonClicked(){
        activityActions.addNews();
    }
}
