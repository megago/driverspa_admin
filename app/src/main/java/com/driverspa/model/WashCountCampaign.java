package com.driverspa.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Yerzhan Tanatov on 31/01/17.
 */
public class WashCountCampaign {
    @SerializedName("uid")
    String id;

    @SerializedName("description")
    String description;

    @SerializedName("wash_count")
    Integer washCount;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getWashCount() {
        return washCount;
    }

    public void setWashCount(Integer washCount) {
        this.washCount = washCount;
    }

    // GSON
    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static WashCountCampaign deserialize(String serializedData) {
        Gson gson = new Gson();
        return gson.fromJson(serializedData, WashCountCampaign.class);
    }
}
