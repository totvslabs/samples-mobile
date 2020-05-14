package ai.carol.deeplinking.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.List;
import java.util.Locale;

import ai.carol.deeplinking.R;
import ai.carol.deeplinking.helper.AlertHelper;
import ai.carol.deeplinking.helper.ConverterHelper;
import ai.carol.deeplinking.helper.DatabaseHelper;
import ai.carol.deeplinking.model.ClockInObject;

public final class MainActivity extends AppCompatActivity {

    private static final String ZERO_TEXT = "0";
    private static final String EMPTY_TEXT = "-";

    private AppCompatTextView mTxtOrganization;
    private AppCompatTextView mTxtEnvironment;
    private AppCompatTextView mTxtEmail;
    private AppCompatTextView mTxtPassword;
    private AppCompatTextView mTxtAppScheme;
    private AppCompatTextView mTxtAppName;
    private AppCompatTextView mTxtAppIdentifier;
    private AppCompatTextView mTxtClockInsCounter;
    private AppCompatTextView mTxtClockIns;
    private AppCompatButton mBtnEdit;
    private AppCompatButton mBtnResetClockIns;
    private AppCompatButton mBtnSendData;

    //region - AppCompatActivity

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTxtOrganization = findViewById(R.id.txt_organization);
        mTxtEnvironment = findViewById(R.id.txt_environment);
        mTxtEmail = findViewById(R.id.txt_email);
        mTxtPassword = findViewById(R.id.txt_password);
        mTxtAppScheme = findViewById(R.id.txt_app_scheme);
        mTxtAppName = findViewById(R.id.txt_app_name);
        mTxtAppIdentifier = findViewById(R.id.txt_app_identifier);
        mTxtClockInsCounter = findViewById(R.id.txt_clockins_counter);
        mTxtClockIns = findViewById(R.id.txt_clockins);
        mBtnEdit = findViewById(R.id.btn_edit);
        mBtnResetClockIns = findViewById(R.id.btn_reset_clockins);
        mBtnSendData = findViewById(R.id.btn_send_data);

        mTxtClockIns.setMovementMethod(new ScrollingMovementMethod());

        mBtnEdit.setOnClickListener((view) -> startEditActivity());
        mBtnResetClockIns.setOnClickListener((view) -> resetClockIns());
        mBtnSendData.setOnClickListener((view) -> startSendDataActivity());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    //endregion

    //region - Private Action

    private void startEditActivity() {
        final Intent editIntent = new Intent(getBaseContext(), EditActivity.class);
        startActivity(editIntent);
    }

    private void startSendDataActivity() {
        final String organization = getString(mTxtOrganization);
        final String environment = getString(mTxtEnvironment);
        final String email = getString(mTxtEmail);
        final String password = getString(mTxtPassword);

        if (organization.isEmpty() || environment.isEmpty() || email.isEmpty() || password.isEmpty()) {
            AlertHelper.showFillFieldsAlert(this);
            return;
        }

        final Intent sendDataIntent = new Intent(getBaseContext(), SendDataActivity.class);

        sendDataIntent.putExtra(SendDataActivity.ORGANIZATION_EXTRA, organization);
        sendDataIntent.putExtra(SendDataActivity.ENVIRONMENT_EXTRA, environment);
        sendDataIntent.putExtra(SendDataActivity.EMAIL_EXTRA, email);
        sendDataIntent.putExtra(SendDataActivity.PASSWORD_EXTRA, password);
        sendDataIntent.putExtra(SendDataActivity.APP_SCHEME_EXTRA, getString(mTxtAppScheme));
        sendDataIntent.putExtra(SendDataActivity.APP_NAME_EXTRA, getString(mTxtAppName));
        sendDataIntent.putExtra(SendDataActivity.APP_IDENTIFIER_EXTRA, getString(mTxtAppIdentifier));

        startActivity(sendDataIntent);
    }

    private void resetClockIns() {
        final Context context = getApplicationContext();
        DatabaseHelper.saveClockIns(context, null);

        mTxtClockInsCounter.setText(ZERO_TEXT);
        mTxtClockIns.setText(EMPTY_TEXT);
    }

    //endregion

    //region - Private Helper

    private String getString(final AppCompatTextView textView) {
        return textView.getText().toString();
    }

    private void refreshData() {
        final Context context = getApplicationContext();

        mTxtOrganization.setText(DatabaseHelper.fetchOrganization(context));
        mTxtEnvironment.setText(DatabaseHelper.fetchEnvironment(context));
        mTxtEmail.setText(DatabaseHelper.fetchEmail(context));
        mTxtPassword.setText(DatabaseHelper.fetchPassword(context));
        mTxtAppScheme.setText(DatabaseHelper.fetchAppScheme(context));
        mTxtAppName.setText(DatabaseHelper.fetchAppName(context));
        mTxtAppIdentifier.setText(DatabaseHelper.fetchAppIdentifier(context));

        final String clockInsStr = DatabaseHelper.fetchClockIns(context);

        if (clockInsStr == null) {
            resetClockIns();
            return;
        }

        final List<ClockInObject> clockIns = ConverterHelper.getClockInsFromString(clockInsStr);
        final String clockInsCounter = String.format(Locale.getDefault(), "%d", clockIns.size());

        mTxtClockInsCounter.setText(clockInsCounter);
        mTxtClockIns.setText(clockInsStr);
    }

    //endregion

}
