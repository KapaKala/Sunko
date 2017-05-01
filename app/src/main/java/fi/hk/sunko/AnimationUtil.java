package fi.hk.sunko;

import android.animation.ObjectAnimator;
import android.view.View;


public class AnimationUtil {

    public void hideAnimation(View v) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(v, "alpha", 1, 0);
        animation.setDuration(500);
        animation.start();
    }

    public void showAnimation(View v) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(v, "alpha", 0, 1);
        animation.setDuration(500);
        animation.start();
    }

}
