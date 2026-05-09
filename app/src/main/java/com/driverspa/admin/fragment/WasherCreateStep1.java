package com.driverspa.admin.fragment;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import org.codepond.wizardroid.WizardStep;
import org.codepond.wizardroid.persistence.ContextVariable;

import java.util.ArrayList;
import java.util.stream.IntStream;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.BookingType;
import com.driverspa.model.Box;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.InitialWasherCreateRequest;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.WizardNextStepAllowEvent;
import com.driverspa.util.otto.WizardNextStepVerifyEvent;
import com.driverspa.util.otto.WizardPreviousStepVerifyEvent;
import com.driverspa.util.otto.ws.EditWasherRequestEvent;
import com.driverspa.util.otto.ws.NewCarwashCreateRequestEvent;
import com.driverspa.util.otto.ws.NewCarwashCreateResponseEvent;
import com.driverspa.util.otto.ws.OwnerWasherListGetRequestEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

/**
 * Created by Yerzhan Tanatov on 11/04/17.
 */
public class WasherCreateStep1 extends WizardStep implements OnMapReadyCallback {

    @ContextVariable
    private String washerCreateString;

    @BindView(R.id.companyName)
    EditText companyName;
    @BindView(R.id.streetName)
    EditText street;
    String city = UserPreferences.getCity(BA.getContext());
    private ProgressDialog pd = null;
    Toolbar mToolbar;
    private GoogleMap mMap;
    private LatLng latLng;
    Washer washer;
    TextView titleView;
    //Wire the layout to the step
    public WasherCreateStep1() {
    }

    private ArrayList<Box> createDefaultBoxSettings() {
        return IntStream.range(0, 1)
                .mapToObj(i -> {
                    Box boxSetting = new Box();
                    boxSetting.setBoxName("Бокс " + (i + 1));
                    boxSetting.setBookingType(BookingType.Online);
                    return boxSetting;
                })
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.washer_create_step1, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
        titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        ((TextView)(mToolbar.findViewById(R.id.action_done))).setText("Выйти");
        mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(washerCreateString)) {
                    getActivity().finish();
                }
            }
        });
        titleView.setText("Название и адрес");
    }

    /**
     * Called whenever the wizard proceeds to the next step or goes back to the previous step
     */

    /***** Sets up the map if it is possible to do so *****/
    public void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapThis)).getMapAsync(this);
            // Check if we were successful in obtaining the map.
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null)
            setUpMap();
    }

    private void setUpMap() {
        UiSettings ui = mMap.getUiSettings();
        ui.setCompassEnabled(false);
        ui.setZoomControlsEnabled(true);
        ui.setMyLocationButtonEnabled(true);
        ui.setAllGesturesEnabled(true);
        mMap.setMyLocationEnabled(true);
//       final LatLng myLL = new LatLng(55.754684, 37.623164);
//       mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLL, ZOOM_LEVEL));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                WasherCreateStep1.this.latLng = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory
                                .fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_partner))));
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });
        LatLng cityDefaultLatLng = Functions.getCityLatLng(city);
        if(latLng == null) latLng = cityDefaultLatLng;
        MarkerOptions marker = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_partner)));
        if(!(cityDefaultLatLng.latitude == latLng.latitude && cityDefaultLatLng.longitude == latLng.longitude))
            mMap.addMarker(marker);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
    }

    @Subscribe
    public void onNextStepVerifyRequested(WizardNextStepVerifyEvent event){
        if(event.getPosition()==0){
            if(TextUtils.isEmpty(washerCreateString) && (washer==null)){
                saveData(true);
            }
            else{
                saveData(false);
            }
        }
    }

    @Subscribe
    public void onPreviousStepVerifyRequested(WizardPreviousStepVerifyEvent event){
    }

    private void saveData(boolean sendData){
        String washerName = companyName.getText().toString();
        String addressStr = street.getText().toString();
        if(TextUtils.isEmpty(washerName)){
            companyName.setError("Обязательное поле");
            ToastUtil.display(getActivity(),"Введите название мойки");
            return;
        }

        if(TextUtils.isEmpty(addressStr)){
            street.setError("Обязательное поле");
            ToastUtil.display(getActivity(),"Введите адрес");
            return;
        }

        LatLng cityDefaultLatLng = Functions.getCityLatLng(city);
        if((cityDefaultLatLng.latitude == latLng.latitude && cityDefaultLatLng.longitude == latLng.longitude)) {
            ToastUtil.display(getActivity(), "Выберите на карте местоположение мойки");
//            L.d(cityDefaultLatLng.latitude+" lat "+ latLng.latitude);
//            L.d(cityDefaultLatLng.longitude+" lat "+latLng.longitude);
            return;
        }

        ArrayList<Double> lonlat = new ArrayList<Double>();
        lonlat.add(latLng.longitude);
        lonlat.add(latLng.latitude);

        InitialWasherCreateRequest request = new InitialWasherCreateRequest();
        request.setCity(UserPreferences.getCity(BA.getContext()));
        request.setLonLat(lonlat);
        request.setAddress(addressStr);
        request.setName(washerName);

        request.setBoxSettings(createDefaultBoxSettings());

        if(sendData) {
            setWaitScreen(true);
            BA.getEventBus().post(new NewCarwashCreateRequestEvent(request));
        }
        else if(washer != null){
            washer.setLonLat(lonlat);
            washer.setAddress(addressStr);
            washer.setName(washerName);
            BA.getEventBus().post(new WizardNextStepAllowEvent());
        }
    }

    @Subscribe
    public void onCarwashCreateResponseEvent(NewCarwashCreateResponseEvent event){
        setWaitScreen(false);
        if(event.getData() != null) {
            if (BaseAssist.isSuccess(event.getData())) {

                //Make this washer default washer and request for the new list of washers
                Washer washer = event.getData().getResponse().getWasher();
                washerCreateString = washer.getName();
                UserPreferences.putWasherData(BA.getContext(), washer.serialize());
//                BA.getEventBus().post(new WasherGetResponseEvent(washer));
                BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
                BA.getEventBus().post(new OwnerWasherListGetRequestEvent());

                //Go to next step
                BA.getEventBus().post(new WizardNextStepAllowEvent());
            } else {
                ToastUtil.display(getActivity(), event.getData().getMessage());
            }
        }
        else{
            ToastUtil.display(getActivity(), "Ошибка при создании мойки");
        }
    }

    boolean isVisible = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if(isVisible){
           if(mToolbar != null) {
//               ((TextView)(mToolbar.findViewById(R.id.action_done))).setText("Отложить");
               mToolbar.findViewById(R.id.action_done).setVisibility(View.VISIBLE);
            }
        }
        else {
            if(mToolbar != null) {
//                ((TextView) (mToolbar.findViewById(R.id.action_done))).setText("Отложить");
                mToolbar.findViewById(R.id.action_done).setVisibility(View.GONE);
            }
        }

        if(titleView!=null && isVisible) {
            titleView.setText("Название и адрес");
            mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BA.getEventBus().post(new EditWasherRequestEvent(washer.getId(), washer));
                    if(getActivity()!=null) getActivity().finish();
                }
            });
        }
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
       if(event.getWasher() != null && event.getWasher().getStatus().equals(Constants.PENDING) && isVisible) {
           washer = event.getWasher();
           if(!TextUtils.isEmpty(washer.getName()) && !washer.getName().equals("NA"))
               companyName.setText(washer.getName());
           street.setText(washer.getAddress());
           double lat  = washer.getLonLat().get(1);
           double lon = washer.getLonLat().get(0);
           latLng = new LatLng(lat, lon);
           setUpMapIfNeeded();
           if(mToolbar != null) {
               mToolbar.findViewById(R.id.action_done).setVisibility(View.GONE);
           }
       }
       else if(isVisible){ //By default should be called at initial time
           mToolbar.findViewById(R.id.action_done).setVisibility(View.VISIBLE);
           setUpMapIfNeeded();
       }
    }

    void setWaitScreen(boolean set) {
        if(pd == null) {
            pd = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pd.setTitle("");
            pd.setIndeterminate(true);
            pd.setMessage("Загрузка...");
        }

        if(set) pd.show();
        else  pd.dismiss();
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
}
