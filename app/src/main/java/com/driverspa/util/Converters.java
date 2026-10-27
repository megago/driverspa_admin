package com.driverspa.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Locale;

public class Converters {

	public static final String PHONE_PATTERN = "## ### ### ####";
	public static final String PIN_PATTERN = 	"##-##-##";

	private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[] {
		DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
		DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
		DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US),
		DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
		DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US),
		DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
	};
	private static final DateTimeFormatter OUTPUT_DATE_FORMAT =
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).withZone(ZoneOffset.UTC);

	private static String normalizeIsoDate(String value) {
		if (value == null) {
			return null;
		}
		if (value.endsWith("Z")) {
			return value.substring(0, value.length() - 1) + "Z";
		}
		return value.replaceFirst("([+-]\\d{2}):(\\d{2})$", "$1$2");
	}

	public static Gson gsonWithDate() {
		return builderWithDate().create();
	}

	/** {@link #gsonWithDate()} for API requests: fields marked {@link ApiReadOnly} are not sent. */
	public static Gson gsonForApi() {
		return builderWithDate()
				.addSerializationExclusionStrategy(new ExclusionStrategy() {
					@Override
					public boolean shouldSkipField(FieldAttributes f) {
						return f.getAnnotation(ApiReadOnly.class) != null;
					}

					@Override
					public boolean shouldSkipClass(Class<?> clazz) {
						return false;
					}
				})
				.create();
	}

	private static GsonBuilder builderWithDate() {
		final GsonBuilder builder = new GsonBuilder();
//		builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
//		builder.excludeFieldsWithoutExposeAnnotation();
		builder.registerTypeAdapter(Instant.class, new InstantSerializer());
		return builder;
	}
//	
	public static Instant parseInstant(String rawValue) {
		if (rawValue == null || rawValue.trim().isEmpty()) {
			return null;
		}

		String normalizedValue = normalizeIsoDate(rawValue);

		try {
			// Parse as OffsetDateTime (preserves explicit timezone) and convert to UTC Instant
			return OffsetDateTime.parse(rawValue).toInstant();
		} catch (DateTimeParseException ignored) {
			// Keep trying java.time fallbacks below.
		}

		if (normalizedValue != null && !normalizedValue.equals(rawValue)) {
			try {
				// All backend timestamps are UTC; parse and convert to UTC Instant
				return OffsetDateTime.parse(normalizedValue, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)).toInstant();
			} catch (DateTimeParseException ignored) {
				// Keep fallback parsing for legacy backend formats.
			}
		}

		for (DateTimeFormatter formatter : DATE_FORMATS) {
			Instant parsed = tryParseWithFormatter(rawValue, formatter);
			if (parsed != null) {
				return parsed;
			}

			if (normalizedValue != null && !normalizedValue.equals(rawValue)) {
				parsed = tryParseWithFormatter(normalizedValue, formatter);
				if (parsed != null) {
					return parsed;
				}
			}
		}

		throw new JsonParseException("Unparseable date: \"" + rawValue
				+ "\". Supported formats: " + Arrays.toString(DATE_FORMATS));
	}

	public static String formatInstant(Instant instant) {
		return instant == null ? null : OUTPUT_DATE_FORMAT.format(instant);
	}

	private static Instant tryParseWithFormatter(String value, DateTimeFormatter formatter) {
		try {
			TemporalAccessor parsed = formatter.parse(value);
			if (parsed.isSupported(ChronoField.INSTANT_SECONDS) || parsed.isSupported(ChronoField.OFFSET_SECONDS)) {
				// Explicit timezone info present; convert to UTC Instant
				return OffsetDateTime.from(parsed).toInstant();
			}
			// All backend timestamps without explicit timezone are treated as UTC
			return LocalDateTime.from(parsed).atZone(ZoneId.of("UTC")).toInstant();
		} catch (DateTimeParseException ignored) {
			return null;
		} catch (DateTimeException ignored) {
			return null;
		}
	}

	static class InstantSerializer implements JsonDeserializer<Instant>, JsonSerializer<Instant> {

		@Override
		public Instant deserialize(JsonElement jsonElement, Type typeOF,
				JsonDeserializationContext context) throws JsonParseException {
			return parseInstant(jsonElement.getAsString());
		}

		@Override
		public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
			return src == null ? null : new JsonPrimitive(formatInstant(src));
		}
	}

////
////	public static Gson gsonAuthForget() {
////		final GsonBuilder builder = new GsonBuilder();
////		
////		builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
////		builder.registerTypeAdapter(AuthForgotResponseHolder.class, new AuthForgotResponseHolderSeriazlizer());
////		return builder.create();
////	}
//	
//	static class AuthForgotResponseHolderSerializer implements JsonDeserializer<AuthForgotResponseHolder> {
//
//		@Override
//		public AuthForgotResponseHolder deserialize(JsonElement jsonElement, Type typeOF,
//				JsonDeserializationContext context) throws JsonParseException {
//			AuthForgotResponseHolder result;
//			
//			Gson gson = new Gson();
//			
//			JsonObject jsonObject = jsonElement.getAsJsonObject();
//			int status = jsonObject.get("status").getAsInt();
//			
//			if( status != 0 ) {
//				result = gson.fromJson(jsonElement, AuthForgotResponseHolder.class);
//			} else {
//				String responseString = jsonObject.get("response").getAsString();
//				result = new AuthForgotResponseHolder();
//				result.setResponseString(responseString);
//				result.setResponse(null);
//			}
//			return result;
//		}
//	}
//	
//	static class StatusGetResponseHolderSerializer implements JsonDeserializer<StatusGetResponseHolder> {
//
//		@Override
//		public StatusGetResponseHolder deserialize(JsonElement jsonElement, Type typeOF,
//				JsonDeserializationContext context) throws JsonParseException {
//			
//			StatusGetResponseHolder result = new StatusGetResponseHolder();
//			
//			Gson gson = new Gson();
//			
//			JsonObject jsonObject = jsonElement.getAsJsonObject();
//			int status = jsonObject.get("status").getAsInt();
//			
//			if( status != 0 ) {
//				BaseErrorResponse errorResponse = gson.fromJson(jsonObject.get("response"), BaseErrorResponse.class);
//				result.setErrorResponse(errorResponse);
//			} else {
//				result = gson.fromJson(jsonElement, StatusGetResponseHolder.class);
//			}
//			
//			return result;
//		}
//		
//	}
//	
	/**
	 * This method converts dp unit to equivalent pixels, depending on device density.
	 * 
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float convertDpToPixel(float dp, Context context){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	/**
	 * This method converts device specific pixels to density independent pixels.
	 * 
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / (metrics.densityDpi / 160f);
		return dp;
	}



}
