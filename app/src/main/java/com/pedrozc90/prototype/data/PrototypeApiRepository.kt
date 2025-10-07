package com.pedrozc90.prototype.data

import com.pedrozc90.prototype.network.PrototypeApiService

interface PrototypeApiRepository {

    suspend fun get(): List<String>

}

class DefaultPrototypeApiRepository(
    private val prototypeApiService: PrototypeApiService
) : PrototypeApiRepository {

    override suspend fun get(): List<String> = prototypeApiService.get()

}
