package com.inmobi.devtest

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val mp3Player = Mp3Player(application)

    init {
        val assetsManager = AssetsManager(application)
        mp3Player.loadFile(assetsManager.getBeatUri())
    }

    override fun onCleared() {
        super.onCleared()
        mp3Player.release()
    }
}
