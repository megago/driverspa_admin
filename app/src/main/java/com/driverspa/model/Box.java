package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by Yerzhan Tanatov on 15/04/17.
 */
public class Box {
    @SerializedName("booking_type")
    protected BookingType bookingType;
    protected String uid;
    @SerializedName("box_name")
    protected String boxName;
    protected Date ts;
    @SerializedName("washer_person")
    protected String washerPerson;
    @SerializedName("default_staffs")
    protected List<String> defualtStaffsId;

    @SerializedName("status_disabled")
    protected boolean statusDisabled;

    public boolean isStatusDisabled() {
        return statusDisabled;
    }

    public void setStatusDisabled(boolean statusDisabled) {
        this.statusDisabled = statusDisabled;
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public void setBookingType(BookingType bookingType) {
        this.bookingType = bookingType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBoxName() {
        return boxName;
    }

    public void setBoxName(String boxName) {
        this.boxName = boxName;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }

    public String getWasherPerson() {
        return washerPerson;
    }

    public void setWasherPerson(String washerPerson) {
        this.washerPerson = washerPerson;
    }

    public List<String> getDefualtStaffsId() {
        return defualtStaffsId;
    }

    public void setDefualtStaffsId(List<String> defualtStaffsId) {
        this.defualtStaffsId = defualtStaffsId;
    }
}