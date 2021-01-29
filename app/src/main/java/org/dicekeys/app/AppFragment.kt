package org.dicekeys.app

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import javax.inject.Inject

/**
 * AppFragment
 *
 * This class is a useful abstract base class. Extend all other Fragments if possible.
 * Some of the features can be turned on/off in the constructor.
 *
 * It's crucial every AppFragment implementation to call @AndroidEntryPoint
 *
 * @property layout the layout id of the fragment
 * is called when the fragment is not actually visible
 *
 */

abstract class AppFragment<T : ViewDataBinding>(
        @LayoutRes val layout: Int
) : Fragment() {
    internal lateinit var binding: T

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, layout, container, false)
        binding.lifecycleOwner = this

        return binding.root
    }

    fun navigate(directions: NavDirections) {
        navigate(directions.actionId, directions.arguments)
    }

    fun navigate(@IdRes resId: Int) {
        navigate(resId, null)
    }

    fun navigate(@IdRes resId: Int, args: Bundle?) {

        val navOptionsBuilder = NavOptions.Builder()

        // Add Animations

        findNavController().navigate(resId, args, navOptionsBuilder.build())
    }

}