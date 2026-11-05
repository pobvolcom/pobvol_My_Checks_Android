/*
 *  My software is <a href="https://en.wikipedia.org/wiki/Free_software" target="_blank" rel="nofollow">Free Software</a>☯ and delivered as open source via <a href="https://github.com/pobvolcom/" target="_blank" rel="nofollow">GitHub</a>☯. You can download, install, operate, modify, and redistribute my software under the terms of the <a href="https://www.gnu.org/licenses/gpl-3.0" target="_blank" rel="nofollow">GNU General Public License v3</a>☯. Copyright © 2026 Volker Pobloth, pobvol Software Services, poppi66292@gmail.com. Developed with Google Android Studio and Gemini 3.5 Flash Lite and Gemini 3.6 Flash
 */

package com.pobvol.mychecks.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pobvol.mychecks.ui.screens.ChartTimeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChecklistChartViewModel : ViewModel() {

    private val _chartTimeMode = MutableStateFlow(ChartTimeMode.ALL)
    val chartTimeMode: StateFlow<ChartTimeMode> = _chartTimeMode.asStateFlow()

    fun setChartTimeMode(mode: ChartTimeMode) {
        _chartTimeMode.value = mode
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChecklistChartViewModel() as T
        }
    }
}
