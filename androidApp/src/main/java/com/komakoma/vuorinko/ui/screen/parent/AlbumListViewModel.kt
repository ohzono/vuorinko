package com.komakoma.vuorinko.ui.screen.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komakoma.vuorinko.domain.model.AlbumInfo
import com.komakoma.vuorinko.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlbumListViewModel(
    private val albumRepository: AlbumRepository,
) : ViewModel() {

    val albums: StateFlow<List<AlbumInfo>> = albumRepository.observeAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun createAlbum(name: String) {
        viewModelScope.launch {
            try {
                albumRepository.createAlbum(name)
            } catch (e: IllegalStateException) {
                _error.value = e.message
            }
        }
    }

    fun deleteAlbum(id: String) {
        viewModelScope.launch {
            albumRepository.deleteAlbum(id)
        }
    }

    fun renameAlbum(id: String, name: String) {
        viewModelScope.launch {
            try {
                albumRepository.renameAlbum(id, name)
            } catch (e: IllegalStateException) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
