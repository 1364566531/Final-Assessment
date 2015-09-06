package nyc.c4q;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.RawRes;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class LibraryActivity extends Activity {
    public EditText inputParameter;
    private TextView textDisplay;
    private DatabaseHelper databaseHelper;
    private static final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        inputParameter = (EditText) findViewById(R.id.input_parameter);
        textDisplay = (TextView) findViewById(R.id.text_display);
        databaseHelper = new DatabaseHelper(this);

        if (databaseHelper.isEmpty()) {
            new LoadDataAsyncTask().execute();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputParameter.getWindowToken(), 0);
    }

    public void checkOut(int memberId, int bookId) {
        // TODO This method is called when the member with the given ID checks
        //      out the book with the given ID. Update the system accordingly.
        //      The due date for the book is two weeks from today.
    }

    public boolean checkIn(int memberId, int bookId) {
        // TODO This method is called when the member with the given ID returns
        //      the book with the given ID. Update the system accordingly. If
        //      the member is returning the book on time, return true. If it's
        //      late, return false.

        return false;
    }

    public void button_getMember_onClick(View view) {
        hideKeyboard();
        String name = inputParameter.getText().toString();

        // TODO Display member information for the member with the given name.
        textDisplay.setText("");
        final Cursor cursor = databaseHelper.getMemberInfo(name, null);
        if (null != cursor && cursor.moveToNext()) {
            DatabaseHelper.MemberColumns.Wrapper member = DatabaseHelper.MemberColumns.Wrapper.create(cursor);

            textDisplay.setText(
                String.format(
                    "id: %d\nname: %s\ndob: %s\nlocation: %s, %s",
                    member.id, member.name, member.birthday, member.city, member.state));
            IOUtils.closeQuietly(cursor);
        }
    }

    public void button_getBook_onClick(View view) {
        hideKeyboard();
        String isbn = inputParameter.getText().toString();

        // TODO Display book information for the book with the given ISBN.

        textDisplay.setText("");
        final Cursor cursor = databaseHelper.getBookInfo(isbn);

        if (null != cursor && cursor.moveToNext()) {
            DatabaseHelper.BookColumns.Wrapper book = DatabaseHelper.BookColumns.Wrapper.create(cursor);
            DatabaseHelper.BookStatusColumns.Wrapper status = DatabaseHelper.BookStatusColumns.Wrapper.create(cursor);

            textDisplay.setText(
                String.format(
                    "id: %d\n" +
                        "title: %s\n" +
                        "author: %s\n" +
                        "isbn: %s\n" +
                        "isbn13: %s\n" +
                        "publisher: %s\n" +
                        "publication year: %d",
                    book.id, book.title, book.author,
                    TextUtils.isEmpty(book.isbn) ? "" : book.isbn,
                    TextUtils.isEmpty(book.isbn13) ? "" : book.isbn13,
                    book.publisher, book.publishYear));

            if (status.checkedOut) {

                final Date checkedDate;
                final Date dueDate;
                DatabaseHelper.MemberColumns.Wrapper member = null;

                try {
                    checkedDate = DateTimeUtils.fromSqlDateTime(status.checkedDate);
                    dueDate = DateTimeUtils.fromSqlDateTime(status.dueDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                    textDisplay.append("\n\n** Error parsing checked or due date **");
                    return;
                }

                Cursor memberCursor = databaseHelper.getMemberInfo(
                    status.checkedMemberId, new String[]{
                        DatabaseHelper.MemberColumns._ID,
                        DatabaseHelper.MemberColumns.NAME
                    });

                Logger.debug("member cursor: %s, count: %d", memberCursor, memberCursor.getCount());

                if (null != memberCursor && memberCursor.moveToFirst()) {
                    member = DatabaseHelper.MemberColumns.Wrapper.create(memberCursor);
                }

                textDisplay.append(
                    String.format(
                        "\n\nChecked out by: %s\nChecked date: %s\nDue date: %s",
                        null != member ? member.name : status.checkedMemberId,
                        dateFormatter.format(checkedDate),
                        dateFormatter.format(dueDate)));

                IOUtils.closeQuietly(memberCursor);
            }
            IOUtils.closeQuietly(cursor);
        }

    }

    public void button_getCheckedOut_onClick(View view) {
        String name = inputParameter.getText().toString();

        // TODO Display a list of books that the member with the given name
        //      currently has checked out, ordered by due date, with the
        //      earliest due first.

        hideKeyboard();
        long memberId = -1;
        String memberName = null;
        textDisplay.setText("");

        Cursor cursor = databaseHelper.getMemberInfo(
            name, new String[]{
                DatabaseHelper.MemberColumns._ID,
                DatabaseHelper.MemberColumns.NAME
            });

        if (null != cursor && cursor.moveToNext()) {
            memberId = cursor.getLong(0);
            memberName = cursor.getString(1);
            IOUtils.closeQuietly(cursor);
        }

        if (memberId == -1 || TextUtils.isEmpty(memberName)) {
            textDisplay.setText("No results");
            return;
        }

        textDisplay.setText(String.format("name: %s\n", memberName));
        textDisplay.append("-----\n");

        cursor = databaseHelper.getMemberCheckedOutList(memberId);

        if (null != cursor) {
            while (cursor.moveToNext()) {
                DatabaseHelper.BookColumns.Wrapper book = DatabaseHelper.BookColumns.Wrapper.create(cursor);
                DatabaseHelper.BookStatusColumns.Wrapper status = DatabaseHelper.BookStatusColumns.Wrapper.create(cursor);

                textDisplay.append(
                    String.format(
                        "title: %s\nauthor: %s\n",
                        book.title, book.author));

                Date checkedDate = null;
                Date dueDate = null;

                try {
                    checkedDate = DateTimeUtils.fromSqlDateTime(status.checkedDate);
                    dueDate = DateTimeUtils.fromSqlDateTime(status.dueDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                    textDisplay.append("** Error parsing checked or due date **\n");
                    return;
                }

                if (null != checkedDate && null != dueDate) {
                    textDisplay.append(
                        String.format(
                            "checkout date: %s\ndue date: %s\n",
                            dateFormatter.format(checkedDate),
                            dateFormatter.format(dueDate)));
                }

                if (!cursor.isLast()) {
                    textDisplay.append("-----\n");
                }
            }
            IOUtils.closeQuietly(cursor);
        }

    }

    private class LoadDataAsyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPostExecute(final Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            Cursor c = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM " + DatabaseHelper.BookColumns.TABLE_NAME,
                null);

            SQLUtils.printCursorAndReset("TABLE MEMBERS", c);
            IOUtils.closeQuietly(c);

            c = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM " + DatabaseHelper.BookStatusColumns.TABLE_NAME,
                null);

            SQLUtils.printCursorAndReset("TABLE BOOK", c);
            IOUtils.closeQuietly(c);

        }

        @Override
        protected Boolean doInBackground(final Void... params) {

            if (isFinishing()) {
                return false;
            }

            final SQLiteDatabase db;

            try {
                db = databaseHelper.getWritableDatabase();
            } catch (RuntimeException e) {
                e.printStackTrace();
                return false;
            }

            if (null == db || db.isReadOnly()) {
                Logger.error("db is null or not writeable");
                return false;
            }

            databaseHelper.empty(databaseHelper.getWritableDatabase());

            db.beginTransaction();

            try {
                if (loadMembers(db) && loadBooks(db)) {
                    db.setTransactionSuccessful();
                }
            } finally {
                db.endTransaction();
            }

            return true;
        }

        private boolean loadBooks(final SQLiteDatabase db) {
            final JSONArray json = loadJson(R.raw.books);
            if (null != json) {
                for (int i = 0; i < json.length(); i++) {
                    JSONObject book = json.optJSONObject(i);
                    if (null != book) {
                        int id = book.optInt("id", -1);
                        String title = book.optString("title");
                        String author = book.optString("author");
                        String isbn = book.optString("isbn");
                        String isbn13 = book.optString("isbn13");
                        String publisher = book.optString("publisher");
                        int publishYear = book.optInt("publishyear", -1);
                        boolean checkedOut = book.optBoolean("checkedout", false);
                        int checkedOutBy = book.optInt("checkedoutby", -1);
                        int checkoutdateyear = book.optInt("checkoutdateyear", -1);
                        int checkoutdatemonth = book.optInt("checkoutdatemonth", -1);
                        int checkoutdateday = book.optInt("checkoutdateday", -1);
                        int duedateyear = book.optInt("duedateyear", -1);
                        int duedatemonth = book.optInt("duedatemonth", -1);
                        int duedateday = book.optInt("duedateday", -1);

                        Date checkoutDate = null;
                        Date dueDate = null;

                        if (id == -1 || TextUtils.isEmpty(title) || (TextUtils.isEmpty(isbn) && TextUtils.isEmpty(isbn13))) {
                            continue;
                        }

                        Logger.debug("checkedout: %b, checkoutby: %d", checkedOut, checkedOutBy);

                        if (checkedOut
                            && checkedOutBy > -1
                            && (checkoutdateday > -1 && checkoutdatemonth > -1 && checkoutdateyear > -1)
                            && (duedateday > -1 && duedatemonth > -1 && duedateyear > -1)) {

                            checkoutDate = new Date(checkoutdateyear - 1900, checkoutdatemonth - 1, checkoutdateday);
                            dueDate = new Date(duedateyear - 1900, duedatemonth - 1, duedateday);
                        } else {
                            checkedOut = false;
                        }

                        long rowId =
                            databaseHelper
                                .insertBook(
                                    db, id, title, author, isbn, isbn13, publisher, publishYear, checkedOut,
                                    checkedOutBy, checkoutDate, dueDate);
                        Logger.debug("book{%d, %s} = %d", id, title, rowId);
                    }
                }
                return true;
            }

            return false;
        }

        private boolean loadMembers(final SQLiteDatabase db) {
            Logger.debug("loadMembers");
            final JSONArray json = loadJson(R.raw.members);

            if (null != json) {
                for (int i = 0; i < json.length(); i++) {
                    JSONObject member = json.optJSONObject(i);

                    if (null != member) {
                        int id = member.optInt("id", -1);
                        String name = member.optString("name");
                        int month = member.optInt("dob_month", -1);
                        int year = member.optInt("dob_year", -1);
                        int day = member.optInt("dob_day", -1);
                        String city = member.optString("city", "");
                        String state = member.optString("state", "");

                        if (id == -1 || TextUtils.isEmpty(name) || month == -1 || year == -1 || day == -1) {
                            Logger.error("invalid entry");
                            continue;
                        }

                        final String birthday = String.format("%d/%d/%d", month, day, year);
                        long rowId = databaseHelper.insertMember(db, id, name, birthday, city, state);
                        Logger.debug("member{%d, %s} = %d", id, name, rowId);
                    }
                }
                return true;
            }
            return false;
        }

        private JSONArray loadJson(@RawRes int resId) {
            final InputStream inputStream = getResources().openRawResource(resId);
            if (null == inputStream) {
                return null;
            }

            final String text;
            try {
                text = IOUtils.toString(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                IOUtils.closeQuietly(inputStream);
            }

            if (!TextUtils.isEmpty(text)) {
                try {
                    return new JSONArray(text);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }
}
