package eu.davidea.flexibleadapter.realm;

import android.support.annotation.Nullable;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * @author Davide Steduto
 * @since 04/05/2017
 * FlexibleAdapter project
 */
public class RealmFlexibleAdapter extends FlexibleAdapter {

	public RealmFlexibleAdapter(@Nullable List items) {
		super(items);
	}

	public RealmFlexibleAdapter(@Nullable List items, @Nullable Object listeners) {
		super(items, listeners);
	}

	public RealmFlexibleAdapter(@Nullable List items, @Nullable Object listeners, boolean stableIds) {
		super(items, listeners, stableIds);
	}

}