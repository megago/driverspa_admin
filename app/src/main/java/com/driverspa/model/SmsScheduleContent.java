package com.driverspa.model;

import com.google.gson.Gson;

import java.util.List;

import com.driverspa.model.api.request.AdminNotificationNewsRequest;

/**
 * Created by Yerzhan Tanatov on 06/02/17.
 */
public class SmsScheduleContent {
    AdminNotificationNewsRequest news;
    List<ClientInfo> clients;

    public SmsScheduleContent(AdminNotificationNewsRequest news, List<ClientInfo> clients) {
        this.news = news;
        this.clients = clients;
    }

    public AdminNotificationNewsRequest getNews() {
        return news;
    }

    public void setNews(AdminNotificationNewsRequest news) {
        this.news = news;
    }

    public List<ClientInfo> getClients() {
        return clients;
    }

    public void setClients(List<ClientInfo> clients) {
        this.clients = clients;
    }

    // GSON
    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static SmsScheduleContent deserialize(String serializedData) {
        Gson gson = new Gson();
        return gson.fromJson(serializedData, SmsScheduleContent.class);
    }
}
