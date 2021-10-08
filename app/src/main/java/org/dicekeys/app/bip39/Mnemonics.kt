package org.dicekeys.app.bip39

import java.io.Closeable
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*

/**
 * Encompasses all mnemonic functionality, which helps keep everything concise and in one place.
 */
object Mnemonics {
    private var cachedList = WordList()

    fun getCachedWords(languageCode: String): List<String> {
        if (cachedList.languageCode != languageCode) {
            cachedList = WordList(languageCode)
        }
        return cachedList.words
    }


    //
    // Inner Classes
    //

    class MnemonicCode(val chars: CharArray, val languageCode: String = Locale.ENGLISH.language) :
        Closeable, Iterable<String> {

        constructor(
            entropy: ByteArray,
            languageCode: String = Locale.ENGLISH.language
        ) : this(computeSentence(entropy), languageCode)

        override fun close() = clear()

        val wordCount get() = chars.count { it == ' ' }.let { if (it == 0) it else it + 1 }

        val mnemonic get() = String(chars)

        /**
         * Converts the [chars] array into a list of CharArrays for each word. This library does not
         * use this value but it exposes it as a convenience. In most cases, just working with the
         * chars can be enough.
         */
        val words: List<CharArray>
            get() = ArrayList<CharArray>(wordCount).apply {
                var cursor = 0
                chars.forEachIndexed { i, c ->
                    if (c == ' ' || i == chars.lastIndex) {
                        add(chars.copyOfRange(cursor, if (chars[i].isWhitespace()) i else i + 1))
                        cursor = i + 1
                    }
                }
        }

        fun clear() = chars.fill(0.toChar())

        fun isEmpty() = chars.isEmpty()


        override fun iterator(): Iterator<String> = object : Iterator<String> {
            var cursor: Int = 0
            override fun hasNext() = cursor < chars.size - 1

            override fun next(): String {
                var nextSpaceIndex = nextSpaceIndex()
                val word = String(chars, cursor, nextSpaceIndex - cursor)
                cursor = nextSpaceIndex + 1
                return word
            }

            private fun nextSpaceIndex(): Int {
                var i = cursor
                while (i < chars.size - 1) {
                    if (chars[i].isWhitespace()) return i else i++
                }
                return chars.size
            }
        }

        companion object {

            /**
             * Utility function to create a mnemonic code as a character array from the given
             * entropy. Typically, new mnemonic codes are created starting with a WordCount
             * instance, rather than entropy, to ensure that the entropy has been created correctly.
             * This function is more useful during testing, when you want to validate that known
             * input produces known output.
             *
             * @param entropy the entropy to use for creating the mnemonic code. Typically, this
             * value is created via WordCount.toEntropy.
             * @param languageCode the language code to use. Typically, `en`.
             *
             * @return an array of characters for the mnemonic code that corresponds to the given
             * entropy.
             *
             * @see WordCount.toEntropy
             */
            private fun computeSentence(
                entropy: ByteArray,
                languageCode: String = Locale.ENGLISH.language
            ): CharArray {
                // initialize state
                var index = 0
                var bitsProcessed = 0
                var words = getCachedWords(languageCode)

                // inner function that updates the index and copies a word after every 11 bits
                // Note: the excess bits of the checksum are intentionally ignored, per BIP-39
                fun processBit(bit: Boolean, chars: ArrayList<Char>) {
                    // update the index
                    index = index shl 1
                    if (bit) index = index or 1
                    // if we're at a word boundary
                    if ((++bitsProcessed).rem(11) == 0) {
                        // copy over the word and restart the index
                        words[index].forEach { chars.add(it) }
                        chars.add(' ')
                        index = 0
                    }
                }

                // Compute the first byte of the checksum by SHA256(entropy)
                val checksum = entropy.toSha256()[0]
                return (entropy + checksum).toBits().let { bits ->
                    // initial size of max char count, to minimize array copies (size * 3/32 * 8)
                    ArrayList<Char>(entropy.size * 3/4).also { chars ->
                        bits.forEach { processBit(it, chars) }
                        // trim final space to avoid the need to track the number of words completed
                        chars.removeAt(chars.lastIndex)
                    }.let { result ->
                        // returning the result as a charArray creates a copy so clear the original
                        // so that it doesn't sit in memory until garbage collection
                        result.toCharArray().also { result.clear() }
                    }
                }
            }
        }
    }
}


//
// Private Extensions
//

private fun ByteArray?.toSha256() = MessageDigest.getInstance("SHA-256").digest(this)

private fun ByteArray.toBits(): List<Boolean> = flatMap { it.toBits() }

private fun Byte.toBits(): List<Boolean> = (7 downTo 0).map { (toInt() and (1 shl it)) != 0 }

private fun CharArray.toBytes(): ByteArray {
    val byteBuffer = CharBuffer.wrap(this).let { Charset.forName("UTF-8").encode(it) }
    return byteBuffer.array().copyOfRange(byteBuffer.position(), byteBuffer.limit())
}
