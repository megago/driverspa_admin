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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import com.driverspa.BA;
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

public class DriverSpaOrmLiteSqlHelper extends OrmLiteSqliteOpenHelper {

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

	private void forEachCursorRow(Cursor cursor, Consumer<Cursor> rowConsumer) {
		if (cursor != null && cursor.getCount() != 0) {
			IntStream.range(0, cursor.getCount()).forEach(i -> {
				cursor.moveToPosition(i);
				rowConsumer.accept(cursor);
			});
		}
	}

	public DriverSpaOrmLiteSqlHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
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

	public List<Statistic> getStatisticData(Washer washer, Instant date){
			List<Statistic> statistics = new ArrayList<Statistic>();

		try {
			// Total counts and for cartypes
			if (washer != null && washer.getCarTypes() != null && washer.getCarTypes().size() > 0) {

				final HashMap<Integer, String> allCarTypes = BA.getReference().getCarType();
				final HashMap<Integer, String> allServices = BA.getReference().getServices();
				Statistic statistic = new Statistic();
				List<Statistic.StatisticDetail> statisticDetails = new ArrayList<Statistic.StatisticDetail>();
				final List<Statistic.StatisticDetail> baseDetails = statisticDetails;
				statistic.setTitle("Общие показатели");

				//Total count of booked and queued items
				Cursor c = null;
				c = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished')", null);

				forEachCursorRow(c, row -> baseDetails.add(new Statistic.StatisticDetail("Количество машин", row.getString(row.getColumnIndex("cnt")))));

				if (washer != null && washer.getCarTypes() != null && washer.getCarTypes().size() > 0) {
					//Booked items for each carType
					washer.getCarTypes().forEach(carType -> {
						Cursor carTypeCursor = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished') and cartype=" + carType, null);
						forEachCursorRow(carTypeCursor, row -> baseDetails.add(new Statistic.StatisticDetail(allCarTypes.get(carType), row.getString(row.getColumnIndex("cnt")))));
					});
				}

				//Total sum of books
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT sum(CAST(price AS INTEGER) + CAST(discount_amount AS INTEGER)) as sum_without, sum(CAST(price AS INTEGER)) as sum_with, " +
						"sum(CAST( (case when nocash=1 then price else 0 end) AS INTEGER)) nocash," +
						"sum(CAST( (case when nocash=0 then price else 0 end) AS INTEGER)) cash" +
						" FROM bookinfo where status in ('finished','queued_finished')", null);

				forEachCursorRow(c, row -> {
					Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail("Общая сумма", "Cо скидкой "+(TextUtils.isEmpty(row.getString(row.getColumnIndex("sum_with"))) ? "0" : decimalFormatter.format(row.getLong(row.getColumnIndex("sum_with")))) + " ₸ "+
							" \nБез скидок "+(TextUtils.isEmpty(row.getString(row.getColumnIndex("sum_without"))) ? "0" : decimalFormatter.format(row.getLong(row.getColumnIndex("sum_without")))) + " ₸");
					baseDetails.add(statisticDetail);
				});

				//Total sum of books
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT sum(CAST(discount_amount AS INTEGER)) as sum FROM bookinfo where status in ('finished','queued_finished')", null);

				forEachCursorRow(c, row -> baseDetails.add(new Statistic.StatisticDetail("Скидки", (TextUtils.isEmpty(row.getString(row.getColumnIndex("sum"))) ? "0" : decimalFormatter.format(row.getLong(row.getColumnIndex("sum")))) + " ₸")));

				statistic.setStatisticsList(statisticDetails);
				statistics.add(statistic);


				statistic = new Statistic();
				statisticDetails = new ArrayList<Statistic.StatisticDetail>();
				final List<Statistic.StatisticDetail> queueDetails = statisticDetails;
				statistic.setTitle("Живая очередь");
				//Total count of booked items
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished') and queued=1", null);

				forEachCursorRow(c, row -> queueDetails.add(new Statistic.StatisticDetail("Количество машин", row.getString(row.getColumnIndex("cnt")))));

				//Booked items for each carType
				if (washer != null && washer.getCarTypes() != null && washer.getCarTypes().size() > 0) {
					washer.getCarTypes().forEach(carType -> {
						Cursor carTypeCursor = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished') and queued=1 and cartype=" + carType, null);
						forEachCursorRow(carTypeCursor, row -> queueDetails.add(new Statistic.StatisticDetail(allCarTypes.get(carType), row.getString(row.getColumnIndex("cnt")))));
					});
				}

				//Total sum of books
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT sum(CAST(price AS INTEGER) + CAST(discount_amount AS INTEGER)) as sum_without, sum(CAST(price AS INTEGER)) as sum_with, " +
						"sum(CAST( (case when nocash=1 then price else 0 end) AS INTEGER)) nocash," +
						"sum(CAST( (case when nocash=0 then price else 0 end) AS INTEGER)) cash " +
						"FROM bookinfo where status in ('finished','queued_finished') and queued=1", null);

				forEachCursorRow(c, row -> {
					Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail("Общая сумма", "Cо скидкой "+(TextUtils.isEmpty(row.getString(row.getColumnIndex("sum_with"))) ? "0" : decimalFormatter.format(row.getLong(row.getColumnIndex("sum_with")))) + " ₸ "+
							" \nБез скидок "+(TextUtils.isEmpty(row.getString(row.getColumnIndex("sum_without"))) ? "0" : decimalFormatter.format(row.getLong(row.getColumnIndex("sum_without")))) + " ₸");
					queueDetails.add(statisticDetail);
				});

				statistic.setStatisticsList(statisticDetails);
				statistics.add(statistic);


				statistic = new Statistic();
				statisticDetails = new ArrayList<Statistic.StatisticDetail>();
				final List<Statistic.StatisticDetail> bookingDetails = statisticDetails;
				statistic.setTitle("Брони");
				//Total count of booked items
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished') and queued=0", null);

				forEachCursorRow(c, row -> bookingDetails.add(new Statistic.StatisticDetail("Количество машин", row.getString(row.getColumnIndex("cnt")))));

				//Booked items for each carType
				if (washer != null && washer.getCarTypes() != null && washer.getCarTypes().size() > 0) {
					washer.getCarTypes().forEach(carType -> {
						Cursor carTypeCursor = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished') and queued=0 and cartype=" + carType, null);
						forEachCursorRow(carTypeCursor, row -> bookingDetails.add(new Statistic.StatisticDetail(allCarTypes.get(carType), row.getString(row.getColumnIndex("cnt")))));
					});
				}

				//Total sum of books
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT sum(CAST(price AS INTEGER) + CAST(discount_amount AS INTEGER)) as sum_without, sum(CAST(price AS INTEGER)) as sum_with," +
						"sum(CAST( (case when nocash=1 then price else 0 end) AS INTEGER)) nocash," +
						"sum(CAST( (case when nocash=0 then price else 0 end) AS INTEGER)) cash " +
						" FROM bookinfo where status in ('finished','queued_finished') and queued=0", null);

				forEachCursorRow(c, row -> {
					Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail("Общая сумма", "Cо скидкой "+(TextUtils.isEmpty(row.getString(row.getColumnIndex("sum_with"))) ? "0" : decimalFormatter.format(row.getLong(row.getColumnIndex("sum_with")))) + " ₸ "+
							" \nБез скидок "+(TextUtils.isEmpty(row.getString(row.getColumnIndex("sum_without"))) ? "0" : decimalFormatter.format(row.getLong(row.getColumnIndex("sum_without")))) + " ₸");
					bookingDetails.add(statisticDetail);
				});
				statistic.setStatisticsList(statisticDetails);
				statistics.add(statistic);


				statistic = new Statistic();
				statisticDetails = new ArrayList<Statistic.StatisticDetail>();
				final List<Statistic.StatisticDetail> boxDetails = statisticDetails;
				statistic.setTitle("По боксам");

				//Total count of booked items
				c = null;
				c = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt FROM bookinfo where status in ('finished','queued_finished')", null);

				if (washer != null && washer.getBoxSettings() != null && washer.getBoxSettings().size() > 0) {
					washer.getBoxSettings().stream().filter(bs -> !bs.isStatusDisabled()).forEach(bs -> {
							Cursor boxCursor = this.getWritableDatabase().rawQuery("SELECT count(*) as cnt,sum(CAST(price AS INTEGER) + CAST(discount_amount AS INTEGER)) as sum_without, sum(CAST(price AS INTEGER)) as sum_with" +
									" FROM bookinfo where status in ('finished','queued_finished') and boxId='" + bs.getUid() + "'", null);
							forEachCursorRow(boxCursor, row -> {
								Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail(Functions.getBoxName(washer, bs.getUid())
										+ "(" + bs.getBookingType() + ")" + (TextUtils.isEmpty(bs.getWasherPerson()) ? "" : " - " + bs.getWasherPerson())
										, "Количество " + row.getString(row.getColumnIndex("cnt"))
										+ "\nсо скидкой " + (TextUtils.isEmpty(row.getString(row.getColumnIndex("sum_with"))) ? "0" : decimalFormatter.format(row.getLong(row.getColumnIndex("sum_with")))) + " ₸"
										+ "\nбез скидок " + (TextUtils.isEmpty(row.getString(row.getColumnIndex("sum_without"))) ? "0" : decimalFormatter.format(row.getLong(row.getColumnIndex("sum_without")))) + " ₸"
								);
								boxDetails.add(statisticDetail);
							});
					});
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
							final List<Statistic.StatisticDetail> staffDetails = statisticDetails;
							statistic.setTitle("По сотрудникам");
							washer.getStaffs().forEach(staff -> {
								long count = books.stream()
										.filter(book -> book.getStaffs().contains(staff.getUid()) && (book.getStatus().equals(Constants.FINISHED) || book.getStatus().equals(Constants.QUEUED_FINISHED)))
										.count();
								long sum = books.stream()
										.filter(book -> book.getStaffs().contains(staff.getUid()) && (book.getStatus().equals(Constants.FINISHED) || book.getStatus().equals(Constants.QUEUED_FINISHED)))
										.mapToLong(book -> {
											try {
												return (long) Double.parseDouble(book.getPrice());
											} catch(Exception e) {
												return 0L;
											}
										})
										.sum();
								Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail(staff.getFirstName(),"Количество: "+count+" \nна сумму "+decimalFormatter.format(sum)+" ₸");
								staffDetails.add(statisticDetail);
							});
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
							final List<Statistic.StatisticDetail> serviceDetails = statisticDetails;
							statistic.setTitle("По услугам");
							washer.getGroupMenu().forEach(groupMenu -> {
								long count = books.stream()
										.filter(book -> book.getGroupServices().contains(groupMenu.getId()) && (book.getStatus().equals(Constants.FINISHED) || book.getStatus().equals(Constants.QUEUED_FINISHED)))
										.count();
								Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail(groupMenu.getName(),"Количество: "+count);
								serviceDetails.add(statisticDetail);
							});

							washer.getServices().forEach(service -> {
								long count = books.stream()
										.filter(book -> book.getServices().contains(service) && (book.getStatus().equals(Constants.FINISHED) || book.getStatus().equals(Constants.QUEUED_FINISHED)))
										.count();
								Statistic.StatisticDetail statisticDetail = new Statistic.StatisticDetail(allServices.get(service),"Количество: "+count);
								serviceDetails.add(statisticDetail);
							});
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
