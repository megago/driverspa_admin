package com.driverspa.model;

import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Washer extends WasherPublic {

	public Washer() {
		super();
		setImages(new ArrayList<Image>());
//		setBoxSettings(new ArrayList<BoxSettings>());
		setPayOptions(new ArrayList<String>());
//        setReviews(new ArrayList<Review>());
	}

	private List<ServicePriceExpandable> servicePriceList;

	@SerializedName("images")
	private ArrayList<Image> images;
	private ArrayList<Review> reviews;

	@SerializedName("group_menu")
	private List<GroupMenu> groupMenu;
	@SerializedName("staffs")
	private List<Staff> staffs;
	@SerializedName("wash_count_campaigns")
	private List<WashCountCampaign> washCountCampaigns;

	@SerializedName("sms_for_queued_book")
	boolean smsForQueuedBook;

	@SerializedName("booking_available_after_time")
	Integer bookingAvailableAfterTime;

	@SerializedName("auto_accept_bookings")
	boolean autoAcceptBookings;

	@SerializedName("bonus_for_new_clients")
	Integer bonusForNewClients;

	@SerializedName("sms_by_carwash")
	boolean smsByCarwash;


	//	private AdditionalInfo additionalInfo;
	private Location washerLocation;

	@SerializedName("role_tran")
	RoleTran roleTransactions;

	public static class RoleTran {
		@SerializedName("director_role")
		ArrayList<Transactions> directorRole;
		@SerializedName("operator_role")
		ArrayList<Transactions> operatorRole;

		public ArrayList<Transactions> getDirectorRole() {
			return directorRole;
		}

		public void setDirectorRole(ArrayList<Transactions> directorRole) {
			this.directorRole = directorRole;
		}

		public ArrayList<Transactions> getOperatorRole() {
			return operatorRole;
		}

		public void setOperatorRole(ArrayList<Transactions> operatorRole) {
			this.operatorRole = operatorRole;
		}
	}

	public static class Transactions{
		@SerializedName("tran_cd")
		String transactionCode;
		boolean enabled;

		public Transactions(String transactionCode, boolean enabled) {
			this.transactionCode = transactionCode;
			this.enabled = enabled;
		}

		public String getTransactionCode() {
			return transactionCode;
		}

		public void setTransactionCode(String transactionCode) {
			this.transactionCode = transactionCode;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}

	public RoleTran getRoleTransactions() {
		return roleTransactions;
	}

	public void setRoleTransactions(RoleTran roleTransactions) {
		this.roleTransactions = roleTransactions;
	}

	public static class GroupMenu{
		@SerializedName("uid")
		String id;
		String name;
		List<Integer> services;
		List<ReversePrices> prices;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<Integer> getServices() {
			return services;
		}

		public void setServices(List<Integer> services) {
			this.services = services;
		}

		public List<ReversePrices> getPrices() {
			return prices;
		}

		public void setPrices(List<ReversePrices> prices) {
			this.prices = prices;
		}
	}

	public static class Prices {
		private Double price;
		private Integer time;
		private Integer type;

		public Double getPrice() {
			return price;
		}

		public void setPrice(Double price) {
			this.price = price;
		}

		public Integer getTime() {
			return time;
		}

		public void setTime(Integer time) {
			this.time = time;
		}

		public Integer getType() {
			return type;
		}

		public void setType(Integer type) {
			this.type = type;
		}
	}

	public static class AdditionalInfo {

		@SerializedName("carwash_automatic")
		private boolean carwashAutomatic;

		@SerializedName("carwash_cafe")
		private boolean carwashCafe;

		@SerializedName("carwash_chemical")
		private boolean carwashChemical;

		@SerializedName("carwash_hand")
		private boolean carwashHand;

		@SerializedName("carwash_truck")
		private boolean carwashTruck;

		@SerializedName("carwash_wifi")
		private boolean carwashWifi;

		@SerializedName("free_tea")
		private boolean freeTea;



		public boolean isFreeTea() {
			return freeTea;
		}

		public void setFreeTea(boolean freeTea) {
			this.freeTea = freeTea;
		}

		private String currency;

		public boolean isCarwashAutomatic() {
			return carwashAutomatic;
		}

		public void setCarwashAutomatic(boolean carwashAutomatic) {
			this.carwashAutomatic = carwashAutomatic;
		}

		public boolean isCarwashCafe() {
			return carwashCafe;
		}

		public void setCarwashCafe(boolean carwashCafe) {
			this.carwashCafe = carwashCafe;
		}

		public boolean isCarwashChemical() {
			return carwashChemical;
		}

		public void setCarwashChemical(boolean carwashChemical) {
			this.carwashChemical = carwashChemical;
		}

		public boolean isCarwashHand() {
			return carwashHand;
		}

		public void setCarwashHand(boolean carwashHand) {
			this.carwashHand = carwashHand;
		}

		public boolean isCarwashTruck() {
			return carwashTruck;
		}

		public void setCarwashTruck(boolean carwashTruck) {
			this.carwashTruck = carwashTruck;
		}

		public boolean isCarwashWifi() {
			return carwashWifi;
		}

		public void setCarwashWifi(boolean carwashWifi) {
			this.carwashWifi = carwashWifi;
		}

		public String getCurrency() {
			return currency;
		}

		public void setCurrency(String currency) {
			this.currency = currency;
		}
	}

	public static class Contact {

		private String alias;

		private String comment;

		private String type;

		private String value;

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class Schedule {

		String comment;

		@SerializedName("mon")
		ArrayList<Day> monday;

		@SerializedName("tue")
		ArrayList<Day> tuesday;

		@SerializedName("wed")
		ArrayList<Day> wednesday;

		@SerializedName("thu")
		ArrayList<Day> thursday;

		@SerializedName("fri")
		ArrayList<Day> friday;

		@SerializedName("sat")
		ArrayList<Day> saturday;

		@SerializedName("sun")
		ArrayList<Day> sunday;

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public ArrayList<Day> getMonday() {
			return monday;
		}

		public void setMonday(ArrayList<Day> monday) {
			this.monday = monday;
		}

		public ArrayList<Day> getTuesday() {
			return tuesday;
		}

		public void setTuesday(ArrayList<Day> tuesday) {
			this.tuesday = tuesday;
		}

		public ArrayList<Day> getWednesday() {
			return wednesday;
		}

		public void setWednesday(ArrayList<Day> wednesday) {
			this.wednesday = wednesday;
		}

		public ArrayList<Day> getThursday() {
			return thursday;
		}

		public void setThursday(ArrayList<Day> thursday) {
			this.thursday = thursday;
		}

		public ArrayList<Day> getFriday() {
			return friday;
		}

		public void setFriday(ArrayList<Day> friday) {
			this.friday = friday;
		}

		public ArrayList<Day> getSaturday() {
			return saturday;
		}

		public void setSaturday(ArrayList<Day> saturday) {
			this.saturday = saturday;
		}

		public ArrayList<Day> getSunday() {
			return sunday;
		}

		public void setSunday(ArrayList<Day> sunday) {
			this.sunday = sunday;
		}

		public ArrayList<Day> getToday() {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			
			switch (dayOfWeek) {
			case 2:
				return getMonday();
			case 3:
				return getTuesday();
			case 4:
				return getWednesday();
			case 5:
				return getThursday();
			case 6:
				return getFriday();
			case 7:
				return getSaturday();
			case 1:
				return getSunday();
			default:
				return null;
			}
		}

		public ArrayList<Day> getTomorrow() {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DAY_OF_YEAR, 1);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			switch (dayOfWeek) {
			case Calendar.MONDAY:
				return monday;
			case Calendar.TUESDAY:
				return tuesday;
			case Calendar.WEDNESDAY:
				return wednesday;
			case Calendar.THURSDAY:
				return thursday;
			case Calendar.FRIDAY:
				return friday;
			case Calendar.SATURDAY:
				return saturday;
			case Calendar.SUNDAY:
				return sunday;
			default:
				return null;
			}
		}
	}

	public static class Day {
		@SerializedName("fr")
		int from;
		int to;

		public int getFrom() {
			return from;
		}

		public void setFrom(int from) {
			this.from = from;
		}

		public int getTo() {
			return to;
		}

		public void setTo(int to) {
			this.to = to;
		}
	}

	public static class Review {
		String author;
		@SerializedName("author_id")
		String authorId;
		String id;
		Integer mark;
		String text;

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getAuthorId() {
			return authorId;
		}

		public void setAuthorId(String authorId) {
			this.authorId = authorId;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Integer getMark() {
			return mark;
		}

		public void setMark(Integer mark) {
			this.mark = mark;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}


	public ArrayList<Image> getImages() {
		return images;
	}

	public void setImages(ArrayList<Image> images) {
		this.images = images;
	}


	public Location getWasherLocation() {
		try {
			washerLocation.setLatitude(Double.parseDouble(getLonLat().get(1)
					.toString()));
			washerLocation.setLongitude(Double.parseDouble(getLonLat().get(0)
					.toString()));
		} catch (Exception e) {
		}
		return washerLocation;
	}

	public List<WashCountCampaign> getWashCountCampaigns() {
		return washCountCampaigns;
	}

	public void setWashCountCampaigns(List<WashCountCampaign> washCountCampaigns) {
		this.washCountCampaigns = washCountCampaigns;
	}

	public ArrayList<Review> getReviews() {
		return reviews;
	}

	public void setReviews(ArrayList<Review> reviews) {
		this.reviews = reviews;
	}

	public List<ServicePriceExpandable> getServicePriceList() {
		return servicePriceList;
	}

	public void setServicePriceList(List<ServicePriceExpandable> servicePriceList) {
		this.servicePriceList = servicePriceList;
	}

	public List<GroupMenu> getGroupMenu() {
		return groupMenu;
	}

	public void setGroupMenu(List<GroupMenu> groupMenu) {
		this.groupMenu = groupMenu;
	}

	public boolean isSmsForQueuedBook() {
		return smsForQueuedBook;
	}

	public void setSmsForQueuedBook(boolean smsForQueuedBook) {
		this.smsForQueuedBook = smsForQueuedBook;
	}

	public boolean isSmsByCarwash() {
		return smsByCarwash;
	}

	public void setSmsByCarwash(boolean smsByCarwash) {
		this.smsByCarwash = smsByCarwash;
	}


	public Integer getBookingAvailableAfterTime() {
		return bookingAvailableAfterTime;
	}

	public void setBookingAvailableAfterTime(Integer bookingAvailableAfterTime) {
		this.bookingAvailableAfterTime = bookingAvailableAfterTime;
	}

	public boolean isAutoAcceptBookings() {
		return autoAcceptBookings;
	}

	public void setAutoAcceptBookings(boolean autoAcceptBookings) {
		this.autoAcceptBookings = autoAcceptBookings;
	}

	public Integer getBonusForNewClients() {
		return bonusForNewClients;
	}

	public void setBonusForNewClients(Integer bonusForNewClients) {
		this.bonusForNewClients = bonusForNewClients;
	}

	public List<Staff> getStaffs() {
		return staffs;
	}

	public void setStaffs(List<Staff> staffs) {
		this.staffs = staffs;
	}

	// GSON
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static Washer deserialize(String serializedData) {
		Gson gson = new Gson();
		return gson.fromJson(serializedData, Washer.class);
	}

}
