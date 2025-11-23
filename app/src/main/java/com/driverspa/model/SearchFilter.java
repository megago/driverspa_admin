package com.driverspa.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Class for filtering search results
 */
public class SearchFilter implements Cloneable {

	public final static int DEFAULT_OFFSET = 100;
	private String offset;	
	private String searchText;
	private String status;
	private String day;
	private String washerId;
	private String rating;
	private String comforts;
	private String payments;
	private String free;
	private String wifi;
	private String cafe;
	private String coffee;
	private String maxPrice;
	private String city;
	private String mobile;

	private String lonLat;

	public SearchFilter () {
		setLimit("20");
		setMaxDistance("50000");
		setOffset("10000");
		setMaxPrice("3500");
		setRating("0");
	}

	public boolean isDefaultFilter(String limit, String distance, String offset, String price, String rating){
		 if(getLimit().equals(limit)
				 && getMaxDistance().equals(distance)
				 && getOffset().equals(offset)
				 &&	getMaxPrice().equals(price)
				 && getRating().equals(rating)
				 )
			 return true;
		return false;
	}

	public SearchFilter (boolean bookmarks) {
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(String maxPrice) {
		this.maxPrice = maxPrice;
	}

	public String getWifi() {
		return wifi;
	}

	public void setWifi(String wifi) {
		this.wifi = wifi;
	}

	public String getCafe() {
		return cafe;
	}

	public void setCafe(String cafe) {
		this.cafe = cafe;
	}

	public String getCoffee() {
		return coffee;
	}

	public void setCoffee(String coffee) {
		this.coffee = coffee;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getPayments() {
		return payments;
	}

	public void setPayments(String payments) {
		this.payments = payments;
	}

	public String getFree() {
		return free;
	}

	public void setFree(String free) {
		this.free = free;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	private String limit;
	
	@SerializedName("lonlat__max_distance")
	private String maxDistance;
	
	public String getLimit() {
		return limit;
	}


	public void setLimit(String limit) {
		this.limit = limit;
	}


	public String getMaxDistance() {
		return maxDistance;
	}


	public void setMaxDistance(String maxDistance) {
		this.maxDistance = maxDistance;
	}


	public String getOffset() {
		return offset;
	}


	public void setOffset(String offset) {
		this.offset = offset;
	}


	public String getSearchText() {
		return searchText;
	}


	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}


	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getLonLat() {
		return lonLat;
	}

	public void setLonLat(String lonLat) {
		this.lonLat = lonLat;
	}

	/**
	 * This map will be used to query search from server
	 * @return map - data to query
	 */
	public HashMap<String, String> toMap() {
		HashMap<String, String> result = new HashMap<String, String>();
		result.put("comforts","");
		if( offset != null )
			result.put("offset", offset);

		if( searchText != null )
			result.put("name__icontains", searchText);

		if( maxDistance != null )
			result.put("lonlat__max_distance", maxDistance);

		if( limit != null )
			result.put("limit", limit);

		if( status != null )
			result.put("status", status);

		if( maxPrice != null )
			result.put("max_price", maxPrice);

		if( day != null )
			result.put("day", day);
		
		if( washerId != null )
			result.put("carwash", washerId);

		if(!TextUtils.isEmpty(wifi))
			result.put("comforts", result.get("comforts").toString().contains("wifi")?result.get("comforts"):result.get("comforts")+"wifi");

		if(!TextUtils.isEmpty(cafe))
			result.put("comforts", result.get("comforts").toString().contains("cafe")?result.get("comforts"):result.get("comforts")+"cafe");

		if(!TextUtils.isEmpty(coffee))
			result.put("comforts", result.get("comforts").toString().contains("coffee")?result.get("comforts"):result.get("comforts")+"coffee");

		if(!TextUtils.isEmpty(payments) && payments.equals("noncash"))
			result.put("payments", "noncash");

		if(!TextUtils.isEmpty(free) && free.equals("true"))
			result.put("free","true");

		if(!TextUtils.isEmpty(rating) && !rating.equals("1"))
			result.put("rating__gte",rating);

		if(TextUtils.isEmpty(result.get("comforts"))){
			result.remove("comforts");
		}

		if(!TextUtils.isEmpty(city)){
			result.put("city", city);
		}
		if(!TextUtils.isEmpty(mobile)){
			result.put("mobile", mobile);
		}
		if(!TextUtils.isEmpty(lonLat)){
			result.put("lonlat__near", lonLat);
		}
		return result;
	}


	public String getWasherId() {
		return washerId;
	}


	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

	@Override
	public SearchFilter clone() {
		try {
			SearchFilter cloned = (SearchFilter) super.clone();
			cloned.setFree(cloned.getFree());
			cloned.setMaxDistance(cloned.getMaxDistance());
			cloned.setOffset(cloned.getOffset());
			cloned.setRating(cloned.getRating());
			cloned.setCafe(cloned.getCafe());
			cloned.setCoffee(cloned.getCoffee());
			cloned.setDay(cloned.getDay());
			cloned.setLimit(cloned.getLimit());
			cloned.setPayments(cloned.getPayments());
			cloned.setMaxPrice(cloned.getMaxPrice());
			cloned.setSearchText(cloned.getSearchText());
			cloned.setStatus(cloned.getStatus());
			cloned.setWasherId(cloned.getWasherId());
			cloned.setWifi(cloned.getWifi());
			return cloned;
		}
		catch (CloneNotSupportedException e){
		}
		return null;
	}
}
