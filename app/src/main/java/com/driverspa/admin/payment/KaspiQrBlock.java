package com.driverspa.admin.payment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.assist.BaseAssist;
import com.driverspa.model.Washer;
import com.driverspa.model.api.response.PaymentQrResponse;
import com.driverspa.model.api.response.PaymentQrResponseHolder;
import com.driverspa.util.ActivityForResult;
import com.driverspa.util.HttpClient;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminPaymentQrDeleteRequestEvent;
import com.driverspa.util.otto.ws.AdminPaymentQrDeleteResponseEvent;
import com.driverspa.util.otto.ws.AdminPaymentQrUploadRequestEvent;
import com.driverspa.util.otto.ws.AdminPaymentQrUploadResponseEvent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The "Kaspi QR" block of the payment settings (view_kaspi_qr_block): preview, upload or replace,
 * delete. The QR is stored on the car wash (image_upload/payment_qr) and shown to the client in
 * the payment window. The upload also ticks Kaspi QR in the accepted payment options.
 */
public class KaspiQrBlock {

	public interface Host {
		/** Starts the image picker from the activity or fragment that gets the result. */
		void startActivityForResult(Intent intent, int requestCode);

		/** The washer's payment QR (and maybe its payment options) changed: save and redraw. */
		void onPaymentQrChanged();
	}

	private final Activity activity;
	private final Host host;
	private final View previewFrame, none;
	private final ImageView preview;
	private final Button upload, delete;
	private Washer washer;
	private ProgressDialog progress;
	private boolean registered;

	public KaspiQrBlock(Activity activity, View root, Host host) {
		this.activity = activity;
		this.host = host;
		previewFrame = root.findViewById(R.id.kaspiQrPreviewFrame);
		preview = root.findViewById(R.id.kaspiQrPreview);
		none = root.findViewById(R.id.kaspiQrNone);
		upload = root.findViewById(R.id.kaspiQrUpload);
		delete = root.findViewById(R.id.kaspiQrDelete);
		upload.setOnClickListener(v -> pickImage());
		delete.setOnClickListener(v -> confirmDelete());
	}

	public void bind(Washer washer) {
		this.washer = washer;
		String url = washer != null ? washer.getKaspiQrUrl() : null;
		boolean has = url != null;
		previewFrame.setVisibility(has ? View.VISIBLE : View.GONE);
		none.setVisibility(has ? View.GONE : View.VISIBLE);
		delete.setVisibility(has ? View.VISIBLE : View.GONE);
		upload.setText(BA.str(has ? R.string.kaspi_qr_replace : R.string.kaspi_qr_upload));
		if (has) HttpClient.getPicasso().load(url).into(preview);
	}

	public void onResume() {
		if (registered) return;
		BA.getEventBus().register(this);
		registered = true;
	}

	public void onPause() {
		if (!registered) return;
		BA.getEventBus().unregister(this);
		registered = false;
	}

	private void pickImage() {
		if (washer == null) return;
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		host.startActivityForResult(intent, ActivityForResult.KASPI_QR_UPLOAD);
	}

	/** @return whether the result was the QR picker's */
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != ActivityForResult.KASPI_QR_UPLOAD) return false;
		if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null || washer == null) return true;
		Uri uri = data.getData();
		String mimeType = activity.getContentResolver().getType(uri);
		File file = copyToCache(uri, "image/png".equals(mimeType) ? ".png" : ".jpg");
		if (file == null) {
			ToastUtil.display(activity, BA.str(R.string.cant_open_image));
			return true;
		}
		showProgress(true);
		BA.getEventBus().post(new AdminPaymentQrUploadRequestEvent(file, mimeType, washer.getId()));
		return true;
	}

	private void confirmDelete() {
		if (washer == null) return;
		AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(activity);
		dialog.setTitle(BA.str(R.string.kaspi_qr_delete_q));
		dialog.setNeutralButton(BA.str(R.string.no_word), null);
		dialog.setPositiveButton(BA.str(R.string.yes_word), (d, which) -> {
			showProgress(true);
			BA.getEventBus().post(new AdminPaymentQrDeleteRequestEvent(washer.getId()));
		});
		dialog.show();
	}

	@Subscribe
	public void onPaymentQrUploaded(AdminPaymentQrUploadResponseEvent event) {
		if (applied(event.getResult(), true)) ToastUtil.display(activity, BA.str(R.string.kaspi_qr_uploaded));
	}

	@Subscribe
	public void onPaymentQrDeleted(AdminPaymentQrDeleteResponseEvent event) {
		if (applied(event.getResult(), false)) ToastUtil.display(activity, BA.str(R.string.kaspi_qr_deleted));
	}

	private boolean applied(PaymentQrResponseHolder result, boolean uploaded) {
		showProgress(false);
		if (result == null) return false; // the connection error is shown by BA
		if (!BaseAssist.isSuccess(result)) {
			ToastUtil.display(activity, TextUtils.isEmpty(result.getMessage()) ? BA.str(R.string.error_word) : result.getMessage());
			return false;
		}
		PaymentQrResponse data = result.getResponse();
		if (washer != null && data != null) {
			washer.setPaymentQr(data.getPaymentQr());
			// The backend ticks Kaspi QR as an accepted payment option on upload.
			if (uploaded && data.getPayOptions() != null) washer.setPayOptions(data.getPayOptions());
		}
		bind(washer);
		host.onPaymentQrChanged();
		return true;
	}

	/** Streams the picked image into the app's cache (direct paths fail under scoped storage). */
	private File copyToCache(Uri uri, String extension) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = activity.getContentResolver().openInputStream(uri);
			if (in == null) return null;
			File file = new File(activity.getCacheDir(), "kaspi_qr_" + System.currentTimeMillis() + extension);
			out = new FileOutputStream(file);
			byte[] buffer = new byte[8192];
			int read;
			while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
			out.flush();
			return file;
		} catch (Exception e) {
			return null;
		} finally {
			try { if (in != null) in.close(); } catch (Exception ignored) {}
			try { if (out != null) out.close(); } catch (Exception ignored) {}
		}
	}

	private void showProgress(boolean show) {
		if (activity.isFinishing() || activity.isDestroyed()) return;
		if (progress == null) {
			progress = new ProgressDialog(activity);
			progress.setTitle("");
			progress.setIndeterminate(true);
			progress.setCancelable(false);
			progress.setMessage(BA.str(R.string.loading_dots));
		}
		if (show) progress.show();
		else if (progress.isShowing()) progress.dismiss();
	}
}
