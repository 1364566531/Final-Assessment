package nyc.c4q;

import android.database.Cursor;
import android.util.Log;

/**
 * Created by alessandro on 05/09/15.
 */
public class SQLUtils {
    private static final String LOG_TAG = SQLUtils.class.getSimpleName();

    public static void printCursorAndReset(String tag, Cursor cursor) {
        if (!BuildConfig.DEBUG) {
            return;
        }

        if (null != cursor) {
            int position = cursor.getPosition();
            final int cols = cursor.getColumnCount();
            int i;
            String name, value;
            StringBuffer buffer = new StringBuffer();

            for (i = 0; i < cols; i++) {
                name = cursor.getColumnName(i);
                buffer.append(name);
                buffer.append("\t");
                buffer.append("|");
                buffer.append("\t");
            }
            Log.d(LOG_TAG, buffer.toString());
            int size = buffer.length();

            buffer = new StringBuffer();
            for (i = 0; i < size; i++) {
                buffer.append("-");
            }
            Log.d(LOG_TAG, buffer.toString());

            if (!cursor.isAfterLast() && cursor.getCount() > 0) {
                if (cursor.isBeforeFirst()) {
                    cursor.moveToPosition(0);
                }
                do {
                    buffer = new StringBuffer("");
                    for (i = 0; i < cols; i++) {
                        name = cursor.getColumnName(i);
                        value = cursor.getString(i);
                        buffer.append(value);
                        buffer.append("\t");
                        buffer.append("|");
                        buffer.append("\t");
                    }
                    Log.d(tag, buffer.toString());

                } while (cursor.moveToNext());
            } else {
                Log.w(tag, "empty cursor");
            }
            cursor.moveToPosition(position);
        } else {
            Log.w(LOG_TAG, "null cursor");
        }
    }
}
