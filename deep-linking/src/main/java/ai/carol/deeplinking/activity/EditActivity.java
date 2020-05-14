package ai.carol.deeplinking.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import ai.carol.deeplinking.R;
import ai.carol.deeplinking.helper.DatabaseHelper;

public final class EditActivity extends AppCompatActivity {

    private AppCompatEditText mEdtOrganization;
    private AppCompatEditText mEdtEnvironment;
    private AppCompatEditText mEdtEmail;
    private AppCompatEditText mEdtPassword;
    private AppCompatEditText mEdtAppScheme;
    private AppCompatEditText mEdtAppName;
    private AppCompatEditText mEdtAppIdentifier;
    private AppCompatButton mBtnSave;

    //region - AppCompatActivity

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mEdtOrganization = findViewById(R.id.edt_organization);
        mEdtEnvironment = findViewById(R.id.edt_environment);
        mEdtEmail = findViewById(R.id.edt_email);
        mEdtPassword = findViewById(R.id.edt_password);
        mEdtAppScheme = findViewById(R.id.edt_app_scheme);
        mEdtAppName = findViewById(R.id.edt_app_name);
        mEdtAppIdentifier = findViewById(R.id.edt_app_identifier);
        mBtnSave = findViewById(R.id.btn_save);

        final Context context = getApplicationContext();
        mEdtOrganization.setText(DatabaseHelper.fetchOrganization(context));
        mEdtEnvironment.setText(DatabaseHelper.fetchEnvironment(context));
        mEdtEmail.setText(DatabaseHelper.fetchEmail(context));
        mEdtPassword.setText(DatabaseHelper.fetchPassword(context));
        mEdtAppScheme.setText(DatabaseHelper.fetchAppScheme(context));
        mEdtAppName.setText(DatabaseHelper.fetchAppName(context));
        mEdtAppIdentifier.setText(DatabaseHelper.fetchAppIdentifier(context));

        mEdtAppScheme.setEnabled(false);
        mEdtAppName.setEnabled(false);
        mEdtAppIdentifier.setEnabled(false);

        mBtnSave.setOnClickListener((view) -> save());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    //endregion

    //region - Private Action

    private void save() {
        final Context context = getApplicationContext();
        DatabaseHelper.saveOrganization(context, getString(mEdtOrganization));
        DatabaseHelper.saveEnvironment(context, getString(mEdtEnvironment));
        DatabaseHelper.saveEmail(context, getString(mEdtEmail));
        DatabaseHelper.savePassword(context, getString(mEdtPassword));
        DatabaseHelper.saveAppScheme(context, getString(mEdtAppScheme));
        DatabaseHelper.saveAppName(context, getString(mEdtAppName));
        DatabaseHelper.saveAppIdentifier(context, getString(mEdtAppIdentifier));

        finish();
    }

    //endregion

    //region - Private Helper

    private String getString(final AppCompatEditText editText) {
        final Editable editable = editText.getText();

        if (editable == null) {
            return null;
        }

        return editable.toString().trim();
    }

    //endregion
}
