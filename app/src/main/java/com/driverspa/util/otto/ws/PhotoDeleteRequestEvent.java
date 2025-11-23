package com.driverspa.util.otto.ws;

public class PhotoDeleteRequestEvent {


	String photoId;

	public PhotoDeleteRequestEvent(String photoId) {
		this.photoId = photoId;
	}

	public String getPhotoId() {
		return photoId;
	}

	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}
}
