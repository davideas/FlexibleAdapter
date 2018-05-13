package eu.davidea.samples.flexibleadapter.overall;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;

/**
 * Model object representing the entity of all features.
 */
@Entity(tableName = "features")
public class Feature {

    @PrimaryKey
    private int id;
    private int title;
    private int description;
    private String icon;

    public Feature(@IdRes int id, @StringRes int title) {
        this.id = id;
        this.title = title;
    }

    public Feature withDescription(@StringRes int description) {
        this.description = description;
        return this;
    }

    public Feature withIcon(String icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feature that = (Feature) o;
        return id == that.id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @StringRes
    public int getTitle() {
        return title;
    }

    public void setTitle(@StringRes int title) {
        this.title = title;
    }

    @StringRes
    public int getDescription() {
        return description;
    }

    public void setDescription(@StringRes int description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

}