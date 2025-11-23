package com.driverspa.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Yerzhan Tanatov on 27/09/16.
 */
public class Statistic {

    String title;
    @SerializedName("statistics")
    List<StatisticDetail> statisticsList;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<StatisticDetail> getStatisticsList() {
        return statisticsList;
    }

    public void setStatisticsList(List<StatisticDetail> statisticsList) {
        this.statisticsList = statisticsList;
    }

    public static class StatisticDetail{
        @SerializedName("item_name")
        String itemName;
        @SerializedName("item_value")
        String itemValue;
        @SerializedName("item_value_string")
        String itemValueString;
        @SerializedName("manual_info")
        Boolean manualInfo;

        public StatisticDetail(String itemName, String itemValue) {
            this.itemName = itemName;
            this.itemValue = itemValue;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getItemValue() {
            return itemValue;
        }

        public void setItemValue(String itemValue) {
            this.itemValue = itemValue;
        }

        public String getItemValueString() {
            return itemValueString;
        }

        public void setItemValueString(String itemValueString) {
            this.itemValueString = itemValueString;
        }

        public Boolean getManualInfo() {
            return manualInfo;
        }

        public void setManualInfo(Boolean manualInfo) {
            this.manualInfo = manualInfo;
        }
    }


    // GSON
    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Statistic deserialize(String serializedData) {
        Gson gson = new Gson();
        return gson.fromJson(serializedData, Statistic.class);
    }

}
