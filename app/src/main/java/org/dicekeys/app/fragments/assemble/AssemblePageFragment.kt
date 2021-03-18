package org.dicekeys.app.fragments.assemble

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.*
import org.dicekeys.app.fragments.backup.BackupPageFragment
import org.dicekeys.app.views.DiceKeyView
import org.dicekeys.app.views.StickerTargetSheetView
import org.dicekeys.dicekey.DiceKey

class AssemblePageFragment : Fragment() {

    private var position: Int = 0

    companion object{
        const val POSITION = "POSITION"

        fun create(position: Int): AssemblePageFragment {

            return AssemblePageFragment().also {
                val bundle = Bundle()
                bundle.putInt(POSITION, position)
                it.arguments = bundle
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        position = arguments?.getInt(BackupPageFragment.POSITION) ?: 0

        val parent = (parentFragment as AssembleFragment)
        val viewModel = parent.assembleViewModel

        val binding = when (position) {
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
            // Clear previous dicekey
            viewModel.setDiceKey(null)
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