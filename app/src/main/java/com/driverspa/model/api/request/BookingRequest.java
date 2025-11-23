package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

import com.driverspa.model.BookInfo;

public class BookingRequest {
	
	public static final String NEW = "NEW";
	public static final String MODIFY = "MODIFY";
	
	private String requestType;
	private String id;
	private String bookId;

	@SerializedName("client_key")
	private String clientKey;
	private String carwash;
	@SerializedName("company_client")
	private String companyClient;
	private Date time;
	private List<Integer> services;
	private List<String> staffs;
	@SerializedName("group_services")
	private List<String> groupServices;
	@SerializedName("cartype")
	private Integer carType;		
	private String carNo;
	@SerializedName("carmark")
	private String carMark;		
	private String clientName;	
    private String servicePrice;
    private Integer serviceTotalTime;
    private Integer duration;
	@SerializedName("is_offline")
	private boolean offline;
	@SerializedName("is_queued")
	private boolean queued;
	@SerializedName("is_direct_wash")
	private boolean directWash;

	@SerializedName("box_id")
	private String boxId;
	private String mobile;

	private String comment;
	private Double discount;
	@SerializedName("discount_amount")
	private Double discountAmount;
	@SerializedName("is_paid")
	private Boolean paid;
	@SerializedName("is_nocash")
	private Boolean isNocash;
	@SerializedName("price_details")
	private List<BookInfo.PriceDetail> priceDetails;

	public BookingRequest(){
		requestType = NEW; //By default this is new request
	}
	public BookingRequest(String id){this.id = id;
	requestType = NEW; //By default this is new request
	}

	public List<String> getGroupServices() {
		return groupServices;
	}

	public void setGroupServices(List<String> groupServices) {
		this.groupServices = groupServices;
	}

	public List<Integer> getServices() {
		return services;
	}

	public void setServices(List<Integer> services) {
		this.services = services;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getCarwash() {
		return carwash;
	}

	public void setCarwash(String carwash) {
		this.carwash = carwash;
	}
	
	public Integer getCarType() {
		return carType;
	}

	public void setCarType(Integer carType) {
		this.carType = carType;
	}

	public String getClientKey() {
		return clientKey;
	}

	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}
	
	public String getCarNo() {
		return carNo;
	}

	public void setCarNo(String carNo) {
		this.carNo = carNo;
	}

	public String getCarMark() {
		return carMark;
	}

	public void setCarMark(String carMark) {
		this.carMark = carMark;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	
	public boolean isEditRequest(){
		return requestType.equals(MODIFY);
	}
	
	public String getBookId() {
		return bookId;
	}
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public String getServicePrice() {
		return servicePrice;
	}

	public void setServicePrice(String servicePrice) {
		this.servicePrice = servicePrice;
	}

	public Integer getServiceTotalTime() {
		return serviceTotalTime;
	}

	public void setServiceTotalTime(Integer serviceTotalTime) {
		this.serviceTotalTime = serviceTotalTime;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public boolean isOffline() {
		return offline;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public boolean isDirectWash() {
		return directWash;
	}

	public void setDirectWash(boolean directWash) {
		this.directWash = directWash;
	}

	public boolean isQueued() {
		return queued;
	}

	public void setQueued(boolean queued) {
		this.queued = queued;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Double getDiscount() {
		return discount;
	}

	public void setDiscount(Double discount) {
		this.discount = discount;
	}

	public Double getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(Double discountAmount) {
		this.discountAmount = discountAmount;
	}

	public Boolean getNocash() {
		return isNocash;
	}

	public void setNocash(Boolean nocash) {
		isNocash = nocash;
	}

	public Boolean getPaid() {
		return paid;
	}

	public void setPaid(Boolean paid) {
		this.paid = paid;
	}

	public List<BookInfo.PriceDetail> getPriceDetails() {
		return priceDetails;
	}

	public void setPriceDetails(List<BookInfo.PriceDetail> priceDetails) {
		this.priceDetails = priceDetails;
	}

	public List<String> getStaffs() {
		return staffs;
	}

	public void setStaffs(List<String> staffs) {
		this.staffs = staffs;
	}

	public String getCompanyClient() {
		return companyClient;
	}

	public void setCompanyClient(String companyClient) {
		this.companyClient = companyClient;
	}
}
