package eu.davidea.samples.flexibleadapter.overall;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.infrastructure.AppExecutors;
import eu.davidea.samples.flexibleadapter.persistence.db.FlexibleDao;
import eu.davidea.samples.flexibleadapter.persistence.db.FlexibleDatabase;
import eu.davidea.samples.flexibleadapter.persistence.db.OfflineBoundItems;

/**
 * @author Davide Steduto
 * @since 10/05/2018
 */
public class FlexibleRepository {

    private FlexibleDao flexibleDao;
    private AppExecutors appExecutors;

    @Inject
    public FlexibleRepository(FlexibleDatabase database, AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        this.flexibleDao = database.flexibleDao();
    }

    public LiveData<List<Feature>> getFeatures() {
        return new OfflineBoundItems<List<Feature>>(appExecutors) {
            @NonNull
            @Override
            protected LiveData<List<Feature>> loadFromDb() {
                return flexibleDao.getOverallItems();
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Feature> data) {
                return data == null || data.isEmpty();
            }

            @NonNull
            @Override
            protected List<Feature> createOfflineItems() {
                return createFeatures();
            }

            @Override
            protected void saveCallResult(@NonNull List<Feature> items) {
                flexibleDao.saveOverallItems(items);
            }
        }.asLiveData();
    }

    /*
     * List of CardView as entry list, showing the functionality of the library.
     */
    private List<Feature> createFeatures() {
        List<Feature> items = new ArrayList<>();

        // Main features
        items.add(new Feature(R.id.nav_selection_modes, R.string.selection_modes)
                .withDescription(R.string.selection_modes_description)
                .withIcon("cmd_select_all"));

        items.add(new Feature(R.id.nav_filter, R.string.filter)
                .withDescription(R.string.filter_description)
                .withIcon("cmd_filter_outline"));

        items.add(new Feature(R.id.nav_animator, R.string.animator)
                .withDescription(R.string.animator_description)
                .withIcon("cmd_chart_gantt"));

        items.add(new Feature(R.id.nav_headers_and_sections, R.string.headers_sections)
                .withDescription(R.string.headers_sections_description)
                .withIcon("cmd_sections"));

        items.add(new Feature(R.id.nav_expandable_sections, R.string.expandable_sections)
                .withDescription(R.string.expandable_sections_description)
                .withIcon("cmd_expandable"));

        items.add(new Feature(R.id.nav_multi_level_expandable, R.string.multi_level_expandable)
                .withDescription(R.string.multi_level_expandable_description)
                .withIcon("cmd_expandable"));

        items.add(new Feature(R.id.nav_endless_scrolling, R.string.endless_scrolling)
                .withDescription(R.string.endless_scrolling_description)
                .withIcon("cmd_playlist_play"));

        // Special use cases
        items.add(new Feature(R.id.nav_db_headers_and_sections, R.string.databinding)
                .withDescription(R.string.databinding_description)
                .withIcon("cmd_link"));

        items.add(new Feature(R.id.nav_model_holders, R.string.model_holders)
                .withDescription(R.string.model_holders_description)
                .withIcon("cmd_select_inverse"));

        items.add(new Feature(R.id.nav_instagram_headers, R.string.instagram_headers)
                .withDescription(R.string.instagram_headers_description)
                .withIcon("cmd_instagram"));

        items.add(new Feature(R.id.nav_staggered, R.string.staggered_layout)
                .withDescription(R.string.staggered_description)
                .withIcon("cmd-dashboard"));

        items.add(new Feature(R.id.nav_viewpager, R.string.viewpager)
                .withDescription(R.string.viewpager_description)
                .withIcon("cmd_view_carousel"));

        return items;
    }

}