package ai.carol.deeplinking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import ai.carol.deeplinking.manager.ClockInManager;

public final class SendDataActivity extends AppCompatActivity {

    static final String TENANT_EXTRA = "tenant_extra";
    static final String EMAIL_EXTRA = "email_extra";
    static final String PASSWORD_EXTRA = "password_extra";
    static final String APP_SCHEME_EXTRA = "app_scheme_extra";
    static final String APP_NAME_EXTRA = "app_name_extra";
    static final String APP_IDENTIFIER_EXTRA = "app_identifier_extra";

    private final ClockInManager mManager = new ClockInManager(this);

    //region - AppCompatActivity

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendDataToClockIn();
    }

    //endregion

    //region - Private Action

    private void sendDataToClockIn() {
        final Intent intent = getIntent();

        final String tenant = intent.getStringExtra(TENANT_EXTRA);
        final String email = intent.getStringExtra(EMAIL_EXTRA);
        final String password = intent.getStringExtra(PASSWORD_EXTRA);
        final String appScheme = intent.getStringExtra(APP_SCHEME_EXTRA);
        final String appName = intent.getStringExtra(APP_NAME_EXTRA);
        final String appIdentifier = intent.getStringExtra(APP_IDENTIFIER_EXTRA);

        mManager.startClockInActivity(this, tenant, email, password, appScheme, appName, appIdentifier);
        finish();
    }

    //endregion

}
