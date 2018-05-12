package eu.davidea.samples.flexibleadapter.persistence.preferences;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

abstract class AbstractPreferences {

    private final SharedPreferences sharedPreferences;

	/*-------------*/
    /* CONSTRUCTOR */
    /*-------------*/

    /**
     * Instantiates a new Preferences Service.
     * <p>User Dagger Injection.</p>
     *
     * @param application Application Context
     */
    AbstractPreferences(Application application) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
    }

	/*----------------*/
    /* HELPER METHODS */
    /*----------------*/

    /**
     * Gets boolean value.
     *
     * @return the boolean
     */
    boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Puts boolean.
     *
     */
    void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Gets string value.
     *
     * @return the string value
     */
    String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Puts string.
     */
    void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Gets long value.
     *
     * @return the long value
     */
    long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    /**
     * Puts long.
     */
    void putLong(String key, long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * Puts strings from map.
     *
     * @param map the map
     */
    void putStringsFromMap(Map<String, String> map) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Map.Entry<String, String> pair : map.entrySet()) {
            editor.putString(pair.getKey(), pair.getValue());
        }
        editor.apply();
    }

    /**
     Checks whether the preferences contains a preference.
     *
     * @param key the name of the preference to check
     * @return true if the preference exists in the preferences, otherwise false.
     */
    boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * Removes the preference identified by the {@code key}.
     *
     * @param key the name of the preference to remove
     */
    void remove(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

}