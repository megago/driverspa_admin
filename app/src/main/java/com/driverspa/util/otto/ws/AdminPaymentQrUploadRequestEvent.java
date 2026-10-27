package com.driverspa.util.otto.ws;

import java.io.File;

public class AdminPaymentQrUploadRequestEvent {

	private final File file;
	private final String mimeType;
	private final String washerId;

	public AdminPaymentQrUploadRequestEvent(File file, String mimeType, String washerId) {
		this.file = file;
		this.mimeType = mimeType;
		this.washerId = washerId;
	}

	public File getFile() {
		return file;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getWasherId() {
		return washerId;
	}
}
