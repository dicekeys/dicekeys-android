package org.dicekeys.app.openpgp

import com.google.common.io.ByteStreams
import java.security.MessageDigest

class UserIdPacket(name: String, email: String) : Packet() {

    override val pTag: Int
        get() = 0xb4

    override val body: ByteArray by lazy {
        val body = ByteStreams.newDataOutput()

        val user = "$name <$email>"
        body.write(user.toByteArray())

        body.toByteArray()
    }

    override fun hash(digest: MessageDigest) {
        val buffer = ByteStreams.newDataOutput()
        buffer.writeByte(pTag)
        buffer.writeInt(this.body.size) // 4-bytes
        buffer.write(body)

        digest.update(buffer.toByteArray())
    }
}