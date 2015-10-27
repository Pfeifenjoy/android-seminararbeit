package org.dhbw.arwed_dominic.piccer;

import android.widget.ListView;

/**
 * Created by arwed on 27.10.15.
 */
public class Scroller implements Runnable {
    final private ListView listView;
    final private int position;
    public Scroller(ListView listView, int position) {
        this.listView = listView;
        this.position = position;
    }

    @Override
    public void run() {
        this.listView.smoothScrollBy(this.listView.getHeight() * this.position, 9000);
    }
}
