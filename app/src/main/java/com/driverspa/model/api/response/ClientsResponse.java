package com.driverspa.model.api.response;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import com.driverspa.model.ClientInfo;

public class ClientsResponse {

	 @SerializedName("company_clients")
     List<ClientInfo> clients;

	@SerializedName("meta")
	private ListMetaDataResponse meta;

	public ListMetaDataResponse getMeta() {
		return meta;
	}

	public void setMeta(ListMetaDataResponse meta) {
		this.meta = meta;
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

	public static ClientsResponse deserialize(String serializedData) {
		Gson gson = new Gson();
		return gson.fromJson(serializedData, ClientsResponse.class);
	}
}
