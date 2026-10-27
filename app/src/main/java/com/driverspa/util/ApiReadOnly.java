package com.driverspa.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A field the API sends but never accepts back: it is read from responses, kept when the
 * object is cached locally, and left out of request bodies ({@link Converters#gsonForApi()}).
 * The app PATCHes the whole car wash, so without this a server-managed field would be sent
 * back on every settings save.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ApiReadOnly {
}
