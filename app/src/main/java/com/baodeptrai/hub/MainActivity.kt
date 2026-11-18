package com.baodeptrai.hub

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GppBad
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF8B5CF6),
                    secondary = Color(0xFF06B6D4),
                    background = Color(0xFF07090F),
                    surface = Color(0xFF111522)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun TypewriterHeader(
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val targetText = "bao tools"
    var displayedText by remember { mutableStateOf("") }
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(450)
            cursorVisible = !cursorVisible
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            for (i in 1..targetText.length) {
                displayedText = targetText.substring(0, i)
                delay(140)
            }
            delay(1300)
            for (i in targetText.length - 1 downTo 0) {
                displayedText = targetText.substring(0, i)
                delay(85)
            }
            delay(400)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF161B2E), Color(0xFF111522))
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF8B5CF6).copy(alpha = 0.6f), Color(0xFF06B6D4).copy(alpha = 0.6f))
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = Color(0xFF8B5CF6), spotColor = Color(0xFF06B6D4))
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher),
                    contentDescription = "Bao Tools Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.width(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayedText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (cursorVisible) "|" else " ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF06B6D4)
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("baodeptrai_prefs", Context.MODE_PRIVATE) }
    var isUnlocked by remember { mutableStateOf(prefs.getBoolean("is_key_verified", false)) }

    AnimatedContent(
        targetState = isUnlocked,
        transitionSpec = {
            (fadeIn(tween(450)) + scaleIn(initialScale = 0.94f, animationSpec = tween(450)))
                .togetherWith(fadeOut(tween(300)))
        },
        label = "AuthToHub"
    ) { unlocked ->
        if (!unlocked) {
            KeyLoginScreen(
                onSuccess = {
                    prefs.edit().putBoolean("is_key_verified", true).apply()
                    isUnlocked = true
                }
            )
        } else {
            MainHubScreen(
                onResetKey = {
                    prefs.edit().putBoolean("is_key_verified", false).apply()
                    isUnlocked = false
                }
            )
        }
    }
}

@Composable
fun KeyLoginScreen(onSuccess: () -> Unit) {
    var keyText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D101D), Color(0xFF07090F))
                )
            )
            .padding(24.dp)
    ) {
        TypewriterHeader(modifier = Modifier.align(Alignment.TopCenter))

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF111522).copy(alpha = 0.9f))
                .border(
                    1.dp,
                    Brush.linearGradient(listOf(Color(0xFF8B5CF6).copy(alpha = 0.5f), Color(0xFF06B6D4).copy(alpha = 0.3f))),
                    RoundedCornerShape(26.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.VpnKey,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(18.dp))
            Text("XÁC THỰC HỆ THỐNG", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("Nhập key kích hoạt để mở khóa Bao Tools", fontSize = 13.sp, color = Color(0xFF94A3B8))
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = keyText,
                onValueChange = {
                    keyText = it
                    errorMsg = false
                },
                label = { Text("Nhập Key truy cập") },
                singleLine = true,
                isError = errorMsg,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = errorMsg,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    text = "Key không đúng! Vui lòng kiểm tra lại.",
                    color = Color(0xFFF43F5E),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = {
                    if (keyText.trim() == "baodeptrai") {
                        Toast.makeText(context, "Chào mừng đến với Bao Tools!", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    } else {
                        errorMsg = true
                    }
                },
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("XÁC THỰC NGAY", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MainHubScreen(onResetKey: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("baodeptrai_prefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var hasShizuku by remember { mutableStateOf(isShizukuReady()) }
    var fileList by remember { mutableStateOf(loadSavedUris(prefs)) }
    var targetPath by remember {
        mutableStateOf(
            prefs.getString("last_target_path", "/storage/emulated/0/Android/data")
                ?: "/storage/emulated/0/Android/data"
        )
    }
    var showFolderPicker by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    fun updateFileList(newList: List<Uri>) {
        val distinctList = newList.distinct()
        fileList = distinctList
        saveUris(prefs, distinctList)
    }

    DisposableEffect(Unit) {
        val permListener = Shizuku.OnRequestPermissionResultListener { _, result ->
            hasShizuku = result == PackageManager.PERMISSION_GRANTED
        }
        val binderReceivedListener = Shizuku.OnBinderReceivedListener {
            hasShizuku = isShizukuReady()
        }
        val binderDeadListener = Shizuku.OnBinderDeadListener {
            hasShizuku = false
        }
        Shizuku.addRequestPermissionResultListener(permListener)
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        hasShizuku = isShizukuReady()

        onDispose {
            Shizuku.removeRequestPermissionResultListener(permListener)
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
        }
    }

    val pickFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }
            }
            updateFileList(fileList + uris)
        }
    }

    if (showFolderPicker) {
        ShizukuFolderBrowserDialog(
            initialPath = targetPath,
            onDismiss = { showFolderPicker = false },
            onSelectFolder = { selected ->
                targetPath = selected
                prefs.edit().putString("last_target_path", selected).apply()
                showFolderPicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0D101D), Color(0xFF07090F)))
            )
            .padding(18.dp)
            .animateContentSize()
    ) {
        TypewriterHeader(
            trailingContent = {
                IconButton(
                    onClick = onResetKey,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                ) {
                    Icon(Icons.Rounded.Logout, contentDescription = "Khóa lại", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                }
            }
        )

        Spacer(Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (hasShizuku) Color(0xFF0D2822) else Color(0xFF2B121D)
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (hasShizuku) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFF43F5E).copy(alpha = 0.5f),
                    RoundedCornerShape(18.dp)
                )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (hasShizuku) Icons.Rounded.Verified else Icons.Rounded.GppBad,
                    contentDescription = null,
                    tint = if (hasShizuku) Color(0xFF10B981) else Color(0xFFF43F5E),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (hasShizuku) "Shizuku: Đã kích hoạt" else "Shizuku: Chưa cấp quyền",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (hasShizuku) "Quyền hệ thống sẵn sàng" else "Bấm Cấp quyền để kết nối Shizuku",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                }
                if (!hasShizuku) {
                    Button(
                        onClick = {
                            if (Shizuku.pingBinder()) {
                                Shizuku.requestPermission(101)
                            } else {
                                Toast.makeText(context, "Hãy mở app Shizuku và Start dịch vụ trước!", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cấp quyền", color = Color.White, fontWeight = FontWeight.Bold)
                        
