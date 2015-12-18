package net.nueca.concessioengine.tools;

import android.animation.ObjectAnimator;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by rhymart on 11/27/15.
 */
public class AnimationTools {

    public static void toggleShowHide(ViewGroup viewGroup, boolean shouldHide, int duration) {
        if(viewGroup == null)
            return;
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(viewGroup, "translationY", (shouldHide) ? 1000f : 0f);
        objectAnimator.setDuration(duration);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.start();
    }

}
