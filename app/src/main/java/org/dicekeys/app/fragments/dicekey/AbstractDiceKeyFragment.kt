package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.dicekeys.app.AppFragment
import org.dicekeys.app.MainActivity
import org.dicekeys.app.R
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import javax.inject.Inject

/*
 * Extend this class to access the DiceKeyViewModel
 */
abstract class AbstractDiceKeyFragment<T : ViewDataBinding>(@LayoutRes layout: Int) :
    AppFragment<T>(layout, R.menu.dicekey_menu) {

    lateinit var diceKey: DiceKey<Face>

    val viewModel: DiceKeyViewModel by viewModels()

    @Inject
    lateinit var repository: DiceKeyRepository

    // Guard initialization code for fragments that requires a connected session
    abstract fun onViewCreatedGuarded(view: View, savedInstanceState: Bundle?)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (guard()) {
            onViewCreatedGuarded(view, savedInstanceState)

            viewModel.diceKey.observe(viewLifecycleOwner) {
                (requireActivity() as MainActivity).setTitle(
                    getString(
                        R.string.dicekey_with_center,
                        it?.centerFace()?.toHumanReadableForm(false)
                    )
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        guard()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.save -> {
                navigate(R.id.save)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    // Check if DiceKey is still in memory
    private fun guard(): Boolean {
        // Guard: If DiceKey is not available
        return repository.getActiveDiceKey()?.let {
            diceKey = it
            true
        } ?: run {
            findNavController().popBackStack()
            false
        }
    }
}