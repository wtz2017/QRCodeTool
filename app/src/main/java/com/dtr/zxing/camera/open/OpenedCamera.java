package com.dtr.zxing.camera.open;

import android.hardware.Camera;

/**
 * Created by WTZ on 2018/3/18.
 */

public class OpenedCamera {

    private Camera camera;
    private int id;

    public OpenedCamera(Camera camera, int id) {
        this.camera = camera;
        this.id = id;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
