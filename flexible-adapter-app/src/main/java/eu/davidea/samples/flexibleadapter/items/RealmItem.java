/*
 * Copyright 2016 Realm Inc.
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
package eu.davidea.samples.flexibleadapter.items;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.realm.model.Parent;
import eu.davidea.viewholders.FlexibleViewHolder;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * AbstractRealmFlexibleItem can't be used right now:
 * #761 - Inheritance / Polymorphism - https://github.com/realm/realm-java/issues/761
 */
public class RealmItem extends RealmObject implements IFlexible<RealmItem.RealmViewHolder> {

	public static final String FIELD_ID = "id";

	private static AtomicInteger INTEGER_COUNTER = new AtomicInteger(0);

	@PrimaryKey
	private int id;

	public int getId() {
		return id;
	}

	private String getIdAsString() {
		return Integer.toString(id);
	}

	// create() & delete() needs to be called inside a transaction.
	public static void create(Realm realm) {
		create(realm, false);
	}

	public static void create(Realm realm, boolean randomlyInsert) {
		Parent parent = realm.where(Parent.class).findFirst();
		RealmList<RealmItem> items = parent.getItemList();
		RealmItem counter = realm.createObject(RealmItem.class, increment());
		if (randomlyInsert && items.size() > 0) {
			Random rand = new Random();
			items.listIterator(rand.nextInt(items.size())).add(counter);
		} else {
			items.add(counter);
		}
	}

	public static void delete(Realm realm, long id) {
		RealmItem item = realm.where(RealmItem.class).equalTo(FIELD_ID, id).findFirst();
		// Otherwise it has been deleted already.
		if (item != null) {
			item.deleteFromRealm();
		}
	}

	private static int increment() {
		return INTEGER_COUNTER.getAndIncrement();
	}

	/*-------------------*/
	/* IFLEXIBLE METHODS */
	/*-------------------*/

	/* Item flags recognized by FlexibleAdapter */
	private boolean mEnabled = true, mHidden = false,
			mSelectable = true, mDraggable = false, mSwipeable = false;

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Override
	public boolean isEnabled() {
		return mEnabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	@Override
	public boolean isHidden() {
		return mHidden;
	}

	@Override
	public void setHidden(boolean hidden) {
		mHidden = hidden;
	}

	@Override
	public int getSpanSize(int spanCount, int position) {
		return 1;
	}

	@Override
	public boolean shouldNotifyChange(IFlexible newItem) {
		return true;
	}

	/*--------------------*/
	/* SELECTABLE METHODS */
	/*--------------------*/

	@Override
	public boolean isSelectable() {
		return mSelectable;
	}

	@Override
	public void setSelectable(boolean selectable) {
		this.mSelectable = selectable;
	}

	/*-------------------*/
	/* TOUCHABLE METHODS */
	/*-------------------*/

	@Override
	public boolean isDraggable() {
		return mDraggable;
	}

	@Override
	public void setDraggable(boolean draggable) {
		mDraggable = draggable;
	}

	@Override
	public boolean isSwipeable() {
		return mSwipeable;
	}

	@Override
	public void setSwipeable(boolean swipeable) {
		mSwipeable = swipeable;
	}

	/*------------*/
	/* VH METHODS */
	/*------------*/

	public int getLayoutRes() {
		return R.layout.recycler_realm_item;
	}

	public RealmViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new RealmViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	public void bindViewHolder(final FlexibleAdapter adapter, RealmViewHolder holder, final int position, List payloads) {
		holder.title.setText(getIdAsString());
		holder.deletedCheckBox.setChecked(adapter.isSelected(position));
		boolean isMultiSelection = adapter.getMode() == SelectableAdapter.MODE_MULTI;
		holder.deletedCheckBox.setVisibility(isMultiSelection ? View.VISIBLE : View.GONE);
	}

	@Override
	public void unbindViewHolder(FlexibleAdapter adapter, RealmViewHolder holder, int position) {
		// Empty implementation
	}

	class RealmViewHolder extends FlexibleViewHolder {
		TextView title;
		CheckBox deletedCheckBox;

		RealmViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);

			title = (TextView) view.findViewById(R.id.textview);
			deletedCheckBox = (CheckBox) view.findViewById(R.id.checkBox);
			deletedCheckBox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAdapter.toggleSelection(getFlexibleAdapterPosition());
				}
			});
		}

		@Override
		public void onClick(View view) {
			super.onClick(view);
			deletedCheckBox.setChecked(mAdapter.isSelected(getFlexibleAdapterPosition()));
		}
	}

}