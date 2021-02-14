package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.NavHostFragment
import org.dicekeys.app.AppFragment
import org.dicekeys.app.viewmodels.DiceKeyViewModel

/*
 * Extend this class to access the DiceKeyViewModel of the RootDiceKeyFragment
 */
abstract class AbstractDiceKeyFragment<T : ViewDataBinding>(@LayoutRes layout: Int) : AppFragment<T>(layout) {

    protected lateinit var viewModel: DiceKeyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState).also {
            viewModel = getDiceKeyRootFragment().viewModel
        }
    }

    fun getDiceKeyRootFragment(): RootDiceKeyFragment {
        return ((parentFragment as NavHostFragment).parentFragment as RootDiceKeyFragment)
    }

}