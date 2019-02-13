package ai.carol.deeplinking.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.widget.AppCompatButton;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import ai.carol.deeplinking.R;
import ai.carol.deeplinking.helper.ClockInHelper;
import ai.carol.deeplinking.manager.ClockInManager;
import ai.carol.deeplinking.model.ClockInObject;

public final class MainActivity extends AppActivity {

    private static final String ZERO_TEXT = "0";
    private static final String EMPTY_TEXT = "-";

    private TextView mTxtTenant;
    private TextView mTxtEmail;
    private TextView mTxtPassword;
    private TextView mTxtAppScheme;
    private TextView mTxtAppName;
    private TextView mTxtAppIdentifier;
    private TextView mTxtDataCounter;
    private TextView mTxtData;
    private AppCompatButton mBtnReset;
    private AppCompatButton mBtnGoToClockIn;

    private final ClockInManager mManager = new ClockInManager(this);

    //region - Activity

    @Override
    protected void onResume() {
        super.onResume();
        receiveClockInData();
    }

    //endregion

    //region - AppActivity

    @Override
    int getContentView() {
        return R.layout.activity_main;
    }

    @SuppressLint("SetTextI18n")
    @Override
    void findViewsById() {
        mTxtTenant = findViewById(R.id.txt_tenant);
        mTxtEmail = findViewById(R.id.txt_email);
        mTxtPassword = findViewById(R.id.txt_password);
        mTxtAppScheme = findViewById(R.id.txt_app_scheme);
        mTxtAppName = findViewById(R.id.txt_app_name);
        mTxtAppIdentifier = findViewById(R.id.txt_app_identifier);
        mTxtDataCounter = findViewById(R.id.txt_data_counter);
        mTxtData = findViewById(R.id.txt_data);
        mBtnReset = findViewById(R.id.btn_reset);
        mBtnGoToClockIn = findViewById(R.id.btn_go_to_clock_in);

        mTxtTenant.setText("deeplinking");
        mTxtEmail.setText("user@mycompany.com");
        mTxtPassword.setText("1a2B3#");
        mTxtAppScheme.setText("deeplinking");
        mTxtAppName.setText("My App");
        mTxtAppIdentifier.setText("ai.carol.deeplinking");
        mTxtData.setMovementMethod(new ScrollingMovementMethod());

        resetReceivedData();
    }

    @Override
    void addListeners() {
        mBtnReset.setOnClickListener((view) -> resetReceivedData());
        mBtnGoToClockIn.setOnClickListener((view) -> sendDataToClockIn());
    }

    //endregion

    //region - Private

    private void sendDataToClockIn() {
        final String tenant = mTxtTenant.getText().toString();
        final String email = mTxtEmail.getText().toString();
        final String password = mTxtPassword.getText().toString();
        final String appScheme = mTxtAppScheme.getText().toString();
        final String appName = mTxtAppName.getText().toString();
        final String appIdentifier = mTxtAppIdentifier.getText().toString();

        final Intent intent = mManager.buildIntent(tenant, email, password, appScheme, appName, appIdentifier);

        if (intent == null) {
            ClockInHelper.showPlayStoreAlert(MainActivity.this);
            return;
        }

        startActivity(intent);
    }

    private void receiveClockInData() {
        final Intent intent = getIntent();
        if (intent == null) {
            resetReceivedData();
            return;
        }

        final String data = mManager.receivedData(intent);

        intent.setData(null);

        if (data == null) {
            resetReceivedData();
            return;
        }

        mTxtData.setText(data);

        final List<ClockInObject> clockIns = mManager.serializeReceivedData(data);
        final String clockInsCounter = String.format(Locale.getDefault(), "%d", clockIns.size());

        mTxtDataCounter.setText(clockInsCounter);
        mTxtData.setText(data);
    }

    private void resetReceivedData() {
        mTxtDataCounter.setText(ZERO_TEXT);
        mTxtData.setText(EMPTY_TEXT);
    }

    //endregion

}
