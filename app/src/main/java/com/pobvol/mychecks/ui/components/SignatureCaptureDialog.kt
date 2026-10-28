package com.pobvol.mychecks.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.pobvol.mychecks.R
import java.io.File
import java.io.FileOutputStream
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Matrix as AndroidMatrix
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath

@Composable
fun SignatureCaptureDialog(
    onDismiss: () -> Unit,
    onSaveSignature: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val paths = remember { mutableStateListOf<AndroidPath>() }
    var currentPath by remember { mutableStateOf<AndroidPath?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.capture_signature),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Sign inside the box below:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .onGloballyPositioned { canvasSize = it.size.toSize() }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = AndroidPath().apply {
                                        moveTo(offset.x, offset.y)
                                    }
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val offset = change.position
                                    currentPath?.lineTo(offset.x, offset.y)
                                },
                                onDragEnd = {
                                    currentPath?.let { paths.add(it) }
                                    currentPath = null
                                }
                            )
                        },
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        paths.forEach { path ->
                            drawPath(
                                path = path.asComposePath(),
                                color = Color.Black,
                                style = Stroke(width = 6f)
                            )
                        }
                        currentPath?.let { path ->
                            drawPath(
                                path = path.asComposePath(),
                                color = Color.Black,
                                style = Stroke(width = 6f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    currentPath?.let {
                        paths.add(it)
                        currentPath = null
                    }

                    val bitmapWidth = 600f
                    val bitmapHeight = 200f
                    val bitmap = Bitmap.createBitmap(
                        bitmapWidth.toInt(),
                        bitmapHeight.toInt(),
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = AndroidCanvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)

                    val paint = AndroidPaint().apply {
                        color = android.graphics.Color.BLACK
                        strokeWidth = 6f
                        style = AndroidPaint.Style.STROKE
                        strokeCap = AndroidPaint.Cap.ROUND
                        strokeJoin = AndroidPaint.Join.ROUND
                        isAntiAlias = true
                    }

                    val scaleX = if (canvasSize.width > 0f) bitmapWidth / canvasSize.width else 1f
                    val scaleY =
                        if (canvasSize.height > 0f) bitmapHeight / canvasSize.height else 1f

                    val matrix = AndroidMatrix().apply {
                        setScale(scaleX, scaleY)
                    }

                    paths.forEach { path ->
                        val scaledPath = AndroidPath(path)
                        scaledPath.transform(matrix)
                        canvas.drawPath(scaledPath, paint)
                    }

                    try {
                        val picturesDir = File(context.filesDir, "pictures")
                        if (!picturesDir.exists()) picturesDir.mkdirs()
                        val sigFile = File(picturesDir, "user_signature.png")
                        val fos = FileOutputStream(sigFile)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                        fos.flush()
                        fos.close()
                        onSaveSignature(sigFile.absolutePath)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { paths.clear(); currentPath = null },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(stringResource(R.string.clear))
                }
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        modifier = modifier,
    )
}
