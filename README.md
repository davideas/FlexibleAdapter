# FlexibleAdapter
#### Version of 2015.07.21
#### A pattern for every RecyclerView

The functionalities are taken from different Blogs (see at the bottom of the page), merged and methods have been improved for speed and scalability, for all Activities that use a RecycleView.

* At lower class there is SelectableAdapter that provides selection functionalities and it's able to _maintain the state_ after the rotation, you just need to call the onSave/onRestore methods from the Activity!
* Then, the class FlexibleAdapter handles the content paying attention at the animations (calling notify only for the position. _Note:_ you still need to set your animation to the RecyclerView when you create it in the Activity).
* Then you need to extend over again this class. Here you add and implement methods as you wish for your own ViewHolder and your Domain/Model class (data holder).

I've put the click listeners inside the ViewHolder and the Set is done at the creation and not in the Binding method, that is called at each invalidate when calling notify..() methods.

Also note that this adapter handles the basic clicks: _single_ and _long clicks_. If you need a double tap you need to implement the android.view.GestureDetector.

# Screenshots
![Main screen](/screenshots/main_screen.png) ![Multi Selection](/screenshots/multi_selection.png)
![Search screen](/screenshots/filter.png) ![Undo Screen](/screenshots/undo.png)

#Setup
Ultra simple:
No needs to create and import library for just 2 files, so just *copy* SelectableAdapter.java & FlexibleAdapter.java in your *common* package and start to *extend* FlexibleAdapter from your custom Adapter (see my ExampleAdapter).

####Pull requests / Issues / Improvement requests
Feel free to do and ask!

#Usage for Multi Selection
In your activity change the Mode for the _ActionMode_ object.

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

#Change Log
**2015.07.21**
- Added filter engine (experimental), it works but not practical if you perform operations (Add/Del/Mod) on filtered list.
- Added Undo functionality.

**2015.07.20**
- New full working example Android Studio project! (with some nice extra-features)

**2015.07.03**
- Added new method _updateItem()_
- Deprecated _removeSelection()_ -> Use _toggleSelection()_ instead!
- In _clearSelection_ removed call to _notifyDataSetChanged()_.
- Improved others methods.
- Added more comments.

**2015.06.19**
- Added **Mode** for Multi and Single fixed selection. The Multi selection was already active, but the Single fixed selection mode still not.
- Reviewed method: _toggleSelection(int position)_ - Adapted for Mode functionality. For more details see the comment of the method!
- Added new method _getPositionForItem(T item)_ - Self explanatory
- Added new method _contains(T item)_ - Another useful method
- Reviewed method _updateDataSet(String param)_ - Added the parameter to filter DataSet

**2015.05.03**
- Initial release

#Thanks
I've used these blogs as starting point:

http://enoent.fr/blog/2015/01/18/recyclerview-basics/

https://www.grokkingandroid.com/statelistdrawables-for-recyclerview-selection/

#License

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
