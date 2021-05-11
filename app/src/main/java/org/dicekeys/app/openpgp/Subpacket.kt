package org.dicekeys.app.openpgp

import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams

class Subpacket(val type: Int, val out: ByteArrayDataOutput) {
    private val temp: ByteArrayDataOutput = ByteStreams.newDataOutput()

    init {
        writeByte(type)
    }

    fun writeByte(i: Int) {
        temp.writeByte(i)
    }

    fun writeInt(i: Int) {
        temp.writeInt(i)
    }

    fun write(data: ByteArray) {
        temp.write(data)
    }

    fun write(){
        temp.toByteArray().let {
            // Should follow the spec as described in RFC4880-bis-10 - Section 5.2.3.1.
            // Hardcoded to one byte as 191 length is enough for our use case.
            out.writeByte(it.size)
            out.write(it)
        }
    }
}