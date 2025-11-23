package com.driverspa.model;

import java.util.List;

/**
 * Created by Yerzhan Tanatov on 23/11/16.
 */
public class ServicePriceExpandable {
    private Integer serviceId;
    private String serviceName;
    private List<ReversePrices> pricesWithCarType;

    public ServicePriceExpandable(List<ReversePrices> pricesWithCarType) {

        this.pricesWithCarType = pricesWithCarType;
    }

    public ServicePriceExpandable(){

    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<ReversePrices> getPricesWithCarType() {
        return pricesWithCarType;
    }

    public void setPricesWithCarType(List<ReversePrices> pricesWithCarType) {
        this.pricesWithCarType = pricesWithCarType;
    }

}
