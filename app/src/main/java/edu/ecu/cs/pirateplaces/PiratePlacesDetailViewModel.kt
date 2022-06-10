package edu.ecu.cs.pirateplaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*

class PiratePlacesDetailViewModel: ViewModel() {

    private val piratePlacesRepository = PiratePlacesRepository.get()
    private val pirateIdLiveData = MutableLiveData<UUID>()

    var piratePlaceLiveData: LiveData<PiratePlace?> =
        Transformations.switchMap(pirateIdLiveData) { piratePlaceId ->
            piratePlacesRepository.getPiratePlace(piratePlaceId)
        }

    fun loadPiratePlace(piratePlaceId: UUID) {
        pirateIdLiveData.value = piratePlaceId
    }

    fun savePiratePlace(place: PiratePlace) {
        piratePlacesRepository.updatePiratePlace(place)
    }

   fun getPhotoFile(place: PiratePlace): File {
        return piratePlacesRepository.getPhotoFile(place)
    }
}