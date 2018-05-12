package eu.davidea.samples.flexibleadapter.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.view.Window;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;
import eu.davidea.flexibleadapter.utils.Log;
import eu.davidea.samples.flexibleadapter.BuildConfig;
import eu.davidea.samples.flexibleadapter.R;

public class MainActivity extends AppCompatActivity {

    /* ===================
     * ACTIVITY MANAGEMENT
     * =================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (FlexibleUtils.hasLollipop()) requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);

        if (FlexibleUtils.hasLollipop()) {
            getWindow().setEnterTransition(new Fade());
        }

        setContentView(R.layout.activity_main);
        if (BuildConfig.DEBUG) {
            FlexibleAdapter.enableLogs(Log.Level.VERBOSE);
        } else {
            FlexibleAdapter.enableLogs(Log.Level.SUPPRESS);
        }
        Log.v("onCreate");
    }

}