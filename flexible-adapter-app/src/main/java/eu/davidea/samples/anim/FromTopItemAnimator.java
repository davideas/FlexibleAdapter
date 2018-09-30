package eu.davidea.samples.anim;

import android.graphics.Point;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import eu.davidea.utils.Utils;

public class FromTopItemAnimator extends PendingItemAnimator {

    public FromTopItemAnimator() {
        setMoveDuration(200);
        setRemoveDuration(500);
        setAddDuration(300);
    }

    @Override
    protected boolean prepHolderForAnimateRemove(ViewHolder holder) {
        return true;
    }

    @Override
    protected ViewPropertyAnimatorCompat animateRemoveImpl(ViewHolder holder) {
        Point screen = Utils.getScreenDimensions(holder.itemView.getContext());
        int top = holder.itemView.getTop();
        return ViewCompat.animate(holder.itemView)
                         .rotation(80)
                         .translationY(screen.y - top)
                         .setInterpolator(new AccelerateInterpolator());
    }

    @Override
    protected void onRemoveCanceled(ViewHolder holder) {
        ViewCompat.setTranslationY(holder.itemView, 0);
    }

    @Override
    protected boolean prepHolderForAnimateAdd(ViewHolder holder) {
        int bottom = holder.itemView.getBottom();
        ViewCompat.setTranslationY(holder.itemView, -bottom);
        return true;
    }

    @Override
    protected ViewPropertyAnimatorCompat animateAddImpl(ViewHolder holder) {
        return ViewCompat.animate(holder.itemView)
                         .translationY(0)
                         .setInterpolator(new OvershootInterpolator());
    }

    @Override
    protected void onAddCanceled(ViewHolder holder) {
        ViewCompat.setTranslationY(holder.itemView, 0);
    }
}
