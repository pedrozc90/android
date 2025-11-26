package com.pedrozc90.rfid.objects

data class DeviceParams(
    // Chainway Gen2Entity
    val selectTarget: Int? = null,
    val selectAction: Int? = null,
    val selectTruncate: Int? = null,
    val q: Int? = null,
    val startQ: Int? = null,
    val minQ: Int? = null,
    val maxQ: Int? = null,
    val queryDR: Int? = null,
    val queryM: Int? = null,
    val queryTRext: Int? = null,
    val querySel: Int? = null,
    val querySession: Int? = null,
    val queryTarget: Int? = null,
    val linkFrequency: Int? = null,

    // Urovo RfidParameter
    val comAddr: Byte? = null,
    val ivtType: Int? = null,
    val memory: Int? = null,
    val password: String? = null,
    val wordPtr: Int? = null,
    val length: Int? = null,
    val session: Int? = null,
    val qValue: Int? = null,
    val scanTime: Int? = null,
    val antenna: Int? = null,
    val interval: Int? = null,
    val maskMem: Byte? = null,
    val maskAdr: ByteArray? = null,
    val maskLen: Byte? = null,
    val maskData: ByteArray? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceParams

        if (selectTarget != other.selectTarget) return false
        if (selectAction != other.selectAction) return false
        if (selectTruncate != other.selectTruncate) return false
        if (q != other.q) return false
        if (startQ != other.startQ) return false
        if (minQ != other.minQ) return false
        if (maxQ != other.maxQ) return false
        if (queryDR != other.queryDR) return false
        if (queryM != other.queryM) return false
        if (queryTRext != other.queryTRext) return false
        if (querySel != other.querySel) return false
        if (querySession != other.querySession) return false
        if (queryTarget != other.queryTarget) return false
        if (linkFrequency != other.linkFrequency) return false
        if (comAddr != other.comAddr) return false
        if (ivtType != other.ivtType) return false
        if (memory != other.memory) return false
        if (wordPtr != other.wordPtr) return false
        if (length != other.length) return false
        if (session != other.session) return false
        if (qValue != other.qValue) return false
        if (scanTime != other.scanTime) return false
        if (antenna != other.antenna) return false
        if (interval != other.interval) return false
        if (maskMem != other.maskMem) return false
        if (maskLen != other.maskLen) return false
        if (password != other.password) return false
        if (!maskAdr.contentEquals(other.maskAdr)) return false
        if (!maskData.contentEquals(other.maskData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = selectTarget ?: 0
        result = 31 * result + (selectAction ?: 0)
        result = 31 * result + (selectTruncate ?: 0)
        result = 31 * result + (q ?: 0)
        result = 31 * result + (startQ ?: 0)
        result = 31 * result + (minQ ?: 0)
        result = 31 * result + (maxQ ?: 0)
        result = 31 * result + (queryDR ?: 0)
        result = 31 * result + (queryM ?: 0)
        result = 31 * result + (queryTRext ?: 0)
        result = 31 * result + (querySel ?: 0)
        result = 31 * result + (querySession ?: 0)
        result = 31 * result + (queryTarget ?: 0)
        result = 31 * result + (linkFrequency ?: 0)
        result = 31 * result + (comAddr ?: 0)
        result = 31 * result + (ivtType ?: 0)
        result = 31 * result + (memory ?: 0)
        result = 31 * result + (wordPtr ?: 0)
        result = 31 * result + (length ?: 0)
        result = 31 * result + (session ?: 0)
        result = 31 * result + (qValue ?: 0)
        result = 31 * result + (scanTime ?: 0)
        result = 31 * result + (antenna ?: 0)
        result = 31 * result + (interval ?: 0)
        result = 31 * result + (maskMem ?: 0)
        result = 31 * result + (maskLen ?: 0)
        result = 31 * result + (password?.hashCode() ?: 0)
        result = 31 * result + (maskAdr?.contentHashCode() ?: 0)
        result = 31 * result + (maskData?.contentHashCode() ?: 0)
        return result
    }

}
