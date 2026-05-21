package com.driverspa.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.driverspa.db.InstantPersister;
import com.driverspa.util.Converters;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

public class BookInfo {

	private ArrayList<Integer> services;

	@DatabaseField
	@SerializedName("client_key")
	private String clientKey;

	@DatabaseField
	private String mobile;

	@DatabaseField
	private String client;

	@DatabaseField
	private String status;

	@DatabaseField
	private String cartype;

	@SerializedName("group_services")
	private List<String> groupServices;

	@SerializedName("group_menu_name")
	private String groupMenuName;

	@SerializedName("group_menu_services")
	private List<Integer> groupMenuServices;

	@DatabaseField
	private String carwash;

	@DatabaseField(id = true)
	private String id;

	@DatabaseField(persisterClass = InstantPersister.class)
	private Instant time;

	@DatabaseField(persisterClass = InstantPersister.class)
	private Instant ts;

	@DatabaseField
	private String duration;

	@DatabaseField
	private String price;

	@DatabaseField(persisterClass = InstantPersister.class)
	@SerializedName("end_time")
	private Instant endTime;

	@DatabaseField(persisterClass = InstantPersister.class)
	@SerializedName("server_time")
	private Instant serverTime;

	@DatabaseField
	@SerializedName("carwash_name")
	private String carwashName;

	@DatabaseField
	@SerializedName("box_id")
	private String boxId;

	@DatabaseField
	@SerializedName("resource_uri")
	private String resourceUri;

	@DatabaseField(persisterClass = InstantPersister.class)
	@SerializedName("finish_time")
	Instant finishTime;

	@DatabaseField(persisterClass = InstantPersister.class)
	@SerializedName("start_time")
	Instant startTime;

	@DatabaseField
	@SerializedName("is_direct_wash")
	boolean directWash;

	@DatabaseField
	@SerializedName("is_offline")
	boolean offline;

	@DatabaseField
	@SerializedName("is_queued")
	boolean queued;

	@DatabaseField
	private Integer order;

	@SerializedName("carwash_detail")
	private Washer washer;

	@DatabaseField
	@SerializedName("carmark")
	String carMark;
	@DatabaseField
	String comment;
	@DatabaseField
	Integer discount;
	@DatabaseField(columnName = "discount_amount")
	@SerializedName("discount_amount")
	Integer discountAmount;
	@DatabaseField
	@SerializedName("is_paid")
	Boolean paid;
	@DatabaseField
	@SerializedName("is_nocash")
	Boolean nocash;
	@SerializedName("price_details")
	List<PriceDetail> priceDetails;
	List<String> staffs;

	@SerializedName("company_client_detail")
	ClientInfo companyClient;

	public List<String> getStaffs() {
		return staffs;
	}

	public void setStaffs(List<String> staffs) {
		this.staffs = staffs;
	}

	public String getClientKey() {
		return clientKey;
	}

	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}

	public Washer getWasher() {
		return washer;
	}

	public void setWasher(Washer washer) {
		this.washer = washer;
	}

	public void setFinishTime(Instant finishTime) {
		this.finishTime = finishTime;
	}

	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	public ArrayList<Integer> getServices() {
		return services;
	}

	public void setServices(ArrayList<Integer> services) {
		this.services = services;
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

	public Instant getEndTime() {
		return endTime;
	}

	public void setEndTime(Instant endTime) {
		this.endTime = endTime;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getResourceUri() {
		return resourceUri;
	}

	public void setResourceUri(String resourceUri) {
		this.resourceUri = resourceUri;
	}

	public Instant getStartTime() {
		return startTime;
	}

	public void setStart_time(Instant startTime) {
		this.startTime = startTime;
	}

	public String getCarwashName() {
		return carwashName;
	}

	public void setCarwashName(String carwashName) {
		this.carwashName = carwashName;
	}

	public Instant getTs() {
		return ts;
	}

	public void setTs(Instant ts) {
		this.ts = ts;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Instant getServerTime() {
		return serverTime;
	}

	public void setServerTime(Instant serverTime) {
		this.serverTime = serverTime;
	}

	public boolean isDirectWash() {
		return directWash;
	}

	public void setDirectWash(boolean directWash) {
		this.directWash = directWash;
	}

	public boolean isOffline() {
		return offline;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	public boolean isQueued() {
		return queued;
	}

	public void setQueued(boolean queued) {
		this.queued = queued;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public String getCarMark() {
		return carMark;
	}

	public void setCarMark(String carMark) {
		this.carMark = carMark;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer getDiscount() {
		return discount;
	}

	public void setDiscount(Integer discount) {
		this.discount = discount;
	}

	public Boolean getPaid() {
		return paid;
	}

	public void setPaid(Boolean paid) {
		this.paid = paid;
	}

	public List<String> getGroupServices() {
		return groupServices;
	}

	public void setGroupServices(List<String> groupServices) {
		this.groupServices = groupServices;
	}

	public String getGroupMenuName() {
		return groupMenuName;
	}

	public void setGroupMenuName(String groupMenuName) {
		this.groupMenuName = groupMenuName;
	}

	public List<Integer> getGroupMenuServices() {
		return groupMenuServices;
	}

	public void setGroupMenuServices(List<Integer> groupMenuServices) {
		this.groupMenuServices = groupMenuServices;
	}

	public Integer getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(Integer discountAmount) {
		this.discountAmount = discountAmount;
	}

	public Boolean getNocash() {
		return nocash;
	}

	public void setNocash(Boolean nocash) {
		this.nocash = nocash;
	}

	public List<PriceDetail> getPriceDetails() {
		return priceDetails;
	}

	public void setPriceDetails(List<PriceDetail> priceDetails) {
		this.priceDetails = priceDetails;
	}

	public static class PriceDetail{
		@SerializedName("payment_type")
		String paymentType;
		Double amount;

		public String getPaymentType() {
			return paymentType;
		}

		public void setPaymentType(String paymentType) {
			this.paymentType = paymentType;
		}

		public Double getAmount() {
			return amount;
		}

		public void setAmount(Double amount) {
			this.amount = amount;
		}
	}

	public ClientInfo getCompanyClient() {
		return companyClient;
	}

	public void setCompanyClient(ClientInfo companyClient) {
		this.companyClient = companyClient;
	}

	// GSON
	public String serialize() {
		Gson gson = Converters.gsonWithDate();
		return gson.toJson(this);
	}

	public static BookInfo deserialize(String serializedData) {
		Gson gson = Converters.gsonWithDate();
		return gson.fromJson(serializedData, BookInfo.class);
	}

}
