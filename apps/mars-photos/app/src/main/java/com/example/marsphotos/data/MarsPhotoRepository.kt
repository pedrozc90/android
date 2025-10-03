package com.example.marsphotos.data

import com.example.marsphotos.network.MarsApiService
import com.example.marsphotos.network.MarsPhoto

interface MarsPhotoRepository {

    suspend fun getMarsPhotos(): List<MarsPhoto>

}

class NetworkMarsPhotosRepository(private val marsApiService: MarsApiService) : MarsPhotoRepository {

    override suspend fun getMarsPhotos(): List<MarsPhoto>  = marsApiService.getPhotos()

}
