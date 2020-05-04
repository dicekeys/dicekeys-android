package org.dicekeys.api

import org.dicekeys.crypto.seeded.SignatureVerificationKey

interface GenerateSignatureResult {
  val signature: ByteArray
  val signatureVerificationKey: SignatureVerificationKey
}