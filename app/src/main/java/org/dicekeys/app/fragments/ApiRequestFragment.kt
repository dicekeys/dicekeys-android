package org.dicekeys.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.dicekeys.api.ApiStrings
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.api.UserDeclinedToAuthorizeOperation
import org.dicekeys.app.R
import org.dicekeys.app.apicommands.permissionchecked.PermissionCheckedUrlCommands
import org.dicekeys.app.data.DiceKeyDescription
import org.dicekeys.app.databinding.ApiRequestFragmentBinding
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.app.extensions.*
import org.dicekeys.app.viewmodels.ApiRequestViewModel
import org.dicekeys.app.viewmodels.RecipeViewModel
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.SimpleDiceKey
import javax.inject.Inject

@AndroidEntryPoint
class ApiRequestFragment : AbstractListDiceKeysFragment<ApiRequestFragmentBinding>(R.layout.api_request_fragment, menuRes = 0) {

    private lateinit var urlCommand: PermissionCheckedUrlCommands

    @Inject
    lateinit var viewModelFactory: ApiRequestViewModel.AssistedFactory

    private val viewModel: ApiRequestViewModel by viewModels {
        ApiRequestViewModel.provideFactory(assistedFactory = viewModelFactory , command = urlCommand.command)
    }

    override val staticViewsCount: Int = 1

    override val linearLayoutContainer: LinearLayout
        get() = binding.container

    private val hostOrAppName by lazy {
        urlCommand.getHost()?.let {
            when (it) {
                "apple.com" -> "Apple"
                "live.com", "microsoft.com" -> "Microsoft"
                "bitwarden.com" -> "BitWarden"
                "1password.com" -> "1Password"
                else -> it
            }
        }
    }

    override fun clickOnDiceKey(view: View, diceKeyDescription: DiceKeyDescription) {
        val diceKey = diceKeyRepository.get(diceKeyDescription.keyId)

        // Needs Decryption
        if(diceKey == null){
            encryptedStorage.getEncryptedData(diceKeyDescription.keyId)?.let {
                biometricsHelper.decrypt(it , this, {
                    setDiceKey(it)
                })
            }
        }else{
            setDiceKey(diceKey)
        }
    }

    override fun longClickOnDiceKey(view: View, diceKey: DiceKeyDescription) {}

    private val onBackCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            userCancel()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try{
            val intent = requireArguments().getParcelable<Intent>(NavController.KEY_DEEP_LINK_INTENT)

            // Our API commands don't get a copy of the raw DiceKey seed, but only an accessor
            // which must be passed parameters to check.
            urlCommand = PermissionCheckedUrlCommands(
                    intent!!.data!!, ::loadDiceKeyAsync, ::requestUsersConsentAsync, requireActivity()
            )
        }catch (e: Exception){
            e.printStackTrace()
            toast(e)
            findNavController().popBackStack()
            return
        }

        getNavigationResult<String>(ScanFragment.READ_DICEKEY)?.observe(viewLifecycleOwner) { humanReadableOrNull ->
            humanReadableOrNull?.let { humanReadable ->
                clearNavigationResult(ScanFragment.READ_DICEKEY)
                setDiceKey(DiceKey.fromHumanReadableForm(humanReadable))
            }
        }

        binding.vm = viewModel

        binding.dicekey.setOnClickListener {
            viewModel.toggleHideFaces()
        }

        // TODO implement recreate
        val create = getString(R.string.s_create)

        viewModel.title.value = when (urlCommand.command) {
            ApiStrings.Commands.getPassword -> {
                getString(R.string.may_use_password, hostOrAppName, create)
            }
            ApiStrings.Commands.getSecret -> {
                getString(R.string.may_use_secret_code, hostOrAppName, create)
            }
            ApiStrings.Commands.getUnsealingKey -> {
                getString(R.string.may_use_keys_encode_decode_secrets, hostOrAppName, create)
            }
            ApiStrings.Commands.getSymmetricKey -> {
                getString(R.string.may_use_key_encode_decode_secrets, hostOrAppName, create)
            }
            ApiStrings.Commands.sealWithSymmetricKey -> {
                getString(R.string.may_use_encode_secret, hostOrAppName)
            }
            ApiStrings.Commands.unsealWithSymmetricKey, ApiStrings.Commands.unsealWithUnsealingKey -> {
                getString(R.string.may_use_decode_secret, hostOrAppName)
            }
            ApiStrings.Commands.getSigningKey -> {
                getString(R.string.may_use_keys_sign_data, create, hostOrAppName)
            }
            ApiStrings.Commands.generateSignature -> {
                getString(R.string.may_use_digital_signature, hostOrAppName)
            }
            ApiStrings.Commands.getSignatureVerificationKey -> {
                getString(R.string.may_use_key_verify, hostOrAppName, create)
            }
            ApiStrings.Commands.getSealingKey -> {
                getString(R.string.may_use_keys_store_secrets, hostOrAppName, create)
            }
            else -> ""
        }
        viewModel.subtitle.value = getString(R.string.will_not_see_your_dicekey, hostOrAppName)

        viewModel.approve.value = when (urlCommand.command) {
            ApiStrings.Commands.getPassword -> {
                getString(R.string.send_password)
            }
            ApiStrings.Commands.getSecret -> {
                getString(R.string.send_secret)
            }
            ApiStrings.Commands.getUnsealingKey, ApiStrings.Commands.getSealingKey, ApiStrings.Commands.getSigningKey -> {
                getString(R.string.send_keys)
            }
            ApiStrings.Commands.getSymmetricKey, ApiStrings.Commands.getSignatureVerificationKey -> {
                getString(R.string.send_key)
            }
            ApiStrings.Commands.sealWithSymmetricKey -> {
                getString(R.string.send_encoded_message)
            }
            ApiStrings.Commands.unsealWithSymmetricKey, ApiStrings.Commands.unsealWithUnsealingKey -> {
                getString(R.string.send_encoded_message)
            }
            else -> getString(R.string.approve) // ApiStrings.Commands.generateSignature
        }

        binding.load.setOnClickListener {
            navigate(ListDiceKeysFragmentDirections.actionGlobalScanFragment(showEnterByHand = true))
        }

        binding.buttonConfirm.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                try {
                    val sendCenterLetterAndDigit = binding.centerLetterAndDigitSwitch.isChecked
                    urlCommand.send(sendCenterLetterAndDigit = sendCenterLetterAndDigit)
                    popBackStack()
                } catch (e: Exception) {
                    errorDialog(e.message ?: "Error") {
                        popBackStack()
                    }
                }
            }
        }

        binding.buttonCancel.setOnClickListener {
            userCancel()
        }

        binding.buttonSequenceUp.setOnClickListener {
            viewModel.sequenceUp()
        }

        binding.buttonSequenceDown.setOnClickListener {
            viewModel.sequenceDown()
        }

        viewModel.sequence.observe(viewLifecycleOwner) {
            executeCommand()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackCallback)
    }

    private fun userCancel(){
        onBackCallback.isEnabled = false
        urlCommand.sendException(UserDeclinedToAuthorizeOperation("User Declined To Authorize Operation"))
        popBackStack()
    }

    private fun popBackStack(){
        if(!findNavController().popBackStack()){
            requireActivity().finish()
        }
    }

    private fun executeCommand(){
        if(viewModel.diceKey.value == null){
            return
        }

        lifecycleScope.launchWhenCreated {
            urlCommand.setRecipeSequence(viewModel.sequence.value?.toIntOrNull())
            urlCommand.executeCommand()

            viewModel.recipe.value = urlCommand.recipeWithSequence()

            if(urlCommand.hasException()){
                dialog(getString(R.string.error), getString(R.string.another_app_made_an_invalid_request, urlCommand.exception?.message ?: "unknown error")) {
                    urlCommand.sendException()
                    popBackStack()
                }
            }else{
                viewModel.createLabel.value = when (urlCommand.command) {
                    ApiStrings.Commands.getPassword -> {
                        getString(R.string.password_created)
                    }
                    ApiStrings.Commands.getSecret -> {
                        getString(R.string.secret_created)
                    }
                    ApiStrings.Commands.getUnsealingKey, ApiStrings.Commands.getSealingKey, ApiStrings.Commands.getSigningKey -> {
                        getString(R.string.key_created)
                    }
                    ApiStrings.Commands.getSymmetricKey, ApiStrings.Commands.getSignatureVerificationKey -> {
                        getString(R.string.key_created)
                    }
                    ApiStrings.Commands.sealWithSymmetricKey -> {
                        getString(R.string.message_to_seal)
                    }
                    ApiStrings.Commands.unsealWithSymmetricKey, ApiStrings.Commands.unsealWithUnsealingKey -> {
                        getString(R.string.key_created)
                    }
                    else -> getString(R.string.message_to_sign) // ApiStrings.Commands.generateSignature
                }

                viewModel.dataCreated.value = urlCommand.createdDataOrPlainText ?: ""
            }
        }
    }

    private fun setDiceKey(diceKey: DiceKey<Face>){
        diceKeyRepository.set(diceKey)
        viewModel.diceKey.value = diceKey

        binding.centerLetterAndDigitSwitch.text = getString(R.string.reveal_that_the_center_die, diceKey.centerFace().toHumanReadableForm(false), hostOrAppName)

        executeCommand()
    }

    private fun loadDiceKeyAsync(): Deferred<SimpleDiceKey> = (
                    CompletableDeferred<SimpleDiceKey>().also { completableDeferred ->
                        diceKeyRepository.getActiveDiceKey()?.also {
                            completableDeferred.complete(it)
                        } ?: run {
                            completableDeferred.completeExceptionally(Throwable("DiceKey not found in Repository"))
                        }
                    }
            )

    /**
     * Ask the user for consent to unseal a message if the UnsealingInstructions
     * encountered on an `unseal` operation require the user's consent
     */
    private fun requestUsersConsentAsync(
            requestForUsersConsent: UnsealingInstructions.RequestForUsersConsent
    ): Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse> =
            CompletableDeferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>().also {
                deferredDialogResult -> requireActivity().runOnUiThread {
                    MaterialAlertDialogBuilder(requireContext())
                            .setMessage(requestForUsersConsent.question)
                            .setCancelable(false)
                            .setPositiveButton(requestForUsersConsent.actionButtonLabels.allow) { _, _ ->
                                deferredDialogResult.complete(UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow)
                            }
                            .setNegativeButton(requestForUsersConsent.actionButtonLabels.deny) { _, _ ->
                                deferredDialogResult.complete(UnsealingInstructions.RequestForUsersConsent.UsersResponse.Deny)
                            }
                            .show()
                }
            }
}

