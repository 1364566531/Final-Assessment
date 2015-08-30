package nyc.c4q;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sufeizhao on 8/30/15.
 */
public class BooksSQLiteHelper extends SQLiteOpenHelper {

    private static final String DB_books = "books";
    private static final int VERSION = 1;

    public BooksSQLiteHelper(Context context) {
        super(context, DB_books, null, VERSION);
    }

    private static BooksSQLiteHelper INSTANCE;

    public static synchronized BooksSQLiteHelper getInstance(Context context) {
        if (INSTANCE == null)
            INSTANCE = new BooksSQLiteHelper(context.getApplicationContext());

        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public static abstract class DataEntry implements BaseColumns {
        public static final String TABLE_NAME = "Books";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_ISBN = "isbn";
        public static final String COLUMN_ISBN13 = "isbn13";
        public static final String COLUMN_PUBLISHER = "publisher";
        public static final String COLUMN_PUBLISH_YEAR = "publish_year";
    }

    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
            DataEntry.TABLE_NAME + " (" +
            DataEntry.COLUMN_ID + " INTEGER," +
            DataEntry.COLUMN_TITLE + " TEXT," +
            DataEntry.COLUMN_AUTHOR + " TEXT," +
            DataEntry.COLUMN_ISBN + " TEXT," +
            DataEntry.COLUMN_ISBN13 + " TEXT," +
            DataEntry.COLUMN_PUBLISHER + " TEXT," +
            DataEntry.COLUMN_PUBLISH_YEAR + " INTEGER" + ")";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DataEntry.TABLE_NAME;

    public void insertData(List<Books> books) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DataEntry.TABLE_NAME, null, null);

        for (Books book : books) {
            insertRow(book);
        }
    }

    public void insertRow(Books book) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DataEntry.COLUMN_ID, book.getId());
        values.put(DataEntry.COLUMN_TITLE, book.getTitle());
        values.put(DataEntry.COLUMN_AUTHOR, book.getAuthor());
        values.put(DataEntry.COLUMN_ISBN, book.getIsbn());
        values.put(DataEntry.COLUMN_ISBN13, book.getIsbn13());
        values.put(DataEntry.COLUMN_PUBLISHER, book.getPublisher());
        values.put(DataEntry.COLUMN_PUBLISH_YEAR, book.getPublishyear());

        db.insertOrThrow(DataEntry.TABLE_NAME, null, values);
    }

    public List<String> loadData(String find, String[] what) {
        SQLiteDatabase db = getWritableDatabase();
        String[] projection = {
                DataEntry.COLUMN_ID,
                DataEntry.COLUMN_TITLE,
                DataEntry.COLUMN_AUTHOR,
                DataEntry.COLUMN_ISBN,
                DataEntry.COLUMN_ISBN13,
                DataEntry.COLUMN_PUBLISHER,
                DataEntry.COLUMN_PUBLISH_YEAR
        };

        List<String> data = new ArrayList<>();

        Cursor cursor = db.query(
                DataEntry.TABLE_NAME, projection, find, what, null, null, DataEntry.COLUMN_PUBLISHER);

        while (cursor.moveToNext()) {
            data.add(cursor.getInt(cursor.getColumnIndex(DataEntry.COLUMN_ID)) + " " +
                    cursor.getString(cursor.getColumnIndex(DataEntry.COLUMN_TITLE)) + " " +
                    cursor.getString(cursor.getColumnIndex(DataEntry.COLUMN_AUTHOR)) + " " +
                    cursor.getString(cursor.getColumnIndex(DataEntry.COLUMN_ISBN)) + " " +
                    cursor.getString(cursor.getColumnIndex(DataEntry.COLUMN_ISBN13)) + " " +
                    cursor.getString(cursor.getColumnIndex(DataEntry.COLUMN_PUBLISHER)) + " " +
                    cursor.getInt(cursor.getColumnIndex(DataEntry.COLUMN_PUBLISH_YEAR)));
        }

        cursor.close();
        return data;
    }
}
