package com.driverspa.admin.fragment;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.driverspa.adapter.WasherAdminReviewAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherInfoRequestEvent;
import com.driverspa.util.otto.ws.WasherInfoResponseEvent;
import com.driverspa.view.DividerItemDecoration;
import com.driverspa.view.ProgressViewHolder;

public class AdminWasherReviewFragment extends AdminBaseFragment implements View.OnClickListener{


	    @BindView(R.id.noInternetLayout)
		LinearLayout noInternetLayout;	    
	    @BindView(R.id.progressBar)
		ProgressBar progressBar;
	    @BindView(R.id.repeatConnect)
		Button repeatConnect;
	    @BindView(R.id.noInternetMessage)
	    TextView noIternetMsg;
	    Location currentLoc;
	    private Washer washer;
	    @BindView(R.id.noRecords)
		View noRecords;
		Toolbar toolbar;
		boolean visible = false;

		@BindView(R.id.recycler_linear)
		RecyclerView mRecyclerLinear;
		private List<Washer.Review> reviewAdapterData;
		private WasherAdminReviewAdapter listAdapter;
		DividerItemDecoration itemDecoration;

		private boolean loading = false;
		private boolean noData = false;
		private int numberPage = 0;

		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	        return inflater.inflate(R.layout.fragment_admin_washer_review, container, false);
	    }
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}
		
	    @Override
	    public void onViewCreated(View view, Bundle savedInstanceState) {	       
	        ButterKnife.bind(this, view);
			toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
			TextView titleView = (TextView) toolbar.findViewById(R.id.action_bar_title);
			titleView.setText(BA.str(R.string.reviews));

			itemDecoration = new DividerItemDecoration(getActivity());
			makeList();
	        progressBar.setVisibility(View.GONE);
	        noInternetLayout.setVisibility(View.GONE);

	    }
						 
        @OnClick(R.id.repeatConnect)
	    public void onRepeatConnect(){
				if (Functions.checkOnline(getActivity())) {
				} else {
					noRecords.setVisibility(View.GONE);
					progressBar.setVisibility(View.GONE);
					noInternetLayout.setVisibility(View.VISIBLE);
			}
        }

	private void makeList() {
		reviewAdapterData = new ArrayList<Washer.Review>();
		listAdapter = new WasherAdminReviewAdapter(getActivity(), reviewAdapterData);
		listAdapter.setData(reviewAdapterData);
		listAdapter.notifyDataSetChanged();
		createListLayout();
	}

	private void createListLayout(){
		LinearLayoutManager manager = new LinearLayoutManager(getActivity());
		manager.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerLinear.setLayoutManager(manager);
		mRecyclerLinear.setAdapter(listAdapter);
		mRecyclerLinear.removeItemDecoration(itemDecoration);
		mRecyclerLinear.addItemDecoration(itemDecoration);
	}

	private void createScrollListener(final RecyclerView mRecyclerView, final LinearLayoutManager mLayoutManager, final WasherAdminReviewAdapter adapter) {
		mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				int totalItem = mLayoutManager.getItemCount();
				int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
				if (!noData && !loading && lastVisibleItem == totalItem - 1) {
					if (reviewAdapterData.size() > 0) {
						loading = true;
						View view = getActivity().getLayoutInflater().inflate(R.layout.progress_item, mRecyclerView, false);
						ProgressViewHolder holder = new ProgressViewHolder(view);
					}
				}
			}
		});
	}

	@Subscribe
		public void onWasherInfoReceived(WasherInfoResponseEvent event) {
		reviewAdapterData.clear();
			progressBar.setVisibility(View.GONE);
			if(event.getWasher() !=null && event.getWasher().getReviews() !=null && event.getWasher().getReviews().size() > 0){

				reviewAdapterData.addAll(event.getWasher().getReviews());
				Collections.sort(reviewAdapterData, new Comparator<Washer.Review>() {
					@Override
					public int compare(Washer.Review lhs, Washer.Review rhs) {
					  return rhs.getId().compareTo(lhs.getId());
					}
				});

				listAdapter.notifyItemInserted(event.getWasher().getReviews().size());
	        }
			else{
				noRecords.setVisibility(noInternetLayout.getVisibility() == View.VISIBLE?View.GONE:View.VISIBLE);
		    }
		}
		
		public void requestReviews(){
			progressBar.setVisibility(View.VISIBLE);
			noRecords.setVisibility(View.GONE);
			makeList();
			BA.getEventBus().post(new WasherInfoRequestEvent(washer.getId()));
		}
				
		public void requestReviewsWithoutLoader(){
			makeList();
			BA.getEventBus().post(new WasherInfoRequestEvent(washer.getId()));
		}

		/**
		 * Received location and request people nearby
		 */
		@Override
		public void onDestroy() {
			super.onDestroy();		
		}

		@Override
		public void onResume() {
			super.onResume();			
			BA.getEventBus().register(this);

			if (!Functions.checkOnline(getActivity())) {
				progressBar.setVisibility(View.GONE);
				noInternetLayout.setVisibility(View.VISIBLE);
				noRecords.setVisibility(View.GONE);
				if (listAdapter != null) {
					listAdapter.notifyDataSetChanged();
				}
			}
		}
		
		@Override
		public void onPause() {
			super.onPause();
			BA.getEventBus().unregister(this);
		}	

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);			
		}

		@Override
		public void onDetach() {
			super.onDetach();
		}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event){
		washer = event.getWasher();
		requestReviewsWithoutLoader();
	}

	@Override
	public void onClick(View v) {
		requestReviews();
	}
}



