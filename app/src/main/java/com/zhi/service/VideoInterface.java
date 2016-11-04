package com.zhi.service;

import android.view.SurfaceHolder;

/**
 * Created by Administrator on 2016/11/3.
 */
public interface VideoInterface {

    void play(String path, SurfaceHolder surfaceHolder);

    int pause();

    void replay();

    void stop();
}
