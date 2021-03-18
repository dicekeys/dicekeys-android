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
abstract class AbstractDiceKeyFragment<T : ViewDataBinding>(@LayoutRes layout: Int) : AppFragment<T>(layout, R.menu.dicekey_menu) {

    lateinit var diceKey: DiceKey<Face>

    // isGuarded is used to prevent fragment to continue initialization,
    // as the DiceKey is no longer available in memory
    var isGuarded = false

    val viewModel: DiceKeyViewModel by viewModels()

    @Inject
    lateinit var repository: DiceKeyRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState).also {
            guard()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.diceKey.observe(viewLifecycleOwner) {
            (requireActivity() as MainActivity).setTitle(getString(R.string.dicekey_with_center, it?.centerFace()?.toHumanReadableForm(false)))
        }
    }

    override fun onResume() {
        super.onResume()

        guard()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId){
            R.id.save -> {
                navigate(R.id.save)
                true
            }
            R.id.lock -> {
                viewModel.forget()
                findNavController().popBackStack()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    // Check if DiceKey is still in memory
    private fun guard(){
        // Guard: If DiceKey is not available
        repository.getActiveDiceKey()?.also {
            diceKey = it
        } ?: run {
            findNavController().popBackStack()
            isGuarded = true
        }
    }
}