package com.driverspa.util;

import android.content.Context;

import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpClient {

	protected static Picasso picasso;
	protected static HttpResponseCache responseCache;
	protected static OkHttpClient httpClient;

	public static void init(Context context, int cacheSizeMb) {
		
		// Trigger the download of the URL asynchronously into the image view.
		File cacheDir = context.getExternalCacheDir();
		if (cacheDir == null) {
		    // Fall back to using the internal cache directory
		    cacheDir = context.getCacheDir();
		}
		// Create a response cache using the cache directory and size restriction
		try {
			responseCache = new HttpResponseCache(cacheDir, cacheSizeMb * 1024 * 1024);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		httpClient = new OkHttpClient();				
		httpClient.setConnectTimeout(25, TimeUnit.SECONDS); // connect timeout
		httpClient.setReadTimeout(25, TimeUnit.SECONDS);    // socket timeout
		httpClient.setResponseCache(responseCache);
		
		picasso = new Picasso.Builder(context).downloader(new OkHttpDownloader(httpClient)).build();
	}

	public static Picasso getPicasso() {
		if (picasso != null) {
			return picasso;
		} else {
			throw new IllegalStateException("Picasso not initialized");
		}
	}
	
	public static void setPicassoDebugging( boolean bool ){
		if( picasso != null )
			picasso.setDebugging(bool);
	}
	
	public static OkHttpClient getHttpClient() {
		return httpClient;
	}
	
}
