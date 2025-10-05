package com.example.amphibians.data

import com.example.amphibians.model.Amphibian
import com.example.amphibians.network.AmphibianApiService

/**
 * Repository retrieves amphibian data from underlying data source.
 */
interface AmphibiansRepository {

    /**
     * Network Implementation of repository that retrieves amphibian data from underlying data source.
     */
    suspend fun getAmphibians(): List<Amphibian>

}

/**
 * Network Implementation of repository that retrieves amphibian data from underlying data source.
 */
class DefaultAmphibiansRepository(val service: AmphibianApiService) : AmphibiansRepository {

    /**
     * Network Implementation of repository that retrieves amphibian data from underlying data source.
     */
    override suspend fun getAmphibians(): List<Amphibian> = service.getAmphibians()

}
