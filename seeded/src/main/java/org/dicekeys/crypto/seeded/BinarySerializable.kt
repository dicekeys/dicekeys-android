package org.dicekeys.crypto.seeded

interface BinarySerializable {
  fun toSerializedBinaryForm(): ByteArray
}