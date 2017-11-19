package eu.davidea.samples.flexibleadapter.infrastructure.injection;

import javax.inject.Singleton;

import dagger.Component;
import eu.davidea.samples.flexibleadapter.FlexibleApplication;
import eu.davidea.samples.flexibleadapter.ui.SplashActivity;
import eu.davidea.samples.flexibleadapter.ui.ViewPagerActivity;

/**
 * Annotated as a Singleton since we don't want to have multiple instances of a Single FlexibleDatabase.
 *
 * @author Davide Steduto
 * @since 18/11/2017
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    @Component.Builder
    interface Builder {

        Builder applicationModule(ApplicationModule module);

        ApplicationComponent build();

    }
    void inject(FlexibleApplication application);

    void inject(SplashActivity activity);

    void inject(ViewPagerActivity activity);

}