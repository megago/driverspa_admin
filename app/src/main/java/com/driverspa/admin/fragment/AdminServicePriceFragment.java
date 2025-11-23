package com.driverspa.admin.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.ServicePriceExpandableListAdapter;
import com.driverspa.admin.activity.AdminServicePriceDetailActivity;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.ServicePriceExpandable;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.otto.WizardNextStepVerifyEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

public class AdminServicePriceFragment extends AdminBaseFragment {

    public interface ActivityActions {
    }

    private ActivityActions activityActions;

    @BindView(R.id.expandable_list_view)
    ExpandableListView listView;
    Washer washer;
    ServicePriceExpandableListAdapter adapter;
    List<ServicePriceExpandable> servicePriceExpandableList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_service_price, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        adapter = new ServicePriceExpandableListAdapter(getActivity());
        servicePriceExpandableList = new ArrayList<ServicePriceExpandable>();
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                parent.smoothScrollToPosition(groupPosition);
                if (servicePriceExpandableList.get(groupPosition).getPricesWithCarType() != null && servicePriceExpandableList.get(groupPosition).getPricesWithCarType().size() > 0) {
                    if (parent.isGroupExpanded(groupPosition)) {
                        parent.collapseGroup(groupPosition);
                    } else {
                        parent.expandGroup(groupPosition);
                    }
                } else {
                    Intent intent = new Intent(getActivity(), AdminServicePriceDetailActivity.class);
                    intent.putExtra(Constants.SERVICE, servicePriceExpandableList.get(groupPosition).getServiceId());
                    startActivity(intent);
                    overridePendingTransitionWithCommoOpeningTransition(getActivity());
                }
                return true;
            }
        });

    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if (servicePriceExpandableList.size() == 0 && washer != null) {
            servicePriceExpandableList = new ArrayList<ServicePriceExpandable>();
            //Set all services
            final HashMap<Integer, String> allServices = BA.getReference().getServices();
            final HashMap<Integer, String> allCarTypes = BA.getReference().getCarType();
            for (Map.Entry<Integer, String> entry : allServices.entrySet()) {
                HashMap<String, String> serviceMap = new HashMap<String, String>();
                Integer serviceId = entry.getKey();
                String serviceName = entry.getValue();

                //expandable model
                ServicePriceExpandable servicePriceExpandable = new ServicePriceExpandable();
                servicePriceExpandable.setServiceId(serviceId);
                servicePriceExpandable.setServiceName(serviceName);

                //Check menu object
                LinkedHashMap<Integer, ArrayList<Washer.Prices>> carTypeMenuAll = new LinkedHashMap<Integer, ArrayList<Washer.Prices>>();
                carTypeMenuAll = washer.getMenu();
                List<ReversePrices> reversePrices = new ArrayList<ReversePrices>();

                //Iterating menu to get reversed menu object(fill expandable model)
                if (carTypeMenuAll.size() > 0) { // If menu has values
                    for (Map.Entry<Integer, ArrayList<Washer.Prices>> menu : carTypeMenuAll.entrySet()) {
                        Integer carType = menu.getKey();
                        ArrayList<Washer.Prices> prices = menu.getValue();

                        //check service id, then fill price and time for cartype
                        if (prices != null && prices.size() > 0) {
                            for (Washer.Prices price : prices) {
                                if (serviceId == price.getType()) {
                                    ReversePrices reversePrice = new ReversePrices();
                                    reversePrice.setCarType(carType);
                                    reversePrice.setCarTypeName(allCarTypes.get(carType));
                                    reversePrice.setPrice(price.getPrice());
                                    reversePrice.setTime(price.getTime());
                                    reversePrices.add(reversePrice);
                                }
                            }
                        }
                    }
                }

                servicePriceExpandable.setPricesWithCarType(reversePrices);

                servicePriceExpandableList.add(servicePriceExpandable);
            }

//            try {
//                //Sort services
//                Collections.sort(servicePriceExpandableList, new Comparator<ServicePriceExpandable>() {
//                    @Override
//                    public int compare(ServicePriceExpandable lhs, ServicePriceExpandable rhs) {
//                        if (lhs.getServiceId() < rhs.getServiceId() ||
//                                (
//                                        lhs.getPricesWithCarType() != null && lhs.getPricesWithCarType().size() > 0
//                                                &&
//                                                !(rhs.getPricesWithCarType() != null && rhs.getPricesWithCarType().size() > 0)
//                                )
//                                )
//                            return -1;
//                        else
//                            return 1;
//                    }
//                });
//            }
//            catch(Exception e){}

            //create adapter
            adapter = new ServicePriceExpandableListAdapter(getActivity(), servicePriceExpandableList);

            washer.setServicePriceList(servicePriceExpandableList);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

////            for(int i=0; i < adapter.getGroupCount(); i++)
//            if(adapter!=null && adapter.getGroupCount() > 0)
//                  listView.expandGroup(0);

        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void saveData() {
        getActivity().finish();
        overridePendingTransitionWithCommonCloseTransition();
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
        if (washer != null) {
            reverseReversedServices(servicePriceExpandableList);
            BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
        }
    }

    @Subscribe
    public void onNextStepVerifyRequested(WizardNextStepVerifyEvent event){

        //Go to next step
        if(event.getPosition() == 2){
            if (washer != null) {
                reverseReversedServices(servicePriceExpandableList);
                BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
            }
        }
    }

    private void reverseReversedServices(List<ServicePriceExpandable> servicePriceExpandableList) {
        if (servicePriceExpandableList != null && servicePriceExpandableList.size() > 0) {
            LinkedHashMap<Integer, Integer> uniqueCarTypes = new LinkedHashMap<Integer, Integer>();
            final ArrayList<Integer> requestCarTypes = new ArrayList<Integer>();
            final ArrayList<Integer> requestServices = new ArrayList<Integer>();

            //Full scan -> find car types
            for (ServicePriceExpandable s : servicePriceExpandableList) {
                //Generating unique se
                if (s.getPricesWithCarType() != null && s.getPricesWithCarType().size() > 0)
                    requestServices.add(s.getServiceId());
                for (ReversePrices p : s.getPricesWithCarType()) {
                    uniqueCarTypes.put(p.getCarType(), p.getCarType());
                }
            }

            //Add group car types
            if(washer != null && washer.getGroupMenu() != null && washer.getGroupMenu().size() > 0){
                for(Washer.GroupMenu g : washer.getGroupMenu()){
                    for(ReversePrices p: g.getPrices()){
                        uniqueCarTypes.put(p.getCarType(), p.getCarType());
//                        L.d("setting cartypes "+p.getCarType());
                    }
                }
            }

            LinkedHashMap<Integer, ArrayList<Washer.Prices>> carTypeMenuAll = new LinkedHashMap<Integer, ArrayList<Washer.Prices>>();
            //Create Menu for cartypes and services
            for (Map.Entry<Integer, Integer> entry : uniqueCarTypes.entrySet()) {
                Integer carType = entry.getKey();
                requestCarTypes.add(carType);
                ArrayList<Washer.Prices> prices = new ArrayList<Washer.Prices>();
                boolean found = false;
                for (ServicePriceExpandable s : servicePriceExpandableList) {
                    for (ReversePrices p : s.getPricesWithCarType()) {
                        if (p.getCarType() == carType) {
                            found = true;
                            Washer.Prices price = new Washer.Prices();
                            price.setPrice(p.getPrice());
                            price.setTime(p.getTime());
                            price.setType(s.getServiceId());
                            prices.add(price);
                        }
                    }
                }
                if(found)
                   carTypeMenuAll.put(carType, prices);
            }
            washer.setCarTypes(new ArrayList<Integer>(requestCarTypes));
            washer.setServices(new ArrayList<Integer>(requestServices));
            washer.setMenu(carTypeMenuAll);
        }
        else {
            LinkedHashMap<Integer, Integer> uniqueCarTypes = new LinkedHashMap<Integer, Integer>();
            //Add group car types
            if (washer != null && washer.getGroupMenu() != null && washer.getGroupMenu().size() > 0) {
                for (Washer.GroupMenu g : washer.getGroupMenu()) {
                    for (ReversePrices p : g.getPrices()) {
                        uniqueCarTypes.put(p.getCarType(), p.getCarType());
                    }
                }
                final ArrayList<Integer> requestCarTypes = new ArrayList<Integer>();
                for (Map.Entry<Integer, Integer> entry : uniqueCarTypes.entrySet()) {
                    Integer carType = entry.getKey();
                    requestCarTypes.add(carType);
//                    L.d("setting cartypes "+carType);
                }


                washer.setCarTypes(new ArrayList<Integer>(requestCarTypes));
            }
        }
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
}
