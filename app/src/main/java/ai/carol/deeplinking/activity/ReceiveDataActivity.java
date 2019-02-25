package ai.carol.deeplinking.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import ai.carol.deeplinking.helper.ConverterHelper;
import ai.carol.deeplinking.helper.DatabaseHelper;
import ai.carol.deeplinking.manager.ClockInManager;
import ai.carol.deeplinking.model.ClockInObject;

public final class ReceiveDataActivity extends AppCompatActivity {

    private final ClockInManager mManager = new ClockInManager(this);

    //region - AppCompatActivity

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiveDataFromClockIn();
    }

    //endregion

    //region - Private Action

    private void receiveDataFromClockIn() {
        final List<ClockInObject> clockIns = mManager.getClockIns(getIntent());
        handleData(clockIns);

        if (isTaskRoot()) {
            final Context context = getApplicationContext();

            Intent mainIntent = new Intent(context, MainActivity.class);
            mainIntent = mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            startActivity(mainIntent);
        }

        finish();
    }

    //endregion

    //region - Private Helper

    private void handleData(final List<ClockInObject> clockIns) {
        final Context context = getApplicationContext();
        String clockInsStr = null;

        if (clockIns != null) {
            clockInsStr = ConverterHelper.getStringFromClockIns(clockIns);
        }

        DatabaseHelper.saveClockIns(context, clockInsStr);
    }

    //endregion



}
