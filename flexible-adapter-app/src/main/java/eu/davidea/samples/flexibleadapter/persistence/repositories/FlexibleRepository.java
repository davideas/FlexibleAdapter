package eu.davidea.samples.flexibleadapter.persistence.repositories;

import android.arch.lifecycle.LiveData;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.infrastructure.AppExecutors;
import eu.davidea.samples.flexibleadapter.items.OverallItem;
import eu.davidea.samples.flexibleadapter.persistence.db.FlexibleDao;
import eu.davidea.samples.flexibleadapter.persistence.db.FlexibleDatabase;
import eu.davidea.samples.flexibleadapter.persistence.db.OfflineBoundItems;

/**
 * @author Davide Steduto
 * @since 19/09/2017
 */
public class FlexibleRepository {

    private FlexibleDao flexibleDao;
    private AppExecutors appExecutors;

    @Inject
    public FlexibleRepository(FlexibleDatabase database, AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        this.flexibleDao = database.flexibleDao();
    }

    public LiveData<List<OverallItem>> getOverallItems(Resources resources) {
        return new OfflineBoundItems<List<OverallItem>>(appExecutors) {
            @NonNull
            @Override
            protected LiveData<List<OverallItem>> loadFromDb() {
                return flexibleDao.getOverallItems();
            }

            @Override
            protected boolean shouldFetch(@Nullable List<OverallItem> data) {
                return data == null || data.isEmpty();
            }

            @NonNull
            @Override
            protected List<OverallItem> createOfflineItems() {
                return createOverallItems(resources);
            }

            @Override
            protected void saveCallResult(@NonNull List<OverallItem> items) {
                flexibleDao.saveOverallItems(items);
            }
        }.asLiveData();
    }

    /*
     * List of CardView as entry list, showing the functionality of the library.
     * TODO: Review the description of all examples.
     */
    private List<OverallItem> createOverallItems(Resources resources) {
        List<OverallItem> items = new ArrayList<>();

        items.add(new OverallItem(R.id.nav_selection_modes, resources.getString(R.string.selection_modes))
                .withDescription(resources.getString(R.string.selection_modes_description))
                .withIcon(resources.getDrawable(R.drawable.ic_select_all_grey600_24dp)));

        items.add(new OverallItem(R.id.nav_filter, resources.getString(R.string.filter))
                .withDescription(resources.getString(R.string.filter_description))
                .withIcon(resources.getDrawable(R.drawable.ic_filter_outline_grey600_24dp)));

        items.add(new OverallItem(R.id.nav_animator, resources.getString(R.string.animator))
                .withDescription(resources.getString(R.string.animator_description))
                .withIcon(resources.getDrawable(R.drawable.ic_chart_gantt_grey600_24dp)));

        items.add(new OverallItem(R.id.nav_headers_and_sections, resources.getString(R.string.headers_sections))
                .withDescription(resources.getString(R.string.headers_sections_description))
                .withIcon(resources.getDrawable(R.drawable.ic_sections_grey600_24dp)));

        items.add(new OverallItem(R.id.nav_expandable_sections, resources.getString(R.string.expandable_sections))
                .withDescription(resources.getString(R.string.expandable_sections_description))
                .withIcon(resources.getDrawable(R.drawable.ic_expandable_grey_600_24dp)));

        items.add(new OverallItem(R.id.nav_multi_level_expandable, resources.getString(R.string.multi_level_expandable))
                .withDescription(resources.getString(R.string.multi_level_expandable_description))
                .withIcon(resources.getDrawable(R.drawable.ic_expandable_grey_600_24dp)));

        items.add(new OverallItem(R.id.nav_endless_scrolling, resources.getString(R.string.endless_scrolling))
                .withDescription(resources.getString(R.string.endless_scrolling_description))
                .withIcon(resources.getDrawable(R.drawable.ic_playlist_play_grey600_24dp)));

        //Special Use Cases
        items.add(new OverallItem(R.id.nav_db_headers_and_sections, resources.getString(R.string.databinding))
                .withDescription(resources.getString(R.string.databinding_description))
                .withIcon(resources.getDrawable(R.drawable.ic_link_grey_600_24dp)));

        items.add(new OverallItem(R.id.nav_model_holders, resources.getString(R.string.model_holders))
                .withDescription(resources.getString(R.string.model_holders_description))
                .withIcon(resources.getDrawable(R.drawable.ic_select_inverse_grey600_24dp)));

        items.add(new OverallItem(R.id.nav_instagram_headers, resources.getString(R.string.instagram_headers))
                .withDescription(resources.getString(R.string.instagram_headers_description))
                .withIcon(resources.getDrawable(R.drawable.ic_instagram_grey600_24dp)));

        items.add(new OverallItem(R.id.nav_staggered, resources.getString(R.string.staggered_layout))
                .withDescription(resources.getString(R.string.staggered_description))
                .withIcon(resources.getDrawable(R.drawable.ic_dashboard_grey600_24dp)));

        items.add(new OverallItem(R.id.nav_viewpager, resources.getString(R.string.viewpager))
                .withDescription(resources.getString(R.string.viewpager_description))
                .withIcon(resources.getDrawable(R.drawable.ic_view_carousel_grey600_24dp)));

        return items;
    }

}