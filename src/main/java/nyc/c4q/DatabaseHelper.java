package nyc.c4q;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Calendar;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int VERSION = 3;
    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

    public DatabaseHelper(final Context context) {
        super(context, "library.sqlite", null, VERSION);
    }

    @Override
    public void onConfigure(final SQLiteDatabase db) {
        super.onConfigure(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        MemberColumns.create(db);
        BookColumns.create(db);
        BookStatusColumns.create(db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        empty(db);
        onCreate(db);
    }

    public boolean isEmpty() {
        final SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(BookColumns.TABLE_NAME);
        Cursor cursor = builder.query(
            db, new String[]{BookColumns._ID}, BookColumns._ID + ">?", new String[]{"0"}, null, null, null, "1");
        int count = cursor.getCount();
        Logger.debug(LOG_TAG, "count: %d", count);
        cursor.close();
        return count == 0;

    }

    public void empty(@NonNull final SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS " + BookColumns.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MemberColumns.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + BookStatusColumns.TABLE_NAME);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public long insertMember(
        @Nullable final SQLiteDatabase db, int id, @NonNull final String name, @NonNull String dob, @Nullable final String city,
        @Nullable final String state) {

        if (null == db || !db.isOpen() || db.isReadOnly()) {
            Logger.error(LOG_TAG, "database not opened or readonly");
            return -1;
        }

        final ContentValues values = new ContentValues();
        values.put(MemberColumns._ID, id);
        values.put(MemberColumns.NAME, name);
        values.put(MemberColumns.DOB, dob);

        if (TextUtils.isEmpty(city)) {
            values.putNull(MemberColumns.CITY);
        } else {
            values.put(MemberColumns.CITY, city);
        }

        if (TextUtils.isEmpty(state)) {
            values.putNull(MemberColumns.STATE);
        } else {
            values.put(MemberColumns.STATE, state);
        }

        return db.insertWithOnConflict(MemberColumns.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public long insertBook(
        final SQLiteDatabase db, final int id, @NonNull final String title, @NonNull final String author,
        @Nullable final String isbn, @Nullable final String isbn13, final String publisher,
        final int publishYear, final boolean checkedOut, final int checkedOutBy,
        final Date checkoutDate, final Date dueDate) {

        if (null == db || !db.isOpen() || db.isReadOnly()) {
            throw new IllegalArgumentException("database not opened or readonly");
        }

        if (!db.inTransaction()) {
            throw new IllegalArgumentException("database must be in transaction");
        }

        ContentValues values = new ContentValues();
        values.put(BookColumns._ID, id);
        values.put(BookColumns.TITLE, title);
        values.put(BookColumns.AUTHOR, author);

        if (TextUtils.isEmpty(isbn)) {
            values.putNull(BookColumns.ISBN);
        } else {
            values.put(BookColumns.ISBN, isbn);
        }

        if (TextUtils.isEmpty(isbn13)) {
            values.putNull(BookColumns.ISBN13);
        } else {
            values.put(BookColumns.ISBN13, isbn13);
        }

        values.put(BookColumns.PUBLISHER, publisher);
        values.put(BookColumns.PUBLISH_YEAR, publishYear);
        long book_id = db.insertWithOnConflict(BookColumns.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        if (checkedOut) {
            values = new ContentValues();
            values.put(BookStatusColumns.BOOK_ID, book_id);
            values.put(BookStatusColumns.CHECKED_STATUS, 1);
            values.put(BookStatusColumns.CHECKED_MEMBER_ID, checkedOutBy);
            values.put(BookStatusColumns.CHECKED_DATE, DateTimeUtils.toSqlDateTime(checkoutDate));
            values.put(BookStatusColumns.DUE_DATE, DateTimeUtils.toSqlDateTime(dueDate));

            db.insertWithOnConflict(BookStatusColumns.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }

        return book_id;
    }

    public Cursor getMemberInfo(@NonNull final String name, @Nullable final String[] projection) {
        final SQLiteDatabase db = getReadableDatabase();
        if (null == db || !db.isOpen()) {
            return null;
        }
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(MemberColumns.TABLE_NAME);
        return builder.query(
            db, projection, MemberColumns.NAME + " LIKE '%" + name + "%'",
            null, null, null,
            MemberColumns.NAME + " " + "ASC");
    }

    public Cursor getMemberInfo(final long id, @Nullable final String[] projection) {
        final SQLiteDatabase db = getReadableDatabase();
        if (null == db || !db.isOpen()) {
            return null;
        }
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(MemberColumns.TABLE_NAME);

        return builder.query(
            db, projection, MemberColumns._ID + "=?",
            new String[]{String.valueOf(id)}, null, null, null);
    }

    public Cursor getBookInfo(final String isbn) {
        final SQLiteDatabase db = getReadableDatabase();
        if (null == db || !db.isOpen()) {
            return null;
        }
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        builder.setTables(
            BookColumns.TABLE_NAME + " LEFT JOIN "
                + BookStatusColumns.TABLE_NAME
                + " ON(" + BookColumns._ID + "="
                + BookStatusColumns.BOOK_ID + ")");

        return builder.query(
            db, null,
            BookColumns.TITLE + " LIKE '%" + isbn + "%' OR "
                + BookColumns.ISBN + " LIKE '%" + isbn + "%' OR "
                + BookColumns.ISBN13 + " LIKE '%" + isbn + "%'",
            null, null, null, null);
    }

    public Cursor getMemberCheckedOutList(final long id) {
        final SQLiteDatabase db = getReadableDatabase();
        if (null == db || !db.isOpen()) {
            return null;
        }
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        builder.setTables(
            BookColumns.TABLE_NAME + " LEFT JOIN "
                + BookStatusColumns.TABLE_NAME
                + " ON(" + BookColumns._ID + "="
                + BookStatusColumns.BOOK_ID + ")");

        return builder.query(
            db, null,
            BookStatusColumns.CHECKED_STATUS + "=1 AND " + BookStatusColumns.CHECKED_MEMBER_ID + "=?",
            new String[]{String.valueOf(id)}, null, null, BookStatusColumns.DUE_DATE + " ASC");
    }

    public long checkOut(final int memberId, final int bookId) throws SQLiteConstraintException {
        final SQLiteDatabase db = getWritableDatabase();
        if (null == db || !db.isOpen()) {
            return -1;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 14);

        ContentValues values = new ContentValues();
        values.put(BookStatusColumns.BOOK_ID, bookId);
        values.put(BookStatusColumns.CHECKED_STATUS, 1);
        values.put(BookStatusColumns.CHECKED_MEMBER_ID, memberId);
        values.put(BookStatusColumns.CHECKED_DATE, DateTimeUtils.toSqlDateTime(new Date()));
        values.put(BookStatusColumns.DUE_DATE, DateTimeUtils.toSqlDateTime(calendar.getTime()));

        return db.insertWithOnConflict(BookStatusColumns.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
    }

    public boolean checkIn(final int memberId, final int bookId) {
        final SQLiteDatabase db = getWritableDatabase();
        if (null == db || !db.isOpen()) {
            return false;
        }

        return db.delete(
            BookStatusColumns.TABLE_NAME,
            BookStatusColumns.BOOK_ID + "=? AND " + BookStatusColumns.CHECKED_MEMBER_ID + "=?",
            new String[]{String.valueOf(bookId), String.valueOf(memberId)}) == 1;
    }

    public static final class MemberColumns {
        static final String TABLE_NAME = "members";
        public static final String _ID = "member_id";
        public static final String NAME = "member_name";
        public static final String DOB = "member_dob";
        public static final String CITY = "member_city";
        public static final String STATE = "member_state";

        private static void create(@NonNull final SQLiteDatabase db) {
            if (null == db) {
                throw new IllegalArgumentException("db is null");
            }
            if (!db.isOpen() || db.isReadOnly()) {
                throw new IllegalArgumentException("db is not open or read only");
            }

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS "
                    + TABLE_NAME + "("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + NAME + " TEXT, "
                    + DOB + " TEXT, "
                    + CITY + " TEXT, "
                    + STATE + " TEXT"
                    + ")");
        }

        public static final class Wrapper {
            public long id;
            public String name;
            public String birthday;
            public String city;
            public String state;

            private Wrapper() { }

            public static final Wrapper create(@NonNull Cursor cursor) {
                int index;
                Wrapper wrapper = new Wrapper();

                if ((index = cursor.getColumnIndex(_ID)) > -1) {
                    wrapper.id = cursor.getLong(index);
                } else {
                    return null;
                }

                if ((index = cursor.getColumnIndex(NAME)) > -1) {
                    wrapper.name = cursor.getString(index);
                }

                if ((index = cursor.getColumnIndex(DOB)) > -1) {
                    wrapper.birthday = cursor.getString(index);
                }
                if ((index = cursor.getColumnIndex(CITY)) > -1) {
                    wrapper.city = cursor.getString(index);
                }
                if ((index = cursor.getColumnIndex(STATE)) > -1) {
                    wrapper.state = cursor.getString(index);
                }

                return wrapper;
            }
        }
    }

    public static final class BookColumns {
        static final String TABLE_NAME = "books";
        public static final String _ID = "book_id";
        public static final String TITLE = "book_title";
        public static final String AUTHOR = "book_author";
        public static final String ISBN = "book_isbn";
        public static final String ISBN13 = "book_isbn13";
        public static final String PUBLISHER = "book_publisher";
        public static final String PUBLISH_YEAR = "book_publish_year";

        private static void create(@NonNull final SQLiteDatabase db) {
            if (null == db) {
                throw new IllegalArgumentException("db is null");
            }
            if (!db.isOpen() || db.isReadOnly()) {
                throw new IllegalArgumentException("db is not open or read only");
            }

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS "
                    + TABLE_NAME + "("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TITLE + " TEXT, "
                    + AUTHOR + " TEXT, "
                    + ISBN + " TEXT, "
                    + ISBN13 + " TEXT, "
                    + PUBLISHER + " TEXT, "
                    + PUBLISH_YEAR + " INTEGER"
                    + ")");
        }

        public static final class Wrapper {
            public long id;
            public String title;
            public String author;
            public String isbn;
            public String isbn13;
            public String publisher;
            public int publishYear;

            private Wrapper() { }

            public static final Wrapper create(@NonNull Cursor cursor) {
                int index;
                Wrapper wrapper = new Wrapper();

                if ((index = cursor.getColumnIndex(_ID)) > -1) {
                    wrapper.id = cursor.getLong(index);
                } else {
                    return null;
                }

                if ((index = cursor.getColumnIndex(TITLE)) > -1) {
                    wrapper.title = cursor.getString(index);
                }

                if ((index = cursor.getColumnIndex(AUTHOR)) > -1) {
                    wrapper.author = cursor.getString(index);
                }
                if ((index = cursor.getColumnIndex(PUBLISHER)) > -1) {
                    wrapper.publisher = cursor.getString(index);
                }
                if ((index = cursor.getColumnIndex(PUBLISH_YEAR)) > -1) {
                    wrapper.publishYear = cursor.getInt(index);
                }
                if ((index = cursor.getColumnIndex(ISBN)) > -1) {
                    wrapper.isbn = cursor.getString(index);
                }
                if ((index = cursor.getColumnIndex(ISBN13)) > -1) {
                    wrapper.isbn13 = cursor.getString(index);
                }

                return wrapper;
            }
        }
    }

    public static final class BookStatusColumns {
        static final String TABLE_NAME = "book_status";
        public static final String _ID = "status_id";
        public static final String BOOK_ID = "status_book_id";
        public static final String CHECKED_STATUS = "book_checked_status";
        public static final String CHECKED_MEMBER_ID = "book_checked_member_id";
        public static final String CHECKED_DATE = "book_checked_date";
        public static final String DUE_DATE = "book_checked_due_date";

        private static void create(@NonNull final SQLiteDatabase db) {
            if (null == db) {
                throw new IllegalArgumentException("db is null");
            }
            if (!db.isOpen() || db.isReadOnly()) {
                throw new IllegalArgumentException("db is not open or read only");
            }

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS "
                    + TABLE_NAME + "("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + BOOK_ID + " INTEGER UNIQUE NOT NULL,"
                    + CHECKED_STATUS + " INTEGER NOT NULL DEFAULT 0, "
                    + CHECKED_MEMBER_ID + " INTEGER, "
                    + CHECKED_DATE + " DATETIME, "
                    + DUE_DATE + " DATETIME, "
                    + "FOREIGN KEY(" + BOOK_ID + ") REFERENCES "
                    + BookColumns.TABLE_NAME + "(" + BookColumns._ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY(" + CHECKED_MEMBER_ID + ") REFERENCES "
                    + MemberColumns.TABLE_NAME + "(" + MemberColumns._ID + ") ON DELETE RESTRICT "
                    + ")");

            db.execSQL(
                "CREATE INDEX status_book_id_index ON " + BookStatusColumns.TABLE_NAME + "(" + BookStatusColumns.BOOK_ID + ")");
            db.execSQL(
                "CREATE INDEX status_member_id_index ON " + BookStatusColumns.TABLE_NAME + "(" + BookStatusColumns.CHECKED_MEMBER_ID
                    + ")");
        }

        public static final class Wrapper {
            public long id;
            public long bookId;
            public boolean checkedOut;
            public long checkedMemberId;
            public String checkedDate;
            public String dueDate;

            private Wrapper() { }

            public static final Wrapper create(@NonNull Cursor cursor) {
                int index;
                Wrapper wrapper = new Wrapper();

                if ((index = cursor.getColumnIndex(_ID)) > -1) {
                    wrapper.id = cursor.getLong(index);
                } else {
                    return null;
                }

                if ((index = cursor.getColumnIndex(BOOK_ID)) > -1) {
                    wrapper.bookId = cursor.getLong(index);
                } else {
                    return null;
                }

                if ((index = cursor.getColumnIndex(CHECKED_STATUS)) > -1) {
                    wrapper.checkedOut = cursor.getInt(index) == 1;
                }

                if (wrapper.checkedOut) {
                    if ((index = cursor.getColumnIndex(CHECKED_MEMBER_ID)) > -1) {
                        wrapper.checkedMemberId = cursor.getLong(index);
                    }
                    if ((index = cursor.getColumnIndex(CHECKED_DATE)) > -1) {
                        wrapper.checkedDate = cursor.getString(index);
                    }
                    if ((index = cursor.getColumnIndex(DUE_DATE)) > -1) {
                        wrapper.dueDate = cursor.getString(index);
                    }
                }

                return wrapper;
            }
        }
    }
}
