package com.driverspa.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FareRequest {

	private String id;
	private String booking;
	@SerializedName("client_key")
	private String clientKey;
	private List<Integer> services;
	@SerializedName("cartype")
	private Integer carType;
	private String carNo;
	@SerializedName("carmark")
	private String carMark;
	private String mobile;
	private String comment;
	@SerializedName("lonlat")
	private ArrayList<Double> lonLat;
	@SerializedName("price")
	private Double fare;
	private String city;
	private String status;
	private String client;
	@SerializedName("client_name")
	private String clientName;
	@SerializedName("client_avatar")
	private String clientAvatar;
	private Date ts;
	private Date initialDate = new Date();

	List<BidObject> bids;
	List<BidObject> actgiveBids;
	@SerializedName("duration_arrival")
	Integer durationArrival;
	@SerializedName("bid")
	String acceptedBid;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBooking() {
		return booking;
	}

	public void setBooking(String booking) {
		this.booking = booking;
	}

	public String getClientKey() {
		return clientKey;
	}

	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}

	public List<Integer> getServices() {
		return services;
	}

	public void setServices(List<Integer> services) {
		this.services = services;
	}

	public Integer getCarType() {
		return carType;
	}

	public void setCarType(Integer carType) {
		this.carType = carType;
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

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public ArrayList<Double> getLonLat() {
		return lonLat;
	}

	public void setLonLat(ArrayList<Double> lonLat) {
		this.lonLat = lonLat;
	}

	public Double getFare() {
		return fare;
	}

	public void setFare(Double fare) {
		this.fare = fare;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Date getTs() {
		return ts;
	}

	public void setTs(Date ts) {
		this.ts = ts;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}


	public List<BidObject> getBids() {
		return bids;
	}

	public void setBids(List<BidObject> bids) {
		this.bids = bids;
	}

	public static class BidObject{
		@SerializedName("carwash_lonlat")
		List<Double> carwashLonLat;
		@SerializedName("carwash_name")
		String carwashName;
		String id;
		Double price;
		@SerializedName("carwash_rating")
		Double carwashRating;
		@SerializedName("carwash_address")
		String carwashAddress;

		long initialTime = System.currentTimeMillis();

		public List<Double> getCarwashLonLat() {
			return carwashLonLat;
		}

		public void setCarwashLonLat(List<Double> carwashLonLat) {
			this.carwashLonLat = carwashLonLat;
		}

		public String getCarwashName() {
			return carwashName;
		}

		public void setCarwashName(String carwashName) {
			this.carwashName = carwashName;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Double getPrice() {
			return price;
		}

		public void setPrice(Double price) {
			this.price = price;
		}

		public Double getCarwashRating() {
			return carwashRating;
		}

		public void setCarwashRating(Double carwashRating) {
			this.carwashRating = carwashRating;
		}

		public String getCarwashAddress() {
			return carwashAddress;
		}

		public void setCarwashAddress(String carwashAddress) {
			this.carwashAddress = carwashAddress;
		}

		public long getInitialTime() {
			return initialTime;
		}

		public void setInitialTime(long initialTime) {
			this.initialTime = initialTime;
		}
	}

	public List<BidObject> getActgiveBids() {
		return actgiveBids;
	}

	public void setActgiveBids(List<BidObject> actgiveBids) {
		this.actgiveBids = actgiveBids;
	}

	public Integer getDurationArrival() {
		return durationArrival;
	}

	public void setDurationArrival(Integer durationArrival) {
		this.durationArrival = durationArrival;
	}

	public String getAcceptedBid() {
		return acceptedBid;
	}

	public void setAcceptedBid(String acceptedBid) {
		this.acceptedBid = acceptedBid;
	}

	public String getClientAvatar() {
		return clientAvatar;
	}

	public void setClientAvatar(String clientAvatar) {
		this.clientAvatar = clientAvatar;
	}

	public Date getInitialDate() {
		return initialDate;
	}

	public void setInitialDate(Date initialDate) {
		this.initialDate = initialDate;
	}

	// GSON
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static FareRequest deserialize(String serializedData) {
		Gson gson = new Gson();
		return gson.fromJson(serializedData, FareRequest.class);
	}
}
