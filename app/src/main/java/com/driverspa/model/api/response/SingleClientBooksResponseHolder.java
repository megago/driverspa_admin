package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import com.driverspa.model.BookInfo;

public class SingleClientBooksResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private List<BookInfo> books;

	public List<BookInfo> getBooks() {
		return books;
	}

	public void setBooks(List<BookInfo> books) {
		this.books = books;
	}
}
