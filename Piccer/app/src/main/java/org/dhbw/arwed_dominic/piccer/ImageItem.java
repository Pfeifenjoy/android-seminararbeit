package org.dhbw.arwed_dominic.piccer;

import java.util.Date;

/**
 * Created by arwed on 24.10.15.
 */
public class ImageItem {
    private Date created;
    private String path;

    public ImageItem(Date created, String path) {
        this.created = created;
        this.path = path;
    }

    public String getCreated() {
        return this.created.toString();
    }
}
