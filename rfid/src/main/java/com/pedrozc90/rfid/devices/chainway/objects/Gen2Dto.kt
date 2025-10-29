package com.pedrozc90.rfid.devices.chainway.objects

import com.rscja.deviceapi.entity.Gen2Entity

/**
 * DTO representing the Gen2 configuration we expose to the app.
 * Add fields as necessary — fields mirror what the Java plugin sent.
 */
data class Gen2Dto(
    val selectTarget: Int = 0,
    val selectAction: Int = 0,
    val selectTruncate: Int = 0,
    val queryTarget: Int = 0,
    val startQ: Int = 0,
    val minQ: Int = 0,
    val maxQ: Int = 0,
    val queryDR: Int = 0,
    val queryM: Int = 0,
    val queryTRext: Int = 0,
    val querySel: Int = 0,
    val querySession: Int = 0,
    val q: Int = 0,
    val linkFrequency: Int = 0
) {
    init {
        require(selectTarget in 0..3) { "selectTarget must be 0..3 (s0..s3)" }
        require(selectAction in 0..6) { "selectAction must be 0..6" }
        require(selectTruncate in 0..1) { "selectTruncate must be 0..1" }
        require(queryTarget in 0..Int.MAX_VALUE) { "queryTarget must be >= 0" } // adjust if a bounded range exists
        require(startQ in 0..15) { "startQ must be 0..15" }
        require(minQ in 0..15) { "minQ must be 0..15" }
        require(maxQ in 0..15) { "maxQ must be 0..15" }
        require(queryDR in 0..1) { "queryDR (dr) must be 0..1" }
        require(queryM in 0..3) { "queryM (coding) must be 0..3" }
        require(queryTRext in 0..1) { "queryTRext (p) must be 0..1" }
        require(querySel in 0..3) { "querySel (Sel) must be 0..3" }
        require(querySession in 0..3) { "querySession (Session) must be 0..3" }
        require(q in 0..1) { "q (g) must be 0..1" }
        require(linkFrequency in 0..7) { "linkFrequency must be 0..7" }
        // Additional semantic checks (e.g. minQ <= maxQ) can also be added:
        require(minQ <= maxQ) { "minQ must be greater or equal to maxQ" }
    }

    fun toMap(): Map<String, Any> = mapOf(
        "target" to selectTarget,
        "action" to selectAction,
        "t" to selectTruncate,
        "query" to queryTarget,
        "startQ" to startQ,
        "minQ" to minQ,
        "maxQ" to maxQ,
        "dr" to queryDR,
        "coding" to queryM,
        "p" to queryTRext,
        "sel" to querySel,
        "session" to querySession,
        "g" to q,
        "linkFrequency" to linkFrequency
    )

    fun toEntity(): Gen2Entity {
        val obj = Gen2Entity()
        obj.selectTarget = selectTarget
        obj.selectAction = selectAction
        obj.selectTruncate = selectTruncate
        obj.queryTarget = queryTarget
        obj.startQ = startQ
        obj.minQ = minQ
        obj.maxQ = maxQ
        obj.queryDR = queryDR
        obj.queryM = queryM
        obj.queryTRext = queryTRext
        obj.querySel = querySel
        obj.querySession = querySession
        obj.q = q
        obj.linkFrequency = linkFrequency
        return obj
    }

    companion object {

        private fun Int.clamp(min: Int, max: Int): Int = coerceIn(min, max)

        fun toDto(obj: Gen2Entity): Gen2Dto {
            return Gen2Dto(
                selectTarget = obj.selectTarget,
                selectAction = obj.selectAction,
                selectTruncate = obj.selectTruncate,
                queryTarget = obj.queryTarget,
                startQ = obj.startQ,
                minQ = obj.minQ,
                maxQ = obj.maxQ,
                queryDR = obj.queryDR,
                queryM = obj.queryM,
                queryTRext = obj.queryTRext,
                querySel = obj.querySel,
                querySession = obj.querySession,
                q = obj.q,
                linkFrequency = obj.linkFrequency
            )
        }

        fun create(
            selectTarget: Int = -1,
            selectAction: Int = -1,
            selectTruncate: Int = -1,
            queryTarget: Int = -1,
            startQ: Int = -1,
            minQ: Int = -1,
            maxQ: Int = -1,
            queryDR: Int = -1,
            queryM: Int = -1,
            queryTRext: Int = -1,
            querySel: Int = -1,
            querySession: Int = -1,
            q: Int = -1,
            linkFrequency: Int = -1
        ): Gen2Dto {
            val sTarget = selectTarget.clamp(0, 3)
            val sAction = selectAction.clamp(0, 6)
            val sTrunc = selectTruncate.clamp(0, 1)
            val sStartQ = startQ.clamp(0, 15)
            val sMinQ = minQ.clamp(0, 15)
            val sMaxQ = maxQ.clamp(0, 15)
            val sDR = queryDR.clamp(0, 1)
            val sM = queryM.clamp(0, 3)
            val sTRext = queryTRext.clamp(0, 1)
            val sSel = querySel.clamp(0, 3)
            val sSession = querySession.clamp(0, 3)
            val sg = q.clamp(0, 1)
            val sLinkFreq = linkFrequency.clamp(0, 7)
            val sQueryTarget = if (queryTarget < 0) 0 else queryTarget

            // Ensure minQ <= maxQ after clamping
            val (finalMinQ, finalMaxQ) = if (sMinQ <= sMaxQ) sMinQ to sMaxQ else sMaxQ to sMinQ

            return Gen2Dto(
                selectTarget = sTarget,
                selectAction = sAction,
                selectTruncate = sTrunc,
                queryTarget = sQueryTarget,
                startQ = sStartQ,
                minQ = finalMinQ,
                maxQ = finalMaxQ,
                queryDR = sDR,
                queryM = sM,
                queryTRext = sTRext,
                querySel = sSel,
                querySession = sSession,
                q = sg,
                linkFrequency = sLinkFreq
            )
        }

    }

}
