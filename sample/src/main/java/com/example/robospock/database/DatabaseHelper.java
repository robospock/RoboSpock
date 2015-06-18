package com.example.robospock.database;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import roboguice.inject.RoboApplicationProvider;

import java.sql.SQLException;

@Singleton
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    @Inject
    public DatabaseHelper(final RoboApplicationProvider<Application> application) {
        super(application.get(), "myDatabase", null, 1);
    }

    @Override
    public void onCreate(final SQLiteDatabase db, final ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, DatabaseObject.class);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final ConnectionSource connectionSource, final int oldVersion,
                          final int newVersion) {
        try {

            TableUtils.dropTable(connectionSource, DatabaseObject.class, true);

        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
        onCreate(db, connectionSource);

    }

}
