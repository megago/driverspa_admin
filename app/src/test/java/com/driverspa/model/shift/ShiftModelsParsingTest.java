package com.driverspa.model.shift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.driverspa.admin.shift.Shifts;
import com.driverspa.util.Converters;
import com.google.gson.Gson;

import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * The shift models against real answers of the backend (docs/shift.md), captured from a local
 * backend into src/test/resources/shift by the probe script android-fixtures.mjs: an operator
 * opens with 5 000 and the crew [st1], pays 300 to st1, puts in 2 000, a wash is paid 3 000 in
 * cash, a car stays in box 2; the count is 9 500 against 9 700 expected, 3 000 stay; the owner
 * writes off the 200 shortage. Parsed with the app's own Gson (Converters.gsonForApi).
 */
public class ShiftModelsParsingTest {

	private static final Gson GSON = Converters.gsonForApi();

	private static <T> T read(String name, Class<T> type) throws Exception {
		try (InputStream in = ShiftModelsParsingTest.class.getClassLoader().getResourceAsStream("shift/" + name + ".json")) {
			assertNotNull("fixture " + name, in);
			try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				return GSON.fromJson(reader, type);
			}
		}
	}

	@Test
	public void currentWithoutShift() throws Exception {
		ShiftCurrent c = read("current_none", ShiftResponses.Current.class).getData();
		assertNull(c.getShift());
		assertNull(c.getPrevious());
		assertFalse(c.getSettings().isRequired());
		assertEquals(2, c.getWashers().size());
		assertEquals("st1", c.getWashers().get(0).getUid());
	}

	@Test
	public void settings() throws Exception {
		ShiftSettings s = read("settings", ShiftResponses.Settings.class).getData();
		assertEquals(300, s.getDiscrepancyLimit());
		assertNull(s.getCloseTime());
		assertFalse(s.isAutoClose());
		assertEquals(60, s.getAutoCloseAfter());
	}

	@Test
	public void openedShift() throws Exception {
		ShiftResponses.One r = read("open", ShiftResponses.One.class);
		assertEquals("success", r.getStatus());
		Shift s = r.getData();
		assertTrue(s.isOpen());
		assertFalse(s.isLegacy());
		assertEquals(Double.valueOf(5000), s.getOpeningCash());
		assertNull(s.getOpeningExpected());
		assertEquals(1, s.getCrew().size());
		assertEquals("st1", s.getCrew().get(0).getUid());
		assertNotNull(s.getStartTime());
		assertEquals(5000, s.getOpeningFloat(), 0.001);
	}

	@Test
	public void errorEnvelope() throws Exception {
		ShiftResponses.One r = read("error_already_open", ShiftResponses.One.class);
		assertEquals("error", r.getStatus());
		assertNull(r.getData());
	}

	@Test
	public void cashApplied() throws Exception {
		assertTrue(read("cash", ShiftResponses.Cash.class).getData().isApplied());
	}

	@Test
	public void operatorDoesNotSeeExpectedCash() throws Exception {
		Shift op = read("current_open_operator", ShiftResponses.Current.class).getData().getShift();
		assertNull(op.getExpectedCash());
		Shift owner = read("current_open_owner", ShiftResponses.Current.class).getData().getShift();
		assertEquals(Double.valueOf(9700), owner.getExpectedCash());

		ShiftReport x = read("report_x_operator", ShiftResponses.Report.class).getData();
		assertFalse(x.isFinal());
		assertNull(x.getCash().getExpected());
		assertNull(x.getCash().getFlows());
		assertEquals(3000, x.getRevenue().getTotal(), 0.001);
		assertNotNull(x.getOpenOrders());
		assertEquals(1, x.getOpenOrders().getInBox().size());
		assertNotNull(x.getInfo());
		assertEquals("Shift Probe", x.getCarwashName());
	}

	@Test
	public void ownerXReport() throws Exception {
		ShiftReport x = read("report_x_owner", ShiftResponses.Report.class).getData();
		assertEquals(Double.valueOf(9700), x.getCash().getExpected());
		assertEquals(Double.valueOf(5000), x.getCash().getOpening());
		assertEquals(Double.valueOf(3000), x.getCash().getFlows().get("washes"));
		assertEquals(Double.valueOf(-300), x.getCash().getFlows().get("payouts"));
		assertEquals(Double.valueOf(2000), x.getCash().getFlows().get("cash_in"));
		assertEquals(3000, x.getRevenue().byPayment("C"), 0.001);
		assertEquals(1, x.getWashes().getCars());
		assertEquals(2, x.getCashMoves().size());
		ShiftReport.WasherPay w = x.getWashers().get(0);
		assertEquals("st1", w.getStaffUid());
		assertEquals(900, w.getEarned(), 0.001);
		assertEquals(300, w.getPaid(), 0.001);
		assertTrue(w.isOnShift());
		assertEquals(1, x.getBoxes().get(0).getCars());
	}

	@Test
	public void crewUpdated() throws Exception {
		Shift s = read("crew", ShiftResponses.One.class).getData();
		assertEquals(2, s.getCrew().size());
	}

	@Test
	public void blindCountAnswer() throws Exception {
		ShiftResponses.CountResult c = read("count", ShiftResponses.Count.class).getData();
		assertEquals(9500, c.getCountedCash(), 0.001);
		assertEquals(9700, c.getExpectedCash(), 0.001);
		assertEquals(-200, c.getDifference(), 0.001);
		assertEquals(300, c.getLimit());
		assertFalse(c.isCommentRequired());
	}

	@Test
	public void closeAndZReport() throws Exception {
		ShiftResponses.ClosedData closed = read("close", ShiftResponses.Closed.class).getData();
		assertFalse(closed.isOpen());
		assertEquals(Shift.KIND_OPERATOR, closed.getCloseKind());
		assertEquals(Double.valueOf(-200), closed.getDifference());
		assertEquals(Double.valueOf(3000), closed.getCashLeft());
		assertEquals(Double.valueOf(6500), closed.getCashRemoved());
		assertNotNull(closed.getReport());
		assertTrue(closed.getReport().isFinal());

		ShiftReport z = read("report_z", ShiftResponses.Report.class).getData();
		assertTrue(z.isFinal());
		assertEquals(Double.valueOf(9500), z.getCash().getCounted());
		assertEquals(Double.valueOf(-200), z.getCash().getDifference());
		assertEquals(Double.valueOf(3000), z.getCash().getLeft());
		assertEquals("не хватает 200", z.getComment());
		assertEquals(1, z.getInfo().getCounts().size());
		Instant from = z.getFrom();
		assertNotNull(from);
		assertTrue(!z.getTo().isBefore(from));
	}

	@Test
	public void correctionAndHistory() throws Exception {
		Shift s = read("correction", ShiftResponses.One.class).getData();
		assertEquals(1, s.getCorrections().size());
		assertTrue(s.getCorrections().get(0).isWriteoff());
		assertEquals(200, s.getCorrections().get(0).getAmount(), 0.001);

		ShiftResponses.HistoryData h = read("history", ShiftResponses.History.class).getData();
		assertEquals(1, h.getTotalCount());
		assertEquals(Double.valueOf(-200), h.getObjects().get(0).getDifference());
		assertEquals(Double.valueOf(3000), h.getObjects().get(0).getRevenue());
		assertNull(h.getObjects().get(0).getCounts());
	}

	@Test
	public void cashParsingAndSigns() {
		assertEquals(Double.valueOf(0), Shifts.parseCash("0"));
		assertEquals(Double.valueOf(1000.5), Shifts.parseCash("1 000,5"));
		assertNull(Shifts.parseCash("-1"));
		assertNull(Shifts.parseCash("1.234"));
		assertNull(Shifts.parseCash(""));
		assertTrue(Shifts.aboveLimit(-200.01, 200));
		assertFalse(Shifts.aboveLimit(-200, 200));
		assertEquals("3000", Shifts.plain(3000));
		assertEquals("2500.5", Shifts.plain(2500.5));
	}
}
