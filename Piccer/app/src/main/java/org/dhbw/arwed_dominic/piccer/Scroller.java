package org.dhbw.arwed_dominic.piccer;

import android.widget.ListView;

/**
 * Created by arwed on 27.10.15.
 */
public class Scroller implements Runnable {
    final private ListView listView;
    final private int position;
    final private boolean direction;
    public Scroller(ListView listView, int position, boolean direction) {
        this.listView = listView;
        this.position = position;
        this.direction = direction;
    }

    @Override
    public void run() {
        this.listView.smoothScrollBy((direction ? this.listView.getHeight() * this.position : 0), 9000);
    }
}
