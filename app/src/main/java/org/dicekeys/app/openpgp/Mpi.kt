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