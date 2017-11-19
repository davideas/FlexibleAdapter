package eu.davidea.samples.flexibleadapter.persistence.preferences;

import android.app.Application;

/**
 * @author Davide
 * @since 30/09/2017
 */
public class PreferencesService extends AbstractPreferences {

    private static final String FIRST_TIME = "first_time";
    private static final String LOCAL_LANGUAGE = "local_language";

    /**
     * Instantiates a new Preferences Service.
     *
     * @param application Application Context
     */
    public PreferencesService(Application application) {
        super(application);
    }

    /* ==================
     * OTHERS PREFERENCES
     * ================== */

    public String getLocalLanguage() {
        return getString(LOCAL_LANGUAGE, "en-UK");
    }

    public void setLocalLanguage(String language) {
        putString(LOCAL_LANGUAGE, language);
    }

    public boolean isFirstTimeRunning() {
        return getBoolean(FIRST_TIME, true);
    }

    public void updateFirstTimeRunning(boolean firstTime) {
        putBoolean(FIRST_TIME, firstTime);
    }

}