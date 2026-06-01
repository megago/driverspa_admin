package com.driverspa.util;

import android.text.TextUtils;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.driverspa.BA;
import com.driverspa.model.Device;
import com.driverspa.model.Settings;
import com.driverspa.model.UserLocation;
import com.driverspa.model.Washer;
import com.driverspa.model.WasherBase;
import com.driverspa.model.api.request.AddReviewRequest;
import com.driverspa.model.api.request.AddWorkshiftRequest;
import com.driverspa.model.api.request.AdminNotificationNewsRequest;
import com.driverspa.model.api.request.AuthAdminRegistrationRequest;
import com.driverspa.model.api.request.AuthClientRegistrationRequest;
import com.driverspa.model.api.request.AuthVerificationRequest;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.model.api.request.BoxOperationRequest;
import com.driverspa.model.api.request.CampaignOperationRequest;
import com.driverspa.model.api.request.CheckPhoneRequest;
import com.driverspa.model.api.request.ClientCreateRequest;
import com.driverspa.model.api.request.ForgotPasswordRequest;
import com.driverspa.model.api.request.LoginPasswordRequest;
import com.driverspa.model.api.request.ResetPasswordRequest;
import com.driverspa.model.api.request.SetPasswordRequest;
import com.driverspa.model.api.request.CloseWorkshiftRequest;
import com.driverspa.model.api.request.FareRequestBid;
import com.driverspa.model.api.request.GroupMenuOperationRequest;
import com.driverspa.model.api.request.InitialWasherCreateRequest;
import com.driverspa.model.api.request.StaffOperationRequest;
import com.driverspa.model.api.request.UserUpdateRequest;
import com.driverspa.model.api.request.WashCountCampaignOperationRequest;
import com.driverspa.model.api.request.WasherRequest;
import com.driverspa.model.api.response.AboutUSResponseHolder;
import com.driverspa.model.api.response.AdminAnalyticsResponseHolder;
import com.driverspa.model.api.response.AdminBoxResponseHolder;
import com.driverspa.model.api.response.AdminBoxesResponseHolder;
import com.driverspa.model.api.response.AdminCampaignResponseHolder;
import com.driverspa.model.api.response.AdminCampaignsResponseHolder;
import com.driverspa.model.api.response.AdminCarwashTransactionResponseHolder;
import com.driverspa.model.api.response.AdminCarwashTransactionsResponseHolder;
import com.driverspa.model.api.response.AdminCashFinancialResponseHolder;
import com.driverspa.model.api.response.AdminFinancialStatisticResponseHolder;
import com.driverspa.model.api.response.AdminGroupMenuListResponseHolder;
import com.driverspa.model.api.response.AdminGroupMenuResponseHolder;
import com.driverspa.model.api.response.AdminStaffFinancialResponseHolder;
import com.driverspa.model.api.response.AdminStaffResponseHolder;
import com.driverspa.model.api.response.AdminStaffsResponseHolder;
import com.driverspa.model.api.response.AdminWashCountCampaignResponseHolder;
import com.driverspa.model.api.response.AdminWashCountCampaignsResponseHolder;
import com.driverspa.model.api.response.AdminWorkshiftResponseHolder;
import com.driverspa.model.api.response.AdminWorkshiftsResponseHolder;
import com.driverspa.model.api.response.AuthAdminRegistrationResponseHolder;
import com.driverspa.model.api.response.AuthAdminVerificationResponseHolder;
import com.driverspa.model.api.response.AuthClientRegistrationResponseHolder;
import com.driverspa.model.api.response.AuthClientVerificationResponseHolder;
import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.model.api.response.BookInfoResponseHolder;
import com.driverspa.model.api.response.CheckPhoneResponseHolder;
import com.driverspa.model.api.response.OkResponseHolder;
import com.driverspa.model.api.response.BookResponseHolder;
import com.driverspa.model.api.response.BooksResponseHolder;
import com.driverspa.model.api.response.ClientResponseHolder;
import com.driverspa.model.api.response.ClientsResponseHolder;
import com.driverspa.model.api.response.NotificationNewsResponseHolder;
import com.driverspa.model.api.response.PhotoResponseHolder;
import com.driverspa.model.api.response.QueuedBooksResponseHolder;
import com.driverspa.model.api.response.ReferenceResponseHolder;
import com.driverspa.model.api.response.SingleClientBooksResponseHolder;
import com.driverspa.model.api.response.UserResponseHolder;
import com.driverspa.model.api.response.WasherResponseHolder;
import com.driverspa.model.api.response.WasherTimeTableResponseHolder;
import com.driverspa.model.api.response.WashersResponseHolder;
import com.driverspa.util.otto.ws.AdminCarwashTransactionAddRequestEvent;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import retrofit.mime.TypedFile;

/**
 * HTTP client for connecting to REST API
 */


public class RetrofitClient {

	//local
//	static String ip = "http://192.168.0.102";
//	public static String API_URL = ip+":8000/api/"; // api url
//	public static String API_URL_IMAGES = ip+":8000"; // api url

//	public static String API_URL = "http://192.168.100.5:8000/api/"; // api url
//	public static String API_URL_IMAGES = "http://192.168.100.5:8000"; // api url

	// mobile hotspot
//	public static String API_URL = "http://192.168.43.61:8000/api/"; // api url
//	public static String API_URL_IMAGES = "http://192.168.43.61:8000"; // api url

//  public static String API_URL = "http://192.168.1.17:8000/api/"; // api url
//	public static String API_URL_IMAGES = "http://192.168.1.17:8000"; // api url

	//Avral kafe
//	public static String API_URL = "http://192.168.1.108:8000/api/"; // api url
//	public static String API_URL_IMAGES = "http://192.168.1.108:8000"; // api url

//	public static String API_URL = "http://172.20.10.2:8000/api/"; // api url
//	public static String API_URL_IMAGES = "http://172.20.10.2:8000"; // api url

//	public static String API_URL = "http://10.45.22.243:8000/api/"; // api url
//	public static String API_URL_IMAGES = "http://10.45.22.243:8000"; // api url

//	public static String API_URL = "http://192.168.1.178:8000/api/"; // api url
//	public static String API_URL_IMAGES = "http://192.168.1.178:8000"; // api url

	public static String API_URL = "http://192.168.31.177:8002/api/"; // api url
	public static String API_URL_IMAGES = "http://192.168.31.177:8002"; // api url

//
//	public static String API_URL = "http://192.168.31.73:8000/api/"; // api url
//	public static String API_URL_IMAGES = "http://192.168.31.73:8000"; // api url

	///prod
//	public static String API_URL = "http://api.driverspa.kz/"; // api url
//	public static String API_URL_IMAGES = "http://api.driverspa.kz"; // api url

	/////test
//	public static String API_URL = "http://test.api.driverspa.kz/"; // api url
//	public static String API_URL_IMAGES = "http://test.api.driverspa.kz"; // api urlr

//	public static String API_URL = "http://balhash.driverspa.kz:5000/api/"; // api url
//	public static String API_URL_IMAGES = "http://balhash.driverspa.kz:5000/api"; // api url

	/**
	 * Interface for connecting to DriverSpa API
	 */
	public interface DriverSpaApi {

		@GET("/v1/public_carwash/ref")
		void getReference(
				@Header("Accept-Language") String lang,
				Callback<ReferenceResponseHolder> callback);

		@GET("/v1/about_us/html/")
		void getAboutUS(
				Callback<AboutUSResponseHolder> callback);
		
		@POST("/v1/account/check_phone")
		void checkPhone(
				@Body CheckPhoneRequest body,
				Callback<CheckPhoneResponseHolder> callback);

		@POST("/v1/account/set_password")
		void setPassword(
				@Body SetPasswordRequest body,
				Callback<OkResponseHolder> callback);

		@POST("/v1/account/login_password")
		void loginPassword(
				@Body LoginPasswordRequest body,
				Callback<AuthAdminVerificationResponseHolder> callback);

		@POST("/v1/account/forgot_password")
		void forgotPassword(
				@Body ForgotPasswordRequest body,
				Callback<OkResponseHolder> callback);

		@POST("/v1/account/reset_password")
		void resetPassword(
				@Body ResetPasswordRequest body,
				Callback<AuthAdminVerificationResponseHolder> callback);

		@POST("/v1/account/register")
		void authClientRegistration(
				@Body AuthClientRegistrationRequest body,
				Callback<AuthClientRegistrationResponseHolder> callback);
		
		@POST("/v1/account/activate/")
		void authClientVerify(
				@Body AuthVerificationRequest body,
				Callback<AuthClientVerificationResponseHolder> callback);
		
		@POST("/v1/account/register")
		void authAdminRegistration(
				@Body AuthAdminRegistrationRequest body,
				Callback<AuthAdminRegistrationResponseHolder> callback);

		@GET("/v1/account/{id}")
		void userGet(
				@Path("id") String userId,
				Callback<UserResponseHolder> callback);
		
		@PATCH("/v1/account/{id}")
		void userUpdate(
				@Path("id") String userId,
				@Body UserUpdateRequest body,
				Callback<UserResponseHolder> callback);		
		
		@POST("/v1/image_upload/default/{id}")
		void userPhotoChange(
				@Path("id") String id,
				Callback<BaseResponseHolder> callback);

		@POST("/v1/account/logout")
		void authLogout(
				Callback<BaseResponseHolder> callback);

		@GET("/v1/client_bookmarks")
		void washersSearchFavourite(
				@QueryMap Map<String, String> options,
				Callback<WashersResponseHolder> callback);

		@GET("/v1/public_carwash")
		void washersSearchAvailable(
				@QueryMap Map<String, String> options,
				Callback<WashersResponseHolder> callback);

		@GET("/v1/public_carwash")
		void washersSearchPromo(
				@QueryMap Map<String, String> options,
				Callback<WashersResponseHolder> callback);

		@GET("/v1/public_carwash")
		void washersSearchNearBy(
				@QueryMap Map<String, String> options,
				Callback<WashersResponseHolder> callback);

		@GET("/v1/public_carwash/{washerid}")
		void washerInfo(
				@Path("washerid") String washerId,
				Callback<WasherResponseHolder> callback);

		@GET("/v1/public_carwash/timetable")
		void washerTimeTable(
				@Query("carwash") String washerId,
				@Query("day") String day,
				Callback<WasherTimeTableResponseHolder> callback);

		@GET("/v1/company_booking/timetable")
		void washerTimeTableWithBook(
				@Query("carwash") String washerId,
				@Query("day") String day,
				@Query("date") Long date,
				Callback<WasherTimeTableResponseHolder> callback);

//		@POST("/v1/client_booking")
		@POST("/v1/company_booking")
		void bookRequest(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);

//		@GET("/v1/company_booking/{bookid}")
		@GET("/v1/client_booking/{bookid}")
		void bookingInfo(
				@Path("bookid") String bookId,
				Callback<BookInfoResponseHolder> callback);
		
//		@GET("/v1/company_booking")
		@GET("/v1/client_booking")
		void booksList(
				@QueryMap Map<String, String> options,
				Callback<BooksResponseHolder> callback);
		
		@POST("/v1/client_booking/cancel")
		void bookingCancel(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);		
		
		@POST("/v1/client_bookmarks")
		void addToFavourite(
				@Body WasherRequest body,
				Callback<WasherResponseHolder> callback);

		@DELETE("/v1/client_bookmarks/{washerid}")
		void removeFromFavourite(
				@Path("washerid") String washerId,
				Callback<BaseResponseHolder> callback);
		
		@Multipart
		@POST("/v1/image_upload/new")
		void clientPhotoPost(
				@Part("image") TypedFile photo,
				Callback<PhotoResponseHolder> callback);

		@POST("/v1/image_upload/delete/{photoid}")
		void photoDelete(
				@Path("photoid") String photoId,
				Callback<BaseResponseHolder> callback);


		@POST("/v1/client_reviews")
		void addClientReview(
				@Body AddReviewRequest body,
				Callback<BaseResponseHolder> callback);
		
		///////Admin part starting\\\\\\\\
		
		@POST("/v1/account/activate/")
		void authAdminVerify(
				@Body AuthVerificationRequest body,
				Callback<AuthAdminVerificationResponseHolder> callback);
		
		@GET("/v1/company_carwash")
		void  getOwnWashers(
				@QueryMap Map<String, String> options,
				Callback<WashersResponseHolder> callback);

		@GET("/v1/company_carwash")
		void  getWasherById(
				@QueryMap Map<String, String> options,
				Callback<WashersResponseHolder> callback);

		@POST("/v1/company_carwash")
		void createInitialWasher(
				@Body InitialWasherCreateRequest body,
				Callback<WasherResponseHolder> callback);
		
		@Multipart
		@POST("/v1/image_upload/new")
		void adminPhotoPost(
				@Part("image") TypedFile photo,
				@Part("carwash") String washerId,
				Callback<PhotoResponseHolder> callback);
		
		@GET("/v1/company_carwash/{washerid}")
		void adminWasherInfo(
				@Path("washerid") String washerId,
				Callback<WasherResponseHolder> callback);
		
		@PATCH("/v1/company_carwash/{washerid}")
		void editWasher(
				@Path("washerid") String washerId,
				@Body WasherBase body,
				Callback<WasherResponseHolder> callback);
		
		@GET("/v1/company_booking")
		void companyBooksList(
				@QueryMap Map<String, String> options,
				Callback<BooksResponseHolder> callback);

		@GET("/v1/company_clients/")
		void companyClients(
				@QueryMap Map<String, String> options,
				Callback<ClientsResponseHolder> callback);

		@GET("/v1/company_clients/single/")
		void companyClient(
				@QueryMap Map<String, String> options,
				Callback<ClientResponseHolder> callback);

		@GET("/v1/company_clients/{id}")
		void companySingleClient(
				@Path("id") String id,
				Callback<ClientResponseHolder> callback);

		@GET("/v1/company_booking/queued/")
		void companyQueuedBooksList(
				@QueryMap Map<String, String> options,
				Callback<QueuedBooksResponseHolder> callback);

		@GET("/v1/company_booking/single_client_books/")
		void companySingleClientBooksList(
				@QueryMap Map<String, String> options,
				Callback<SingleClientBooksResponseHolder> callback);

		@GET("/v1/company_booking/statistics/")
		void companyStatisticsList(
				@QueryMap Map<String, String> options,
				Callback<QueuedBooksResponseHolder> callback);

		@POST("/v1/company_booking")
		void companyBookRequest(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);

		@GET("/v1/company_booking/{bookid}")
		void companyBookingInfo(
				@Path("bookid") String bookId,
				Callback<BookInfoResponseHolder> callback);

		@POST("/v1/company_booking/start_queue")
		void startQueueRequest(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);

		@POST("/v1/company_booking/edit_entry")
		void editQueueOrBookRequest(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);

		@POST("/v1/company_booking/finish_queue")
		void finishQueueRequest(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);

		@POST("/v1/company_booking/postpone_queue")
		void postponeQueueRequest(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);

		@POST("/v1/company_booking/client_paid")
		void markAsClientPaidRequest(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);

		@POST("/v1/company_booking/reject_queue")
		void rejectQueueRequest(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);

		@POST("/v1/company_booking/reject")
		void adminBookingCancel(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);
		
		@POST("/v1/company_booking/approve")
		void adminBookingApprove(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);		

		@POST("/v1/company_booking/start")
		void adminBookingStart(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);
		
		@POST("/v1/company_booking/finish")
		void adminBookingStop(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);		

		@POST("/v1/company_booking/nocome")
		void adminBookingNoClient(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);		

		@POST("/v1/company_booking/fail")
		void adminBookingFail(
				@Body BookingRequest body,
				Callback<BookResponseHolder> callback);	
		
		@GET("/v1/company_carwash/{washerid}/box")
		void adminBoxList(
				@Path("washerid") String washerId,
				Callback<AdminBoxesResponseHolder> callback);
		
		@GET("/v1/company_carwash/{washerid}/box/{boxid}")
		void adminBox(
				@Path("washerid") String washerId,
				@Path("boxid") String boxId,
				Callback<AdminBoxResponseHolder> callback);
		
		@PATCH("/v1/company_carwash/{washerid}/box/{boxid}")
		void adminChangeBox(
				@Path("washerid") String washerId,
				@Path("boxid") String boxId,
				@Body BoxOperationRequest body,
				Callback<AdminBoxResponseHolder> callback);

		@POST("/v1/company_carwash/{washerid}/box/")
		void adminAddBox(
				@Path("washerid") String washerId,
				@Body BoxOperationRequest body,
				Callback<AdminBoxResponseHolder> callback);

		@DELETE("/v1/company_carwash/{washerid}/box/{boxid}")
		void adminDeleteBox(
				@Path("washerid") String washerId,
				@Path("boxid") String boxId,
				Callback<AdminBoxResponseHolder> callback);

		@GET("/v1/company_carwash/{washerid}/group_menu")
		void adminGroupMenuList(
				@Path("washerid") String washerId,
				Callback<AdminGroupMenuListResponseHolder> callback);

		@GET("/v1/company_carwash/{washerid}/group_menu/{id}")
		void adminGroupMenu(
				@Path("washerid") String washerId,
				@Path("id") String id,
				Callback<AdminGroupMenuResponseHolder> callback);

		@PATCH("/v1/company_carwash/{washerid}/group_menu/{id}")
		void adminChangeGroupMenu(
				@Path("washerid") String washerId,
				@Path("id") String id,
				@Body GroupMenuOperationRequest body,
				Callback<AdminGroupMenuResponseHolder> callback);

		@POST("/v1/company_carwash/{washerid}/group_menu/")
		void adminAddGroupMenu(
				@Path("washerid") String washerId,
				@Body GroupMenuOperationRequest body,
				Callback<AdminGroupMenuResponseHolder> callback);

		@DELETE("/v1/company_carwash/{washerid}/group_menu/{id}")
		void adminDeleteGroupMenu(
				@Path("washerid") String washerId,
				@Path("id") String id,
				Callback<AdminGroupMenuResponseHolder> callback);

		@POST("/v1/company_carwash")
		void createTmpWasher(
				@Body Washer body,
				Callback<WasherResponseHolder> callback);

		@GET("/v1/settings")
		void settingsList(
				Callback<BaseResponseHolder> callback);

		@POST("/v1/settings")
		void updateSettings(
				@Body Settings body,
				Callback<BaseResponseHolder> callback);

		@GET("/v1/devices/{deviceId}")
		void getDevice(
                String deviceId,
				Callback<BaseResponseHolder> callback);

		@PUT("/v1/devices/{deviceId}")
		void addDevice(
				@Path("deviceId") String deviceId,
				@Body Device body,
				Callback<BaseResponseHolder> callback);

		@DELETE("/v1/devices/{deviceId}")
		void deleteDevice(
				@Path("deviceId") String deviceId,
				Callback<BaseResponseHolder> callback);

       //Campaigns
		@GET("/v1/company_carwash/{washerid}/company_campaigns")
		void adminCampaignList(
				@Path("washerid") String washerId,
				Callback<AdminCampaignsResponseHolder> callback);

		@GET("/v1/company_carwash/{washerid}/company_campaigns/{campaignid}")
		void adminCampaign(
				@Path("washerid") String washerId,
				@Path("campaignid") String boxId,
				Callback<AdminCampaignResponseHolder> callback);

		@PATCH("/v1/company_carwash/{washerid}/company_campaigns/{campaignid}")
		void adminChangeCampaign(
				@Path("washerid") String washerId,
				@Path("campaignid") String campaignId,
				@Body CampaignOperationRequest body,
				Callback<AdminCampaignResponseHolder> callback);

		@POST("/v1/company_carwash/{washerid}/company_campaigns/")
		void adminAddCampaign(
				@Path("washerid") String washerId,
				@Body CampaignOperationRequest body,
				Callback<BaseResponseHolder> callback);

		@DELETE("/v1/company_carwash/{washerid}/company_campaigns/{campaignid}")
		void adminDeleteCampaign(
				@Path("washerid") String washerId,
				@Path("campaignid") String campaignId,
				Callback<AdminCampaignResponseHolder> callback);

		//Wash count Campaigns
		@GET("/v1/company_carwash/{washerid}/wash_count")
		void adminWashCountCampaignList(
				@Path("washerid") String washerId,
				Callback<AdminWashCountCampaignsResponseHolder> callback);

		@GET("/v1/company_carwash/{washerid}/wash_count/{campaignid}")
		void adminWashCountCampaign(
				@Path("washerid") String washerId,
				@Path("campaignid") String boxId,
				Callback<AdminWashCountCampaignResponseHolder> callback);

		@PATCH("/v1/company_carwash/{washerid}/wash_count/{campaignid}")
		void adminWashCountChangeCampaign(
				@Path("washerid") String washerId,
				@Path("campaignid") String campaignId,
				@Body CampaignOperationRequest body,
				Callback<AdminWashCountCampaignResponseHolder> callback);

		@POST("/v1/company_carwash/{washerid}/wash_count/")
		void adminWashCountAddCampaign(
				@Path("washerid") String washerId,
				@Body WashCountCampaignOperationRequest body,
				Callback<BaseResponseHolder> callback);

		@DELETE("/v1/company_carwash/{washerid}/wash_count/{campaignid}")
		void adminWashCountDeleteCampaign(
				@Path("washerid") String washerId,
				@Path("campaignid") String campaignId,
				Callback<AdminWashCountCampaignResponseHolder> callback);

		@POST("/v1/company_notifications")
		void addAdminNotificationNews(
				@Body AdminNotificationNewsRequest body,
				Callback<BaseResponseHolder> callback);

		@POST("/v1/company_notifications/send_push/")
		void sendAdminNotificationNews(
				@Body AdminNotificationNewsRequest body,
				Callback<BaseResponseHolder> callback);

		@GET("/v1/company_notifications")
		void getAdminNotificationNews(
				@QueryMap Map<String, String> options,
				Callback<NotificationNewsResponseHolder> callback);

		@POST("/v1/company_clients")
		void createCompanyClient(
				@Body ClientCreateRequest body,
				Callback<BaseResponseHolder> callback);

		@PATCH("/v1/company_clients/{id}")
		void editCompanyClient(
				@Path("id") String id,
				@Body ClientCreateRequest body,
				Callback<BaseResponseHolder> callback);

		@DELETE("/v1/company_clients/{id}")
		void deleteCompanyClient(
				@Path("id") String id,
				Callback<BaseResponseHolder> callback);


		@GET("/v1/company_carwash/{washerid}/staff")
		void adminStaffList(
				@Path("washerid") String washerId,
				Callback<AdminStaffsResponseHolder> callback);

		@GET("/v1/company_carwash/{washerid}/staff/{id}")
		void adminStaff(
				@Path("washerid") String washerId,
				@Path("id") String id,
				Callback<AdminStaffResponseHolder> callback);

		@PATCH("/v1/company_carwash/{washerid}/staff/{id}")
		void adminChangeStaff(
				@Path("washerid") String washerId,
				@Path("id") String id,
				@Body StaffOperationRequest body,
				Callback<AdminStaffResponseHolder> callback);

		@POST("/v1/company_carwash/{washerid}/staff/")
		void adminAddStaff(
				@Path("washerid") String washerId,
				@Body StaffOperationRequest body,
				Callback<AdminStaffResponseHolder> callback);

		@DELETE("/v1/company_carwash/{washerid}/staff/{id}")
		void adminDeleteStaff(
				@Path("washerid") String washerId,
				@Path("id") String id,
				Callback<AdminStaffResponseHolder> callback);

		@GET("/v1/work_shift/")
		void adminWorkshiftList(
				@Query("carwash") String washerId,
				Callback<AdminWorkshiftsResponseHolder> callback);

		@GET("/v1/work_shift/{id}")
		void adminWorkshift(
				@Path("id") String id,
				Callback<AdminWorkshiftResponseHolder> callback);

		@POST("/v1/work_shift/close")
		void adminWorkshiftClose(
				@Body CloseWorkshiftRequest request,
				Callback<AdminWorkshiftResponseHolder> callback);

		@POST("/v1/work_shift/")
		void adminAddWorkshift(
				@Body AddWorkshiftRequest request,
				Callback<AdminWorkshiftResponseHolder> callback);

		@GET("/v1/financial_statistics/current/")
		void adminFinancialStatistics(
				@Query("carwash") String washerId,
				@Query("start_time") String startTime,
				Callback<AdminFinancialStatisticResponseHolder> callback);

		@GET("/v1/carwash_money_transaction/")
		void adminCarwashTransactionList(
				@QueryMap Map<String, String> options,
				Callback<AdminCarwashTransactionsResponseHolder> callback);

		@GET("/v1/carwash_money_transaction/{id}")
		void adminCarwashTransaction(
				@Path("id") String id,
				Callback<AdminCarwashTransactionResponseHolder> callback);

		@POST("/v1/carwash_money_transaction/")
		void adminAddCarwashTransaction(
				@Body AdminCarwashTransactionAddRequestEvent request,
				Callback<AdminCarwashTransactionResponseHolder> callback);


		@GET("/v1/staff_details/current/")
		void adminStaffFinancialInfo(
				@Query("carwash") String washerId,
				@Query("staff_id") String staffId,
				@Query("start_time") String startTime,
				@Query("end_time") String endTime,
				Callback<AdminStaffFinancialResponseHolder> callback);


		@GET("/v1/cash_financial_statistics/current/")
		void adminCashFinancial(
				@Query("carwash") String washerId,
				@Query("start_time") String startTime,
				@Query("end_time") String endTime,
				Callback<AdminCashFinancialResponseHolder> callback);

		@GET("/v1/analytical_statistics/")
		void adminAnalytics(
				@Query("carwash") String washerId,
				@Query("start_time") String startTime,
				@Query("end_time") String endTime,
				Callback<AdminAnalyticsResponseHolder> callback);

		@POST("/v1/company_bid/")
		void adminCompanyBid(
				@Body FareRequestBid request,
				Callback<BaseResponseHolder> callback);

		@PUT("/v1/location/{locationText}")
		void updateLocation(
				@Path("locationText") String locationText,
				@Body UserLocation body,
				Callback<BaseResponseHolder> callback);
   /*
		@POST("/v1/client_bookmarks")
		void addToFavourite(
				@Body WasherRequest body,
				Callback<WasherResponseHolder> callback);

		@DELETE("/v1/client_bookmarks/{washerid}")
		void removeFromFavourite(
				@Path("washerid") String washerId,
				Callback<BaseResponseHolder> callback);
     */
	}
	
	public static DriverSpaApi getRestAdapterNoHeader() {
		return getRestAdapterGeneral(false, false, false);
	}
	
	/**
	 * General rest adapter for connecting to REST services
	 * @return - api for connecting to REST services
	 */
	public static DriverSpaApi getRestAdapter() {
		return getRestAdapterGeneral(true, false, false);
	}
	
	public static DriverSpaApi getRestAdapterWithBody() {
		return getRestAdapterGeneral(true, true, false);
	}
	
	public static DriverSpaApi getRestAdapterForUpload() {
		return getRestAdapterGeneral(true, true, true);
	}

	/**
	 * Request adapter
	 * @param withBody - whether to add content type or not
	 * @return - api for connecting to REST services
	 */
	private static DriverSpaApi getRestAdapterGeneral(final boolean accept, final boolean withBody, final boolean forUpload) {
		RequestInterceptor requestInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				// if there is a user token, add that to request
			String token = UserPreferences.getUserToken(BA.getContext());
			request.addHeader("Content-Language", "ru");
			if(!forUpload){
				if( !TextUtils.isEmpty(token) ){
					request.addHeader("Authorization", "ApiKey "+token);
					request.addHeader("Accept-Language", UserPreferences.getUserLocale(BA.getContext()));
			    }
			 }
			else{
				if( !TextUtils.isEmpty(token) ){
					request.addHeader("Authorization", "ApiKey "+token);
			    }								
			 }												 				
			}
		};

		Executor executor = Executors.newCachedThreadPool();
		RestAdapter restAdapter = new RestAdapter.Builder()
		.setEndpoint(API_URL)
		.setRequestInterceptor(requestInterceptor)
		.setConverter(new GsonConverter(Converters.gsonWithDate()))
		.setExecutors(executor, executor)		
		.setClient(new OkClient(BA.getHttpClient()))
		.setLogLevel(RestAdapter.LogLevel.FULL)
//		.setLogLevel(RestAdapter.LogLevel.NONE)
		.build();

		return restAdapter.create(DriverSpaApi.class);
	}
}