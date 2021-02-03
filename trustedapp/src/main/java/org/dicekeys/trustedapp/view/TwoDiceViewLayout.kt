package org.dicekeys.trustedapp.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import org.dicekeys.trustedapp.R


class TwoDiceViewLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    var sourceDiceViewId: Int = 0
    var targetDiceViewId: Int = 0
    var diceOverlayView: DiceOverlayView? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TwoDiceViewLayout)
        sourceDiceViewId = typedArray.getResourceId(R.styleable.TwoDiceViewLayout_sourceDiceViewId, 0)
        targetDiceViewId = typedArray.getResourceId(R.styleable.TwoDiceViewLayout_targetDiceViewId, 0)
        typedArray.recycle()

        addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            diceOverlayView?.measure(
                    View.MeasureSpec.makeMeasureSpec(right - left, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(bottom - top, View.MeasureSpec.UNSPECIFIED)
            )
            diceOverlayView?.layout(0, 0, right - left, bottom - top)
        }
    }

    var sourceDiceViewIndex: Int?
        get() = diceOverlayView?.sourceDiceViewIndex
        set(value) { diceOverlayView?.sourceDiceViewIndex = value }

    var targetDiceViewIndex: Int?
        get() = diceOverlayView?.targetDiceViewIndex
        set(value) { diceOverlayView?.targetDiceViewIndex = value }

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
            if (child.id == sourceDiceViewId && child is DiceBaseView) {
                getOrCreateOverlay().sourceDiceView = child
            } else if (child.id == targetDiceViewId && child is DiceBaseView) {
                getOrCreateOverlay().targetDiceView = child
            }
        }
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        child?.let {
            if (child.id == sourceDiceViewId && child is DiceBaseView) {
                getOrCreateOverlay().sourceDiceView = null
            } else if (child.id == targetDiceViewId && child is DiceBaseView) {
                getOrCreateOverlay().targetDiceView = null
            }
        }
    }
}