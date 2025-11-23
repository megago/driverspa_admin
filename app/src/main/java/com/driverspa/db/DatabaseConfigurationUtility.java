package com.driverspa.db;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.IOException;
import java.sql.SQLException;

import com.driverspa.model.BookInfo;
import com.driverspa.model.CarItem;
import com.driverspa.model.PhotoOrm;
import com.driverspa.model.User;

public class DatabaseConfigurationUtility extends OrmLiteConfigUtil {
	
	private static final Class<?>[] classes = new Class<?>[] {
		User.class,
		PhotoOrm.class,
		CarItem.class,
		BookInfo.class
	};
	
	public static void main(String[] args) throws SQLException, IOException {
		writeConfigFile("ormlite_config.txt", classes);
	}
	
}
