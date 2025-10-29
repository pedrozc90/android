package com.pedrozc90.rfid.utils

/**
 * Simple EPC SGTIN-96 encoder/decoder in Kotlin.
 *
 * Changes:
 * - encode signature changed to: encode(filter: Int, companyPrefix: String, itemReference: String, serialNumber: Long)
 *   This is clearer (filter first, then companyPrefix, itemReference, serialNumber) as you requested.
 * - Epc data class:
 *     - renamed epcHex -> rfid
 *     - added schema = "sgtin"
 *     - added uri (tag URN) and urn (id URN) fields
 *
 * A deprecated compatibility overload is provided for the old parameter order.
 */

import java.math.BigInteger

data class Epc(
    val rfid: String,           // hex representation (24 hex chars, uppercase)
    val header: Int,
    val filter: Int,
    val partition: Int,
    val companyPrefix: String,  // zero-padded to partition.companyPrefixDigits
    val itemReference: String,  // zero-padded to (13 - companyPrefixDigits)
    val serialNumber: Long,
    val gtin14: String,
    val schema: String,         // "sgtin"
    val uri: String,            // urn:epc:tag:sgtin-96:<filter>.<companyPrefix>.<itemRef>.<serial>
    val urn: String             // urn:epc:id:sgtin:<companyPrefix>.<itemRef>.<serial>
)

object EpcUtils {
    // SGTIN-96 standard header
    private const val SGTIN96_HEADER = 0x30 // 0011 0000 = 48 decimal

    // Partition table as per EPC Tag Data Standard
    private val PARTITIONS = listOf(
        Partition(0, companyPrefixBits = 40, itemReferenceBits = 4, companyPrefixDigits = 12),
        Partition(1, companyPrefixBits = 37, itemReferenceBits = 7, companyPrefixDigits = 11),
        Partition(2, companyPrefixBits = 34, itemReferenceBits = 10, companyPrefixDigits = 10),
        Partition(3, companyPrefixBits = 30, itemReferenceBits = 14, companyPrefixDigits = 9),
        Partition(4, companyPrefixBits = 27, itemReferenceBits = 17, companyPrefixDigits = 8),
        Partition(5, companyPrefixBits = 24, itemReferenceBits = 20, companyPrefixDigits = 7),
        Partition(6, companyPrefixBits = 20, itemReferenceBits = 24, companyPrefixDigits = 6)
    )

    private data class Partition(
        val partitionValue: Int,
        val companyPrefixBits: Int,
        val itemReferenceBits: Int,
        val companyPrefixDigits: Int
    )

    /**
     * Encode an SGTIN-96 EPC.
     *
     * New signature: encode(filter, companyPrefix, itemReference, serialNumber)
     *
     * @param filter filter value (0..7)
     * @param companyPrefix GS1 company prefix digits as string (6..12 digits)
     * @param itemReference item reference digits INCLUDING the indicator digit so that:
     *                      companyPrefixDigits + itemReferenceDigits == 13
     * @param serialNumber serial number (fits in 38 bits)
     *
     * @return 24-character hex string (uppercase) representing the SGTIN-96 EPC.
     *
     * Throws IllegalArgumentException on invalid input.
     */
    fun encode(filter: Int, companyPrefix: String, itemReference: String, serialNumber: Long): String {
        val cp = companyPrefix.trim()
        require(cp.all { it.isDigit() }) { "companyPrefix must be numeric digits" }
        val cpLen = cp.length
        require(cpLen in 6..12) { "company prefix must be between 6 and 12 digits (got $cpLen)" }

        val partition = PARTITIONS.find { it.companyPrefixDigits == cpLen }
            ?: throw IllegalArgumentException("no partition matches company prefix length $cpLen")

        val itemRefDigitsExpected = 13 - cpLen
        val irTrim = itemReference.trim()
        require(irTrim.all { it.isDigit() }) { "itemReference must be numeric digits" }
        val ir = irTrim.padStart(itemRefDigitsExpected, '0')
        require(ir.length == itemRefDigitsExpected) {
            "itemReference must have ${itemRefDigitsExpected} digits (after padding). got '${itemReference}'"
        }

        require(filter in 0..7) { "filter must be 0..7" }
        require(serialNumber >= 0L && serialNumber < (1L shl 38)) {
            "serialNumber must fit in 38 bits (0 .. ${((1L shl 38) - 1)})"
        }

        // Convert numeric strings to BigInteger values
        val cpValue = BigInteger(cp)
        val irValue = BigInteger(ir)
        val serialValue = BigInteger.valueOf(serialNumber)

        // Compose the EPC bits into a BigInteger
        var epc = BigInteger.ZERO

        // header (8 bits)
        epc = epc.shiftLeft(8).or(BigInteger.valueOf(SGTIN96_HEADER.toLong()))

        // filter (3 bits)
        epc = epc.shiftLeft(3).or(BigInteger.valueOf(filter.toLong()))

        // partition (3 bits)
        epc = epc.shiftLeft(3).or(BigInteger.valueOf(partition.partitionValue.toLong()))

        // company prefix (companyPrefixBits)
        epc = epc.shiftLeft(partition.companyPrefixBits).or(cpValue)

        // item reference (itemReferenceBits)
        epc = epc.shiftLeft(partition.itemReferenceBits).or(irValue)

        // serial number (38 bits)
        epc = epc.shiftLeft(38).or(serialValue)

        // Ensure exactly 96 bits; convert to hex padded to 24 hex chars
        val hex = epc.toString(16).uppercase().padStart(24, '0')
        return hex
    }

//    /**
//     * Deprecated compatibility overload with the old parameter order:
//     * encode(commissioning, filter, itemReference, serialNumber)
//     *
//     * Maps to encode(filter, companyPrefix, itemReference, serialNumber) expecting filter as numeric string.
//     */
//    @Deprecated(
//        message = "Use encode(filter: Int, companyPrefix: String, itemReference: String, serialNumber: Long) with filter first",
//        replaceWith = ReplaceWith("encode(filter.toInt(), commissioning, itemReference, serialNumber)")
//    )
//    fun encode(commissioning: String, filter: String, itemReference: String, serialNumber: Long): String {
//        val filterInt = filter.toIntOrNull() ?: throw IllegalArgumentException("filter must be an integer string")
//        return encode(filterInt, commissioning, itemReference, serialNumber)
//    }

    /**
     * Decode a SGTIN-96 EPC hex string.
     *
     * Supports uppercase/lowercase hex and with/without 0x prefix.
     */
    fun decode(value: String): Epc {
        var hex = value.trim()
        if (hex.startsWith("0x", ignoreCase = true)) hex = hex.substring(2)
        require(hex.length == 24) { "EPC hex must be 24 hex characters representing 96 bits (got length ${hex.length})" }

        val epc = BigInteger(hex, 16)

        // We'll extract by shifting and masking.
        var cursor = 96 // bits remaining in epc representation

        fun take(n: Int): BigInteger {
            cursor -= n
            val value = epc.shiftRight(cursor).and(BigInteger.ONE.shiftLeft(n).minus(BigInteger.ONE))
            return value
        }

        val header = take(8).toInt()
        val filter = take(3).toInt()
        val partitionValue = take(3).toInt()

        val partition = PARTITIONS.find { it.partitionValue == partitionValue }
            ?: throw IllegalArgumentException("unknown partition value $partitionValue in EPC")

        val cpValue = take(partition.companyPrefixBits)
        val irValue = take(partition.itemReferenceBits)
        val serialValue = take(38)

        // Convert to decimal strings with zero padding according to digits requirements
        val companyPrefixStr = cpValue.toString().padStart(partition.companyPrefixDigits, '0')
        val itemRefDigits = 13 - partition.companyPrefixDigits
        val itemRefStr = irValue.toString().padStart(itemRefDigits, '0')
        val serialLong = serialValue.toLong()

        // Build GTIN-14 (13-digit body + check digit)
        val body13 = companyPrefixStr + itemRefStr
        require(body13.length == 13) { "reconstructed GTIN body length != 13 (${body13.length})" }
        val checkDigit = computeGtinCheckDigit(body13)
        val gtin14 = body13 + checkDigit.toString()

        // Build URNs:
        // tag URN (sgtin-96) includes filter
        val uri = "urn:epc:tag:sgtin-96:$filter.$companyPrefixStr.$itemRefStr.$serialLong"
        // id URN (sgtin id) does not include filter/sgtin-96 suffix
        val urn = "urn:epc:id:sgtin:$companyPrefixStr.$itemRefStr.$serialLong"

        return Epc(
            rfid = hex.uppercase(),
            header = header,
            filter = filter,
            partition = partitionValue,
            companyPrefix = companyPrefixStr,
            itemReference = itemRefStr,
            serialNumber = serialLong,
            gtin14 = gtin14,
            schema = "sgtin",
            uri = uri,
            urn = urn
        )
    }

    /**
     * Compute GTIN check digit for a 13-digit string (returns 0..9).
     *
     * Standard GS1 algorithm.
     */
    private fun computeGtinCheckDigit(body13: String): Int {
        require(body13.length == 13 && body13.all { it.isDigit() }) { "GTIN body must be 13 numeric digits" }
        var sum = 0
        for (i in 0 until 13) {
            val digit = body13[12 - i].digitToInt()
            val weight = if (i % 2 == 0) 3 else 1 // rightmost position is position 1 with weight 3
            sum += digit * weight
        }
        val mod = sum % 10
        return if (mod == 0) 0 else (10 - mod)
    }
}
