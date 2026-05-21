package com.driverspa.db;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.time.Instant;

public class InstantPersister extends BaseDataType {

    private static final InstantPersister SINGLETON = new InstantPersister();

    public static InstantPersister getSingleton() {
        return SINGLETON;
    }

    private InstantPersister() {
        super(SqlType.STRING, new Class<?>[]{Instant.class});
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) {
        return Instant.parse(defaultStr);
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getString(columnPos);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        return sqlArg == null ? null : Instant.parse((String) sqlArg);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        return javaObject == null ? null : javaObject.toString();
    }
}

