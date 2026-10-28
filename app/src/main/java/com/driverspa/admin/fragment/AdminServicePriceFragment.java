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
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
            final LinkedHashMap<Integer, ArrayList<Washer.Prices>> carTypeMenuAll = washer.getMenu() != null
                    ? washer.getMenu()
                    : new LinkedHashMap<Integer, ArrayList<Washer.Prices>>();

            servicePriceExpandableList = allServices.entrySet().stream()
                    .map(entry -> {
                        Integer serviceId = entry.getKey();
                        String serviceName = entry.getValue();

                        ServicePriceExpandable servicePriceExpandable = new ServicePriceExpandable();
                        servicePriceExpandable.setServiceId(serviceId);
                        servicePriceExpandable.setServiceName(serviceName);

                        List<ReversePrices> reversePrices = carTypeMenuAll.entrySet().stream()
                                .flatMap(menu -> {
                                    Integer carType = menu.getKey();
                                    ArrayList<Washer.Prices> prices = menu.getValue();
                                    if (prices == null || prices.isEmpty()
                                            || (carType != null && carType == com.driverspa.Reference.OTHER_SERVICES_CAR_TYPE)) {
                                        return java.util.stream.Stream.empty();
                                    }
                                    return prices.stream()
                                            .filter(price -> serviceId.equals(price.getType()))
                                            .map(price -> toReversePrice(carType, allCarTypes.get(carType), price));
                                })
                                .collect(Collectors.toList());

                        servicePriceExpandable.setPricesWithCarType(reversePrices);
                        return servicePriceExpandable;
                    })
                    .collect(Collectors.toList());

            // Configured services float to the top; within each group, original
            // (natural) order is preserved because List.sort is stable.
            servicePriceExpandableList.sort((a, b) -> {
                boolean aConfigured = a.getPricesWithCarType() != null && !a.getPricesWithCarType().isEmpty();
                boolean bConfigured = b.getPricesWithCarType() != null && !b.getPricesWithCarType().isEmpty();
                if (aConfigured == bConfigured) return 0;
                return aConfigured ? -1 : 1;
            });

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
            final Set<Integer> uniqueCarTypes = servicePriceExpandableList.stream()
                    .filter(s -> s.getPricesWithCarType() != null)
                    .flatMap(s -> s.getPricesWithCarType().stream())
                    .map(ReversePrices::getCarType)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            final ArrayList<Integer> requestCarTypes = new ArrayList<Integer>();
            final ArrayList<Integer> requestServices = servicePriceExpandableList.stream()
                    .filter(s -> s.getPricesWithCarType() != null && s.getPricesWithCarType().size() > 0)
                    .map(ServicePriceExpandable::getServiceId)
                    .collect(Collectors.toCollection(ArrayList::new));

            //Add group car types
            if(washer != null && washer.getGroupMenu() != null && washer.getGroupMenu().size() > 0){
                washer.getGroupMenu().stream()
                        .filter(Objects::nonNull)
                        .flatMap(g -> g.getPrices() == null ? java.util.stream.Stream.empty() : g.getPrices().stream())
                        .map(ReversePrices::getCarType)
                        .forEach(uniqueCarTypes::add);
            }

            LinkedHashMap<Integer, ArrayList<Washer.Prices>> carTypeMenuAll = new LinkedHashMap<Integer, ArrayList<Washer.Prices>>();
            //Create Menu for cartypes and services
            uniqueCarTypes.forEach(carType -> {
                requestCarTypes.add(carType);
                ArrayList<Washer.Prices> prices = servicePriceExpandableList.stream()
                        .filter(s -> s.getPricesWithCarType() != null)
                        .flatMap(s -> s.getPricesWithCarType().stream()
                                .filter(p -> carType.equals(p.getCarType()))
                                .map(p -> toWasherPrice(p, s.getServiceId())))
                        .collect(Collectors.toCollection(ArrayList::new));
                if (!prices.isEmpty()) {
                    carTypeMenuAll.put(carType, prices);
                }
            });
            washer.setCarTypes(new ArrayList<Integer>(requestCarTypes));
            washer.setServices(new ArrayList<Integer>(requestServices));
            washer.setMenu(carTypeMenuAll);
        }
        else {
            Set<Integer> uniqueCarTypes = new LinkedHashSet<Integer>();
            //Add group car types
            if (washer != null && washer.getGroupMenu() != null && washer.getGroupMenu().size() > 0) {
                washer.getGroupMenu().stream()
                        .filter(Objects::nonNull)
                        .flatMap(g -> g.getPrices() == null ? java.util.stream.Stream.empty() : g.getPrices().stream())
                        .map(ReversePrices::getCarType)
                        .forEach(uniqueCarTypes::add);
                final ArrayList<Integer> requestCarTypes = new ArrayList<Integer>(uniqueCarTypes);


                washer.setCarTypes(new ArrayList<Integer>(requestCarTypes));
            }
        }
    }

    private ReversePrices toReversePrice(Integer carType, String carTypeName, Washer.Prices price) {
        ReversePrices reversePrice = new ReversePrices();
        reversePrice.setCarType(carType);
        reversePrice.setCarTypeName(carTypeName);
        reversePrice.setPrice(price.getPrice());
        reversePrice.setTime(price.getTime());
        return reversePrice;
    }

    private Washer.Prices toWasherPrice(ReversePrices reversePrice, Integer serviceId) {
        Washer.Prices washerPrice = new Washer.Prices();
        washerPrice.setPrice(reversePrice.getPrice());
        washerPrice.setTime(reversePrice.getTime());
        washerPrice.setType(serviceId);
        return washerPrice;
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
