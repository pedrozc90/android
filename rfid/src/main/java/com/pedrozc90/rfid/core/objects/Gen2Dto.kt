package com.pedrozc90.rfid.core.objects

import com.rscja.deviceapi.entity.Gen2Entity

/**
 * DTO representing the Gen2 configuration we expose to the app.
 * Add fields as necessary — fields mirror what the Java plugin sent.
 */
data class Gen2Dto(
    val selectTarget: Int,
    val selectAction: Int,
    val selectTruncate: Int,
    val queryTarget: Int,
    val startQ: Int,
    val minQ: Int,
    val maxQ: Int,
    val queryDR: Int,
    val queryM: Int,
    val queryTRext: Int,
    val querySel: Int,
    val querySession: Int,
    val q: Int,
    val linkFrequency: Int
) {
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

    }

}
