package com.wordglass.app.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.graphics.BitmapShader
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.design.WgTheme
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Avatar picker: pick from gallery → adjust circle crop → save.
 * Simple square photo → circular avatar, like QQ/WeChat.
 */
@Composable
fun AvatarPickerDialog(
    currentAvatarUri: String?,
    onAvatarSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = WgTheme.colors
    val context = LocalContext.current

    var pickedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cropScale by remember { mutableFloatStateOf(1f) }
    var cropOffsetX by remember { mutableFloatStateOf(0f) }
    var cropOffsetY by remember { mutableFloatStateOf(0f) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val input = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(input)
                input?.close()
                if (bitmap != null) {
                    // Scale to max 1024
                    val maxDim = 1024
                    val w = bitmap.width
                    val h = bitmap.height
                    val scaled = if (min(w, h) > maxDim) {
                        val ratio = maxDim.toFloat() / min(w, h)
                        Bitmap.createScaledBitmap(bitmap, (w * ratio).toInt(), (h * ratio).toInt(), true)
                    } else bitmap
                    pickedBitmap = scaled
                    cropScale = 1f
                    cropOffsetX = 0f
                    cropOffsetY = 0f
                }
            } catch (_: Exception) { }
        }
    }

    if (pickedBitmap != null) {
        // === CROP SCREEN ===
        CropScreen(
            bitmap = pickedBitmap!!,
            scale = cropScale,
            offsetX = cropOffsetX,
            offsetY = cropOffsetY,
            onScaleChange = { cropScale = it },
            onOffsetChange = { x, y -> cropOffsetX = x; cropOffsetY = y },
            onConfirm = {
                try {
                    val cropped = cropToCircleCenter(pickedBitmap!!, cropScale, cropOffsetX, cropOffsetY)
                    val avatarDir = File(context.filesDir, "avatars").apply { mkdirs() }
                    val file = File(avatarDir, "avatar_${System.currentTimeMillis()}.png")
                    FileOutputStream(file).use { out ->
                        cropped.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    cropped.recycle()
                    onAvatarSelected(file.absolutePath)
                } catch (_: Exception) { }
                onDismiss()
            },
            onCancel = { pickedBitmap = null },
        )
    } else {
        // === PICKER SCREEN ===
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = colors.surfaceElevated,
            title = { Text("设置头像", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    val avatarFile = currentAvatarUri?.let { File(it) }
                    if (avatarFile != null && avatarFile.exists()) {
                        Image(
                            bitmap = BitmapFactory.decodeFile(avatarFile.absolutePath)?.asImageBitmap()
                                ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, colors.blue, CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(Modifier.size(80.dp).clip(CircleShape).background(colors.blue.copy(alpha = 0.12f)), Alignment.Center) {
                            Text("?", fontSize = MaterialTheme.typography.headlineLarge.fontSize, color = colors.blue)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.blue.copy(alpha = 0.08f))
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, null, tint = colors.blue, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("从相册选择照片", style = MaterialTheme.typography.bodyLarge, color = colors.ink)
                            Text("选择后可裁剪为圆形头像", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("取消", color = colors.inkSecondary) }
            },
        )
    }
}

/**
 * Crop screen: show photo, circular overlay, drag+zoom to adjust.
 */
@Composable
private fun CropScreen(
    bitmap: Bitmap,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Float, Float) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = WgTheme.colors
    val img = remember(bitmap) { bitmap.asImageBitmap() }

    // Gesture state for dragging and zooming
    var gestureScale by remember { mutableFloatStateOf(scale) }
    var gestureOffsetX by remember { mutableFloatStateOf(offsetX) }
    var gestureOffsetY by remember { mutableFloatStateOf(offsetY) }

    Column(Modifier.fillMaxSize().background(Color(0xFF222222))) {
        // Top bar
        Row(Modifier.fillMaxWidth().padding(12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) { onCancel() }, Alignment.Center) {
                Icon(Icons.Rounded.Close, "取消", tint = Color.White)
            }
            Text("拖动照片到圆框内", style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.8f))
            Box(
                Modifier.clip(RoundedCornerShape(50)).background(colors.blue)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onScaleChange(gestureScale)
                        onOffsetChange(gestureOffsetX, gestureOffsetY)
                        onConfirm()
                    }
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("确定", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Crop area: full screen for image, circle ring on top
        Box(Modifier.fillMaxWidth().weight(1f)) {
            // Photo layer - fully draggable and zoomable
            Image(
                bitmap = img,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = gestureScale,
                        scaleY = gestureScale,
                        translationX = gestureOffsetX,
                        translationY = gestureOffsetY,
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            gestureScale = (gestureScale * zoom).coerceIn(0.5f, 5f)
                            gestureOffsetX += pan.x
                            gestureOffsetY += pan.y
                        }
                    },
                contentScale = ContentScale.Crop,
            )

            // Circle ring overlay - just draws the ring, doesn't block touches
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val r = size.minDimension * 0.38f
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Light darkening outside circle (not too dark, just enough to see)
                drawRect(
                    color = Color.Black.copy(alpha = 0.3f),
                    size = size,
                )

                // Clear circle
                val path = androidx.compose.ui.graphics.Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r))
                }
                drawPath(
                    path = path,
                    color = Color.Transparent,
                    blendMode = androidx.compose.ui.graphics.BlendMode.Clear,
                )

                // White circle ring
                drawCircle(
                    color = Color.White,
                    radius = r,
                    center = androidx.compose.ui.geometry.Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx()),
                )
            }
        }
    }
}

/**
 * Crop bitmap to center square based on scale and offset, then apply circular mask.
 */
private fun cropToCircleCenter(bitmap: Bitmap, scale: Float, offsetX: Float, offsetY: Float): Bitmap {
    // Determine the crop square (center of image, adjusted by offset)
    val size = min(bitmap.width, bitmap.height)
    val cx = ((bitmap.width - size) / 2f - offsetX / scale).toInt().coerceIn(0, bitmap.width - size)
    val cy = ((bitmap.height - size) / 2f - offsetY / scale).toInt().coerceIn(0, bitmap.height - size)

    val square = Bitmap.createBitmap(bitmap, cx, cy, size, size)

    // Apply circular mask
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Draw circle
    val shader = BitmapShader(square, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    paint.shader = shader
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

    square.recycle()
    return output
}
