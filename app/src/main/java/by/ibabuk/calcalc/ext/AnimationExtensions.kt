package by.ibabuk.calcalc.ext

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import by.ibabuk.calcalc.R

/**
 * Created by Artem Babuk on 10,November,2020
 * Skype: archiecrown
 */
fun Activity.closeScreen(action: () -> Unit = {}) {
    val snackbarContent: CoordinatorLayout? = findViewById(R.id.snackbarContent)
    if (snackbarContent != null) {
        val animatorSet = slideOutView(snackbarContent)
        animatorSet.addListener(object : AnimatorListener() {
            override fun onAnimationEnd(animation: Animator?) {
                action.invoke()
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        })

        animatorSet.start()
    } else {
        action.invoke()
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}

fun slideOutView(view: View): AnimatorSet {
    val translationY =
        ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, view.height.toFloat())
    val animatorSet = AnimatorSet()
    animatorSet.playTogether(translationY)
    animatorSet.duration = 200
    animatorSet.addListener(object : AnimatorListener() {
        override fun onAnimationStart(animation: Animator?) {
            view.isVisible = true
        }

        override fun onAnimationEnd(animation: Animator?) {
            view.isGone = true
        }
    })
    return animatorSet
}

fun slideInView(view: View): AnimatorSet {
    val translationY =
        ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.height.toFloat(), 0f)
    val animatorSet = AnimatorSet()
    animatorSet.playTogether(translationY)
    animatorSet.duration = 400
    animatorSet.addListener(object : AnimatorListener() {
        override fun onAnimationStart(animation: Animator?) {
            view.isVisible = true
        }
    })
    return animatorSet
}

fun animateInt(start: Int, end: Int, action: (value: Int) -> Unit) {
    val anim = ValueAnimator.ofInt(start, end)
    anim.interpolator = LinearInterpolator()
    anim.addUpdateListener {
        action.invoke(it.animatedValue as Int)
    }
    anim.start()
}

open class AnimatorListener : Animator.AnimatorListener {
    override fun onAnimationStart(animation: Animator?) = Unit

    override fun onAnimationEnd(animation: Animator?) = Unit

    override fun onAnimationCancel(animation: Animator?) = Unit

    override fun onAnimationRepeat(animation: Animator?) {
        //nothing
    }
}