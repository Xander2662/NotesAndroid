package ondrej.wrubel.myapplication;

import android.content.Context;
import android.util.DisplayMetrics;

public class Utils {
    // Calculates the number of columns for a given desired column width (in dp)
    public static int calculateNoOfColumns(Context context, float columnWidthDp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for rounding
        return noOfColumns;
    }
}
