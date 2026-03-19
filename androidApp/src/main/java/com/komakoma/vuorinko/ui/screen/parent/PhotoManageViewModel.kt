package com.komakoma.vuorinko.ui.screen.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komakoma.vuorinko.domain.model.PhotoRef
import com.komakoma.vuorinko.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PhotoManageViewModel(
    private val albumId: String,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    val photos: StateFlow<List<PhotoRef>> = photoRepository.observePhotos(albumId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun addPhotos(uris: List<String>) {
        viewModelScope.launch {
            try {
                photoRepository.addPhotos(albumId, uris)
            } catch (e: IllegalStateException) {
                _error.value = e.message
            }
        }
    }

    fun removePhoto(id: String) {
        viewModelScope.launch {
            photoRepository.removePhoto(id)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
