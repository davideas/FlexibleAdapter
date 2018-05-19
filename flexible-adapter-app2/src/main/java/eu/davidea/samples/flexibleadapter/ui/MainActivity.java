package eu.davidea.samples.flexibleadapter.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;
import eu.davidea.flexibleadapter.utils.Log;
import eu.davidea.samples.flexibleadapter.BuildConfig;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.views.HeaderView;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private HeaderView mHeaderView;
    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;

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
        Timber.v("onCreate");

        initializeToolbar();
        initializeNavigationDrawer();
        initializeEmptyView();
    }

    private void initializeToolbar() {
        Timber.d("initializeToolbar as actionBar");
        mToolbar = findViewById(R.id.toolbar);
        mHeaderView = findViewById(R.id.toolbar_header_view);
        mHeaderView.bindTo(getString(R.string.app_name), getString(R.string.overall));
        // Toolbar will now take on default Action Bar characteristics
        setSupportActionBar(mToolbar);
    }

    private void initializeNavigationDrawer() {
        Timber.d("initializeNavigationDrawer");
        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        // Version
        TextView appVersion = mNavigationView.getHeaderView(0).findViewById(R.id.app_version);
        appVersion.setText(getString(R.string.about_version, FlexibleUtils.getVersionName(this)));
    }

    private void initializeEmptyView() {
        View emptyView = findViewById(R.id.empty_view);
        emptyView.animate().setDuration(1500L);
        emptyView.animate().alpha(1);
    }

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

}