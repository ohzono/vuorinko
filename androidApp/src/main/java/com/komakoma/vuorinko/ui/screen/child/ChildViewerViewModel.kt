package com.komakoma.vuorinko.ui.screen.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komakoma.vuorinko.domain.model.PhotoRef
import com.komakoma.vuorinko.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ChildViewerViewModel(
    albumId: String,
    photoRepository: PhotoRepository,
) : ViewModel() {

    val photos: StateFlow<List<PhotoRef>> = photoRepository.observePhotos(albumId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
