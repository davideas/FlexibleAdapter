package com.twotoasters.anim;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;

import com.twotoasters.android.support.v7.widget.RecyclerView.ViewHolder;

public class GarageDoorItemAnimator extends PendingItemAnimator {

    public GarageDoorItemAnimator() {
        setAddDuration(300);
        setRemoveDuration(300);
    }

    @Override
    protected boolean prepHolderForAnimateRemove(ViewHolder holder) {
        return true;
    }

    @Override
    protected ViewPropertyAnimatorCompat animateRemoveImpl(ViewHolder holder) {
        return ViewCompat.animate(holder.itemView)
                .rotationX(90)
                .translationY( - (holder.itemView.getMeasuredHeight() / 2));
    }

    @Override
    protected void onRemoveCanceled(ViewHolder holder) {
        ViewCompat.setRotationX(holder.itemView, 0);
        ViewCompat.setTranslationY(holder.itemView, 0);
    }

    @Override
    protected boolean prepHolderForAnimateAdd(ViewHolder holder) {
        ViewCompat.setRotationX(holder.itemView, 90);
        ViewCompat.setTranslationY(holder.itemView, - (holder.itemView.getMeasuredHeight() / 2));
        return true;
    }

    @Override
    protected ViewPropertyAnimatorCompat animateAddImpl(ViewHolder holder) {
        return ViewCompat.animate(holder.itemView)
                .rotationX(0)
                .translationY(0);
    }

    @Override
    protected void onAddCanceled(ViewHolder holder) {
        ViewCompat.setRotationX(holder.itemView, 0);
        ViewCompat.setTranslationY(holder.itemView, 0);
    }
}
