package org.dicekeys.api

class ApiStrings {
  val requestId = "requestId"

  object AndroidIntent {
    const val packageName = "org.dicekeys.trustedapp"
    const val className = "org.dicekeys.trustedapp.activities.ExecuteApiCommandActivity"
  }

  class Inputs {

    open class withDerivationOptions {
      val derivationOptionsJson = "derivationOptionsJson"
    }

    open class getObject : withDerivationOptions() {}


    open class unseal {
      val packagedSealedMessage = "packagedSealedMessage"
    }

    open class generateSignature: withDerivationOptions() {
      val message = "message"
    }

    object getSealingKey : getObject() {}
    object getSecret : getObject() {}
    object getSignatureVerificationKey : getObject() {}
    object getSigningKey : getObject() {}
    object getSymmetricKey : getObject() {}
    object getUnsealingKey : getObject() {}

    object sealWithSymmetricKey : withDerivationOptions() {
      val plaintext = "plaintext"
      val unsealingInstructions = "unsealingInstructions"
    }

    object unsealWithSymmetricKey: unseal() {}
    object unsealWithUnsealingKey: unseal() {}

  }
  class Outputs {
    val exception = "exception"

    object generateSignature {
      const val signature = "signature"
      const val signatureVerificationKey = "signatureVerificationKey"
    }

    object getSealingKey {
      const val sealingKey = "sealingKey"
    }

    object getSecret {
      const val secret = "secret"
    }

    object getSignatureVerificationKey {
      const val signatureVerificationKey = "signatureVerificationKey"
    }

    object getSigningKey {
      const val signingKey = "signingKey"
    }

    object getSymmetricKey {
      const val symmetricKey = "symmetricKey"
    }

    object getUnsealingKey {
      const val unsealingKey = "symmetricKey"
    }

    object sealWithSymmetricKey {
      const val packagedSealedMessage = "packagedSealedMessage"
    }

    object unsealWithSymmetricKey {
      const val plaintext = "plaintext"
    }

    object unsealWithUnsealingKey {
      const val plaintext = "plaintext"
    }
  }
}
