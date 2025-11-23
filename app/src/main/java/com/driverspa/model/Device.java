package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Yerzhan Tanatov on 05/07/16.
 */
public class Device {
    String device;
    String os;
    String osVersion;
    String appVersion;
    @SerializedName("token_id")
    String tokenId;
    @SerializedName("push_type")
    String pushType;
    @SerializedName("carwash_id")
    String carwashId;
    @SerializedName("is_company")
    boolean isCompany;
    @SerializedName("is_staff")
    boolean isStaff;

    public Device(String device, String os,String osVersion,String appVersion, String tokenId, String pushType, String carwashId, boolean isCompany, boolean isStaff) {
        this.device = device;
        this.os = os;
        this.osVersion = osVersion;
        this.tokenId = tokenId;
        this.pushType = pushType;
        this.isCompany = isCompany;
        this.carwashId = carwashId;
        this.isStaff = isStaff;
        this.appVersion = appVersion;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getPushType() {
        return pushType;
    }

    public void setPushType(String pushType) {
        this.pushType = pushType;
    }
}
