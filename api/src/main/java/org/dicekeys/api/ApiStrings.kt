package org.dicekeys.api

class ApiStrings {

  object AndroidIntent {
    const val packageName = "org.dicekeys.trustedapp"
    const val className = "org.dicekeys.trustedapp.activities.ExecuteApiCommandActivity"
  }

  object Commands {
    const val generateSignature = "generateSignature"
    const val getPassword = "getPassword"
    const val getSealingKey = "getSealingKey"
    const val getSecret = "getSecret"
    const val getSigningKey = "getSigningKey"
    const val getSignatureVerificationKey = "getSignatureVerificationKey"
    const val getSymmetricKey = "getSymmetricKey"
    const val getUnsealingKey = "getUnsealingKey"
    const val sealWithSymmetricKey = "sealWithSymmetricKey"
    const val unsealWithSymmetricKey = "unsealWithSymmetricKey"
    const val unsealWithUnsealingKey = "unsealWithUnsealingKey"
  }

  object MetaCommands {
    const val getAuthToken = "getAuthToken"
  }

  object Inputs {
    object generateSignature {
      const val derivationOptionsJson = "derivationOptionsJson"
      const val message = "message"
    }
    object getPassword {
      const val derivationOptionsJson = "derivationOptionsJson"
      const val derivationOptionsJsonMayBeModified = "derivationOptionsJsonMayBeModified"
    }
    object getSealingKey {
      const val derivationOptionsJson = "derivationOptionsJson"
      const val derivationOptionsJsonMayBeModified = "derivationOptionsJsonMayBeModified"
    }
    object getSecret {
      const val derivationOptionsJson = "derivationOptionsJson"
      const val derivationOptionsJsonMayBeModified = "derivationOptionsJsonMayBeModified"
    }
    object getSigningKey {
      const val derivationOptionsJson = "derivationOptionsJson"
      const val derivationOptionsJsonMayBeModified = "derivationOptionsJsonMayBeModified"
    }
    object getSignatureVerificationKey {
      const val derivationOptionsJson = "derivationOptionsJson"
      const val derivationOptionsJsonMayBeModified = "derivationOptionsJsonMayBeModified"
    }
    object getSymmetricKey {
      const val derivationOptionsJson = "derivationOptionsJson"
      const val derivationOptionsJsonMayBeModified = "derivationOptionsJsonMayBeModified"
    }
    object getUnsealingKey {
      const val derivationOptionsJson = "derivationOptionsJson"
      const val derivationOptionsJsonMayBeModified = "derivationOptionsJsonMayBeModified"
    }
    object sealWithSymmetricKey {
      const val derivationOptionsJson = "derivationOptionsJson"
      const val derivationOptionsJsonMayBeModified = "derivationOptionsJsonMayBeModified"
      const val plaintext = "plaintext"
      const val unsealingInstructions = "unsealingInstructions"
    }
    object unsealWithSymmetricKey {
      const val packagedSealedMessageJson = "packagedSealedMessageJson"
    }
    object unsealWithUnsealingKey {
      const val packagedSealedMessageJson = "packagedSealedMessageJson"
    }
  }

  object MetaInputs {
    const val command = "command"
    const val requestId = "requestId"
  }

  object UrlMetaInputs {
    const val authToken = "authToken"
    const val respondTo = "respondTo"
  }

  object Outputs {
    object generateSignature {
      const val signature = "signature"
      const val signatureVerificationKeyJson = "signatureVerificationKeyJson"
    }
    object getPassword {
      const val passwordJson = "passwordJson"
    }
    object getSealingKey {
      const val sealingKeyJson = "sealingKeyJson"
    }
    object getSecret {
      const val secretJson = "secretJson"
    }
    object getSigningKey {
      const val signingKeyJson = "signingKeyJson"
    }
    object getSignatureVerificationKey {
      const val signatureVerificationKeyJson = "signatureVerificationKeyJson"
    }
    object getSymmetricKey {
      const val symmetricKeyJson = "symmetricKeyJson"
    }
    object getUnsealingKey {
      const val unsealingKeyJson = "unsealingKeyJson"
    }
    object sealWithSymmetricKey {
      const val packagedSealedMessageJson = "packagedSealedMessageJson"
    }
    object unsealWithSymmetricKey {
      const val plaintext = "plaintext"
    }
    object unsealWithUnsealingKey {
      const val plaintext = "plaintext"
    }
  }

  object MetaOutputs {
    const val requestId = "requestId"
  }

  object ExceptionMetaOutputs {
    const val exception = "exception"
    const val message = "message"
    const val stack = "stack"
  }

}
