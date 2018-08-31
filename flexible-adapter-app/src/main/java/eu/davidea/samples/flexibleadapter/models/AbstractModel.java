package eu.davidea.samples.flexibleadapter.models;

import java.io.Serializable;

import eu.davidea.flexibleadapter.items.IHolder;

/**
 * This class is Pojo for the {@link IHolder} items.
 * It is used as base item for Item and Header model examples.
 *
 * Using Holder pattern, you can implement DB, XML & JSON (de)serialization libraries on this
 * item as usual.
 *
 * @author Davide Steduto
 * @since 19/10/2016
 */
public abstract class AbstractModel implements Serializable {

    private static final long serialVersionUID = -7385882749119849060L;

    private String id;
    private String title;
    private String subtitle;

    public AbstractModel(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof AbstractModel) {
            AbstractModel inItem = (AbstractModel) inObject;
            return this.id.equals(inItem.id);
        }
        return false;
    }

    /**
     * Override this method too, when using functionalities like StableIds, Filter or CollapseAll.
     * FlexibleAdapter is making use of HashSet to improve performance, especially in big list.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", title=" + title;
    }

}