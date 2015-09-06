package nyc.c4q;

import android.database.sqlite.SQLiteException;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.assertj.android.api.Assertions;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@RunWith (RobolectricTestRunner.class)
@Config (manifest = "src/main/AndroidManifest.xml", sdk = 18)
public class Part3LibraryTests {
    private LibraryActivity activity;
    private EditText input;
    private TextView display;
    private Button buttonMemberInfo;
    private Button buttonBookInfo;
    private Button buttonCheckedOut;
    private final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(LibraryActivity.class).create().get();
        Robolectric.getForegroundThreadScheduler().runOneTask();
        Robolectric.getBackgroundThreadScheduler().runOneTask();

        input = (EditText) Helpers.findViewByIdString(activity, "input_parameter");
        display = (TextView) Helpers.findViewByIdString(activity, "text_display");

        buttonMemberInfo = (Button) Helpers.findViewByIdString(activity, "button_getMember");
        buttonBookInfo = (Button) Helpers.findViewByIdString(activity, "button_getBook");
        buttonCheckedOut = (Button) Helpers.findViewByIdString(activity, "button_getCheckedOut");
    }

    @Test
    public void test11CheckMemberInfoQuery() {
        assertThat(input, notNullValue());
        assertThat(display, notNullValue());
        assertThat(buttonMemberInfo, notNullValue());

        input.setText("Richard Nixon");
        buttonMemberInfo.callOnClick();

        Assertions.assertThat(display).containsText(
            "id: 36\n" +
                "name: Richard Nixon\n" +
                "dob: 1/9/1913\n" +
                "location: Yorba Linda, California"
                                                   );
    }

    @Test
    public void test12CheckBookInfoQuery() {
        assertThat(input, notNullValue());
        assertThat(display, notNullValue());
        assertThat(buttonBookInfo, notNullValue());

        input.setText("1554681723");
        buttonBookInfo.callOnClick();

        Assertions.assertThat(display).containsText(
            "id: 54\n" +
                "title: The Art of Racing in the Rain\n" +
                "author: Garth Stein\n" +
                "isbn: 1554681723\n" +
                "isbn13: 9781554681723\n" +
                "publisher: Harper Collins\n" +
                "publication year: 2008"
                                                   );
    }

    @Test
    public void test13CheckCheckedOutQuery() {
        assertThat(input, notNullValue());
        assertThat(display, notNullValue());
        assertThat(buttonCheckedOut, notNullValue());

        input.setText("Millard Fillmore");
        buttonCheckedOut.callOnClick();

        // alessandro: wrong test. it should take into consideration the locale date format

        Assertions.assertThat(display).containsText(
            "name: Millard Fillmore\n" +
                "-----\n" +
                "title: The Tiger's Wife\n" +
                "author: Téa Obreht\n" +
                "checkout date: " + dateFormatter.format(new Date(115, 7, 11)) + "\n" +
                "due date: " + dateFormatter.format(new Date(115, 7, 25)) + "\n" +
                "-----\n" +
                "title: Shadows in Flight (Ender's Shadow, #5)\n" +
                "author: Orson Scott Card\n" +
                "checkout date: " + dateFormatter.format(new Date(115, 7, 19)) + "\n" +
                "due date: " + dateFormatter.format(new Date(115, 8, 2)) + ""
                                                   );
    }

    @Test
    public void test14CheckCheckoutFunctionality() {
        activity.checkOut(43, 17);

        assertThat(input, notNullValue());
        assertThat(display, notNullValue());
        assertThat(buttonCheckedOut, notNullValue());

        input.setText("Barack Obama");
        buttonCheckedOut.callOnClick();

        Calendar calendar = Calendar.getInstance();
        String today = dateFormatter.format(calendar.getTime());
        calendar.add(Calendar.DATE, 14);
        String dueDate = dateFormatter.format(calendar.getTime());

        Assertions.assertThat(display).containsText(
            "name: Barack Obama\n" +
                "-----\n" +
                "title: Gone Girl\n" +
                "author: Gillian Flynn\n" +
                "checkout date: " + today + "\n" +
                "due date: " + dueDate
                                                   );
    }

    @Test (expected = SQLiteException.class)
    public void test15CheckCheckoutConstrainFunctionality() {
        activity.checkOut(43, 17);

        // Another member cannot checkout a book, if already checked out by someone else
        activity.checkOut(25, 17);
    }

    @Test (expected = SQLiteException.class)
    public void test16CheckoutInvalidMemeberFunctionality() {
        long rowId = activity.checkOut(44, 100);
    }

    @Test (expected = SQLiteException.class)
    public void test17CheckoutInvalidBookFunctionality() {
        activity.checkOut(25, 101);
    }

    // alessandro: this test is wrong because Roboelectric uses only sqlite in memory
    // and every time the connection is open, the database will be reset
    @Test
    public void test18CheckCheckinFunctionality() {
        // we actually first have to checkout a book
        long rowId = activity.checkOut(43, 17);
        assertThat(rowId, greaterThan(-1L));

        activity.checkIn(43, 17);

        assertThat(input, notNullValue());
        assertThat(display, notNullValue());
        assertThat(buttonCheckedOut, notNullValue());

        input.setText("Barack Obama");
        buttonCheckedOut.callOnClick();

        Assertions.assertThat(display).containsText(
            "name: Barack Obama"
                                                   );
    }

    @Test
    public void test19CheckinInvalidValuesFunctionality() {
        // currenctly not checked out
        assertThat(activity.checkIn(43, 17), is(false));

        // invalid member
        assertThat(activity.checkIn(50, 1), is(false));

        // invalid book
        assertThat(activity.checkIn(43, 101), is(false));
    }

}
