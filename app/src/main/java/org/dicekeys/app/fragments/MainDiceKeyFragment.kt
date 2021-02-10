package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.MainDicekeyFragmentBinding
import org.dicekeys.app.openDialogDeleteDiceKey
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.dicekey.DiceKey
import javax.inject.Inject

@AndroidEntryPoint
class MainDiceKeyFragment : AppFragment<MainDicekeyFragmentBinding>(R.layout.main_dicekey_fragment) {

    @Inject
    lateinit var repository: DiceKeyRepository

    lateinit var diceKey: DiceKey<*>

    private val args: MainDiceKeyFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: DiceKeyViewModel.AssistedFactory

    val viewModel: DiceKeyViewModel by viewModels {
        DiceKeyViewModel.provideFactory(viewModelFactory, diceKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Guard: If DiceKey is not available, return
        repository.get(args.diceKeyId)?.also {
            diceKey = it
        } ?: run {
            findNavController().popBackStack()
            return
        }

        setHasOptionsMenu(true)

        binding.pager.adapter = FragmentAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.dicekey)
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_scanning_side_view)
                }
                1 -> {
                    tab.text = getString(R.string.solokey)
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_usb_key)
                }
                2 -> {
                    tab.text = getString(R.string.secrets)
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_backup_to_dicekey)
                }
                3 -> {
                    tab.text = getString(R.string.backup)
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_backup_to_dicekey)
                }
            }

        }.attach()

        viewModel.isSaved.observe(viewLifecycleOwner){
            activity?.invalidateOptionsMenu()
        }

        binding.buttonLock.setOnClickListener {
            lock()
        }

        binding.toolbarTitle.text = getString(R.string.dicekey_with_center, diceKey.centerFace().toHumanReadableForm(false))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_dicekey, menu)

        viewModel.isSaved.value?.let {
            menu.findItem(R.id.delete).isVisible = it;
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.forget -> {
                lock()
            }
            R.id.delete -> {
                openDialogDeleteDiceKey(requireContext()) {
                    viewModel.remove()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onResume() {
        super.onResume()
    }

    fun lock(){
        viewModel.forget()
        findNavController().popBackStack()
    }
}

class FragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        return when(position){
            0 -> DiceKeyFragment()
            1 -> SeedSoloKeyFragment()
            2 -> SecretsFragment()
            else -> BackupFragment()
        }
    }
}