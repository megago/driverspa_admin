package com.driverspa.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.BookInfo;
import com.driverspa.model.BooksDataHolder;
import com.driverspa.model.Box;
import com.driverspa.model.CarItem;
import com.driverspa.model.PhotoOrm;
import com.driverspa.model.Staff;
import com.driverspa.model.Statistic;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.UserGetSelfRequestEvent;

/**
 * @author Yerzhan
 *
 */

public class WashmeOrmLiteSqlHelper extends OrmLiteSqliteOpenHelper {

	DecimalFormat decimalFormatter = new DecimalFormat("#,###.##");
	private static final String DATABASE_NAME = "com.driverspa.db";
	private static final int DATABASE_VERSION = 3;
	
	private Dao<User, String> userDao = null;
	private RuntimeExceptionDao<User, String> userRuntimeDao;

	private Dao<PhotoOrm, String> userPhotoDao = null;
	private RuntimeExceptionDao<PhotoOrm, String> userPhotoRuntimeDao;

	private Dao<CarItem, String> userCarsDao = null;
	private RuntimeExceptionDao<CarItem, String> userCarsRuntimeDao;

	private Dao<BookInfo, String> bookingDao = null;
	private RuntimeExceptionDao<BookInfo, String> bookingRuntimeDao;

	public WashmeOrmLiteSqlHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, User.class);
			TableUtils.createTable(connectionSource, PhotoOrm.class);
			TableUtils.createTable(connectionSource, CarItem.class);
			TableUtils.createTable(connectionSource, BookInfo.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			BA.getContext().deleteDatabase(DATABASE_NAME);
	        onCreate(database);
			BA.getEventBus().post(new UserGetSelfRequestEvent(UserPreferences.getUserId(BA.getContext())));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Dao<User, String> getUserDao() throws SQLException {
		if( userDao == null)
			userDao = getDao(User.class);
		return userDao;
	}

	public RuntimeExceptionDao<User, String> getUserRuntimeExceptionDao() {
		if( userRuntimeDao == null)
			userRuntimeDao = getRuntimeExceptionDao(User.class);
		return userRuntimeDao;
	}
	
	public Dao<PhotoOrm, String> getUserPhotoDao() throws SQLException {
		if( userPhotoDao == null)
			userPhotoDao = getDao(PhotoOrm.class);
		return userPhotoDao;
	}

	public RuntimeExceptionDao<PhotoOrm, String> getUserPhotoRuntimeExceptionDao() {
		if( userPhotoRuntimeDao == null)
			userPhotoRuntimeDao = getRuntimeExceptionDao(PhotoOrm.class);
		return userPhotoRuntimeDao;
	}

	public Dao<CarItem, String> getUserCarsDao() throws SQLException {
		if( userCarsDao == null)
			userCarsDao = getDao(CarItem.class);
		return userCarsDao;
	}

	public RuntimeExceptionDao<CarItem, String> getUserCarsRuntimeExceptionDao() {
		if( userCarsRuntimeDao == null)
			userCarsRuntimeDao = getRuntimeExceptionDao(CarItem.class);
		return userCarsRuntimeDao;
	}

	public Dao<BookInfo, String> getBookingDao() throws SQLException {
		if( bookingDao == null)
			bookingDao = getDao(BookInfo.class);
		return bookingDao;
	}

	public RuntimeExceptionDao<BookInfo, String> getBookingRuntimeDao() {
		if( bookingRuntimeDao == null)
			bookingRuntimeDao = getRuntimeExceptionDao(BookInfo.class);
		return bookingRuntimeDao;
	}

	public void removeAll()
	{
		// db.delete(String tableName, String whereClause, String[] whereArgs);
		// If whereClause is null, it will delete all rows.
		SQLiteDatabase db = this.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
		db.delete("User", null, null);
		db.delete("PhotoOrm", null, null);
		db.delete("CarItem", null, null);
		db.delete("BookInfo", null, null);
	}

	public void removePhotos()
	{
		// db.delete(String tableName, String whereClause, String[] whereArgs);
		// If whereClause is null, it will delete all rows.
		SQLiteDatabase db = this.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
		db.delete("PhotoOrm", null, null);
	}

	public void deleteAllBooksData()
	{
		// db.delete(String tableName, String whereClause, String[] whereArgs);
		// If whereClause is null, it will delete all rows.
		SQLiteDatabase db = this.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
		db.delete("BookInfo", null, null);
	}

	public List<Statistic> getStatisticData(Washer washer, Date date){
			List<Statistic> statistics = new ArrayList<Statistic>();

		try {
			// Total counts and for cartypes
			if (washer != null && washer.getCarTypes() != null && washer.getCarTypes().size() > 0) {

				final HashMap<Integer, String> allCarTypes = BA.getReference().getCarType();
				final HashMap<Integer, String> allServices = BA.getReference().getServices();
				Statistic statistic = new Statistic();
				List<Statistic.StatisticDetail> statisticDetails = new ArrayList<Statistic.StatisticDetail>();
				statistic.setTitle("Общие показатели");

				//Total count of booked and queued items
				Cursor c = null;
				c = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished')", null);

				if (c != null && c.getCount() != 0) {
					for (int i = 0; i < c.getCount(); i++) {
						c.moveToPosition(i);
						Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail("Количество машин", c.getString(c.getColumnIndex("cnt")));
						statisticDetails.add(statisticDetail);
					}
				}

				if (washer != null && washer.getCarTypes() != null && washer.getCarTypes().size() > 0) {
					//Booked items for each carType
					for (Integer carType : washer.getCarTypes()) {
						c = null;
						c = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished') and cartype=" + carType, null);

						if (c != null && c.getCount() != 0) {
							for (int i = 0; i < c.getCount(); i++) {
								c.moveToPosition(i);
								Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail(allCarTypes.get(carType), c.getString(c.getColumnIndex("cnt")));
								statisticDetails.add(statisticDetail);
							}
						}
					}
				}

				//Total sum of books
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT sum(CAST(price AS INTEGER) + CAST(discount_amount AS INTEGER)) as sum_without, sum(CAST(price AS INTEGER)) as sum_with, " +
						"sum(CAST( (case when nocash=1 then price else 0 end) AS INTEGER)) nocash," +
						"sum(CAST( (case when nocash=0 then price else 0 end) AS INTEGER)) cash" +
						" FROM bookinfo where status in ('finished','queued_finished')", null);

				if (c != null && c.getCount() != 0) {
					for (int i = 0; i < c.getCount(); i++) {
						c.moveToPosition(i);
						Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail("Общая сумма", "Cо скидкой "+(TextUtils.isEmpty(c.getString(c.getColumnIndex("sum_with"))) ? "0" : decimalFormatter.format(c.getLong(c.getColumnIndex("sum_with")))) + " ₸ "+
								" \nБез скидок "+(TextUtils.isEmpty(c.getString(c.getColumnIndex("sum_without"))) ? "0" : decimalFormatter.format(c.getLong(c.getColumnIndex("sum_without")))) + " ₸");
//								" \nНалич. "+(TextUtils.isEmpty(c.getString(c.getColumnIndex("cash"))) ? "0" : c.getString(c.getColumnIndex("cash"))) + " ₸"+
//								" \nБез нал. "+(TextUtils.isEmpty(c.getString(c.getColumnIndex("nocash"))) ? "0" : c.getString(c.getColumnIndex("nocash"))) + " ₸");
						statisticDetails.add(statisticDetail);
					}
				}

				//Total sum of books
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT sum(CAST(discount_amount AS INTEGER)) as sum FROM bookinfo where status in ('finished','queued_finished')", null);

				if (c != null && c.getCount() != 0) {
					for (int i = 0; i < c.getCount(); i++) {
						c.moveToPosition(i);
						Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail("Скидки", (TextUtils.isEmpty(c.getString(c.getColumnIndex("sum"))) ? "0" : decimalFormatter.format(c.getLong(c.getColumnIndex("sum")))) + " ₸");
						statisticDetails.add(statisticDetail);
					}
				}

				statistic.setStatisticsList(statisticDetails);
				statistics.add(statistic);


				statistic = new Statistic();
				statisticDetails = new ArrayList<Statistic.StatisticDetail>();
				statistic.setTitle("Живая очередь");
				//Total count of booked items
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished') and queued=1", null);

				if (c != null && c.getCount() != 0) {
					for (int i = 0; i < c.getCount(); i++) {
						c.moveToPosition(i);
						Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail("Количество машин", c.getString(c.getColumnIndex("cnt")));
						statisticDetails.add(statisticDetail);
					}
				}

				//Booked items for each carType
				if (washer != null && washer.getCarTypes() != null && washer.getCarTypes().size() > 0) {
					for (Integer carType : washer.getCarTypes()) {
						c = null;
						c = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished') and queued=1 and cartype=" + carType, null);

						if (c != null && c.getCount() != 0) {
							for (int i = 0; i < c.getCount(); i++) {
								c.moveToPosition(i);
								Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail(allCarTypes.get(carType), c.getString(c.getColumnIndex("cnt")));
								statisticDetails.add(statisticDetail);
							}
						}
					}
				}

				//Total sum of books
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT sum(CAST(price AS INTEGER) + CAST(discount_amount AS INTEGER)) as sum_without, sum(CAST(price AS INTEGER)) as sum_with, " +
						"sum(CAST( (case when nocash=1 then price else 0 end) AS INTEGER)) nocash," +
						"sum(CAST( (case when nocash=0 then price else 0 end) AS INTEGER)) cash " +
						"FROM bookinfo where status in ('finished','queued_finished') and queued=1", null);

				if (c != null && c.getCount() != 0) {
					for (int i = 0; i < c.getCount(); i++) {
						c.moveToPosition(i);
						Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail("Общая сумма", "Cо скидкой "+(TextUtils.isEmpty(c.getString(c.getColumnIndex("sum_with"))) ? "0" : decimalFormatter.format(c.getLong(c.getColumnIndex("sum_with")))) + " ₸ "+
								" \nБез скидок "+(TextUtils.isEmpty(c.getString(c.getColumnIndex("sum_without"))) ? "0" : decimalFormatter.format(c.getLong(c.getColumnIndex("sum_without")))) + " ₸");
//								+" \nНалич. "+(TextUtils.isEmpty(c.getString(c.getColumnIndex("cash"))) ? "0" : c.getString(c.getColumnIndex("cash"))) + " ₸"+
//								" \nБез нал. "+(TextUtils.isEmpty(c.getString(c.getColumnIndex("nocash"))) ? "0" : c.getString(c.getColumnIndex("nocash"))) + " ₸");
						statisticDetails.add(statisticDetail);
					}
				}

				statistic.setStatisticsList(statisticDetails);
				statistics.add(statistic);


				statistic = new Statistic();
				statisticDetails = new ArrayList<Statistic.StatisticDetail>();
				statistic.setTitle("Брони");
				//Total count of booked items
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished') and queued=0", null);

				if (c != null && c.getCount() != 0) {
					for (int i = 0; i < c.getCount(); i++) {
						c.moveToPosition(i);
						Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail("Количество машин", c.getString(c.getColumnIndex("cnt")));
						statisticDetails.add(statisticDetail);
					}
				}

				//Booked items for each carType
				if (washer != null && washer.getCarTypes() != null && washer.getCarTypes().size() > 0) {
					for (Integer carType : washer.getCarTypes()) {
						c = null;
						c = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished') and queued=0 and cartype=" + carType, null);

						if (c != null && c.getCount() != 0) {
							for (int i = 0; i < c.getCount(); i++) {
								c.moveToPosition(i);
								Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail(allCarTypes.get(carType), c.getString(c.getColumnIndex("cnt")));
								statisticDetails.add(statisticDetail);
							}
						}
					}
				}

				//Total sum of books
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT sum(CAST(price AS INTEGER) + CAST(discount_amount AS INTEGER)) as sum_without, sum(CAST(price AS INTEGER)) as sum_with," +
						"sum(CAST( (case when nocash=1 then price else 0 end) AS INTEGER)) nocash," +
						"sum(CAST( (case when nocash=0 then price else 0 end) AS INTEGER)) cash " +
						" FROM bookinfo where status in ('finished','queued_finished') and queued=0", null);

				if (c != null && c.getCount() != 0) {
					for (int i = 0; i < c.getCount(); i++) {
						c.moveToPosition(i);
						Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail("Общая сумма", "Cо скидкой "+(TextUtils.isEmpty(c.getString(c.getColumnIndex("sum_with"))) ? "0" : decimalFormatter.format(c.getLong(c.getColumnIndex("sum_with")))) + " ₸ "+
								" \nБез скидок "+(TextUtils.isEmpty(c.getString(c.getColumnIndex("sum_without"))) ? "0" : decimalFormatter.format(c.getLong(c.getColumnIndex("sum_without")))) + " ₸");
//								" \nНалич. "+(TextUtils.isEmpty(c.getString(c.getColumnIndex("cash"))) ? "0" : c.getString(c.getColumnIndex("cash"))) + " ₸"+
//								" \nБез нал. "+(TextUtils.isEmpty(c.getString(c.getColumnIndex("nocash"))) ? "0" : c.getString(c.getColumnIndex("nocash"))) + " ₸");
						statisticDetails.add(statisticDetail);
					}
				}
				statistic.setStatisticsList(statisticDetails);
				statistics.add(statistic);


				statistic = new Statistic();
				statisticDetails = new ArrayList<Statistic.StatisticDetail>();
				statistic.setTitle("По боксам");

				//Total count of booked items
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished')", null);

				if (washer != null && washer.getBoxSettings() != null && washer.getBoxSettings().size() > 0) {
					for (Box bs : washer.getBoxSettings()) {
						if(!bs.isStatusDisabled()) {
							c = null;
							c = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt,sum(CAST(price AS INTEGER) + CAST(discount_amount AS INTEGER)) as sum_without, sum(CAST(price AS INTEGER)) as sum_with" +
									" FROM bookinfo where status in ('finished','queued_finished') and boxId='" + bs.getUid() + "'", null);

							if (c != null && c.getCount() != 0) {
								for (int i = 0; i < c.getCount(); i++) {
									c.moveToPosition(i);
									Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail(Functions.getBoxName(washer, bs.getUid())
											+ "(" + bs.getBookingType() + ")" + (TextUtils.isEmpty(bs.getWasherPerson()) ? "" : " - " + bs.getWasherPerson())
											, "Количество " + c.getString(c.getColumnIndex("cnt"))
											+ "\nсо скидкой " + (TextUtils.isEmpty(c.getString(c.getColumnIndex("sum_with"))) ? "0" : decimalFormatter.format(c.getLong(c.getColumnIndex("sum_with")))) + " ₸"
											+ "\nбез скидок " + (TextUtils.isEmpty(c.getString(c.getColumnIndex("sum_without"))) ? "0" : decimalFormatter.format(c.getLong(c.getColumnIndex("sum_without")))) + " ₸"
									);
									statisticDetails.add(statisticDetail);
								}
							}
						}
					}
				}
				statistic.setStatisticsList(statisticDetails);
				statistics.add(statistic);

				//Ugly temp solution
				if (washer != null && washer.getServices() != null && washer.getServices().size() > 0 && washer.getMenu() != null && washer.getMenu().size() > 0) {
					try {
						BooksDataHolder holder = BooksDataHolder.deserialize(UserPreferences.getBooksData(BA.getContext()));
						List<BookInfo> books = holder.getBooks();

						if(books.size() < 500) { //Limit max 100
							statistic = new Statistic();
							statisticDetails = new ArrayList<Statistic.StatisticDetail>();
							statistic.setTitle("По сотрудникам");
							for(Staff staff : washer.getStaffs()){
								long count = 0, sum = 0;
								for(BookInfo book : books){
									if(book.getStaffs().contains(staff.getUid()) && (book.getStatus().equals(Constants.FINISHED) ||(book.getStatus().equals(Constants.QUEUED_FINISHED)))){
										count++;
										try {
											sum += Double.parseDouble(book.getPrice());
										}
										catch(Exception e){}
									}
								}
								Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail(staff.getFirstName(),"Количество: "+count+" \nна сумму "+decimalFormatter.format(sum)+" ₸");
								statisticDetails.add(statisticDetail);
							}
						}
						statistic.setStatisticsList(statisticDetails);
						statistics.add(statistic);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}

				//Ugly temp solution
				if (washer != null && washer.getServices() != null && washer.getServices().size() > 0 && washer.getMenu() != null && washer.getMenu().size() > 0) {
					try {
						BooksDataHolder holder = BooksDataHolder.deserialize(UserPreferences.getBooksData(BA.getContext()));
						List<BookInfo> books = holder.getBooks();

						if(books.size() < 500) { //Limit max 100
							statistic = new Statistic();
							statisticDetails = new ArrayList<Statistic.StatisticDetail>();
							statistic.setTitle("По услугам");
							for(Washer.GroupMenu groupMenu : washer.getGroupMenu()){
								long count = 0, sum = 0;
								for(BookInfo book : books){
									if(book.getGroupServices().contains(groupMenu.getId()) && (book.getStatus().equals(Constants.FINISHED) ||(book.getStatus().equals(Constants.QUEUED_FINISHED)))){
										count++;
									}
								}
								Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail(groupMenu.getName(),"Количество: "+count);
								statisticDetails.add(statisticDetail);
							}

							for (Integer service : washer.getServices()) {
								long count = 0,sum = 0;
								for(BookInfo book : books){
									if(book.getServices().contains(service) && (book.getStatus().equals(Constants.FINISHED) ||(book.getStatus().equals(Constants.QUEUED_FINISHED)))){
										count++;
									}
								}
								Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail(allServices.get(service),"Количество: "+count);
								statisticDetails.add(statisticDetail);
							}
						}
							statistic.setStatisticsList(statisticDetails);
							statistics.add(statistic);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
				c.close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return statistics;
	}
}
