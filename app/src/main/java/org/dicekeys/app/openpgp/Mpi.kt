package org.dicekeys.app.openpgp

import com.google.common.io.ByteStreams
import java.math.BigInteger

//  Unencrypted Multiprecision Integers
class Mpi(val value: BigInteger) {
    val size : UShort by lazy {
        value.bitLength().toUShort()
    }

    fun toByteArray(): ByteArray{
        val body = ByteStreams.newDataOutput()
        body.writeShort(size.toInt())
        if(size > 0u) {
            value.toByteArray()
            body.write(value.toByteArray().let {
                // TODO add comment
                // remove last byte if is solely a sign byte
                if(it[0] == 0.toByte()){
                    return@let it.takeLast(it.size - 1).toByteArray()
                }
                it
            })
        }
        return body.toByteArray()
    }

    companion object{
        fun fromHex(hex: String): Mpi {
            return Mpi(BigInteger(hex, 16))
        }

        fun fromByteArray(byteArray: ByteArray): Mpi {
            return Mpi(BigInteger(1, byteArray))
        }
    }
}

//    Multiprecision Integers
//
//    Multiprecision integers (also called MPIs) are unsigned integers used
//    to hold large integers such as the ones used in cryptographic
//    calculations.
//
//    An MPI consists of two pieces: a two-octet scalar that is the length
//    of the MPI in bits followed by a string of octets that contain the
//    actual integer.
//
//    These octets form a big-endian number; a big-endian number can be
//    made into an MPI by prefixing it with the appropriate length.
//
//    Examples:
//
//    (all numbers are in hexadecimal)
//
//    The string of octets [00 01 01] forms an MPI with the value 1.  The
//    string [00 09 01 FF] forms an MPI with the value of 511.
//
//    Additional rules:
//
//    The size of an MPI is ((MPI.length + 7) / 8) + 2 octets.
//
//    The length field of an MPI describes the length starting from its
//    most significant non-zero bit.  Thus, the MPI [00 02 01] is not
//    formed correctly.  It should be [00 01 01].
//
//    Unused bits of an MPI MUST be zero.
//
//    Also note that when an MPI is encrypted, the length refers to the
//    plaintext MPI.  It may be ill-formed in its ciphertext.