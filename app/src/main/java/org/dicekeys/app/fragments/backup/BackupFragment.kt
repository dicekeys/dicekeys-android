package org.dicekeys.app.fragments.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.NavGraphDirections
import org.dicekeys.app.R
import org.dicekeys.app.databinding.BackupFragmentBinding
import org.dicekeys.app.extensions.clearNavigationResult
import org.dicekeys.app.extensions.getNavigationResult
import org.dicekeys.app.extensions.setNavigationResult
import org.dicekeys.app.fragments.ScanFragment
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.app.views.DiceKeyView
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import javax.inject.Inject

@AndroidEntryPoint
class BackupFragment: AppFragment<BackupFragmentBinding>(R.layout.backup_fragment, 0), ViewPager.OnPageChangeListener {

    companion object {
        const val VALID_BACKUP = "valid_backup"
    }

    @Inject
    lateinit var repository: DiceKeyRepository

    private val args: BackupFragmentArgs by navArgs()

    var diceKey: DiceKey<Face> = DiceKey.example

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

        getNavigationResult<String>(ScanFragment.READ_DICEKEY)?.observe(viewLifecycleOwner) { humanReadableOrNull ->
            humanReadableOrNull?.let { humanReadable ->
                clearNavigationResult(ScanFragment.READ_DICEKEY)

                val scannedDiceKey = DiceKey.fromHumanReadableForm(humanReadable)
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
            return BackupPageFragment.create(useStickeys, position)
        }
    }
}