package edu.ecu.cs.pirateplaces

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import edu.ecu.cs.pirateplaces.database.PiratePlacesDatabase
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "places-database"

class PiratePlacesRepository private constructor(context: Context) {

    private val database : PiratePlacesDatabase = Room.databaseBuilder(
        context.applicationContext,
        PiratePlacesDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val piratePlacesDao = database.piratePlacesDao()

    private val executor = Executors.newSingleThreadExecutor()

    fun getPiratePlaces() : LiveData<List<PiratePlace>> = piratePlacesDao.getPiratePlaces()

    fun getPiratePlace(id: UUID) : LiveData<PiratePlace?> = piratePlacesDao.getPiratePlace(id)

    fun updatePiratePlace(place: PiratePlace) {
        executor.execute {
            piratePlacesDao.updatePiratePlace(place)
        }
    }

    fun addPiratePlace(place: PiratePlace) {
        executor.execute {
            piratePlacesDao.addPiratePlace(place)
        }
    }

    companion object {
        private var INSTANCE: PiratePlacesRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = PiratePlacesRepository(context)
            }
        }

        fun get() : PiratePlacesRepository {
            return INSTANCE ?:
                    throw IllegalStateException("PiratePlacesRepository must be initialized")
        }
    }
}