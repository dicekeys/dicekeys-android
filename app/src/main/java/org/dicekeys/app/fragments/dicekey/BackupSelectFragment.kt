package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.api.derivationRecipeTemplates
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.BackupSelectFragmentBinding
import org.dicekeys.app.viewmodels.DiceKeyViewModel

@AndroidEntryPoint
class BackupSelectFragment: AppFragment<BackupSelectFragmentBinding>(R.layout.backup_select_fragment) {

    private lateinit var viewModel: DiceKeyViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getDiceKeyRootFragment().viewModel

        binding.vm = viewModel

        binding.wrapStickeys.setOnClickListener {
            navigate(BackupSelectFragmentDirections.actionBackupSelectToBackupNavGraph(viewModel.diceKey.keyId, true))
        }

        binding.wrapDiceKey.setOnClickListener {
            navigate(BackupSelectFragmentDirections.actionBackupSelectToBackupNavGraph(viewModel.diceKey.keyId, false))
        }
    }
}