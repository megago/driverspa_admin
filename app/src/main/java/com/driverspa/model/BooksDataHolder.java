package com.driverspa.model;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Yerzhan Tanatov on 07/02/17.
 */
public class BooksDataHolder {
    List<BookInfo> books;

    public BooksDataHolder(List<BookInfo> books) {
        this.books = books;
    }

    public List<BookInfo> getBooks() {
        return books;
    }

    public void setBooks(List<BookInfo> books) {
        this.books = books;
    }

    // GSON
    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static BooksDataHolder deserialize(String serializedData) {
        Gson gson = new Gson();
        return gson.fromJson(serializedData, BooksDataHolder.class);
    }
}
