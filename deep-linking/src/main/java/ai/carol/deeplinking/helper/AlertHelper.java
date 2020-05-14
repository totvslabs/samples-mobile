package ai.carol.deeplinking.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import ai.carol.deeplinking.R;
import ai.carol.deeplinking.activity.EditActivity;

public final class AlertHelper {

    private static final String PACKAGE = "com.clockinfieldtools";
    private static final String MARKET_URL = "market://details?id=" + PACKAGE;
    private static final String HTTPS_URL = "https://play.google.com/store/apps/details?id=" + PACKAGE;

    private AlertHelper() { }

    //region - Public

    public static void showPlayStoreAlert(@NonNull final Activity activity, @NonNull final Listener listener) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.clock_in_not_found)
                .setMessage(R.string.want_to_install_clock_in)
                .setCancelable(false)
                .setPositiveButton(R.string.yes_install, (dialog, which) -> {
                    openPlayStorePage(activity);
                    listener.onConfirm();
                })
                .show();
    }

    public static void showFillFieldsAlert(@NonNull final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.app_name)
                .setMessage(R.string.fill_all_fields)
                .setCancelable(false)
                .setPositiveButton(R.string.yes_right_away, (dialog, which) -> openEditActivity(activity))
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

    private static void openEditActivity(@NonNull final Activity activity) {
        final Intent editIntent = new Intent(activity, EditActivity.class);
        activity.startActivity(editIntent);
    }

    //endregion

    //region - Listener

    public interface Listener {
        void onConfirm();
    }

    //endregion

}
