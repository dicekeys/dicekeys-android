package org.dicekeys.app.data

import org.dicekeys.app.bip39.Mnemonics
import org.dicekeys.app.extensions.toHexString
import org.dicekeys.crypto.seeded.JsonSerializable

enum class DeriveType {
    Password, Secret, SigningKey, SymmetricKey, UnsealingKey
}

sealed class DerivedValueView(val description: String) {
    class JSON : DerivedValueView("JSON")
    class Password : DerivedValueView("Password")
    class Hex : DerivedValueView("HEX")
    class HexSigningKey : DerivedValueView("HEX (Signing Key)")
    class HexUnsealing : DerivedValueView("HEX (Unsealing Key)")
    class HexSealing : DerivedValueView("HEX (Sealing Key)")
    class BIP39 : DerivedValueView("BIP39")
    class OpenPGPPrivateKey : DerivedValueView("OpenPGP Private Key")
    class OpenSSHPrivateKey : DerivedValueView("OpenSSH Private Key")
    class OpenSSHPublicKey : DerivedValueView("OpenSSH Public Key")
}

sealed class DerivedValue(
    private val derivedValue: JsonSerializable,
    val views: List<DerivedValueView> = listOf(DerivedValueView.JSON())
) {

    open fun valueForView(view: DerivedValueView): String {
        return derivedValue.toJson()
    }

    class Password(private val password: org.dicekeys.crypto.seeded.Password) :
        DerivedValue(password, listOf(DerivedValueView.Password(), DerivedValueView.JSON())) {
        override fun valueForView(view: DerivedValueView): String = when (view) {
            is DerivedValueView.Password -> password.password
            else -> super.valueForView(view)
        }

    }

    class Secret(private val secret: org.dicekeys.crypto.seeded.Secret) : DerivedValue(
        secret,
        listOf(DerivedValueView.JSON(), DerivedValueView.Hex(), DerivedValueView.BIP39())
    ) {
        override fun valueForView(view: DerivedValueView): String = when (view) {
            is DerivedValueView.Hex -> secret.secretBytes.toHexString()
            is DerivedValueView.BIP39 -> String(Mnemonics.MnemonicCode(secret.secretBytes).chars)
            else -> super.valueForView(view)
        }
    }

    class SigningKey(private val signingKey: org.dicekeys.crypto.seeded.SigningKey) : DerivedValue(
        signingKey,
        listOf(
            DerivedValueView.JSON(),
            DerivedValueView.OpenPGPPrivateKey(),
            DerivedValueView.OpenSSHPrivateKey(),
            DerivedValueView.OpenSSHPublicKey(),
            DerivedValueView.HexSigningKey()
        )
    ) {
        override fun valueForView(view: DerivedValueView): String = when (view) {
            is DerivedValueView.HexSigningKey -> signingKey.signingKeyBytes.toHexString()
            is DerivedValueView.OpenPGPPrivateKey -> signingKey.openPgpPemFormatSecretKey
            is DerivedValueView.OpenSSHPrivateKey -> signingKey.openSshPemPrivateKey
            is DerivedValueView.OpenSSHPublicKey -> signingKey.openSshPublicKey
            else -> super.valueForView(view)
        }
    }

    class SymmetricKey(private val symmetricKey: org.dicekeys.crypto.seeded.SymmetricKey) :
        DerivedValue(
            symmetricKey, listOf(
                DerivedValueView.JSON(),
                DerivedValueView.Hex()
            )
        ) {

        override fun valueForView(view: DerivedValueView): String = when (view) {
            is DerivedValueView.Hex -> symmetricKey.keyBytes.toHexString()
            else -> super.valueForView(view)
        }
    }

    class UnsealingKey(private val unsealingKey: org.dicekeys.crypto.seeded.UnsealingKey) :
        DerivedValue(
            unsealingKey, listOf(
                DerivedValueView.JSON(),
                DerivedValueView.HexUnsealing(),
                DerivedValueView.HexSealing()
            )
        ) {
        override fun valueForView(view: DerivedValueView): String = when (view) {
            is DerivedValueView.HexUnsealing -> unsealingKey.unsealingKeyBytes.toHexString()
            is DerivedValueView.HexSealing -> unsealingKey.sealingKeyBytes.toHexString()
            else -> super.valueForView(view)
        }
    }

}