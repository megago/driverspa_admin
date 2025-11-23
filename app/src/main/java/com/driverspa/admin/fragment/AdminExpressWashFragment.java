package com.driverspa.admin.fragment;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.PhotoParcelable;
import com.driverspa.model.Washer;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;


public class AdminExpressWashFragment extends AdminBaseFragment {

    public static final String EXTRA_WASHER_ID = "washer_id";
    public static final int ZOOM_LEVEL = 14;

	public interface ActivityActions{
	}

	private GoogleMap mMap;
	private Washer washer;

	@BindView(R.id.toggle_express)
	ToggleButton expressToggle;
	@BindView(R.id.recycler_linear)
	RecyclerView listView;
	@BindView(R.id.express_count)
    TextView expressCount;
	private LatLng latLng;

//	GPSTracker gps;
	TextView titleView;
	private ArrayList<PhotoParcelable> washerPhotos;
	Toolbar toolbar;
	Spinner spinner;
	ProgressBar actionBarProgress;
	AppBarLayout appBarLayout;
	View actionBarRefreshButton;
	boolean fragmentVisible;

	private ActivityActions activityActions;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_admin_express_wash, container,false);
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}

	    @Override
	    public void onViewCreated(View view, Bundle savedInstanceState) {
	        ButterKnife.bind(this, view);
//			gps = new GPSTracker(getActivity());
			toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
			appBarLayout = (AppBarLayout)getActivity().findViewById(R.id.appbar);
			spinner = (Spinner) toolbar.findViewById(R.id.spinner);
			actionBarProgress = (ProgressBar) toolbar.findViewById(R.id.progress);
			actionBarRefreshButton = toolbar.findViewById(R.id.refresh);
			titleView = (TextView) toolbar.findViewById(R.id.action_bar_title);
			titleView.setText("Экспресс мойка");

			if(fragmentVisible && actionBarRefreshButton != null)
				actionBarRefreshButton.setVisibility(View.GONE);
	    }

		/***** Sets up the map if it is possible to do so *****/
		public void setUpMapIfNeeded() {
//		    // Do a null check to confirm that we have not already instantiated the map.
//		    if (mMap == null) {
//		        // Try to obtain the map from the SupportMapFragment.
//				mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapThis)).getMap();
//		        // Check if we were successful in obtaining the map.
//		        if (mMap != null)
//		            setUpMap();
//		    }
		}

	    private void setUpMap() {
	        UiSettings ui = mMap.getUiSettings();
	        ui.setCompassEnabled(false);
	        ui.setZoomControlsEnabled(false);
	        ui.setMyLocationButtonEnabled(false);
	        ui.setAllGesturesEnabled(false);
	        mMap.setMyLocationEnabled(false);
//	        final LatLng myLL = new LatLng(55.754684, 37.623164);
//	        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLL, ZOOM_LEVEL));
//	        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//	            public void onMapClick(LatLng latLng) {
////	              activityActions.showMapMarker(washer);
//	            }
//	        });
//
//	        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//	            public boolean onMarkerClick(Marker marker) {
////	            	activityActions.showMapMarker(washer);
//	                return true;
//	            }
//	        });
			MarkerOptions marker = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_partner)));
			mMap.addMarker(marker);
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

		}

		@Override
		public void onDestroy() {
			super.onDestroy();
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

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {
			double lat  = washer.getLonLat().get(1);   //getIntent().getDoubleExtra(AddressMapActivity.LOC_LAT, AddressMapActivity.LAT);
			double lon = washer.getLonLat().get(0);//getIntent().getDoubleExtra(AddressMapActivity.LOC_LON, AddressMapActivity.LON);
			latLng = new LatLng(lat, lon);
			setUpMapIfNeeded();
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		fragmentVisible = isVisibleToUser;
		if(fragmentVisible){
			if(appBarLayout != null)
				appBarLayout.setExpanded(true, true);
			if(actionBarRefreshButton != null)
				actionBarRefreshButton.setVisibility(View.GONE);
			if(actionBarProgress != null)
				actionBarProgress.setVisibility(View.GONE);
			if(spinner != null)
				spinner.setVisibility(View.GONE);
			if(titleView != null) {
				titleView.setVisibility(View.VISIBLE);
				titleView.setText("Экспресс мойка");
			}
		}
	}

}
