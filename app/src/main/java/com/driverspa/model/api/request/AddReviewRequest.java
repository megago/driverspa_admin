package com.driverspa.model.api.request;


public class AddReviewRequest {

	private String carwash;
	private Integer mark;
	private String text;
	
	public String getCarwash() {
		return carwash;
	}
	public void setCarwash(String carwash) {
		this.carwash = carwash;
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
