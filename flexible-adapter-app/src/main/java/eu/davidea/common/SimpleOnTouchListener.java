package eu.davidea.common;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

/**
 * {@link View.OnTouchListener} //Necessary to open the Popup at first click
 */
public final class SimpleOnTouchListener implements View.OnTouchListener {

    private Context context;

    public SimpleOnTouchListener(Context context) {
        this.context = context;
    }

    @Override
    public boolean onTouch(final View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            view.requestFocus();
            //MyApplication.hideSoftInputFrom(context, view);
            view.performClick();
        }
        return true;
    }

}