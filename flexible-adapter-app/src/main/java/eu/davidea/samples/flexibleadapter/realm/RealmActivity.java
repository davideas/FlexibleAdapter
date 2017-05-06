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
package eu.davidea.samples.flexibleadapter.realm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.common.FlexibleItemDecoration;
import eu.davidea.flexibleadapter.realm.RealmFlexibleAdapter;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.items.RealmItem;
import eu.davidea.samples.flexibleadapter.realm.model.Parent;
import eu.davidea.samples.flexibleadapter.views.HeaderView;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmActivity extends AppCompatActivity
		implements FlexibleAdapter.OnItemClickListener, FlexibleAdapter.OnItemLongClickListener {

	private Realm realm;
	private RecyclerView recyclerView;
	private Menu menu;
	private RealmFlexibleAdapter<RealmItem> mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initReam();

		setContentView(R.layout.activity_realm);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		HeaderView headerView = (HeaderView) findViewById(R.id.toolbar_header_view);
		headerView.bindTo(getString(R.string.app_name), getString(R.string.realm));

		setUpRecyclerView();
	}

	private void initReam() {
		Realm.init(this);
		RealmConfiguration realmConfig = new RealmConfiguration.Builder()
				.initialData(new Realm.Transaction() {
					@Override
					public void execute(Realm realm) {
						realm.createObject(Parent.class);
					}
				})
				.build();
		Realm.deleteRealm(realmConfig); // Delete Realm between activity restarts
		Realm.setDefaultConfiguration(realmConfig);
		realm = Realm.getDefaultInstance();
	}

	/**
	 * It is good practice to null the reference from the view to the adapter when it is no
	 * longer needed. Because the <code>RealmFlexibleAdapter</code> registers itself as a
	 * <code>RealmResult.ChangeListener</code> the view may still be reachable if anybody is
	 * still holding a reference to the <code>RealmResult</code>.
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		recyclerView.setAdapter(null);
		realm.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.menu_realm, menu);
		menu.setGroupVisible(R.id.group_normal_mode, true);
		menu.setGroupVisible(R.id.group_delete_mode, false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				super.onBackPressed();
				return true;
			case R.id.action_add:
				DataHelper.addItemAsync(realm);
				return true;
			case R.id.action_random:
				DataHelper.randomAddItemAsync(realm);
				return true;
			case R.id.action_start_delete_mode:
				mAdapter.setMode(SelectableAdapter.MODE_MULTI);
				mAdapter.notifyDataSetChanged();
				menu.setGroupVisible(R.id.group_normal_mode, false);
				menu.setGroupVisible(R.id.group_delete_mode, true);
				return true;
			case R.id.action_end_delete_mode:
				List<Integer> selectedItems = mAdapter.getSelectedPositions();
				List<Integer> itemsToDelete = new ArrayList<>();
				for (Integer position : selectedItems) {
					itemsToDelete.add(mAdapter.getItem(position).getId());
				}
				DataHelper.deleteItemsAsync(realm, itemsToDelete);
				// Fall through
			case R.id.action_cancel_delete_mode:
				mAdapter.setMode(SelectableAdapter.MODE_IDLE);
				menu.setGroupVisible(R.id.group_normal_mode, true);
				menu.setGroupVisible(R.id.group_delete_mode, false);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void setUpRecyclerView() {
		OrderedRealmCollection<RealmItem> realmCollection = realm.where(Parent.class).findFirst().getItemList();
		FlexibleAdapter.enableLogs(true);
		mAdapter = new RealmFlexibleAdapter<>(realmCollection, this);
		mAdapter.setNotifyChangeOfUnfilteredItems(true)
				.setNotifyMoveOfFilteredItems(true);

		recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(mAdapter);
		recyclerView.setHasFixedSize(true);
		recyclerView.addItemDecoration(new FlexibleItemDecoration(this));
	}

	@Override
	public boolean onItemClick(int position) {
		if (mAdapter.getMode() == SelectableAdapter.MODE_MULTI) {
			mAdapter.toggleSelection(position);
			return true;
		}
		return false;
	}

	@Override
	public void onItemLongClick(int position) {
		mAdapter.setMode(SelectableAdapter.MODE_MULTI);
		mAdapter.notifyDataSetChanged();
		menu.setGroupVisible(R.id.group_normal_mode, false);
		menu.setGroupVisible(R.id.group_delete_mode, true);
	}

}
