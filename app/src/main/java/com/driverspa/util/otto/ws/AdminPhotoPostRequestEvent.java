package com.driverspa.util.otto.ws;

import java.io.File;

public class AdminPhotoPostRequestEvent {

	private File file;
	String washerId;
	public AdminPhotoPostRequestEvent (File file, String washerId) {
		this.file = file;
		this.washerId = washerId;
	}

	public File getFile() {
		return file;
	}
	
	public String getWasherId(){
		return this.washerId;
	}
		
}
