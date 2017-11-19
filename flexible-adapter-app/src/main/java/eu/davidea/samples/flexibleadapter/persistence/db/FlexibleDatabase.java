package eu.davidea.samples.flexibleadapter.persistence.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import eu.davidea.samples.flexibleadapter.items.OverallItem;

@Database(entities = {OverallItem.class}, version = 1)
@TypeConverters({DateConverters.class, EnumConverters.class})
public abstract class FlexibleDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "FlexibleDatabase.db";

    public abstract FlexibleDao flexibleDao();

}