package edu.ecu.cs.pirateplaces

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import edu.ecu.cs.pirateplaces.database.PiratePlacesDatabase
import edu.ecu.cs.pirateplaces.database.migration_1_2
import java.io.File
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "places-database"

class PiratePlacesRepository private constructor(context: Context) {

    private val database : PiratePlacesDatabase = Room.databaseBuilder(
        context.applicationContext,
        PiratePlacesDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2)
        .build()

    private val piratePlacesDao = database.piratePlacesDao()

    private val executor = Executors.newSingleThreadExecutor()

    private val filesDir = context.applicationContext.filesDir

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
   fun getPhotoFile(place: PiratePlace): File = File(filesDir, place.photoFileName)

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