package org.dicekeys.trustedapp.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import org.dicekeys.trustedapp.R


class TwoDiceViewLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    var leftDiceViewId: Int = 0
    var rightDiceViewId: Int = 0
    var diceOverlayView: DiceOverlayView? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TwoDiceViewLayout)
        leftDiceViewId = typedArray.getResourceId(R.styleable.TwoDiceViewLayout_leftDiceViewId, 0)
        rightDiceViewId = typedArray.getResourceId(R.styleable.TwoDiceViewLayout_rightDiceViewId, 0)
        typedArray.recycle()

        addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            diceOverlayView?.measure(
                    View.MeasureSpec.makeMeasureSpec(right - left, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(bottom - top, View.MeasureSpec.UNSPECIFIED)
            )
            diceOverlayView?.layout(0, 0, right - left, bottom - top)
        }
    }

    private fun getOrCreateOverlay(): DiceOverlayView {
        if (diceOverlayView == null) {
            diceOverlayView = DiceOverlayView(context)
            overlay.add(diceOverlayView!!)
        }
        return diceOverlayView!!
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        child?.let {
            if (child.id == leftDiceViewId && child is DiceKeyBaseView) {
                getOrCreateOverlay().diceView1 = child
            } else if (child.id == rightDiceViewId && child is DiceKeyBaseView) {
                getOrCreateOverlay().diceView2 = child
            }
        }
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        child?.let {
            if (child.id == leftDiceViewId && child is DiceKeyBaseView) {
                getOrCreateOverlay().diceView1 = null
            } else if (child.id == rightDiceViewId && child is DiceKeyBaseView) {
                getOrCreateOverlay().diceView2 = null
            }
        }
    }
}