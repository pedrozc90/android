package com.pedrozc90.rfid.chainway.core

import com.pedrozc90.rfid.objects.DeviceParams
import com.rscja.deviceapi.entity.Gen2Entity

fun Gen2Entity.toDeviceParams(): DeviceParams {
    return DeviceParams(
        selectTarget = selectTarget,
        selectAction = selectAction,
        selectTruncate = selectTruncate,
        queryTarget = queryTarget,
        startQ = startQ,
        minQ = minQ,
        maxQ = maxQ,
        queryDR = queryDR,
        queryM = queryM,
        queryTRext = queryTRext,
        querySel = querySel,
        querySession = querySession,
        q = q,
        linkFrequency = linkFrequency
    )
}

fun DeviceParams.toGen2Entity(): Gen2Entity {
    val obj = Gen2Entity()
    selectTarget?.let { obj.selectTarget = it }
    selectAction?.let { obj.selectAction = it }
    selectTruncate?.let { obj.selectTruncate = it }
    queryTarget?.let { obj.queryTarget = it }
    startQ?.let { obj.startQ = it }
    minQ?.let { obj.minQ = it }
    maxQ?.let { obj.maxQ = it }
    queryDR?.let { obj.queryDR = it }
    queryM?.let { obj.queryM = it }
    queryTRext?.let { obj.queryTRext = it }
    querySel?.let { obj.querySel = it }
    querySession?.let { obj.querySession = it }
    q?.let { obj.q = it }
    linkFrequency?.let { obj.linkFrequency = it }

    require(obj.selectTarget in 0..3) { "selectTarget must be 0..3 (s0..s3)" }
    require(obj.selectAction in 0..6) { "selectAction must be 0..6" }
    require(obj.selectTruncate in 0..1) { "selectTruncate must be 0..1" }
    require(obj.queryTarget in 0..Int.MAX_VALUE) { "queryTarget must be >= 0" } // adjust if a bounded range exists
    require(obj.startQ in 0..15) { "startQ must be 0..15" }
    require(obj.minQ in 0..15) { "minQ must be 0..15" }
    require(obj.maxQ in 0..15) { "maxQ must be 0..15" }
    require(obj.queryDR in 0..1) { "queryDR (dr) must be 0..1" }
    require(obj.queryM in 0..3) { "queryM (coding) must be 0..3" }
    require(obj.queryTRext in 0..1) { "queryTRext (p) must be 0..1" }
    require(obj.querySel in 0..3) { "querySel (Sel) must be 0..3" }
    require(obj.querySession in 0..3) { "querySession (Session) must be 0..3" }
    require(obj.q in 0..1) { "q (g) must be 0..1" }
    require(obj.linkFrequency in 0..7) { "linkFrequency must be 0..7" }
    // Additional semantic checks (e.g. minQ <= maxQ) can also be added:
    require(obj.minQ <= obj.maxQ) { "minQ must be greater or equal to maxQ" }

    val valid = obj.checkParameter()
    require(valid) { "Gen2Entity is not valid." }

    return obj
}
