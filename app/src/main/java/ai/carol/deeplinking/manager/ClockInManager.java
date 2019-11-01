package ai.carol.deeplinking.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.List;

import ai.carol.deeplinking.helper.AlertHelper;
import ai.carol.deeplinking.helper.ConverterHelper;
import ai.carol.deeplinking.model.ClockInObject;

public final class ClockInManager {

    private final Context mContext;

    public ClockInManager(@NonNull final Context context) {
        mContext = context;
    }

    public boolean startClockInActivity(@NonNull final Activity activity,
                                        @NonNull final String tenant,
                                        @NonNull final String email,
                                        @NonNull final String password,
                                        @NonNull final String appScheme,
                                        @NonNull final String appName,
                                        @NonNull final String appIdentifier,
                                        @NonNull final AlertHelper.Listener alertListener) {
        Uri.Builder builder = new Uri.Builder()
                .scheme("clockin")
                .authority("login")
                .appendPath("oauth2")
                .appendQueryParameter("tenant", tenant)
                .appendQueryParameter("email", email)
                .appendQueryParameter("password", password)
                .appendQueryParameter("appScheme", appScheme)
                .appendQueryParameter("appName", appName)
                .appendQueryParameter("appIdentifier", appIdentifier);

        final Uri uri = builder.build();

        Intent clockInIntent = new Intent(Intent.ACTION_VIEW, uri);
        clockInIntent = clockInIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);

        final PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(clockInIntent, 0);

        if (activities.size() > 0) {
            activity.startActivity(clockInIntent);
            return true;
        } else {
            AlertHelper.showPlayStoreAlert(activity, alertListener);
            return false;
        }
    }

    public List<ClockInObject> getClockIns(final Intent intent) {
        if (intent == null) {
            return null;
        }

        final Uri intentData = intent.getData();
        if (intentData == null) {
            return null;
        }

        // IMPORTANT - reset the data
        intent.setData(null);

        final String host = intentData.getHost();
        if (host == null || !host.equals("clockin")) {
            return null;
        }

        final String clockInsStr = intentData.getQueryParameter("data");
        if (clockInsStr == null) {
            return null;
        }

        return ConverterHelper.getClockInsFromString(clockInsStr);
    }

}
