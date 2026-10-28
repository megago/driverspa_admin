package com.driverspa.admin.shift;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.shift.Shift;
import com.driverspa.model.shift.ShiftReport;
import com.driverspa.util.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Z-report as a PDF (android.graphics.pdf.PdfDocument, no library), shared through the
 * app's FileProvider. Text is drawn with the system font, so Cyrillic and ₸ come out as on
 * screen (the platform falls back to a font that has the glyph), and the text stays text.
 */
public final class ShiftPdf {

	/** A4 in points. */
	private static final int PAGE_W = 595;
	private static final int PAGE_H = 842;
	private static final int MARGIN = 40;

	private static final int KIND_TITLE = 0;
	private static final int KIND_HEADING = 1;
	private static final int KIND_ROW = 2;
	private static final int KIND_TEXT = 3;
	private static final int KIND_RULE = 4;

	private static final class Line {
		final int kind;
		final String left;
		final String right;
		final boolean bold;

		Line(int kind, String left, String right, boolean bold) {
			this.kind = kind;
			this.left = left;
			this.right = right;
			this.bold = bold;
		}
	}

	private ShiftPdf() {
	}

	/** Writes the PDF to the cache and opens the share sheet. */
	public static void share(Activity activity, ShiftReport report) {
		try {
			File file = write(activity, report);
			Uri uri = FileProvider.getUriForFile(activity, ShiftFileProvider.authority(activity), file);
			Intent send = new Intent(Intent.ACTION_SEND);
			send.setType("application/pdf");
			send.putExtra(Intent.EXTRA_STREAM, uri);
			send.putExtra(Intent.EXTRA_SUBJECT, file.getName());
			send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			activity.startActivity(Intent.createChooser(send, BA.str(R.string.shift_pdf)));
		} catch (Exception e) {
			ToastUtil.display(activity, BA.str(R.string.shift_pdf_error));
		}
	}

	static File write(Activity activity, ShiftReport report) throws IOException {
		List<Line> lines = lines(report);
		PdfDocument doc = new PdfDocument();
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
		Paint muted = new Paint(paint);
		muted.setColor(Color.rgb(85, 85, 85));

		int pageNo = 0;
		PdfDocument.Page page = null;
		Canvas canvas = null;
		float y = 0;
		for (Line line : lines) {
			float height = heightOf(line);
			if (page == null || y + height > PAGE_H - MARGIN) {
				if (page != null) doc.finishPage(page);
				page = doc.startPage(new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, ++pageNo).create());
				canvas = page.getCanvas();
				y = MARGIN;
			}
			y = draw(canvas, line, y, paint, muted);
		}
		if (page != null) doc.finishPage(page);

		File dir = new File(activity.getCacheDir(), "shifts");
		if (!dir.exists() && !dir.mkdirs()) throw new IOException("no cache dir");
		Shift info = report.getInfo();
		String number = info != null ? info.getNumberText() : (report.getNumber() != null ? String.valueOf(report.getNumber()) : "");
		File file = new File(dir, "z-report-" + number + ".pdf");
		try (FileOutputStream out = new FileOutputStream(file)) {
			doc.writeTo(out);
		} finally {
			doc.close();
		}
		return file;
	}

	private static float heightOf(Line line) {
		switch (line.kind) {
			case KIND_TITLE: return 30;
			case KIND_HEADING: return 26;
			case KIND_RULE: return 10;
			default: return 16;
		}
	}

	private static float draw(Canvas canvas, Line line, float y, Paint paint, Paint muted) {
		switch (line.kind) {
			case KIND_TITLE:
				paint.setTextSize(18);
				paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
				canvas.drawText(line.left, MARGIN, y + 20, paint);
				return y + 30;
			case KIND_HEADING:
				paint.setTextSize(13);
				paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
				canvas.drawText(line.left, MARGIN, y + 20, paint);
				return y + 26;
			case KIND_RULE:
				paint.setStrokeWidth(1);
				canvas.drawLine(MARGIN, y + 5, PAGE_W - MARGIN, y + 5, paint);
				return y + 10;
			case KIND_TEXT:
				muted.setTextSize(10);
				muted.setTypeface(Typeface.DEFAULT);
				canvas.drawText(fit(line.left, muted, PAGE_W - 2 * MARGIN), MARGIN, y + 12, muted);
				return y + 16;
			default:
				paint.setTextSize(10.5f);
				paint.setTypeface(Typeface.create(Typeface.DEFAULT, line.bold ? Typeface.BOLD : Typeface.NORMAL));
				float rightWidth = line.right != null ? paint.measureText(line.right) : 0;
				canvas.drawText(fit(line.left, paint, PAGE_W - 2 * MARGIN - rightWidth - 12), MARGIN, y + 12, paint);
				if (line.right != null) canvas.drawText(line.right, PAGE_W - MARGIN - rightWidth, y + 12, paint);
				return y + 16;
		}
	}

	/** Cuts a text to a width, with an ellipsis. */
	private static String fit(String text, Paint paint, float width) {
		if (text == null) return "";
		if (paint.measureText(text) <= width) return text;
		int n = paint.breakText(text, true, width - paint.measureText("…"), null);
		return text.substring(0, Math.max(0, n)) + "…";
	}

	private static List<Line> lines(ShiftReport r) {
		List<Line> out = new ArrayList<>();
		Shift s = r.getInfo();
		if (r.getCarwashName() != null) out.add(new Line(KIND_TEXT, r.getCarwashName(), null, false));
		out.add(new Line(KIND_TITLE, BA.str(r.isFinal() ? R.string.shift_z_report : R.string.shift_x_report) + " · "
				+ Shifts.title(s), null, true));
		if (s != null) {
			out.add(row(BA.str(R.string.shift_opened), Shifts.dateTime(s.getStartTime()) + " · " + nz(s.getUserName())));
			if (s.getEndTime() != null) {
				String closed = Shifts.dateTime(s.getEndTime()) + " · " + nz(s.getClosedUserName());
				if (s.getCloseKind() != null && !Shift.KIND_OPERATOR.equals(s.getCloseKind())) {
					closed += " (" + Shifts.closeKindLabel(s.getCloseKind()) + ")";
				}
				out.add(row(BA.str(R.string.shift_closed), closed));
			}
			out.add(row(BA.str(R.string.shift_duration), Shifts.duration(s.getDuration())));
			if (s.getOpeningDifference() != null && Math.round(s.getOpeningDifference() * 100) != 0) {
				out.add(row(BA.str(R.string.shift_opening_difference), Shifts.signed(s.getOpeningDifference())
						+ (s.getOpeningComment() != null ? " · " + s.getOpeningComment() : "")));
			}
			if (s.getCrew() != null && !s.getCrew().isEmpty()) {
				StringBuilder crew = new StringBuilder();
				for (Shift.CrewMember m : s.getCrew()) {
					if (crew.length() > 0) crew.append(", ");
					crew.append(m.getName());
				}
				out.add(row(BA.str(R.string.shift_crew), crew.toString()));
			}
		}
		out.add(new Line(KIND_RULE, null, null, false));
		out.add(new Line(KIND_TEXT, Shifts.dateTime(r.getFrom()) + " — " + Shifts.dateTime(r.getTo()), null, false));

		out.add(heading(BA.str(R.string.shift_revenue)));
		out.add(new Line(KIND_ROW, BA.str(R.string.shift_revenue_total), Shifts.money(r.getRevenue().getTotal()), true));
		out.add(row(BA.str(R.string.shift_revenue_washes), Shifts.money(r.getRevenue().getWashes())));
		out.add(row(BA.str(R.string.shift_revenue_shop), Shifts.money(r.getRevenue().getShop())));
		for (String key : ShiftReport.PAYMENT_KEYS) {
			double amount = r.getRevenue().byPayment(key);
			if (amount != 0 || "C".equals(key)) out.add(row("   " + BA.str(ShiftReportView.paymentLabel(key)), Shifts.money(amount)));
		}

		ShiftReport.Washes w = r.getWashes();
		out.add(heading(BA.str(R.string.shift_washes)));
		out.add(row(BA.str(R.string.shift_cars), String.valueOf(w.getCars())));
		out.add(row(BA.str(R.string.shift_average), Shifts.money(w.getAverage())));
		out.add(row(BA.str(R.string.shift_discounts), Shifts.money(w.getDiscounts())));
		out.add(row(BA.str(R.string.shift_cancellations), String.valueOf(r.getCancellations())));
		out.add(row(BA.str(R.string.shift_no_shows), String.valueOf(r.getNoShows())));
		if (r.getShop().getSales() > 0 || r.getShop().getCanceled() > 0) {
			out.add(row(BA.str(R.string.shift_shop_sales), String.valueOf(r.getShop().getSales())));
		}

		ShiftReport.Cash cash = r.getCash();
		out.add(heading(BA.str(R.string.shift_drawer)));
		if (cash.getOpening() != null) out.add(row(BA.str(R.string.shift_opening_cash), Shifts.money(cash.getOpening())));
		Map<String, Double> flows = cash.getFlows();
		if (flows != null) {
			for (String key : ShiftReport.FLOW_KEYS) {
				Double v = flows.get(key);
				if (v != null && Math.round(v * 100) != 0) out.add(row("   " + BA.str(ShiftReportView.flowLabel(key)), Shifts.signed(v)));
			}
		}
		if (cash.getExpected() != null) out.add(new Line(KIND_ROW, BA.str(R.string.shift_expected), Shifts.money(cash.getExpected()), true));
		if (r.isFinal()) {
			if (cash.getCounted() != null) {
				double diff = cash.getDifference() != null ? cash.getDifference() : 0;
				out.add(new Line(KIND_ROW, BA.str(R.string.shift_counted), Shifts.money(cash.getCounted()), true));
				out.add(new Line(KIND_ROW, BA.str(R.string.shift_difference), Shifts.signed(diff) + " (" + Shifts.toneText(diff) + ")", true));
			} else {
				out.add(row(BA.str(R.string.shift_counted), BA.str(R.string.shift_not_counted)));
			}
			if (r.getComment() != null && !r.getComment().isEmpty()) out.add(row(BA.str(R.string.comment), r.getComment()));
			out.add(row(BA.str(R.string.shift_left), Shifts.money(cash.getLeft())));
			out.add(row(BA.str(R.string.shift_removed), Shifts.money(cash.getRemoved())));
		}

		if (r.getCashMoves() != null && !r.getCashMoves().isEmpty()) {
			out.add(heading(BA.str(R.string.shift_cash_moves)));
			for (ShiftReport.CashMove m : r.getCashMoves()) {
				String label = Shifts.time(m.getTs()) + "  " + Shifts.cashKindLabel(m.getKind())
						+ (m.getStaffName() != null ? " · " + m.getStaffName() : "")
						+ (m.getReason() != null ? " · " + m.getReason() : "");
				out.add(row(label, Shifts.signed(m.getSignedAmount())));
			}
		}
		if (r.getWashers() != null && !r.getWashers().isEmpty()) {
			out.add(heading(BA.str(R.string.shift_washer_pay)));
			for (ShiftReport.WasherPay p : r.getWashers()) {
				out.add(row(p.getName(), BA.str(R.string.shift_washer_line, p.getCars(), Shifts.money(p.getEarned()), Shifts.money(p.getPaid()))));
			}
		}
		if (s != null && s.getCorrections() != null && !s.getCorrections().isEmpty()) {
			out.add(heading(BA.str(R.string.shift_corrections)));
			for (Shift.Correction c : s.getCorrections()) {
				String what = c.isWriteoff() ? BA.str(R.string.shift_corr_writeoff) + " " + Shifts.money(c.getAmount())
						: BA.str(R.string.shift_corr_note);
				out.add(new Line(KIND_TEXT, Shifts.dateTime(c.getTs()) + " · " + nz(c.getUserName()) + " · " + what + " · " + nz(c.getComment()), null, false));
			}
		}
		return out;
	}

	private static Line row(String left, String right) {
		return new Line(KIND_ROW, left, right, false);
	}

	private static Line heading(String text) {
		return new Line(KIND_HEADING, text, null, true);
	}

	private static String nz(String s) {
		return s != null ? s : "—";
	}
}
