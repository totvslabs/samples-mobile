package ai.carol.deeplinking.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import ai.carol.deeplinking.model.ClockInObject;

public final class ClockInManager {

    private static final String CLOCK_IN = "clockin";
    private static final String AUTHORITY_LOGIN = "login";
    private static final String PATH_OAUTH2 = "oauth2";
    private static final String PARAM_TENANT = "tenant";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_APP_SCHEME = "appScheme";
    private static final String PARAM_APP_NAME = "appName";
    private static final String PARAM_APP_IDENTIFIER = "appIdentifier";
    private static final String PARAM_DATA = "data";

    private final Context mContext;

    public ClockInManager(@NonNull final Context context) {
        mContext = context;
    }

    //region - Public

    public Intent buildIntent(@NonNull final String tenant,
                              @NonNull final String email,
                              @NonNull final String password,
                              @NonNull final String appScheme,
                              @NonNull final String appName,
                              @NonNull final String appIdentifier) {
        Uri.Builder builder = new Uri.Builder()
                .scheme(CLOCK_IN)
                .authority(AUTHORITY_LOGIN)
                .appendPath(PATH_OAUTH2)
                .appendQueryParameter(PARAM_TENANT, tenant)
                .appendQueryParameter(PARAM_EMAIL, email)
                .appendQueryParameter(PARAM_PASSWORD, password)
                .appendQueryParameter(PARAM_APP_SCHEME, appScheme)
                .appendQueryParameter(PARAM_APP_NAME, appName)
                .appendQueryParameter(PARAM_APP_IDENTIFIER, appIdentifier);

        final Uri uri = builder.build();

        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (!isIntentSave(intent)) {
            return null;
        }

        return intent;
    }

    public String receivedData(@NonNull  final Intent intent) {
        final Uri intentData = intent.getData();
        if (intentData == null) {
            return null;
        }

        final String host = intentData.getHost();
        if (host == null || !host.equals(CLOCK_IN)) {
            return null;
        }

        final String data = intentData.getQueryParameter(PARAM_DATA);
        if (data == null) {
            return null;
        }

        return data;
    }

    public List<ClockInObject> serializeReceivedData(final String data) {
        if (data == null) {
            return null;
        }

        final Type type = new TypeToken<List<ClockInObject>>(){}.getType();
        return new Gson().fromJson(data, type);
    }

    //endregion

    //region - Private

    private boolean isIntentSave(final Intent intent) {
        if (intent == null) {
            return false;
        }

        final PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);

        return activities.size() > 0;
    }

    //endregion

}
