package com.akshar.wallpaperengine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akshar.wallpaperengine.data.local.entity.PlaylistEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlaylistsUiState(
    val playlists: List<PlaylistEntity> = emptyList(),
    val selectedPlaylist: PlaylistEntity? = null,
    val playlistWallpapers: List<WallpaperEntity> = emptyList()
)

class PlaylistsViewModel(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)

    val uiState: StateFlow<PlaylistsUiState> = combine(
        playlistRepository.allPlaylists,
        _selectedPlaylist,
        _selectedPlaylist.flatMapLatest { p ->
            if (p != null) playlistRepository.getWallpapersForPlaylist(p.id) else flowOf(emptyList())
        }
    ) { playlists, selected, wallpapers ->
        PlaylistsUiState(
            playlists = playlists,
            selectedPlaylist = selected,
            playlistWallpapers = wallpapers
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaylistsUiState())

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
    }

    fun createPlaylist(name: String, rotationMode: String = "SEQUENTIAL") {
        if (name.isBlank()) return
        viewModelScope.launch {
            playlistRepository.createPlaylist(name, rotationMode)
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlist)
            if (_selectedPlaylist.value?.id == playlist.id) {
                _selectedPlaylist.value = null
            }
        }
    }

    class Factory(
        private val playlistRepository: PlaylistRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaylistsViewModel(playlistRepository) as T
        }
    }
}
