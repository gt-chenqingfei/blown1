package com.shuashuakan.android.spider;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.VisibleForTesting;

import com.shuashuakan.android.spider.event.EventEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.shuashuakan.android.spider.Utils.newArrayList;
import static com.shuashuakan.android.spider.event.EventEntry.TABLE;

/**
 * Created by twocity on 16/8/26.
 */

class SqlEventStorage implements EventStorage {
    private static final int VERSION = 1;

    @SuppressWarnings("unused")
    private final Context context;
    private EventsDbHelper dbHelper;

    SqlEventStorage(Context context) {
        this.context = context;
        this.dbHelper = new EventsDbHelper(context);
    }

    @Override
    public synchronized void put(EventEntry entry) throws IOException {
        putAll(newArrayList(entry));
    }

    @Override
    public synchronized void putAll(List<EventEntry> entries) throws IOException {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (EventEntry eventEntry : entries) {
                db.insertWithOnConflict(TABLE, null, new EventEntry.Builder().id(eventEntry.identity())
                        .rawData(eventEntry.rawData())
                        .build(), SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } catch (IllegalStateException | SQLException e) {
            throw new IOException(e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public synchronized void remove(EventEntry entry) throws IOException {
        removeAll(newArrayList(entry));
    }

    @Override
    public synchronized void removeAll(List<EventEntry> entries) throws IOException {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (EventEntry eventEntry : entries) {
                db.delete(TABLE, EventEntry.ID + " = ?", new String[]{eventEntry.identity()});
            }
            db.setTransactionSuccessful();
        } catch (IllegalStateException | SQLException e) {
            throw new IOException(e);
        } finally {
            db.endTransaction();
        }
    }

    @VisibleForTesting
    synchronized void clear() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE, null, null);
    }

    @Override
    public synchronized List<EventEntry> query(long size) throws IOException {
        Utils.checkState(size > 0, "size must > 0, but is: " + size);
        String orderBy = EventEntry.ID + " DESC";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, null, null, null, null, orderBy, String.valueOf(size));
        try {
            List<EventEntry> values = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                String identity = getString(cursor, EventEntry.ID);
                String rawData = getString(cursor, EventEntry.DATA);
                values.add(EventEntry.create(identity, rawData));
            }
            return values;
        } catch (SQLException e) {
            throw new IOException(e);
        } finally {
            Utils.closeQuietly(cursor);
        }
    }

    @Override
    public synchronized long size() throws IOException {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            return DatabaseUtils.queryNumEntries(db, TABLE);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    private static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
    }

    private static class EventsDbHelper extends SQLiteOpenHelper {

        private static final String CREATE_LIST = ""
                + "CREATE TABLE "
                + TABLE
                + "("
                + EventEntry.ID
                + " TEXT NOT NULL PRIMARY KEY,"
                + EventEntry.DATA
                + " TEXT NOT NULL"
                + ")";

        EventsDbHelper(Context context) {
            super(context, "events.db", null /* factory */, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_LIST);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
