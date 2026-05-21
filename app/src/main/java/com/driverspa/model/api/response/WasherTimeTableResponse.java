package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import com.driverspa.model.BookInfo;

public class WasherTimeTableResponse {

	Result result;
	@SerializedName("server_time")
	Instant serverTime;

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public static class Result {

		@SerializedName("booking_objects")
		private HashMap<String, BookInfo> bookingObjects;

		private HashMap<String, BoxItem> boxes;
		
		private Instant date;

		public HashMap<String, BoxItem> getBoxes() {
			return boxes;
		}

		public Instant getDate() {
			return date;
		}

		public void setDate(Instant date) {
			this.date = date;
		}

		public HashMap<String, BookInfo> getBookingObjects() {
			return bookingObjects;
		}

		public void setBookingObjects(HashMap<String, BookInfo> bookingObjects) {
			this.bookingObjects = bookingObjects;
		}
	}

	public static class BoxItem {
		List<String> slots;
		String uid;

		public List<String> getSlots() {
			return slots;
		}

		public void setSlots(List<String> slots) {
			this.slots = slots;
		}

		public String getUid() {
			return uid;
		}

		public void setUid(String uid) {
			this.uid = uid;
		}
	}

	public Instant getServerTime() {
		return serverTime;
	}

	public void setServerTime(Instant serverTime) {
		this.serverTime = serverTime;
	}
}
