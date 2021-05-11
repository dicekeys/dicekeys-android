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
            body.write(value.toByteArray().let {
                // BigInteger representation always have a sign bit. This is not a problem as we only
                // handle positive numbers. The only edge case is when we use eg. all 8 bits of a byte
                // to represent a positive number. BigInteger will have to store (8 number bits + 1 sign bits)
                // 1 byte fits the number representation and it will need to consume one more zero byte in our case
                // to represent the sign positive bit.
                // In that case it's safe to remove the first byte if it's 0
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