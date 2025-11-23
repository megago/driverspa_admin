package com.driverspa.assist;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.driverspa.BA;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.model.api.response.BookInfoResponseHolder;
import com.driverspa.model.api.response.BookResponseHolder;
import com.driverspa.model.api.response.BooksResponseHolder;
import com.driverspa.model.api.response.ClientResponseHolder;
import com.driverspa.model.api.response.ClientsResponseHolder;
import com.driverspa.model.api.response.QueuedBooksResponseHolder;
import com.driverspa.model.api.response.SingleClientBooksResponseHolder;
import com.driverspa.util.RetrofitClient.WashmeApi;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminBookApproveRequestEvent;
import com.driverspa.util.otto.ws.AdminBookApproveResponseEvent;
import com.driverspa.util.otto.ws.AdminBookCancelRequestEvent;
import com.driverspa.util.otto.ws.AdminBookCancelResponseEvent;
import com.driverspa.util.otto.ws.AdminBookFailRequestEvent;
import com.driverspa.util.otto.ws.AdminBookFailResponseEvent;
import com.driverspa.util.otto.ws.AdminBookInfoRequestEvent;
import com.driverspa.util.otto.ws.AdminBookInfoResponseEvent;
import com.driverspa.util.otto.ws.AdminBookNoClientRequestEvent;
import com.driverspa.util.otto.ws.AdminBookNoClientResponseEvent;
import com.driverspa.util.otto.ws.AdminBookRequestEvent;
import com.driverspa.util.otto.ws.AdminBookResponseEvent;
import com.driverspa.util.otto.ws.AdminBookStartRequestEvent;
import com.driverspa.util.otto.ws.AdminBookStartResponseEvent;
import com.driverspa.util.otto.ws.AdminBookStopRequestEvent;
import com.driverspa.util.otto.ws.AdminBookStopResponseEvent;
import com.driverspa.util.otto.ws.AdminBooksRequestEvent;
import com.driverspa.util.otto.ws.AdminBooksResponseEvent;
import com.driverspa.util.otto.ws.AdminClientRequestEvent;
import com.driverspa.util.otto.ws.AdminClientResponseEvent;
import com.driverspa.util.otto.ws.AdminClientsRequestEvent;
import com.driverspa.util.otto.ws.AdminClientsResponseEvent;
import com.driverspa.util.otto.ws.AdminEditQueueOrBookRequestEvent;
import com.driverspa.util.otto.ws.AdminEditQueueOrBookResponseEvent;
import com.driverspa.util.otto.ws.AdminFinishQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminFinishQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminMarkAsClientPaidRequestEvent;
import com.driverspa.util.otto.ws.AdminMarkAsClientPaidResponseEvent;
import com.driverspa.util.otto.ws.AdminNotRegisteredClientsRequestEvent;
import com.driverspa.util.otto.ws.AdminNotRegisteredClientsResponseEvent;
import com.driverspa.util.otto.ws.AdminPostponeQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminPostponeQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminQueuedBooksRequestEvent;
import com.driverspa.util.otto.ws.AdminQueuedBooksResponseEvent;
import com.driverspa.util.otto.ws.AdminRejectQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminRejectQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminSingleClientBooksRequestEvent;
import com.driverspa.util.otto.ws.AdminSingleClientBooksResponseEvent;
import com.driverspa.util.otto.ws.AdminStartQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminStartQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminStatisticsRequestEvent;
import com.driverspa.util.otto.ws.AdminStatisticsResponseEvent;
import com.driverspa.util.otto.ws.BookCancelRequestEvent;
import com.driverspa.util.otto.ws.BookCancelResponseEvent;
import com.driverspa.util.otto.ws.BookInfoRequestEvent;
import com.driverspa.util.otto.ws.BookInfoResponseEvent;
import com.driverspa.util.otto.ws.BookRequestEvent;
import com.driverspa.util.otto.ws.BookResponseEvent;
import com.driverspa.util.otto.ws.BooksRequestEvent;
import com.driverspa.util.otto.ws.BooksResponseEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BookAssist extends BaseAssist {

	public BookAssist(WashmeApi api, Bus eventBus) {
		super(api, eventBus);
	}

	@Subscribe
	public void onBookRequested(BookRequestEvent event) {
		getApi().bookRequest(event.getBookingRequest(),
							new Callback<BookResponseHolder>() {
			@Override
			public void success(BookResponseHolder data, Response response) {
					BA.getEventBus().post(new BookResponseEvent(data));					
			}
			
			@Override
			public void failure(RetrofitError error) {				
				BA.getEventBus().post(new BookResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onAdminBookRequested(AdminBookRequestEvent event) {
		getApi().bookRequest(event.getBookingRequest(),
							new Callback<BookResponseHolder>() {
			@Override
			public void success(BookResponseHolder data, Response response) {
					BA.getEventBus().post(new AdminBookResponseEvent(data));
			}

			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new AdminBookResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onAdminStartQueueRequested(AdminStartQueueRequestEvent event) {
		getApi().startQueueRequest(event.getBookingRequest(),
				new Callback<BookResponseHolder>() {
					@Override
					public void success(BookResponseHolder data, Response response) {
						BA.getEventBus().post(new AdminStartQueueResponseEvent(data));
					}

					@Override
					public void failure(RetrofitError error) {
						BA.getEventBus().post(new AdminStartQueueResponseEvent(null));
						onRetrofitError(error);
					}
				});
	}

	@Subscribe
	public void onAdminEditQueueOrBookRequested(AdminEditQueueOrBookRequestEvent event) {
		getApi().editQueueOrBookRequest(event.getBookingRequest(),
				new Callback<BookResponseHolder>() {
					@Override
					public void success(BookResponseHolder data, Response response) {
						BA.getEventBus().post(new AdminEditQueueOrBookResponseEvent(data));
					}

					@Override
					public void failure(RetrofitError error) {
						BA.getEventBus().post(new AdminEditQueueOrBookResponseEvent(null));
						onRetrofitError(error);
					}
				});
	}

	@Subscribe
	public void onAdminRejectQueueRequested(AdminRejectQueueRequestEvent event) {
		getApi().rejectQueueRequest(event.getBookingRequest(),
				new Callback<BookResponseHolder>() {
					@Override
					public void success(BookResponseHolder data, Response response) {
						BA.getEventBus().post(new AdminRejectQueueResponseEvent(data));
					}

					@Override
					public void failure(RetrofitError error) {
						BA.getEventBus().post(new AdminRejectQueueResponseEvent(null));
						onRetrofitError(error);
					}
				});
	}

	@Subscribe
	public void onAdminFinishQueueRequested(AdminFinishQueueRequestEvent event) {
		getApi().finishQueueRequest(event.getBookingRequest(),
				new Callback<BookResponseHolder>() {
					@Override
					public void success(BookResponseHolder data, Response response) {
						BA.getEventBus().post(new AdminFinishQueueResponseEvent(data));
					}

					@Override
					public void failure(RetrofitError error) {
						BA.getEventBus().post(new AdminFinishQueueResponseEvent(null));
						onRetrofitError(error);
					}
				});
	}

	@Subscribe
	public void onAdminPostponeQueueRequested(AdminPostponeQueueRequestEvent event) {
		getApi().postponeQueueRequest(event.getBookingRequest(),
				new Callback<BookResponseHolder>() {
					@Override
					public void success(BookResponseHolder data, Response response) {
						BA.getEventBus().post(new AdminPostponeQueueResponseEvent(data));
					}

					@Override
					public void failure(RetrofitError error) {
						BA.getEventBus().post(new AdminPostponeQueueResponseEvent(null));
						onRetrofitError(error);
					}
				});
	}

	@Subscribe
	public void onAdminMarkAsClientPaidRequested(AdminMarkAsClientPaidRequestEvent event) {
		getApi().markAsClientPaidRequest(event.getBookingRequest(),
				new Callback<BookResponseHolder>() {
					@Override
					public void success(BookResponseHolder data, Response response) {
						BA.getEventBus().post(new AdminMarkAsClientPaidResponseEvent(data));
					}

					@Override
					public void failure(RetrofitError error) {
						BA.getEventBus().post(new AdminMarkAsClientPaidResponseEvent(null));
						onRetrofitError(error);
					}
				});
	}

	@Subscribe
	public void onBookInfoRequested(BookInfoRequestEvent event) {
		getApi().bookingInfo(event.getBookId(),
				new Callback<BookInfoResponseHolder>() {
			@Override
			public void success(BookInfoResponseHolder data, Response response) {
				if( isSuccess(data) ) {
					logD("onBookRequested: success " + response.getUrl());					
					BA.getEventBus().post(new BookInfoResponseEvent(data.getResponse()));					
				} else {
					BA.getEventBus().post(new BookInfoResponseEvent(null));
				}
			}
			
			@Override
			public void failure(RetrofitError error) {				
				BA.getEventBus().post(new BookInfoResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}
	
	@Subscribe
	public void onBooksRequested(BooksRequestEvent event) {
		getApi().booksList(event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
						   new Callback<BooksResponseHolder>() {
			@Override
			public void success(BooksResponseHolder data, Response response) {
				BA.getEventBus().post(new BooksResponseEvent(data));
			}
			@Override
			public void failure(RetrofitError error) {				
				onRetrofitError(error);				
				BA.getEventBus().post(new BooksResponseEvent(null));
			}
		});
	}

	@Subscribe
	public void onAdminBooksRequested(AdminBooksRequestEvent event) {
		getApi().companyBooksList(event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
						   new Callback<BooksResponseHolder>() {
			@Override
			public void success(BooksResponseHolder data, Response response) {
					BA.getEventBus().post(new AdminBooksResponseEvent(data));					
			}
			@Override
			public void failure(RetrofitError error) {				
				onRetrofitError(error);				
				BA.getEventBus().post(new AdminBooksResponseEvent(null));
			}
		});
	}

	@Subscribe
	public void onAdminQueuedBooksRequested(AdminQueuedBooksRequestEvent event) {
		getApi().companyQueuedBooksList(event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
						   new Callback<QueuedBooksResponseHolder>() {
			@Override
			public void success(QueuedBooksResponseHolder data, Response response) {
					BA.getEventBus().post(new AdminQueuedBooksResponseEvent(data));
			}
			@Override
			public void failure(RetrofitError error) {
				onRetrofitError(error);
				BA.getEventBus().post(new AdminQueuedBooksResponseEvent(null));
			}
		});
	}

	@Subscribe
	public void onAdminSingleClientBooksRequested(AdminSingleClientBooksRequestEvent event) {
		getApi().companySingleClientBooksList(event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
				new Callback<SingleClientBooksResponseHolder>() {
					@Override
					public void success(SingleClientBooksResponseHolder data, Response response) {
						BA.getEventBus().post(new AdminSingleClientBooksResponseEvent(data));
					}
					@Override
					public void failure(RetrofitError error) {
						onRetrofitError(error);
						BA.getEventBus().post(new AdminSingleClientBooksResponseEvent(null));
					}
				});
	}


	@Subscribe
	public void onAdminStatisticsRequested(AdminStatisticsRequestEvent event) {
		getApi().companyStatisticsList(event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
						   new Callback<QueuedBooksResponseHolder>() {
			@Override
			public void success(QueuedBooksResponseHolder data, Response response) {
				    if(isSuccess(data)){
					   writeBooks(data.getBooks(),data);
				}
			}
			@Override
			public void failure(RetrofitError error) {
				onRetrofitError(error);
				BA.getEventBus().post(new AdminStatisticsResponseEvent(null));
			}
		});
	}

	@Subscribe
	public void onBookCancelRequested(BookCancelRequestEvent event) {
		getApi().bookingCancel(new BookingRequest(event.getBookId()),
				               new Callback<BookResponseHolder>() {
			@Override
			public void success(BookResponseHolder data, Response response) {				
				if( isSuccess(data) ) {
					logD("onBookCancelRequested: success " + response.getUrl());					
					BA.getEventBus().post(new BookCancelResponseEvent(data.getResponse()));
				} else {
					ToastUtil.display(BA.getContext(), data.getMessage());
				}
			}
			
			@Override
			public void failure(RetrofitError error) {				
				onRetrofitError(error);
				BA.getEventBus().post(new BookCancelResponseEvent(null));
			}
		});
	}

		
	@Subscribe
	public void onAdminBookInfoRequested(AdminBookInfoRequestEvent event) {
		getApi().companyBookingInfo(event.getBookId(),
				new Callback<BookInfoResponseHolder>() {
			@Override
			public void success(BookInfoResponseHolder data, Response response) {
				BA.getEventBus().post(new AdminBookInfoResponseEvent(data));
			}
			
			@Override
			public void failure(RetrofitError error) {				
				BA.getEventBus().post(new BookInfoResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	
	@Subscribe
	public void onAdminBookCancelRequested(AdminBookCancelRequestEvent event) {
		getApi().adminBookingCancel(new BookingRequest(event.getBookId()),
				               new Callback<BookResponseHolder>() {
			@Override
			public void success(BookResponseHolder data, Response response) {				
					BA.getEventBus().post(new AdminBookCancelResponseEvent(data));					
			}
			
			@Override
			public void failure(RetrofitError error) {								
				onRetrofitError(error);
				BA.getEventBus().post(new AdminBookCancelResponseEvent(null));
			}
		});
	}

	
	@Subscribe
	public void onAdminBookApproveRequested(AdminBookApproveRequestEvent event) {
		getApi().adminBookingApprove(new BookingRequest(event.getBookId()),
				               new Callback<BookResponseHolder>() {
			@Override
			public void success(BookResponseHolder data, Response response) {									
					BA.getEventBus().post(new AdminBookApproveResponseEvent(data));
			}
			
			@Override
			public void failure(RetrofitError error) {				
				onRetrofitError(error);
				BA.getEventBus().post(new AdminBookApproveResponseEvent(null));
			}
		});
	}

	@Subscribe
	public void onAdminBookStartRequested(AdminBookStartRequestEvent event) {
		getApi().adminBookingStart(new BookingRequest(event.getBookId()),
				               new Callback<BookResponseHolder>() {
			@Override
			public void success(BookResponseHolder data, Response response) {				
					BA.getEventBus().post(new AdminBookStartResponseEvent(data));					
			}
			
			@Override
			public void failure(RetrofitError error) {				
				onRetrofitError(error);
				BA.getEventBus().post(new AdminBookStartResponseEvent(null));
			}
		});
	}
	
	@Subscribe
	public void onAdminBookStopRequested(AdminBookStopRequestEvent event) {
		getApi().adminBookingStop(new BookingRequest(event.getBookId()),
				               new Callback<BookResponseHolder>() {
			@Override
			public void success(BookResponseHolder data, Response response) {				
					BA.getEventBus().post(new AdminBookStopResponseEvent(data));
			}
			
			@Override
			public void failure(RetrofitError error) {				
				onRetrofitError(error);
				BA.getEventBus().post(new AdminBookStopResponseEvent(null));
			}
		});
	}
	
	@Subscribe
	public void onAdminBookNoClientRequested(AdminBookNoClientRequestEvent event) {
		getApi().adminBookingNoClient(new BookingRequest(event.getBookId()),
				               new Callback<BookResponseHolder>() {
			@Override
			public void success(BookResponseHolder data, Response response) {				
					BA.getEventBus().post(new AdminBookNoClientResponseEvent(data));					
			}
			
			@Override
			public void failure(RetrofitError error) {				
				onRetrofitError(error);
				BA.getEventBus().post(new AdminBookNoClientResponseEvent(null));
			}
		});
	}

	@Subscribe
	public void onAdminBookFailRequested(AdminBookFailRequestEvent event) {
		getApi().adminBookingFail(new BookingRequest(event.getBookId()),
				               new Callback<BookResponseHolder>() {
			@Override
			public void success(BookResponseHolder data, Response response) {				
					BA.getEventBus().post(new AdminBookFailResponseEvent(data));
			}
			
			@Override
			public void failure(RetrofitError error) {				
				onRetrofitError(error);
				BA.getEventBus().post(new AdminBookFailResponseEvent(null));
			}
		});
	}


	@Subscribe
	public void onAdminClientsRequested(AdminClientsRequestEvent event) {
		getApi().companyClients(event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
				new Callback<ClientsResponseHolder>() {
					@Override
					public void success(ClientsResponseHolder data, Response response) {
						BA.getEventBus().post(new AdminClientsResponseEvent(data));
					}
					@Override
					public void failure(RetrofitError error) {
						onRetrofitError(error);
						BA.getEventBus().post(new AdminClientsResponseEvent(null));
					}
				});
	}

	@Subscribe
	public void onAdminNotRegisteredClientsRequested(AdminNotRegisteredClientsRequestEvent event) {
		getApi().companyClients(event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
				new Callback<ClientsResponseHolder>() {
					@Override
					public void success(ClientsResponseHolder data, Response response) {
						BA.getEventBus().post(new AdminNotRegisteredClientsResponseEvent(data));
					}
					@Override
					public void failure(RetrofitError error) {
						onRetrofitError(error);
						BA.getEventBus().post(new AdminNotRegisteredClientsResponseEvent(null));
					}
				});
	}


	@Subscribe
	public void onAdminClientRequested(AdminClientRequestEvent event) {
		getApi().companySingleClient(event.getSearchFilter().getId(),
				new Callback<ClientResponseHolder>() {
					@Override
					public void success(ClientResponseHolder data, Response response) {
						BA.getEventBus().post(new AdminClientResponseEvent(data));
					}
					@Override
					public void failure(RetrofitError error) {
						onRetrofitError(error);
						BA.getEventBus().post(new AdminClientResponseEvent(null));
					}
				});
	}


}
