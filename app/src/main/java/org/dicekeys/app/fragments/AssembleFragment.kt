package org.dicekeys.app.fragments

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.AssembleFragmentBinding
import org.dicekeys.app.fragments.dicekey.BackupSelectFragmentDirections
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.read.ReadDiceKeyActivity

@AndroidEntryPoint
class AssembleFragment: AppFragment<AssembleFragmentBinding>(R.layout.assemble_fragment), ViewPager.OnPageChangeListener {

    private lateinit var pagerAdapter: AssemblePagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pagerAdapter = AssemblePagerAdapter(childFragmentManager)

        binding.viewPager.also {
            it.adapter = pagerAdapter
            it.addOnPageChangeListener(this)
            it.setOnTouchListener { _, _ -> true }
        }

        binding.progressBar.max = pagerAdapter.count - 1

        binding.btnNext.setOnClickListener { onNextPage() }
        binding.btnPrev.setOnClickListener { onPrevPage() }

        onPageSelected(binding.viewPager.currentItem)
    }


    companion object {
        val REQUEST_CODE_SCAN = 1001
        val REQUEST_CODE_BACKUP = 1002
    }
    // Add state management (LiveData, Coroutines Flows, etc)
    private var diceKey: DiceKey<Face> = DiceKey.example

    private var diceKeyScanned: Boolean = false
    private var diceKeyBackedUp: Boolean = false


    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        binding.progressBar.progress = position
        binding.textWarning.visibility = if (position in listOf(0, 5, 6)) View.INVISIBLE else View.VISIBLE
        binding.btnPrev.isEnabled = binding.viewPager.currentItem != 0
        binding.btnNext.isEnabled = when(position) {
            AssemblePagerAdapter.PAGE_SCAN -> diceKeyScanned
            AssemblePagerAdapter.PAGE_BACKUP -> diceKeyBackedUp
            else -> true
        }
        binding.btnNext.text = if (binding.viewPager.currentItem == pagerAdapter.count - 1) "Done" else "Next"


    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (
                resultCode == Activity.RESULT_OK &&
                requestCode == REQUEST_CODE_SCAN &&
                data != null &&
                data.hasExtra(ReadDiceKeyActivity.Companion.Parameters.Response.diceKeyAsJson)
        ) {
            data.getStringExtra(ReadDiceKeyActivity.Companion.Parameters.Response.diceKeyAsJson)?.let { diceKeyAsJson ->
                FaceRead.diceKeyFromJsonFacesRead(diceKeyAsJson)?.let { diceKey ->
                    this.diceKey = DiceKey(faces = diceKey.faces.map {
                        Face(letter = it.letter, digit = it.digit, orientationAsLowercaseLetterTrbl = it.orientationAsLowercaseLetterTrbl)
                    })
                    diceKeyScanned = true
                }
            }
        } else if (requestCode == REQUEST_CODE_BACKUP) {
            diceKeyBackedUp = true
        }
        onPageSelected(binding.viewPager.currentItem)
    }

    fun onSkipStep() {
        binding.viewPager.currentItem = binding.viewPager.currentItem + 1
    }

    fun onScan() {
        val intent = Intent(requireContext(), ReadDiceKeyActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SCAN)
    }

    fun onUseStickeysKit() {
        navigate(AssembleFragmentDirections.actionBackupSelectToBackupNavGraph(null, true))
    }

    fun onUseDiceKeyKit() {
        navigate(AssembleFragmentDirections.actionBackupSelectToBackupNavGraph(null, false))
    }

    fun onNextPage() {
        if (binding.viewPager.currentItem == pagerAdapter.count - 1) {
            findNavController().popBackStack()
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

            return when(position) {
                0 -> AssemblePageFragment(R.layout.fragment_assemble_step1)
                1 -> AssemblePageFragment(R.layout.fragment_assemble_step2)
                2 -> AssemblePageFragment(R.layout.fragment_assemble_step3)
                3 -> AssemblePageFragment(R.layout.fragment_assemble_step4)
                4 -> AssemblePageFragment(R.layout.fragment_assemble_step5)
                5 -> AssemblePageFragment(R.layout.fragment_assemble_step6)
                6 -> AssemblePageFragment(R.layout.fragment_assemble_step7)
                else -> AssemblePageFragment(R.layout.fragment_assemble_step1)
            }
        }
    }

    class AssemblePageFragment(@LayoutRes val contentLayoutId: Int): Fragment(contentLayoutId) {

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            view.findViewById<Button>(R.id.btn_skip)?.setOnClickListener {
                (parentFragment as AssembleFragment).onSkipStep()
            }

            view.findViewById<Button>(R.id.btn_scan)?.setOnClickListener {
                (parentFragment as AssembleFragment).onScan()
            }

            view.findViewById<View>(R.id.wrapDiceKey)?.setOnClickListener {
                (parentFragment as AssembleFragment).onUseDiceKeyKit()
            }

            view.findViewById<View>(R.id.wrapStickeys)?.setOnClickListener {
                (parentFragment as AssembleFragment).onUseStickeysKit()
            }
        }

        override fun onResume() {
            super.onResume()

            // Animate the bag
            view?.findViewById<ImageView>(R.id.img_shaking_bug)?.let {

                val animator = ObjectAnimator.ofFloat(it, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).also {
                    it.duration = 600
                    it.repeatCount = 2
                }

                animator.start()
            }

        }

    }
}