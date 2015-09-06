package nyc.c4q;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by alessandro on 05/09/15.
 */
public class DateTimeUtils {
    private static final SimpleDateFormat SQL_DATE_FORMAT;
    static final TimeZone GMT_TIME_ZONE;
    static final TimeZone LOCAL_TIME_ZONE;

    static {
        GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");
        SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        SQL_DATE_FORMAT.setTimeZone(GMT_TIME_ZONE);
        LOCAL_TIME_ZONE = TimeZone.getDefault();
    }

    public static String toSqlDateTime(long time) {
        return toSqlDateTime(new Date(time));
    }

    public static String toSqlDateTime(Date date) {
        return SQL_DATE_FORMAT.format(date);
    }

    public static Date fromSqlDateTime(String date) throws ParseException {
        return SQL_DATE_FORMAT.parse(date);
    }
}
