package eu.davidea.samples.flexibleadapter.persistence.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.support.annotation.WorkerThread;

import java.util.List;

import eu.davidea.samples.flexibleadapter.overall.Feature;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Dao for offline experience.
 *
 * @author Davide Steduto
 * @since 10/05/2018
 */
@Dao
public interface FlexibleDao {

    @Query("select * from features")
    LiveData<List<Feature>> getOverallItems();

    @WorkerThread
    @Insert(onConflict = REPLACE)
    void saveOverallItems(List<Feature> items);

}