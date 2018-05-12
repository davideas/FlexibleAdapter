package eu.davidea.samples.flexibleadapter.overall;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Model object representing the entity of all features.
 */
@Entity
public class Overall {

    @PrimaryKey
    private int id;
    private String title;
    private String description;
    private int icon;

    public Overall(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public Overall withDescription(String description) {
        this.description = description;
        return this;
    }

    public Overall withIcon(int icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Overall that = (Overall) o;
        return id == that.id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

}