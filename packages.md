# com.dicekeys.fidowriter

This *sample* application seeds FIDO security keys with cryptographic keys derived from the user's DiceKey.

# org.dicekeys.dicekey
This *primarily internal* library is used by the DiceKeys app to represent DiceKeys as a square of
faces, which can not only represent boxes of six-sided dice but also square of
two-sided chips.

# org.dicekeys.read
This *primarily internal* library is used by the DiceKeys app to scan (read) DiceKeys with a camera.

# org.dicekeys.crypto.seeded

This wrapper for the DiceKeys
[Seeded Cryptography C++ Library](https://dicekeys.github.io/seeded-crypto/),
provides cryptographic keys seeded by DiceKeys (or other strings)
and cryptographic operations using those keys.

It includes support for
symmetric keys ([SymmetricKey]);
asymmetric key pairs for public-key encryption ([PublicKey]) and decryption ([PrivateKey]);
asymmetric key pairs for digital signatures ([SigningKey]) and their verification [SignatureVerificationKey]),
as well as a general-purpose derived [Secret].
When messages are sealed with the _seal_ operation of [SymmetricKey] or [PublicKey], the ciphertext
is stored within a [PackagedSealedMessage].

While we built this library and the underlying C++ library for DiceKeys,
we have made them general-purpose libraries for use with any type of seed string.
This library can be used to derive keys from (hopefully strong) passwords or other
secret seeds.

To this end, we have placed features specific to DiceKeys in the [org.dicekeys.api] namespace.
That includes [ApiRecipe], which extends [Recipe] in this package
with fields that would not apply to other seeded cryptography applications (e.g.,
ignoring the orientation of the faces of dice within a DiceKey).
It also includes the format for unsealing_instructions instructions [UnsealingInstructions]
used by DiceKeys, allowing this library to remain format agnostic (as well as
agnostic to whether sealed messages should include post-decryption instructions).

# org.dicekeys.api
Your can use the [DiceKeysIntentApiClient] in this package to ask the DiceKeys app
to derive keys from the user's DiceKey, and to perform cryptographic operations
on your application's behalf.
This package uses and returns keys from the [org.dicekeys.crypto.seeded] package.

You specify how keys are derived, and place restrictions on their use, via the
[Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html/),
which you can construct and parse using [ApiRecipe].

If you are sealing messages with a [SymmetricKey] or [PublicKey], you can
instruct the DiceKeys to enforce additional restrictions before unsealing a
message with a derived key. You can create these
[unsealing instructions](https://dicekeys.github.io/seeded-crypto/unsealing_instructions_format.html) by using the
[UnsealingInstructions] class.
These instructions are not sealed, but are stored in plaintext within a
[PackagedSealedMessage], along with the ciphertext and the key-derivation
options needed to re-derive the unsealing key from the user's DiceKey.

# org.dicekeys.trustedapp
The DiceKeys Application, which uses the device camera to read user's DiceKey
and responds to other applications' requests for access to key _derived_
from that DiceKey.  Since the DiceKey is a master key from which all other
keys are derived,  The API and functionality of this application is kept
small to limit the attack surface. 
