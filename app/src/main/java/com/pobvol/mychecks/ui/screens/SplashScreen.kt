/*
 *  My software is <a href="https://en.wikipedia.org/wiki/Free_software" target="_blank" rel="nofollow">Free Software</a>☯ and delivered as open source via <a href="https://github.com/pobvolcom/" target="_blank" rel="nofollow">GitHub</a>☯. You can download, install, operate, modify, and redistribute my software under the terms of the <a href="https://www.gnu.org/licenses/gpl-3.0" target="_blank" rel="nofollow">GNU General Public License v3</a>☯. Copyright © 2026 Volker Pobloth, pobvol Software Services, poppi66292@gmail.com. Developed with Google Android Studio and Gemini 3.5 Flash Lite and Gemini 3.6 Flash
 */

package com.pobvol.mychecks.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import com.pobvol.mychecks.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(1000) // Show splash for 1 second, timeMillis = 1000
        onTimeout()
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val splashRes =
        if (isLandscape) R.drawable.pobvol_my_checks_start_landscape else R.drawable.pobvol_my_checks_start

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = splashRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}
