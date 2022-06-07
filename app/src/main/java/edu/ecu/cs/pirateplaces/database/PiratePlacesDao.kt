package edu.ecu.cs.pirateplaces.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import edu.ecu.cs.pirateplaces.PiratePlace
import java.util.*

@Dao
interface PiratePlacesDao {

    @Query("SELECT * FROM PiratePlace")
    fun getPiratePlaces() : LiveData<List<PiratePlace>>

    @Query("SELECT * FROM PiratePlace WHERE id=(:id)")
    fun getPiratePlace(id: UUID) : LiveData<PiratePlace?>

    @Update
    fun updatePiratePlace(place: PiratePlace)

    @Insert
    fun addPiratePlace(place: PiratePlace)
}