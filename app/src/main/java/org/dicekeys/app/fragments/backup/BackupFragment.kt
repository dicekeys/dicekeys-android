package org.dicekeys.app.fragments.backup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.NavGraphDirections
import org.dicekeys.app.R
import org.dicekeys.app.adapters.dicekey
import org.dicekeys.app.databinding.BackupFragmentBinding
import org.dicekeys.app.databinding.FragmentBackupDicekitBinding
import org.dicekeys.app.databinding.FragmentBackupStickeysBinding
import org.dicekeys.app.extensions.clearNavigationResult
import org.dicekeys.app.extensions.getNavigationResult
import org.dicekeys.app.extensions.setNavigationResult
import org.dicekeys.app.fragments.ListDiceKeysFragmentDirections
import org.dicekeys.app.fragments.ScanFragment
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.utils.openBrowser
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.app.views.DiceKeyView
import org.dicekeys.app.views.StickerTargetSheetView
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.read.ReadDiceKeyActivity
import javax.inject.Inject

@AndroidEntryPoint
class BackupFragment: AppFragment<BackupFragmentBinding>(R.layout.backup_fragment), ViewPager.OnPageChangeListener {

    companion object {
        const val VALID_BACKUP = "valid_backup"
    }

    @Inject
    lateinit var repository: DiceKeyRepository

    private val args: BackupFragmentArgs by navArgs()

    lateinit var diceKey: DiceKey<Face>

    private val useStickeys by lazy { args.useStickeys }

    private lateinit var pagerAdapter: BackupPagerAdapter

    val viewModel: DiceKeyViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Guard: If DiceKey is not available, return
        args.diceKeyId?.also { diceKeyId ->
            repository.get(diceKeyId)?.also {
                diceKey = it
            } ?: run {
                findNavController().popBackStack()
                return
            }
        } ?: run{
            diceKey = DiceKey.example
        }

        binding.vm = viewModel

        pagerAdapter = BackupPagerAdapter(childFragmentManager, diceKey, useStickeys)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.addOnPageChangeListener(this)
        binding.progressBar.max = pagerAdapter.count - 1
        onPageSelected(0)

        binding.btnFirst.setOnClickListener {
            onFirstPage()
        }

        binding.btnLast.setOnClickListener {
            onLastPage()
        }

        binding.btnNext.setOnClickListener {
            onNextPage()
        }

        binding.btnPrev.setOnClickListener {
            onPrevPage()
        }


        getNavigationResult<String>(ScanFragment.READ_DICEKEY)?.observe(viewLifecycleOwner) {
            it?.let {
                clearNavigationResult(ScanFragment.READ_DICEKEY)

                FaceRead.diceKeyFromJsonFacesRead(it)?.let { diceKeyFaceRead ->
                    val scannedDiceKey = DiceKey.toDiceKey(diceKeyFaceRead)
                    val backupDiceKey = diceKey.mostSimilarRotationOf(scannedDiceKey)
                    val invalidIndexes = (0 until 25).filter {
                        diceKey.faces[it].numberOfFieldsDifferent(backupDiceKey.faces[it]) > 0
                    }.toSet()

                    val perfectMatch = invalidIndexes.isEmpty()
                    val totalMismatch = invalidIndexes.size > 5
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Your scanned copy")

                    if (perfectMatch) {
                        builder.setMessage("You made a perfect copy!")
                        setNavigationResult(result = true, key = VALID_BACKUP)
                    } else if (totalMismatch) {
                        builder.setMessage("That key doesn't look at all like the key you scanned before.")
                    } else {
                        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_backup_verify, null)
                        view.findViewById<DiceKeyView>(R.id.dice_key).highlightedIndexes = invalidIndexes
                        view.findViewById<TextView>(R.id.text).text = "You incorrectly copied the highlighted " + (if (invalidIndexes.size == 1) "die" else "dice") + ". You can fix the copy to match the original, or change the original to match the copy."
                        builder.setView(view)
                    }
                    builder.setPositiveButton("OK") { dialog, _ ->
                        dialog.cancel()
                        if(perfectMatch){
                            finish()
                        }
                    }
                    builder.show()
                }
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        binding.progressBar.setProgressCompat(position, true)
        binding.btnPrev.isEnabled = position > 0
        binding.btnFirst.isEnabled = position > 0
        binding.btnNext.isEnabled = position < pagerAdapter.count - 1
        binding.btnLast.isEnabled = position < pagerAdapter.count - 1
    }

    override fun onPageScrollStateChanged(state: Int) {}

    fun onPrevPage() {
        binding.viewPager.currentItem = binding.viewPager.currentItem - 1
    }

    fun onNextPage() {
        binding.viewPager.currentItem = binding.viewPager.currentItem + 1
    }

    fun onFirstPage() {
        binding.viewPager.currentItem = 0
    }

    fun onLastPage() {
        binding.viewPager.currentItem = pagerAdapter.count - 1
    }

    fun onScan() {
        navigate(NavGraphDirections.actionGlobalScanFragment())
    }

    fun finish(){
        findNavController().popBackStack()
    }

    private class BackupPagerAdapter(fm: FragmentManager, val diceKey: DiceKey<Face>, val useStickeys: Boolean)
        : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = diceKey.faces.size + 2

        override fun getItem(position: Int): Fragment {
            return BackupPageFragment(diceKey, useStickeys, position)
        }
    }

    class BackupPageFragment(
            val diceKey: DiceKey<Face>,
            val useStickeys: Boolean,
            val position: Int) : Fragment() {

        var dicekitBinding: FragmentBackupDicekitBinding? = null
        var stickeysBinding: FragmentBackupStickeysBinding? = null
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            if (position == 0) {
                val layoutId = if (useStickeys) R.layout.fragment_backup_stickeys_intro else R.layout.fragment_backup_dicekit_intro
                return inflater.inflate(layoutId, container, false).also {
                    it.findViewById<Button>(R.id.btn_order_more).setOnClickListener {
                        openBrowser(requireContext(),"https://dicekeys.com/store")
                    }
                }
            } else if (position == diceKey.faces.size + 1) {
                val layoutId = if (useStickeys) R.layout.fragment_backup_stickeys_validate else R.layout.fragment_backup_dicekit_validate
                return inflater.inflate(layoutId, container, false).also {
                    it.findViewById<Button>(R.id.btn_skip).setOnClickListener {
                        (parentFragment as BackupFragment).finish()
                    }
                    it.findViewById<Button>(R.id.btn_scan_copy).setOnClickListener {
                        (parentFragment as BackupFragment).onScan()
                    }
                }
            }
            if (useStickeys) {
                stickeysBinding = FragmentBackupStickeysBinding.inflate(inflater, container, false)
                return stickeysBinding?.root
            } else {
                dicekitBinding = FragmentBackupDicekitBinding.inflate(inflater, container, false)
                return dicekitBinding?.root
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            if (position == 0) return
            if (position == diceKey.faces.size + 1) {
                if (useStickeys) {
                    view.findViewById<StickerTargetSheetView>(R.id.dice_view).diceKey = diceKey
                } else {
                    view.findViewById<DiceKeyView>(R.id.dice_view).diceKey = diceKey
                }
                return
            }
            val index = position - 1
            val face = diceKey.faces[index]

            if (useStickeys) {
                stickeysBinding?.stickerTargetSheetView?.diceKey = diceKey
                stickeysBinding?.stickerTargetSheetView?.showDiceAtIndexes = (0..index-1).toSet()
                stickeysBinding?.stickerSheetView?.setPageIndexForFace(face)
                stickeysBinding?.stickerSheetView?.highlightedIndexes = listOf(stickeysBinding?.stickerSheetView?.getIndexForFace(face) ?: -1).toSet()
                stickeysBinding?.twoDiceviewLayout?.sourceDiceViewIndex = stickeysBinding?.stickerSheetView?.getIndexForFace(face)
                stickeysBinding?.twoDiceviewLayout?.targetDiceViewIndex = index

                val instructions = "Remove the ${face.letter}${face.digit} sticker from the sheet with letters ${stickeysBinding?.stickerSheetView?.firstLetter} through ${stickeysBinding?.stickerSheetView?.lastLetter}.\n" +
                        (if (face.orientationAsLowercaseLetterTrbl != 't') "Rotate it so the top faces to the ${face.orientationAsFacingString}.\n" else "") +
                        "Place it squarely covering the target rectangle" +
                        (if (index == 0) " at the top left of the target sheet" else "") +
                        "."
                stickeysBinding?.textInstruction?.text = instructions

            } else {
                dicekitBinding?.diceKeySource?.diceKey = diceKey
                dicekitBinding?.diceKeySource?.highlightedIndexes = listOf(index).toSet()

                dicekitBinding?.diceKeyTarget?.diceKey = diceKey
                dicekitBinding?.diceKeyTarget?.highlightedIndexes = listOf(index).toSet()
                dicekitBinding?.diceKeyTarget?.showDiceAtIndexes =  (0..index-1).toSet()

                dicekitBinding?.twoDiceviewLayout?.sourceDiceViewIndex = index
                dicekitBinding?.twoDiceviewLayout?.targetDiceViewIndex = index

                val instructions = "Find the ${face.letter} die.\n" +
                        (if (face.orientationAsLowercaseLetterTrbl != 't') "Rotate it so the top faces to the ${face.orientationAsFacingString}.\n" else "") +
                        "Place it squarely into the hole" +
                        (if (index == 0) " at the top left of the target box" else "") +
                        "."
                dicekitBinding?.textInstruction?.text = instructions
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            dicekitBinding = null
            stickeysBinding = null
        }
    }
}