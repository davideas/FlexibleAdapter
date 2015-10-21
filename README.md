[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-FlexibleAdapter-green.svg?style=flat)](https://android-arsenal.com/details/1/2207)

# Flexible Adapter

###### A pattern for every RecyclerView - Master branch: v4 of 2015.10.18

#### Main functionalities
* Base item selection (but also SINGLE & MULTI selection mode) in the Recycler View with ripple effect.
* Undo the deleted items with custom animations.
* Customizable FastScroller (imported library, see change log for details).
* SearchFilter with string selection in Item titles and subtitles.

#### How is made
The base functionality is taken from different Blogs (see at the bottom of the page), merged and methods have been improved for speed and scalability, for all Activities that use a RecyclerView.

* At lower class there is SelectableAdapter that provides selection functionalities and it's able to _maintain the state_ after the rotation, you just need to call the onSave/onRestore methods from the Activity!
* Then, the class FlexibleAdapter handles the content paying attention at the animations (calling notify only for the position. _Note:_ you still need to set your animation to the RecyclerView when you create it in the Activity).
* Then you need to extend over again this class. Here you add and implement methods as you wish for your own ViewHolder and your Domain/Model class (data holder).

I've put the Set click listeners at the creation and not in the Binding method, because onBindViewHolder is called at each invalidate (each notify..() methods).

Finally note that, this adapter handles the basic clicks: _single_ and _long clicks_. If you need a double tap you need to implement the android.view.GestureDetector.

# Screenshots
![Main screen](/screenshots/main_screen.png) ![Multi Selection](/screenshots/multi_selection.png)
![Search screen](/screenshots/search.png) ![Undo Screen](/screenshots/undo.png)

#Setup
Ultra simple:
No needs to create and import a library for just 2 files, so just *copy* SelectableAdapter.java & FlexibleAdapter.java in your *common* package and start to *extend* FlexibleAdapter from your custom Adapter (see my ExampleAdapter).

Remember to initialize `mItems` (List already included in FlexibleAdapter) in order to manage list items. Method `updateDataSet(..)` can help in this.

#### Pull requests / Issues / Improvement requests
Feel free to contribute and ask!

# Usage for Multi Selection
In your activity change the Mode for the _ActionMode_ object.

``` java
@Override
public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	mode.getMenuInflater().inflate(R.menu.menu_context, menu);
	mAdapter.setMode(YourAdapterClass.MODE_MULTI);
	return true;
}

@Override
public void onDestroyActionMode(ActionMode mode) {
	mAdapter.setMode(YourAdapterClass.MODE_SINGLE);
	mAdapter.clearSelection();
	mActionMode = null;
}
```

# Usage for Undo

``` java
@Override
public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	switch (item.getItemId()) {
		//...
		case R.id.action_delete:
			for (int i : mAdapter.getSelectedItems()) {
				//Remove items from your Database. Example:
				DatabaseService.getInstance().removeItem(mAdapter.getItem(i));
			}

			//Keep synchronized the Adapter: Remove selected items from Adapter
			String message = mAdapter.getSelectedItems() + " " + getString(R.string.action_deleted);
			mAdapter.removeItems(mAdapter.getSelectedItems());

			//Any view for Undo, ex. Snackbar
			Snackbar.make(findViewById(R.id.main_view), message, 7000)
					.setAction(R.string.undo, new View.OnClickListener() {
						@Override
						public void onClick(View v) { mAdapter.restoreDeletedItems(); }
					})
					.show();

			//Start countdown with startUndoTimer(millisec)
			mAdapter.startUndoTimer(7000); //Default 5''
			mActionMode.finish();
			return true;
		//...
	}
}
```

# Usage for FastScroller
First add the drawable files to the project, then the layout, finally add the implementation for the Adapter and Activity:

``` java
public class ExampleAdapter extends FlexibleAdapter<ExampleAdapter.SimpleViewHolder, Item>
		implements FastScroller.BubbleTextGetter {
	...
	@Override
	public String getTextToShowInBubble(int position) {
		return getItem(position).getTitle().substring(0,1).toUpperCase();
	}
}

public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		...
		FastScroller fastScroller = (FastScroller) findViewById(R.id.fast_scroller);
		fastScroller.setRecyclerView(mRecyclerView);
		fastScroller.setViewsToUse(R.layout.fast_scroller, R.id.fast_scroller_bubble, R.id.fast_scroller_handle);
	}
	...
}
```

# Change Log
###### v4 - 2015.10.18
- Added **FilterAsyncTask** to asynchronously load the list (This might not work well and binding is excluded from Async)
- Enabled **Search** filter through _updateDataSet_ method (Note: as the example is made, the search regenerate the list!)
- Included some ItemAnimators from https://github.com/wasabeef/recyclerview-animators
  (in the example SlideInRightAnimator is used), but all need to be adapted if used with Support v23.1.0
- Included and customized **FastScroller** library from https://github.com/AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller
  My version has FrameLayout extended instead of LinearLayout, and a friendly layout.
- Added **SwipeRefreshLayout**, just an usage example
- Use of Handler instead of TimerTask while Undo
- _FlexibleAdapter_ can now return deleted items before they are removed from memory
- _SelectableAdapter.selectAll()_ can now skip selection on one specific ViewType
- Adapted MainActivity

###### v3.1 - 2015.09.05
- Updated compileSdkVersion and Support libraries to v23
- Customised Undo timeout in the example Activity with original _Snackbar_

###### v3 - 2015.07.29
- Added **Undo** functionality
- Moved getItem() into FlexibleAdapter, method now is part of the library
- Added synchronized blocks for write operations on mItems list

###### v2.2 - 2015.07.20
- New full working example Android Studio project! (with some nice extra-features)

###### v2.1 - 2015.07.03
- Added new method _updateItem()_
- Deprecated _removeSelection()_ -> Use _toggleSelection()_ instead!
- In _clearSelection_ removed call to _notifyDataSetChanged()_.
- Improved others methods.
- Added more comments.

###### v2 - 2015.06.19
- Added **Mode** for Multi and Single fixed selection. The Multi selection was already active, but the Single fixed selection mode still not.
- Reviewed method: _toggleSelection(int position)_ - Adapted for Mode functionality. For more details see the comment of the method!
- Added new method _getPositionForItem(T item)_ - Self explanatory
- Added new method _contains(T item)_ - Another useful method
- Reviewed method _updateDataSet(String param)_ - Added the parameter to filter DataSet

###### v1 - 2015.05.03
- Initial release

# Thanks
I've used these blogs as starting point:

http://enoent.fr/blog/2015/01/18/recyclerview-basics/

https://www.grokkingandroid.com/statelistdrawables-for-recyclerview-selection/

# License

    Copyright 2015 Davide Steduto

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
