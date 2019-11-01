package ai.carol.deeplinking.helper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import ai.carol.deeplinking.BuildConfig;
import ai.carol.deeplinking.R;

public final class DatabaseHelper {

    private static final String DB_NAME = "ai.carol.deeplinking.database";

    private static final String APP_IDENTIFIER_KEY = "v1.appIdentifier";
    private static final String APP_NAME_KEY = "v1.appName";
    private static final String APP_SCHEME_KEY = "v1.appScheme";
    private static final String CLOCKINS_KEY = "v1.clockins";
    private static final String EMAIL_KEY = "v1.email";
    private static final String PASSWORD_KEY = "v1.password";
    private static final String TENANT_KEY = "v1.tenant";

    private DatabaseHelper() { }

    public static void saveAppIdentifier(@NonNull final Context context, final String appIdentifier) {
        save(context, APP_IDENTIFIER_KEY, appIdentifier);
    }
    public static String fetchAppIdentifier(@NonNull final Context context) {
        return fetch(context, APP_IDENTIFIER_KEY, getAppIdentifierDefault());
    }

    public static void saveAppName(@NonNull final Context context, final String appName) {
        save(context, APP_NAME_KEY, appName);
    }
    public static String fetchAppName(@NonNull final Context context) {
        return fetch(context, APP_NAME_KEY, getAppNameDefault(context));
    }

    public static void saveAppScheme(@NonNull final Context context, final String appScheme) {
        save(context, APP_SCHEME_KEY, appScheme);
    }
    public static String fetchAppScheme(@NonNull final Context context) {
        return fetch(context, APP_SCHEME_KEY, getAppSchemeDefault(context));
    }

    public static void saveClockIns(@NonNull final Context context, final String clockIns) {
        save(context, CLOCKINS_KEY, clockIns);
    }
    public static String fetchClockIns(@NonNull final Context context) {
        return fetch(context, CLOCKINS_KEY);
    }

    public static void saveEmail(@NonNull final Context context, final String email) {
        save(context, EMAIL_KEY, email);
    }
    public static String fetchEmail(@NonNull final Context context) {
        return fetch(context, EMAIL_KEY);
    }

    public static void savePassword(@NonNull final Context context, final String password) {
        save(context, PASSWORD_KEY, password);
    }
    public static String fetchPassword(@NonNull final Context context) {
        return fetch(context, PASSWORD_KEY);
    }

    public static void saveTenant(@NonNull final Context context, final String tenant) {
        save(context, TENANT_KEY, tenant);
    }
    public static String fetchTenant(@NonNull final Context context) {
        return fetch(context, TENANT_KEY);
    }

    //region - Private Helper

    private static void save(@NonNull final Context context, @NonNull final String key, final String value) {
        final SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(key, value);
        editor.apply();
    }

    private static String fetch(@NonNull final Context context, @NonNull final String key) {
        return fetch(context, key, null);
    }

    private static String fetch(@NonNull final Context context, @NonNull final String key, final String defaultValue) {
        final SharedPreferences preferences = getPreferences(context);
        return preferences.getString(key, defaultValue);
    }

    private static SharedPreferences getPreferences(@NonNull final Context context) {
        return context.getSharedPreferences(DB_NAME, Context.MODE_PRIVATE);
    }

    //endregion

    //region - Private Default

    private static String getAppIdentifierDefault() {
        return BuildConfig.APPLICATION_ID;
    }

    private static String getAppNameDefault(final @NonNull Context context) {
        return context.getString(R.string.app_name);
    }

    private static String getAppSchemeDefault(final @NonNull Context context) {
        return BuildConfig.SCHEME;
    }

    //endregion

}
