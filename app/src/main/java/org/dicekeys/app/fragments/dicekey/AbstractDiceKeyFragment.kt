package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import org.dicekeys.app.AppFragment
import org.dicekeys.app.extensions.toast
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import javax.inject.Inject

/*
 * Extend this class to access the DiceKeyViewModel
 */
abstract class AbstractDiceKeyFragment<T : ViewDataBinding>(@LayoutRes layout: Int) : AppFragment<T>(layout) {

    lateinit var diceKey: DiceKey<Face>

    val viewModel: DiceKeyViewModel by activityViewModels()

    @Inject
    lateinit var repository: DiceKeyRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Guard: If DiceKey is not available, return
        repository.getActiveDiceKey()?.also {
            diceKey = it
        } ?: run {
            findNavController().popBackStack()
            toast("DiceKey not found in memory")
            return
        }

    }
}