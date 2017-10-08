package eu.davidea.flexibleadapter.livedata.models;

/**
 * Model item for ItemHolder.
 *
 * @author Davide Steduto
 * @since 07/10/2017
 */
public class ItemModel extends AbstractModel {

    private String type;

    // For the test without Header
    public ItemModel(String id) {
        super(id);
    }

    // For the test with Header
    public ItemModel(String id, String type) {
        super(id);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}