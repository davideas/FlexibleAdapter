package eu.davidea.samples.flexibleadapter.persistence.db;

import android.arch.lifecycle.LiveData;

import java.util.List;

import eu.davidea.samples.flexibleadapter.items.OverallItem;

/**
 * Dao for offline experience.
 *
 * @author Davide Steduto
 * @since 19/11/2017
 */
public interface FlexibleDao {

    LiveData<List<OverallItem>> getOverallItems();

    void saveOverallItems(List<OverallItem> items);

}