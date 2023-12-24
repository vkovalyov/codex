package com.wonderers.codex.android.ui.place.list

import androidx.lifecycle.ViewModel
import com.wonderers.codex.android.common.util.hideIn
import com.wonderers.codex.android.common.util.launch
import com.wonderers.codex.android.data.place.model.Place
import com.wonderers.codex.android.data.place.repository.mock.MockPlaceRepository
import com.wonderers.codex.android.data.region.repository.mock.MockRegionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlacesListVm : ViewModel() {

    private val regionRepository = MockRegionRepository()
    private val placeRepository = MockPlaceRepository()

    private val _uiState = MutableStateFlow<PlacesListUiState>(PlacesListUiState.Loading)
    val uiState = _uiState.hideIn(this)

    init {
        observePlaces()
    }

    fun retry() {
        _uiState.update {
            if (it is PlacesListUiState.Error) {
                it.copy(isRecovering = true)
            } else {
                PlacesListUiState.Loading
            }
        }
        observePlaces()
    }

    private fun observePlaces() = launch {
        val region = regionRepository.getRegionFromCache() ?: run {
            _uiState.update {
                PlacesListUiState.Error(
                    errorText = "Region is not selected",
                    isRecovering = false
                )
            }
            return@launch
        }
        val places = placeRepository.getPlacesByRegion(region.id)
        _uiState.update {
            if (places.isEmpty()) {
                PlacesListUiState.Error(
                    errorText = "Region \"${region.name}\" is not supported yet",
                    isRecovering = false
                )
            } else {
                PlacesListUiState.Loaded(
                    places = convertToUiModel(places)
                )
            }
        }
    }

    private fun convertToUiModel(places: List<Place>) = places.map { place ->
        with(place) {
            PlaceItemUiModel(
                id = id,
                title = title,
                typeName = type.name,
                distance = "1000m", // todo support location tracking
                previewImage = previewImage
            )
        }
    }
}