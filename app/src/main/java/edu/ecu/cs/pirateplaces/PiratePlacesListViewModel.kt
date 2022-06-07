package edu.ecu.cs.pirateplaces

import androidx.lifecycle.ViewModel

class PiratePlacesListViewModel: ViewModel() {
    private val piratePlacesRepository = PiratePlacesRepository.get()
    val piratePlacesListLiveData = piratePlacesRepository.getPiratePlaces()

    fun addPiratePlace(place: PiratePlace) {
        piratePlacesRepository.addPiratePlace(place)
    }
}