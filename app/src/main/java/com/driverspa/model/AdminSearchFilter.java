package com.driverspa.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Class for filtering search results
 */
public class AdminSearchFilter implements Cloneable {

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
	private String date;
	private String carNo;
	private String companyClient;
	private String carwash;
	private String clientType;
	private String bookId;
	private String clientKey;
	private String username;
	private String orderBy;
	private String startDate;
	private String endDate;

	private String startDatePure;
	private String endDatePure;

	private String operationType;
	private String id;

	public AdminSearchFilter() {
	}

	public String getCompanyClient() {
		return companyClient;
	}

	public void setCompanyClient(String companyClient) {
		this.companyClient = companyClient;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
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

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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


	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getClientKey() {
		return clientKey;
	}

	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}

	public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

	public String getCarwash() {
		return carwash;
	}

	public void setCarwash(String carwash) {
		this.carwash = carwash;
	}


	public static int getDefaultOffset() {
		return DEFAULT_OFFSET;
	}

	public String getCarNo() {
		return carNo;
	}

	public void setCarNo(String carNo) {
		this.carNo = carNo;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getStartDatePure() {
		return startDatePure;
	}

	public void setStartDatePure(String startDatePure) {
		this.startDatePure = startDatePure;
	}

	public String getEndDatePure() {
		return endDatePure;
	}

	public void setEndDatePure(String endDatePure) {
		this.endDatePure = endDatePure;
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
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

//		if( searchText != null )
//			result.put("name__icontains", searchText);
		if(!TextUtils.isEmpty(carNo))
			result.put("car_no", carNo);

		if(!TextUtils.isEmpty(operationType) && !operationType.contains(","))
			result.put("operation_type", operationType);
		else if(!TextUtils.isEmpty(operationType) && operationType.contains(",")){
			result.put("operation_type__in", operationType);
		}

		if(!TextUtils.isEmpty(companyClient))
			result.put("company_client", companyClient);

		if( maxDistance != null )
			result.put("lonlat__max_distance", maxDistance);

		if( startDate != null )
			result.put("tran_date__gte", startDate);

		if( endDate != null )
			result.put("tran_date__lte", endDate);

		if( limit != null )
			result.put("limit", limit);

		if( status != null )
			result.put("status", status);

		if( startDatePure != null )
			result.put("start_date", startDatePure);

		if( endDatePure != null )
			result.put("end_date", endDatePure);

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

		if(!TextUtils.isEmpty(date)){
			result.put("date", date);
		}

		if(!TextUtils.isEmpty(carwash)){
			result.put("carwash", carwash);
		}

		if(!TextUtils.isEmpty(bookId)){
			result.put("book_id", bookId);
		}

		if(!TextUtils.isEmpty(clientType)){
			result.put("type", clientType);
		}

		if(!TextUtils.isEmpty(searchText)){
			result.put("search", searchText);
		}

		if(!TextUtils.isEmpty(clientKey)){
			result.put("client_key", clientKey);
		}
		if(!TextUtils.isEmpty(username)){
			result.put("username", username);
		}

		if(!TextUtils.isEmpty(orderBy)){
			result.put("order_by", orderBy);
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
	public AdminSearchFilter clone() {
		try {
			AdminSearchFilter cloned = (AdminSearchFilter) super.clone();
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
