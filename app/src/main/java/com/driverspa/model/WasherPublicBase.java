package com.driverspa.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

import java.util.ArrayList;

import com.driverspa.model.Washer.Contact;

public abstract class WasherPublicBase extends WasherBase {

	public WasherPublicBase() {
		setContacts(new ArrayList<Contact>());
		setLonLat(new ArrayList<Double>());
		setCarTypes(new ArrayList<Integer>());
		setServices(new ArrayList<Integer>());
	}
			
	@DatabaseField(id = true)
	@SerializedName("id")
	protected String id; // readonly
	
	@DatabaseField
	private String city;
	
	@DatabaseField
    @SerializedName("is_working")
	private boolean working;

	@DatabaseField
    @SerializedName("is_bookable")
	private boolean bookable;


	@DatabaseField
	private Double price;

	@DatabaseField
	private Double rating;

	private String status;

	@SerializedName("carwash_tz")
	private String carwashTimeZone; // readonly

    @DatabaseField
    @SerializedName("resource_uri")
	private String resourceUri;
	
	public boolean isWorking() {
		return working;
	}

	public void setWorking(boolean working) {
		this.working = working;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public String getResourceUri() {
		return resourceUri;
	}

	public void setResourceUri(String resourceUri) {
		this.resourceUri = resourceUri;
	}
	
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public boolean isBookable() {
		return bookable;
	}

	public void setBookable(boolean bookable) {
		this.bookable = bookable;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCarwashTimeZone() {
		return carwashTimeZone;
	}

	public void setCarwashTimeZone(String carwashTimeZone) {
		this.carwashTimeZone = carwashTimeZone;
	}
}
