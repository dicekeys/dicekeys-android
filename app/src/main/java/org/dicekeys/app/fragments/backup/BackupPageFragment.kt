package org.dicekeys.app.fragments.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.R
import org.dicekeys.app.databinding.FragmentBackupDicekitBinding
import org.dicekeys.app.databinding.FragmentBackupStickeysBinding
import org.dicekeys.app.utils.openBrowser
import org.dicekeys.app.views.DiceKeyView
import org.dicekeys.app.views.StickerTargetSheetView
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face

@AndroidEntryPoint
class BackupPageFragment : Fragment() {

    companion object{

        const val USE_STICKEYS = "USE_STICKEYS"
        const val POSITION = "POSITION"

        fun create(useStickeys: Boolean,
                   position: Int): BackupPageFragment{

            return BackupPageFragment().also {
                val bundle = Bundle()
                bundle.putBoolean(USE_STICKEYS, useStickeys)
                bundle.putInt(POSITION, position)
                it.arguments = bundle
            }
        }
    }

    private lateinit var diceKey: DiceKey<Face>
    var position: Int = 0
    var useStickeys = false

    var dicekitBinding: FragmentBackupDicekitBinding? = null
    var stickeysBinding: FragmentBackupStickeysBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        diceKey = (parentFragment as BackupFragment).diceKey

        position = arguments?.getInt(POSITION) ?: 0
        useStickeys = arguments?.getBoolean(USE_STICKEYS) ?: false


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