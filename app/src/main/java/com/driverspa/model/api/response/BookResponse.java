package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.time.Instant;

public class BookResponse {

	private ArrayList<String> services;

	@SerializedName("client_key")
	private String clientKey;

	private String client;

	private String status;
	
	private String result;

	private String cartype;

	private String carwash;

	private String id;

	private Instant time;

	private String duration;

	private String price;

	private String end_time;

	@SerializedName("box_id")
	private String boxId;

	@SerializedName("resource_uri")
	private String resource_uri;

	@SerializedName("carwash_name")
	private String carwashName;

	@SerializedName("finish_time")
	Instant finishTime;
	@SerializedName("start_time")
	Instant startTime;

	public ArrayList<String> getServices() {
		return services;
	}

	public void setServices(ArrayList<String> services) {
		this.services = services;
	}

	public String getClient_key() {
		return clientKey;
	}

	public void setClient_key(String client_key) {
		this.clientKey = client_key;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Instant getFinishTime() {
		return finishTime;
	}

	public void setFinish_time(Instant finishTime) {
		this.finishTime = finishTime;
	}

	public String getCartype() {
		return cartype;
	}

	public void setCartype(String cartype) {
		this.cartype = cartype;
	}

	public String getCarwash() {
		return carwash;
	}

	public void setCarwash(String carwash) {
		this.carwash = carwash;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Instant getTime() {
		return time;
	}

	public void setTime(Instant time) {
		this.time = time;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getEnd_time() {
		return end_time;
	}

	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBox_id(String boxId) {
		this.boxId = boxId;
	}

	public String getResource_uri() {
		return resource_uri;
	}

	public void setResource_uri(String resource_uri) {
		this.resource_uri = resource_uri;
	}

	public Instant getStartTime() {
		return startTime;
	}

	public void setStart_time(Instant startTime) {
		this.startTime = startTime;
	}
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getClientKey() {
		return clientKey;
	}

	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getCarwashName() {
		return carwashName;
	}

	public void setCarwashName(String carwashName) {
		this.carwashName = carwashName;
	}

	public void setFinishTime(Instant finishTime) {
		this.finishTime = finishTime;
	}

	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}
}
