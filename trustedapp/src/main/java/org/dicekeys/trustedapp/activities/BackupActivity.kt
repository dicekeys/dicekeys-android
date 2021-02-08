package org.dicekeys.trustedapp.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.read.ReadDiceKeyActivity
import org.dicekeys.trustedapp.R
import org.dicekeys.trustedapp.databinding.ActivityBackupBinding
import org.dicekeys.trustedapp.databinding.FragmentBackupDicekitBinding
import org.dicekeys.trustedapp.databinding.FragmentBackupStickeysBinding

class BackupActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {
    companion object {
        val EXTRA_DICEKEY_KEY = "dicekey"
        val EXTRA_USE_STICKEYS_KEY = "use_stickeys"

        fun backupWithStickeysIntent(context: Activity, diceKey: DiceKey<Face>): Intent {
            val intent = Intent(context, BackupActivity::class.java)
            intent.putExtra(EXTRA_DICEKEY_KEY, diceKey.toHumanReadableForm())
            intent.putExtra(EXTRA_USE_STICKEYS_KEY, true)
            return intent
        }

        fun backupWithDiceKitIntent(context: Activity, diceKey: DiceKey<Face>): Intent {
            val intent = Intent(context, BackupActivity::class.java)
            intent.putExtra(EXTRA_DICEKEY_KEY, diceKey.toHumanReadableForm())
            intent.putExtra(EXTRA_USE_STICKEYS_KEY, false)
            return intent
        }
    }

    var diceKey: DiceKey<Face> = DiceKey.example
    var useStickeys: Boolean = false
    private lateinit var binding: ActivityBackupBinding
    private lateinit var pagerAdapter: BackupPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra(EXTRA_DICEKEY_KEY)) {
            diceKey = DiceKey.fromHumanReadableForm(intent.getStringExtra(EXTRA_DICEKEY_KEY)!!)
        }
        if (intent.hasExtra(EXTRA_USE_STICKEYS_KEY)) {
            useStickeys = intent.getBooleanExtra(EXTRA_USE_STICKEYS_KEY, false)
        }
        binding = ActivityBackupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        pagerAdapter = BackupPagerAdapter(supportFragmentManager, diceKey, useStickeys)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.addOnPageChangeListener(this)
        binding.progressBar.max = pagerAdapter.count - 1
        onPageSelected(0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        binding.progressBar.progress = position
        binding.btnPrev.isEnabled = position > 0
        binding.btnFirst.isEnabled = position > 0
        binding.btnNext.isEnabled = position < pagerAdapter.count - 1
        binding.btnLast.isEnabled = position < pagerAdapter.count - 1
    }

    override fun onPageScrollStateChanged(state: Int) {}

    fun onPrevPage(view: View?) {
        binding.viewPager.currentItem = binding.viewPager.currentItem - 1
    }

    fun onNextPage(view: View?) {
        binding.viewPager.currentItem = binding.viewPager.currentItem + 1
    }

    fun onFirstPage(view: View?) {
        binding.viewPager.currentItem = 0
    }

    fun onLastPage(view: View?) {
        binding.viewPager.currentItem = pagerAdapter.count - 1
    }

    fun onSkipStep(view: View?) {
        finish()
    }

    fun onScan(view: View?) {
        val intent = Intent(this, ReadDiceKeyActivity::class.java)
        startActivityForResult(intent, 0)
    }

    fun onOrderMore(view: View?) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dicekeys.com/store"))
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (
                resultCode == Activity.RESULT_OK &&
                data != null &&
                data.hasExtra(ReadDiceKeyActivity.Companion.Parameters.Response.diceKeyAsJson)
        ) {
            data.getStringExtra(ReadDiceKeyActivity.Companion.Parameters.Response.diceKeyAsJson)?.let { diceKeyAsJson ->
                FaceRead.diceKeyFromJsonFacesRead(diceKeyAsJson)?.let { diceKey ->
                    val scannedDiceKey = DiceKey(faces = diceKey.faces.map {
                        Face(letter = it.letter, digit = it.digit, orientationAsLowercaseLetterTrbl = it.orientationAsLowercaseLetterTrbl)
                    })
                }
            }
        }
    }

    private class BackupPagerAdapter(fm: FragmentManager, val diceKey: DiceKey<Face>, val useStickeys: Boolean)
        : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = diceKey.faces.size + 2

        override fun getItem(position: Int): Fragment {
            return BackupFragment(diceKey, useStickeys, position)
        }
    }

    class BackupFragment(
            val diceKey: DiceKey<Face>,
            val useStickeys: Boolean,
            val position: Int) : Fragment() {

        var dicekitBinding: FragmentBackupDicekitBinding? = null
        var stickeysBinding: FragmentBackupStickeysBinding? = null
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            if (position == 0) {
                val layoutId = if (useStickeys) R.layout.fragment_backup_stickeys_intro else R.layout.fragment_backup_dicekit_intro
                return inflater.inflate(layoutId, container, false)
            } else if (position == diceKey.faces.size + 1) {
                val layoutId = if (useStickeys) R.layout.fragment_backup_stickeys_validate else R.layout.fragment_backup_dicekit_validate
                return inflater.inflate(layoutId, container, false)
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
            if (position == 0 || position == diceKey.faces.size + 1) return
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