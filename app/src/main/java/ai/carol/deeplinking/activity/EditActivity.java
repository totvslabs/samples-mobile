package ai.carol.deeplinking.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;

import ai.carol.deeplinking.R;
import ai.carol.deeplinking.helper.DatabaseHelper;

public final class EditActivity extends AppCompatActivity {

    private AppCompatEditText mEdtTenant;
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

        mEdtTenant = findViewById(R.id.edt_tenant);
        mEdtEmail = findViewById(R.id.edt_email);
        mEdtPassword = findViewById(R.id.edt_password);
        mEdtAppScheme = findViewById(R.id.edt_app_scheme);
        mEdtAppName = findViewById(R.id.edt_app_name);
        mEdtAppIdentifier = findViewById(R.id.edt_app_identifier);
        mBtnSave = findViewById(R.id.btn_save);

        final Context context = getApplicationContext();
        mEdtTenant.setText(DatabaseHelper.fetchTenant(context));
        mEdtEmail.setText(DatabaseHelper.fetchEmail(context));
        mEdtPassword.setText(DatabaseHelper.fetchPassword(context));
        mEdtAppScheme.setText(DatabaseHelper.fetchAppScheme(context));
        mEdtAppName.setText(DatabaseHelper.fetchAppName(context));
        mEdtAppIdentifier.setText(DatabaseHelper.fetchAppIdentifier(context));

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
        DatabaseHelper.saveTenant(context, getString(mEdtTenant));
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
