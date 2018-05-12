package eu.davidea.samples.flexibleadapter.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.view.Window;

import eu.davidea.flexibleadapter.utils.FlexibleUtils;

/**
 * Starter activity without views, displaying splash screen as theme at the startup.
 * <p>See {@code res/values/style.xml}.</p>
 */
@SuppressWarnings("unchecked")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (FlexibleUtils.hasLollipop()) requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);

        if (FlexibleUtils.hasLollipop()) {
            getWindow().setExitTransition(new Fade());
        }

//        Intent intent = new Intent(this, MainActivity.class);
//        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
//        ActivityCompat.startActivity(this, intent, options.toBundle());
//        ActivityCompat.finishAfterTransition(this);
    }

}