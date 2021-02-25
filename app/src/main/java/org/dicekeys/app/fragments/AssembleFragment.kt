package org.dicekeys.app.fragments

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
import org.dicekeys.app.fragments.backup.BackupFragment
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.AssembleViewModel
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.app.views.DiceKeyView
import org.dicekeys.app.views.StickerTargetSheetView
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceRead
import javax.inject.Inject

@AndroidEntryPoint
class AssembleFragment : AppFragment<AssembleFragmentBinding>(R.layout.assemble_fragment) {

    @Inject
    lateinit var diceKeyRepository: DiceKeyRepository

    private lateinit var pagerAdapter: AssemblePagerAdapter


    val assembleViewModel: AssembleViewModel by viewModels()
    val viewModel: DiceKeyViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = assembleViewModel

        getNavigationResult<String>(ScanFragment.READ_DICEKEY)?.observe(viewLifecycleOwner) { facesReadJsonOrNull ->
            facesReadJsonOrNull?.let { facesReadJson ->
                clearNavigationResult(ScanFragment.READ_DICEKEY)

                FaceRead.diceKeyFromJsonFacesRead(facesReadJson)?.let { diceKeyFaceRead ->

                    // Convert to Face
                    assembleViewModel.setDiceKey(DiceKey(faces = diceKeyFaceRead.faces.map {
                        Face(letter = it.letter, digit = it.digit, orientationAsLowercaseLetterTrbl = it.orientationAsLowercaseLetterTrbl)
                    }))
                }
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
                    viewModel.setDiceKey(diceKey)
                    // Go To MainDiceKey view and remove Assemble from the backstack
                    val navOptionsBuilder = NavOptions.Builder().setPopUpTo(R.id.listDiceKeysFragment, false)
                    findNavController().navigate(AssembleFragmentDirections.actionGlobalDicekey(), navOptionsBuilder.build())
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
            return AssemblePageFragment(position)
        }
    }

    class AssemblePageFragment(private val fragmentPosition: Int) : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val parent = (parentFragment as AssembleFragment)
            val viewModel = parent.assembleViewModel

            val binding = when (fragmentPosition) {
                1 -> FragmentAssembleStep2Binding.inflate(inflater, container, false).also {
                    it.vm = viewModel
                }
                2 -> FragmentAssembleStep3Binding.inflate(inflater, container, false).also {
                    it.vm = viewModel
                }
                3 -> FragmentAssembleStep4Binding.inflate(inflater, container, false).also {
                    it.vm = viewModel
                }
                4 -> FragmentAssembleStep5Binding.inflate(inflater, container, false).also {
                    it.vm = viewModel
                }
                5 -> FragmentAssembleStep6Binding.inflate(inflater, container, false).also {
                    it.vm = viewModel
                }
                6 -> FragmentAssembleStep7Binding.inflate(inflater, container, false).also {
                    it.vm = viewModel
                }
                else -> FragmentAssembleStep1Binding.inflate(inflater, container, false).also {
                    it.vm = viewModel
                }
            }

            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val parent = (parentFragment as AssembleFragment)
            val viewModel = parent.assembleViewModel

            view.findViewById<Button>(R.id.btn_skip)?.setOnClickListener {
                viewModel.nextPage()
            }

            view.findViewById<Button>(R.id.btn_scan)?.setOnClickListener {
                parent.navigate(AssembleFragmentDirections.actionGlobalScanFragment())
            }

            view.findViewById<View>(R.id.wrapDiceKey)?.setOnClickListener {
                parent.navigate(AssembleFragmentDirections.actionAssembleFragmentToBackupFragment(viewModel.diceKey.value?.keyId, false))
            }

            view.findViewById<View>(R.id.wrapStickeys)?.setOnClickListener {
                parent.navigate(AssembleFragmentDirections.actionAssembleFragmentToBackupFragment(viewModel.diceKey.value?.keyId, true))
            }

        }

        override fun onResume() {
            super.onResume()

            val parent = (parentFragment as AssembleFragment)

            // Animate the bag
            view?.findViewById<ImageView>(R.id.img_shaking_bug)?.let {

                val animator = ObjectAnimator.ofFloat(it, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).also {
                    it.duration = 600
                    it.repeatCount = 2
                }

                animator.start()
            }

            // Dicekeys
            view?.findViewById<DiceKeyView>(R.id.dice_key_view1_1)?.also {
                it.diceKey = parent.assembleViewModel.diceKey.value ?: DiceKey.example
            }

            view?.findViewById<StickerTargetSheetView>(R.id.dice_key_view1_2)?.also {
                it.diceKey = parent.assembleViewModel.diceKey.value ?: DiceKey.example
            }

            view?.findViewById<DiceKeyView>(R.id.dice_key_view2_1)?.also {
                it.diceKey = parent.assembleViewModel.diceKey.value ?: DiceKey.example
            }

            view?.findViewById<DiceKeyView>(R.id.dice_key_view2_2)?.also {
                it.diceKey = parent.assembleViewModel.diceKey.value ?: DiceKey.example
            }
        }
    }
}