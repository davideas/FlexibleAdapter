package eu.davidea.samples.flexibleadapter.persistence.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import eu.davidea.samples.flexibleadapter.overall.Feature;

@Database(entities = {Feature.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverters.class})
public abstract class FlexibleDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "FlexibleDatabase.db";

    public abstract FlexibleDao flexibleDao();

}