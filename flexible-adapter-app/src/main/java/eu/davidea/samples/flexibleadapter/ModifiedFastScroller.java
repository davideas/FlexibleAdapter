package eu.davidea.samples.flexibleadapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import eu.davidea.fastscroller.FastScroller;

// Sample class that extends the FastScroller
public class ModifiedFastScroller extends FastScroller {

    public ModifiedFastScroller(Context context) {
        super(context);
    }

    public ModifiedFastScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ModifiedFastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void updateBubbleText(int position) {
        super.updateBubbleText(position);

        // You can set a default way to update the text in the bubble.
        String bubbleTextString = bubbleTextCreator.onCreateBubbleText(position);
        if (TextUtils.isEmpty(bubbleTextString)) {
            bubble.setVisibility(View.VISIBLE);
            bubble.setText("...");
            bubble.setTextSize(TypedValue.COMPLEX_UNIT_PT, 32);
        }
    }

    @Override
    protected void setBubbleAndHandlePosition(float y) {
        // You can modify the computed y-position of the bubble and the handle in this method.
        // For this example, I allowed the bubble to be at the bottom-most part of the screen.

        super.setBubbleAndHandlePosition(y);
        if (bubble != null && bubblePosition == FastScrollerBubblePosition.ADJACENT) {
            bubble.setY(getValueInRange(0, height - handle.getHeight(), (int) (y - bubble.getHeight())));
        }
    }
}
