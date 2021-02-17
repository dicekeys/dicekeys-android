package org.dicekeys.app.fragments

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.NavGraphDirections
import org.dicekeys.app.R
import org.dicekeys.app.databinding.*
import org.dicekeys.app.extensions.clearNavigationResult
import org.dicekeys.app.extensions.getNavigationResult
import org.dicekeys.app.fragments.backup.BackupFragment
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.AssembleViewModel
import org.dicekeys.app.views.DiceKeyView
import org.dicekeys.app.views.StickerTargetSheetView
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceRead
import javax.inject.Inject

@AndroidEntryPoint
class AssembleFragment: AppFragment<AssembleFragmentBinding>(R.layout.assemble_fragment), ViewPager.OnPageChangeListener {

    @Inject
    lateinit var diceKeyRepository: DiceKeyRepository

    private lateinit var pagerAdapter: AssemblePagerAdapter


    val viewModel: AssembleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getNavigationResult<String>(ScanFragment.READ_DICEKEY)?.observe(viewLifecycleOwner) {
            it?.let {
                clearNavigationResult(ScanFragment.READ_DICEKEY)

                FaceRead.diceKeyFromJsonFacesRead(it)?.let { faceRead ->

                    DiceKey(faces = faceRead.faces.map {
                        Face(letter = it.letter, digit = it.digit, orientationAsLowercaseLetterTrbl = it.orientationAsLowercaseLetterTrbl)
                    }).also {
                        viewModel.setDiceKey(it)
                    }

                    onPageSelected(binding.viewPager.currentItem)

                }
            }
        }

        getNavigationResult<Boolean>(BackupFragment.VALID_BACKUP)?.observe(viewLifecycleOwner) {
            if(it){
                viewModel.diceKeyBackedUp.postValue(true)
                onPageSelected(binding.viewPager.currentItem)
            }
        }

        pagerAdapter = AssemblePagerAdapter(childFragmentManager)

        binding.viewPager.also {
            it.adapter = pagerAdapter
            it.addOnPageChangeListener(this)
            it.setOnTouchListener { _, _ -> true }
        }

        binding.progressBar.max = pagerAdapter.count - 1

        binding.btnNext.setOnClickListener { onNextPage() }
        binding.btnPrev.setOnClickListener { onPrevPage() }

        ObjectAnimator.ofFloat(binding.textWarning, "alpha", 1f, 0.7f).also {
            it.repeatCount = ValueAnimator.INFINITE
            it.repeatMode = ValueAnimator.REVERSE
            it.duration = 500
        }.start()

        onPageSelected(binding.viewPager.currentItem)
    }


    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        binding.progressBar.setProgressCompat(position, true)
        binding.textWarning.visibility = if (position in listOf(0, 5, 6)) View.INVISIBLE else View.VISIBLE
        binding.btnPrev.isEnabled = binding.viewPager.currentItem != 0
        binding.btnNext.isEnabled = when(position) {
            AssemblePagerAdapter.PAGE_SCAN -> viewModel.diceKey.value != null
            AssemblePagerAdapter.PAGE_BACKUP -> viewModel.diceKeyBackedUp.value ?: false
            else -> true
        }
        binding.btnNext.text = if (binding.viewPager.currentItem == pagerAdapter.count - 1) "Done" else "Next"


    }

    override fun onPageScrollStateChanged(state: Int) {}

    fun onSkipStep() {
        binding.viewPager.currentItem = binding.viewPager.currentItem + 1
    }

    fun onScan() {
        navigate(NavGraphDirections.actionGlobalScanFragment())
    }

    fun onUseStickeysKit() {
        navigate(AssembleFragmentDirections.actionBackupSelectToBackupNavGraph(viewModel.diceKey.value?.keyId, true))
    }

    fun onUseDiceKeyKit() {
        navigate(AssembleFragmentDirections.actionBackupSelectToBackupNavGraph(viewModel.diceKey.value?.keyId , false))
    }

    fun onNextPage() {
        if (binding.viewPager.currentItem == pagerAdapter.count - 1) {
            val diceKey = viewModel.diceKey.value
            if(diceKey == null){
                findNavController().popBackStack()
            }else{
                // Remove Assemble from the backstack
                val navOptionsBuilder = NavOptions.Builder().setPopUpTo(R.id.listDiceKeysFragment,false)
                findNavController().navigate(AssembleFragmentDirections.actionAssembleFragmentToMainDiceKeyRootFragment(diceKeyId = diceKey.keyId), navOptionsBuilder.build())
            }

        } else {
            binding.viewPager.also {
                it.setCurrentItem(it.currentItem + 1, true)
            }
        }
    }

    fun onPrevPage() {
        binding.viewPager.also {
            it.setCurrentItem(it.currentItem - 1, true)
        }
    }

    private class AssemblePagerAdapter(fm: FragmentManager)
        : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        companion object {
            val PAGE_SCAN = 3
            val PAGE_BACKUP = 4
        }
        override fun getCount(): Int = 7

        override fun getItem(position: Int): Fragment {
            return AssemblePageFragment(position)
        }
    }

    class AssemblePageFragment(private val fragmentPosition: Int): Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val parent = (parentFragment as AssembleFragment)
            val viewModel = parent.viewModel

             val binding = when(fragmentPosition) {
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

            view.findViewById<Button>(R.id.btn_skip)?.setOnClickListener {
                parent.onSkipStep()
            }

            view.findViewById<Button>(R.id.btn_scan)?.setOnClickListener {
                parent.onScan()
            }

            view.findViewById<View>(R.id.wrapDiceKey)?.setOnClickListener {
                parent.onUseDiceKeyKit()
            }

            view.findViewById<View>(R.id.wrapStickeys)?.setOnClickListener {
                parent.onUseStickeysKit()
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
                it.diceKey = parent.viewModel.diceKey.value ?: DiceKey.example
            }

            view?.findViewById<StickerTargetSheetView>(R.id.dice_key_view1_2)?.also {
                it.diceKey = parent.viewModel.diceKey.value ?: DiceKey.example
            }

            view?.findViewById<DiceKeyView>(R.id.dice_key_view2_1)?.also {
                it.diceKey = parent.viewModel.diceKey.value ?: DiceKey.example
            }

            view?.findViewById<DiceKeyView>(R.id.dice_key_view2_2)?.also {
                it.diceKey = parent.viewModel.diceKey.value ?: DiceKey.example
            }

        }

    }
}