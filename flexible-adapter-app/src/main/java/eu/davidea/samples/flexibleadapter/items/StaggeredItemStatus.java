package eu.davidea.samples.flexibleadapter.items;

import android.graphics.Color;

import eu.davidea.samples.flexibleadapter.R;

public enum StaggeredItemStatus {


    A(Color.parseColor("#6d4c41"), Color.parseColor("#5d4037"), R.string.status_a),//brown
    B(Color.parseColor("#0097a7"), Color.parseColor("#00838f"), R.string.status_b),//dark cyan
    C(Color.parseColor("#4dd0e1"), Color.parseColor("#00acc1"), R.string.status_c),//cyan
    D(Color.parseColor("#dd191d"), Color.parseColor("#c41411"), R.string.status_d),//red
    E(Color.parseColor("#259b24"), Color.parseColor("#0a7e07"), R.string.status_e);//green

    int color;
    int darkenColor;
    int resId;

    StaggeredItemStatus(int color, int darkenColor, int resId) {
        this.color = color;
        this.darkenColor = darkenColor;
        this.resId = resId;
    }

    public int getColor() {
        return this.color;
    }

    public int getDarkenColor() {
        return this.darkenColor;
    }

    public int getResId() {
        return this.resId;
    }

}