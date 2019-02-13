package ai.carol.deeplinking.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

abstract class AppActivity extends AppCompatActivity {

    //region - Activity

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int contentView = getContentView();
        setContentView(contentView);

        findViewsById();
        addListeners();
    }

    //endregion

    //region - Abstract

    @SuppressWarnings("SameReturnValue")
    abstract int getContentView();

    abstract void findViewsById();

    abstract void addListeners();

    //endregion

}
