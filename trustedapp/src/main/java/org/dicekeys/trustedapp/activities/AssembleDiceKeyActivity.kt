package org.dicekeys.trustedapp.activities

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import org.dicekeys.trustedapp.R
import org.dicekeys.trustedapp.databinding.ActivityAssembleDiceKeyBinding

class AssembleDiceKeyActivity : AppCompatActivity(), View.OnClickListener, ViewPager.OnPageChangeListener {
    private lateinit var binding: ActivityAssembleDiceKeyBinding
    private lateinit var pagerAdapter: AssemblePagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssembleDiceKeyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        pagerAdapter = AssemblePagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.addOnPageChangeListener(this)
        binding.progressBar.max = pagerAdapter.count - 1
        binding.viewPager.currentItem = 4
    }

    override fun onClick(view: View?) {

    }

    private class AssemblePagerAdapter(fm: FragmentManager)
        : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = 6

        override fun getItem(position: Int): Fragment {

            return when(position) {
                0 -> AssembleFragment(R.layout.fragment_assemble_step1)
                1 -> AssembleFragment(R.layout.fragment_assemble_step2)
                2 -> AssembleFragment(R.layout.fragment_assemble_step3)
                3 -> AssembleFragment(R.layout.fragment_assemble_step4)
                4 -> AssembleFragment(R.layout.fragment_assemble_step5)
                else -> AssembleFragment(R.layout.fragment_assemble_step6)
            }
        }
    }

    class AssembleFragment(@LayoutRes contentLayoutId: Int): Fragment(contentLayoutId) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        binding.progressBar.progress = position
        binding.textWarning.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
    }

    override fun onPageScrollStateChanged(state: Int) {

    }
}