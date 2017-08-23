package eu.davidea.samples.flexibleadapter.items;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * This class will benefit of the already implemented methods (getter and setters) in
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem}.
 *
 * It is used as base item for all example models.
 */
public abstract class AbstractItem<VH extends FlexibleViewHolder>
        extends AbstractFlexibleItem<VH> {

    protected String id;
    protected String title;
    protected String subtitle = "";
    /* number of times this item has been refreshed */
    protected int updates;

    public AbstractItem(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof AbstractItem) {
            AbstractItem inItem = (AbstractItem) inObject;
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

    public int getUpdates() {
        return updates;
    }

    public void increaseUpdates() {
        this.updates++;
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", title=" + title;
    }

}