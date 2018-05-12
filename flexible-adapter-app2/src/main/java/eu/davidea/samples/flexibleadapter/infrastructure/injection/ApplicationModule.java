package eu.davidea.samples.flexibleadapter.infrastructure.injection;

import android.app.Application;
import android.arch.persistence.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import eu.davidea.samples.flexibleadapter.FlexibleApplication;
import eu.davidea.samples.flexibleadapter.persistence.db.FlexibleDatabase;
import eu.davidea.samples.flexibleadapter.persistence.preferences.PreferencesService;
import timber.log.Timber;

/**
 * @author Davide Steduto
 * @since 18/11/2017
 */
@Module(includes = ViewModelModule.class)
public class ApplicationModule {

    private FlexibleApplication application;
    private final FlexibleDatabase database;

    public ApplicationModule(FlexibleApplication application) {
        this.application = application;

        Timber.w("Build inMemoryDatabase");
        this.database = Room.inMemoryDatabaseBuilder(
                application,
                FlexibleDatabase.class
        ).build();
    }

    @Provides
    @Singleton
    FlexibleDatabase provideDatabase() {
        return database;
    }

    @Provides
    @Singleton
    PreferencesService providesSharedPreferences(Application application) {
        return new PreferencesService(application);
    }

    @Provides
    @Singleton
    FlexibleApplication provideMyApplication() {
        return application;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return application;
    }

}