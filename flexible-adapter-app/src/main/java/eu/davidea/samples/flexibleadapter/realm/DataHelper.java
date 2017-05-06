/*
 * Copyright 2017 Realm Inc.
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

import java.util.Collection;

import eu.davidea.samples.flexibleadapter.items.RealmItem;
import io.realm.Realm;

public class DataHelper {

	// Create 3 counters and insert them into random place of the list.
	public static void randomAddItemAsync(Realm realm) {
		realm.executeTransactionAsync(new Realm.Transaction() {
			@Override
			public void execute(Realm realm) {
				for (int i = 0; i < 3; i++) {
					RealmItem.create(realm, true);
				}
			}
		});
	}

	public static void addItemAsync(Realm realm) {
		realm.executeTransactionAsync(new Realm.Transaction() {
			@Override
			public void execute(Realm realm) {
				RealmItem.create(realm);
			}
		});
	}

	public static void deleteItemAsync(Realm realm, final long id) {
		realm.executeTransactionAsync(new Realm.Transaction() {
			@Override
			public void execute(Realm realm) {
				RealmItem.delete(realm, id);
			}
		});
	}

	public static void deleteItemsAsync(Realm realm, Collection<Integer> ids) {
		// Create an new array to avoid concurrency problem.
		final Integer[] idsToDelete = new Integer[ids.size()];
		ids.toArray(idsToDelete);
		realm.executeTransactionAsync(new Realm.Transaction() {
			@Override
			public void execute(Realm realm) {
				for (Integer id : idsToDelete) {
					RealmItem.delete(realm, id);
				}
			}
		});
	}
}
