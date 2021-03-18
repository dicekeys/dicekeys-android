package org.dicekeys.app.fragments.assemble

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.*
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.*
import org.dicekeys.app.extensions.clearNavigationResult
import org.dicekeys.app.extensions.getNavigationResult
import org.dicekeys.app.fragments.ScanFragment
import org.dicekeys.app.fragments.backup.BackupFragment
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.AssembleViewModel
import org.dicekeys.app.views.DiceKeyView
import org.dicekeys.app.views.StickerTargetSheetView
import org.dicekeys.dicekey.DiceKey
import javax.inject.Inject

@AndroidEntryPoint
class AssembleFragment : AppFragment<AssembleFragmentBinding>(R.layout.assemble_fragment, 0) {

    @Inject
    lateinit var diceKeyRepository: DiceKeyRepository

    private lateinit var pagerAdapter: AssemblePagerAdapter

    val assembleViewModel: AssembleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = assembleViewModel

        getNavigationResult<String>(ScanFragment.READ_DICEKEY)?.observe(viewLifecycleOwner) { humanReadableOrNull ->
            humanReadableOrNull?.let { humanReadable ->
                clearNavigationResult(ScanFragment.READ_DICEKEY)
                assembleViewModel.setDiceKey(DiceKey.fromHumanReadableForm(humanReadable))
            }
        }

        getNavigationResult<Boolean>(BackupFragment.VALID_BACKUP)?.observe(viewLifecycleOwner) {
            if (it) {
                assembleViewModel.diceKeyBackedUp.postValue(true)
            }
        }

        pagerAdapter = AssemblePagerAdapter(childFragmentManager)

        binding.viewPager.also {
            it.adapter = pagerAdapter
            // Disable swipe gestures from changing ViewPager pages
            it.setOnTouchListener { _, _ -> true }
        }

        binding.progressBar.max = pagerAdapter.count - 1

        binding.btnNext.setOnClickListener {
            if (assembleViewModel.page.value == pagerAdapter.count - 1) {
                val diceKey = assembleViewModel.diceKey.value
                // If user hasn't scanned his dicekey, just go back
                if (diceKey == null) {
                    findNavController().popBackStack()
                } else {
                    diceKeyRepository.set(diceKey)
                    // Go To MainDiceKey view and remove Assemble from the backstack
                    val navOptionsBuilder = NavOptions.Builder().setPopUpTo(R.id.listDiceKeysFragment, false)
                    findNavController()
                            .navigate(AssembleFragmentDirections
                                    .actionGlobalDicekey(), navOptionsBuilder.build())
                }
            } else {
                assembleViewModel.nextPage()
            }
        }

        binding.btnPrev.setOnClickListener {
            assembleViewModel.previousPage()
        }

        // Animate warning text
        ObjectAnimator.ofFloat(binding.textWarning, "alpha", 1f, 0.7f).also {
            it.repeatCount = ValueAnimator.INFINITE
            it.repeatMode = ValueAnimator.REVERSE
            it.duration = 500
        }.start()
    }

    private class AssemblePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = 7

        override fun getItem(position: Int): Fragment {
            return AssemblePageFragment.create(position)
        }
    }
}