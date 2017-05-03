package fi.hk.sunko;

import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Utility class for fading animations.
 *
 * @author Henri Kankaanpää
 * @version 1.0
 * @since 1.0
 */
class AnimationUtil {

    /**
     * Changes the alpha of a view from 1 to 0
     *
     * @param v The view to be animated
     */
    void hideAnimation(View v) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(v, "alpha", 1, 0);
        animation.setDuration(500);
        animation.start();
    }

    /**
     * Changes the alpha of a view from 0 to 1
     *
     * @param v The view to be animated
     */
    void showAnimation(View v) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(v, "alpha", 0, 1);
        animation.setDuration(500);
        animation.start();
    }

    /**
     * Transitions the background based on which one is currently visible.
     *
     * @param v1 First background view
     * @param v2 Second background view
     * @param mDrawable New background drawable
     */
    void animateBackground(View v1, View v2, Drawable mDrawable) {
        if (v1.getAlpha() == 0f) {
            v1.setBackground(mDrawable);
            showAnimation(v1);
            hideAnimation(v2);
        } else {
            v2.setBackground(mDrawable);
            showAnimation(v2);
            hideAnimation(v1);
        }
    }

}
