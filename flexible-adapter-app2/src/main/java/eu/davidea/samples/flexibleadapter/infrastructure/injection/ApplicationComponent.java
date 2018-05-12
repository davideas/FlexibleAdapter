package eu.davidea.samples.flexibleadapter.infrastructure.injection;

import javax.inject.Singleton;

import dagger.Component;
import eu.davidea.samples.flexibleadapter.FlexibleApplication;
import eu.davidea.samples.flexibleadapter.ui.MainActivity;
import eu.davidea.samples.flexibleadapter.ui.SplashActivity;

/**
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

    void inject(MainActivity activity);

}