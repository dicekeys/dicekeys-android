## DiceKeys for Android

<!-- ### What are DiceKeys? -->

### Overview

The DiceKeys app enables mutually-distrusting applications to derive keys and
other secrets from the user's DiceKey without actually seeing the DiceKey.

Your applications can communicate with the DiceKeys app via the [DiceKeysApiClient].
You can ask the DiceKeys app to derive cryptographic keys seeded by the user's DiceKey,
to perform cryptographic operations using the derived keys,
and to give those keys to your application if it is authorized to receive them.
You specify how keys are derived and who can access them via the
[Key-Derivation Options JSON Format](https://dicekeys.github.io/seeded-crypto/key_derivation_options_format.html/),
which you can construct and parse using the [ApiKeyDerivationOptions] class.

The API builds on the the cross-platform
[Seeded Cryptography C++ Library](https://dicekeys.github.io/seeded-crypto/).
That library implements seeded
symmetric keys ([SymmetricKey]);
assymetric key pairs for public-key encryption ([PublicKey]) and decryption ([PrivateKey]);
assymetric key pairs for digital signatures ([SigningKey]) and their verification [SignatureVerificationKey]),
as well as a general-purpose derived [Secret].
When messages are sealed with the _seal_ operation of [SymmetricKey] or [PublicKey],
the ciphertext is stored within a [PackagedSealedMessage].

The FIDO writer application, [com.dicekeys.fidowriter], is a real-world
use case which is also small enough to be a good sample application.
It seeds FIDO security keys with a secret it has the DiceKeys app derive
from the user's DiceKey.
If the FIDO security key is lost, the user can use this app to re-derive the
secret from their DiceKey, and turn a replacement key into a perfect replica
of the security key they lost.
(Only a limited set of FIDO security keys support this write operation.)

<!-- #### Packages primarily intended for internal use by the DiceKeys App
The DiceKeys app itself uses the [org.dicekeys.read] package to scan in a DiceKey via the
Android devices camera, representing the result in a format represented by [org.dicekeys.keysqr].
They are included here for transparency. -->