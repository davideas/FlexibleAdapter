package eu.davidea.flexibleadapter.firebase;

import android.support.annotation.Nullable;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * @author Davide Steduto
 * @since 04/05/2017
 * FlexibleAdapter project
 */
public class FirebaseFlexibleAdapter extends FlexibleAdapter {

	public FirebaseFlexibleAdapter(@Nullable List items) {
		super(items);
	}

	public FirebaseFlexibleAdapter(@Nullable List items, @Nullable Object listeners) {
		super(items, listeners);
	}

	public FirebaseFlexibleAdapter(@Nullable List items, @Nullable Object listeners, boolean stableIds) {
		super(items, listeners, stableIds);
	}

}