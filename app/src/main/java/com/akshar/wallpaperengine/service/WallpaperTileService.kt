package com.akshar.wallpaperengine.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.akshar.wallpaperengine.WallpaperEngineApplication
import com.akshar.wallpaperengine.domain.usecase.RotateWallpaperUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.N)
class WallpaperTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        tile.state = Tile.STATE_INACTIVE
        tile.label = "Next Wallpaper"
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        tile.state = Tile.STATE_ACTIVE
        tile.updateTile()

        val app = applicationContext as? WallpaperEngineApplication ?: return
        val rotateUseCase = RotateWallpaperUseCase(
            wallpaperDao = app.database.wallpaperDao(),
            scheduleDao = app.database.scheduleDao(),
            historyDao = app.database.historyDao(),
            wallpaperService = app.wallpaperService
        )

        CoroutineScope(Dispatchers.IO).launch {
            val success = rotateUseCase.rotateWithContextTrigger(
                triggerType = "QUICK_TILE",
                reasonOverride = "Quick Settings Tile Tap"
            )

            withContext(Dispatchers.Main) {
                tile.state = Tile.STATE_INACTIVE
                tile.updateTile()

                if (success) {
                    Toast.makeText(applicationContext, "Wallpaper Rotated ✦", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "No wallpapers available to rotate", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
