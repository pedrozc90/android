package com.example.grid.data

import com.example.grid.R
import com.example.grid.models.Topic

class DataSource {

    fun getTopics(): List<Topic> {
        return listOf(
            Topic(R.string.architecture, R.drawable.architecture, 58),
            Topic(R.string.automotive, R.drawable.automotive, 120),
            Topic(R.string.biology, R.drawable.biology, 67),
            Topic(R.string.business, R.drawable.business, 78),
            Topic(R.string.crafts, R.drawable.crafts, 121),
            Topic(R.string.culinary, R.drawable.culinary, 118),
            Topic(R.string.design, R.drawable.design, 423),
            Topic(R.string.drawing, R.drawable.drawing, 326),
            Topic(R.string.ecology, R.drawable.ecology, 62),
            Topic(R.string.engineering, R.drawable.engineering, 28),
            Topic(R.string.fashion, R.drawable.fashion, 92),
            Topic(R.string.film, R.drawable.film, 165),
            Topic(R.string.finance, R.drawable.finance, 81),
            Topic(R.string.gaming, R.drawable.gaming, 164),
            Topic(R.string.geology, R.drawable.geology, 71),
            Topic(R.string.history, R.drawable.history, 61),
            Topic(R.string.journalism, R.drawable.journalism, 157),
            Topic(R.string.law, R.drawable.law, 176),
            Topic(R.string.lifestyle, R.drawable.lifestyle, 305),
            Topic(R.string.music, R.drawable.music, 212),
            Topic(R.string.painting, R.drawable.painting, 172),
            Topic(R.string.photography, R.drawable.photography, 321),
            Topic(R.string.physics, R.drawable.physics, 271),
            Topic(R.string.tech, R.drawable.tech, 118)
        )
    }

}