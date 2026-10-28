package com.driverspa.model.api.response;

public class BaseResponseHolder {

	private String status;	
	public void setStatus(String status) {
		this.status = status;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	private String message;
	
	public String getStatus() {
		return status;
	}

	/**
	 * The server's message. The shift messages (docs/shift.md) come in English; they are shown in
	 * the app's language wherever a screen shows the message (payment, finish, shop, finance).
	 */
	public String getMessage() {
		try {
			String localized = com.driverspa.admin.shift.Shifts.localize(message);
			if (localized != null) return localized;
		} catch (RuntimeException ignored) {
			// no resources (e.g. a unit test): the message as it came
		}
		return message;
	}	
	
	@Override
	public String toString() {
		return "BaseResponseHolder [status=" + status + ", message="+message+"]";
	}
	
}
