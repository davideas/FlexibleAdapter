/*
 * Copyright 2016 Realm Inc. & Davide Steduto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.davidea.flexibleadapter.realm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollection;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;

/**
 * The {@code RealmFlexibleAdapter} class is an utility class for binding RecyclerView UI
 * elements to Realm data using {@link FlexibleAdapter}.
 * <p>This adapter will automatically handle any updates to its data and call fine grained
 * notifications as appropriate.</p>
 * The Adapter will stop receiving updates if the Realm instance providing the
 * {@link OrderedRealmCollection} is closed.
 *
 * @param <T> type of {@link RealmModel} and {@link IFlexible} stored in the adapter.
 *
 * @author Davide Steduto
 * @since 04/05/2017
 */
@SuppressWarnings({"unchecked", "ConstantConditions"})
public class RealmFlexibleAdapter<T extends RealmModel & IFlexible> extends FlexibleAdapter<T> {

	private boolean hasAutoUpdates;
	private OrderedRealmCollectionChangeListener listener;
	@Nullable
	private OrderedRealmCollection<T> adapterData;

	public RealmFlexibleAdapter(@Nullable OrderedRealmCollection<T> data,
								@Nullable Object listeners) {
		this(data, listeners, true);
	}

	public RealmFlexibleAdapter(@Nullable OrderedRealmCollection<T> data,
								@Nullable Object listeners, boolean autoUpdate) {
		super(data, listeners, true);
		if (data != null && !data.isManaged())
			throw new IllegalStateException("Only use this adapter with managed RealmCollection");
		this.adapterData = data;
		this.hasAutoUpdates = autoUpdate;
		this.listener = hasAutoUpdates ? createListener() : null;
	}

	private OrderedRealmCollectionChangeListener createListener() {
		return new OrderedRealmCollectionChangeListener() {
			@Override
			public void onChange(Object collection, OrderedCollectionChangeSet changeSet) {
				// null changes means the async query returns the first time.
				if (changeSet == null) {
					notifyDataSetChanged();
					return;
				}

				// For deletions, the adapter has to be notified in reverse order.
				OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
				for (int i = deletions.length - 1; i >= 0; i--) {
					OrderedCollectionChangeSet.Range range = deletions[i];
					notifyItemRangeRemoved(range.startIndex, range.length);
				}

				OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
				for (OrderedCollectionChangeSet.Range range : insertions) {
					notifyItemRangeInserted(range.startIndex, range.length);
				}

				OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
				for (OrderedCollectionChangeSet.Range range : modifications) {
					notifyItemRangeChanged(range.startIndex, range.length);
				}
			}
		};
	}

	@Override
	public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		if (hasAutoUpdates && isDataValid()) {
			addListener(adapterData);
		}
	}

	@Override
	public void onDetachedFromRecyclerView(final RecyclerView recyclerView) {
		super.onDetachedFromRecyclerView(recyclerView);
		if (hasAutoUpdates && isDataValid()) {
			removeListener(adapterData);
		}
	}

	@Override
	public int getItemCount() {
		return isDataValid() ? adapterData.size() : 0;
	}

	/**
	 * Returns the item associated with the specified position.
	 * Can return {@code null} if provided Realm instance by {@link OrderedRealmCollection} is closed.
	 *
	 * @param index index of the item.
	 * @return the item at the specified position, {@code null} if adapter data is not valid.
	 */
	@Nullable
	public T getItem(int index) {
		return isDataValid() ? adapterData.get(index) : null;
	}

	/**
	 * Returns data associated with this adapter.
	 *
	 * @return adapter data.
	 */
	@Nullable
	public OrderedRealmCollection<T> getData() {
		return adapterData;
	}

	/**
	 * Updates the data associated to the Adapter. Useful when the query has been changed.
	 * If the query does not change you might consider using the automaticUpdate feature.
	 *
	 * @param data the new {@link OrderedRealmCollection} to display.
	 */
	public void updateData(@Nullable OrderedRealmCollection<T> data) {
		if (hasAutoUpdates) {
			if (isDataValid()) {
				removeListener(adapterData);
			}
			if (data != null) {
				addListener(data);
			}
		}
		this.adapterData = data;
		updateDataSet(data);
//		notifyDataSetChanged();
	}

	private void addListener(@NonNull OrderedRealmCollection<T> data) {
		if (data instanceof RealmResults) {
			RealmResults<T> results = (RealmResults<T>) data;
			results.addChangeListener(listener);
		} else if (data instanceof RealmList) {
			RealmList<T> list = (RealmList<T>) data;
			list.addChangeListener(listener);
		} else {
			throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
		}
	}

	private void removeListener(@NonNull OrderedRealmCollection<T> data) {
		if (data instanceof RealmResults) {
			RealmResults<T> results = (RealmResults<T>) data;
			results.removeChangeListener(listener);
		} else if (data instanceof RealmList) {
			RealmList<T> list = (RealmList<T>) data;
			list.removeChangeListener(listener);
		} else {
			throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
		}
	}

	private boolean isDataValid() {
		return adapterData != null && adapterData.isValid();
	}

}