package com.samuelberrien.odyspace;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.widget.ProgressBar;

/**
 * Created by samuel on 16/04/17.
 * Copyright samuel, 2016 - 2017.
 * Toute reproduction ou utilisation sans l'autorisation
 * de l'auteur engendrera des poursuites judiciaires.
 */

public class MyGLSurfaceView extends GLSurfaceView {

    private MyGLRenderer renderer;

    /**
     * @param context
     */
    public MyGLSurfaceView(Context context) {
        super(context);
        // Create an OpenGL ES 2.0 context.
        this.setEGLContextClientVersion(2);


        this.renderer = new MyGLRenderer(context, this);
        this.setRenderer(this.renderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        this.renderer.updateMotion(e);
        return true;
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }


}
