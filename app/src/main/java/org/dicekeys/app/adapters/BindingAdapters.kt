package org.dicekeys.app.adapters

import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.progressindicator.BaseProgressIndicator
import org.dicekeys.app.views.DiceBaseView
import org.dicekeys.app.views.DiceKeyView
import org.dicekeys.app.views.StickerTargetSheetView
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face

@BindingAdapter("isVisible")
fun bindIsVisible(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}

@BindingAdapter("isGone")
fun bindIsGone(view: View, isGone: Boolean) {
    view.isVisible = !isGone
}

@BindingAdapter("isInvisible")
fun bindIsInvisible(view: View, isInvisible: Boolean) {
    view.isInvisible = isInvisible
}

@BindingAdapter("dicekey")
fun dicekey(view: DiceBaseView, dicekey: DiceKey<*>?) {
    dicekey?.let { view.diceKey = it as DiceKey<Face>}
}

@BindingAdapter("hideDiceExceptCenterDie")
fun dicekey(view: DiceKeyView, hideDiceExceptCenterDie: Boolean) {
    view.hideDiceExceptCenterDie = hideDiceExceptCenterDie
}

@BindingAdapter("progress")
fun setProgress(progressIndicator: BaseProgressIndicator<*>, progress: Int) {
    if(!progressIndicator.isIndeterminate) {
        progressIndicator.setProgressCompat(progress, true)
    }
}

@BindingAdapter("currentItem")
fun setCurrentItem(viewPager: ViewPager, currentItem: Int) {
    viewPager.setCurrentItem(currentItem, true)
}

@BindingAdapter("checkedIndex")
fun setChecked(toggleGroup: MaterialButtonToggleGroup, checkedIndexes: List<Int>) {
    checkedIndexes.forEach {
        (toggleGroup.getChildAt(it) as MaterialButton).isChecked = true
    }
}
