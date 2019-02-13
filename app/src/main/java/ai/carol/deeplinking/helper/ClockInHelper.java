package ai.carol.deeplinking.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import ai.carol.deeplinking.R;

public final class ClockInHelper {

    private static final String PACKAGE = "com.clockinfieldtools";
    private static final String MARKET_URL = "market://details?id=" + PACKAGE;
    private static final String HTTPS_URL = "https://play.google.com/store/apps/details?id=" + PACKAGE;

    private ClockInHelper() { }

    //region - Public

    public static void showPlayStoreAlert(@NonNull final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.clock_in_not_found)
                .setMessage(R.string.want_to_install_clock_in)
                .setCancelable(false)
                .setPositiveButton(R.string.yes_install, (dialog, which) -> openPlayStorePage(activity))
                .show();
    }

    //endregion

    //region - Private

    private static void openPlayStorePage(@NonNull final Activity activity) {
        final String action = Intent.ACTION_VIEW;

        try {
            activity.startActivity(new Intent(action, Uri.parse(MARKET_URL)));
        } catch (ActivityNotFoundException exception) {
            activity.startActivity(new Intent(action, Uri.parse(HTTPS_URL)));
        }
    }

    //endregion

}
