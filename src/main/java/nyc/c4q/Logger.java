package nyc.c4q;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by alessandro on 05/09/15.
 */
public class Logger {
    private static final String TAG = "C4Q";

    public static void debug(@NonNull String format, @Nullable Object... args) {
        Log.d(TAG, String.format(format, args));
    }

    public static void error(@NonNull String format, @Nullable Object... args) {
        Log.e(TAG, String.format(format, args));
    }

    public static void warn(@NonNull String format, @Nullable Object... args) {
        Log.w(TAG, String.format(format, args));
    }

    public static void info(@NonNull String format, @Nullable Object... args) {
        Log.i(TAG, String.format(format, args));
    }

}
