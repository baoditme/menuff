package com.baodeptrai.hub

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

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
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun TypewriterHeader(modifier: Modifier = Modifier, trailingContent: (@Composable () -> Unit)? = null) {
    val target = "bao tools"
    var text by remember { mutableStateOf("") }
    var cursor by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { while (true) { delay(450); cursor = !cursor } }
    LaunchedEffect(Unit) {
        while (true) {
            for (i in 1..target.length) { text = target.substring(0, i); delay(140) }
            delay(1300)
            for (i in target.length - 1 downTo 0) { text = target.substring(0, i); delay(85) }
            delay(400)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF161B2E), Color(0xFF111522))))
            .border(1.dp, Brush.horizontalGradient(listOf(Color(0xFF8B5CF6).copy(0.6f), Color(0xFF06B6D4).copy(0.6f))), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF06B6D4).copy(0.7f), RoundedCornerShape(12.dp))
            ) {
                Image(painter = painterResource(id = R.drawable.ic_launcher), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(12.dp))
            Text(text, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, color = Color.White, letterSpacing = 1.sp)
            Text(if (cursor) "|" else " ", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF06B6D4))
        }
        trailingContent?.invoke()
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("baodeptrai_prefs", Context.MODE_PRIVATE) }
    var unlocked by remember { mutableStateOf(prefs.getBoolean("is_key_verified", false)) }

    AnimatedContent(targetState = unlocked, label = "Nav") { ok ->
        if (!ok) {
            KeyLoginScreen { prefs.edit().putBoolean("is_key_verified", true).apply(); unlocked = true }
        } else {
            MainHubScreen { prefs.edit().putBoolean("is_key_verified", false).apply(); unlocked = false }
        }
    }
}

@Composable
fun KeyLoginScreen(onSuccess: () -> Unit) {
    var key by remember { mutableStateOf("") }
    var err by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D101D), Color(0xFF07090F)))).padding(24.dp)) {
        TypewriterHeader(modifier = Modifier.align(Alignment.TopCenter))
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF111522))
                .border(1.dp, Brush.linearGradient(listOf(Color(0xFF8B5CF6).copy(0.5f), Color(0xFF06B6D4).copy(0.3f))), RoundedCornerShape(26.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)))),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.VpnKey, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp)) }
            Spacer(Modifier.height(16.dp))
            Text("XÁC THỰC HỆ THỐNG", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("Nhập key kích hoạt Bao Tools", fontSize = 13.sp, color = Color(0xFF94A3B8))
            Spacer(Modifier.height(22.dp))
            OutlinedTextField(
                value = key, onValueChange = { key = it; err = false }, label = { Text("Nhập Key") },
                singleLine = true, isError = err, visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(visible = err) {
                Text("Key không đúng!", color = Color(0xFFF43F5E), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { if (key.trim() == "baodeptrai") { Toast.makeText(ctx, "Mở khóa Bao Tools!", Toast.LENGTH_SHORT).show(); onSuccess() } else err = true },
                shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)))), contentAlignment = Alignment.Center) {
                    Text("XÁC THỰC NGAY", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MainHubScreen(onResetKey: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("baodeptrai_prefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var hasShizuku by remember { mutableStateOf(isShizukuReady()) }
    var fileList by remember { mutableStateOf(loadSavedUris(prefs)) }
    var targetPath by remember { mutableStateOf(prefs.getString("last_target_path", "/storage/emulated/0/Android/data") ?: "/storage/emulated/0/Android/data") }
    var showPicker by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }

    fun updateFiles(list: List<Uri>) {
        val d = list.distinct()
        fileList = d
        saveUris(prefs, d)
    }

    DisposableEffect(Unit) {
        val pL = Shizuku.OnRequestPermissionResultListener { _, r -> hasShizuku = r == PackageManager.PERMISSION_GRANTED }
        val bR = Shizuku.OnBinderReceivedListener { hasShizuku = isShizukuReady() }
        val bD = Shizuku.OnBinderDeadListener { hasShizuku = false }
        Shizuku.addRequestPermissionResultListener(pL); Shizuku.addBinderReceivedListener(bR); Shizuku.addBinderDeadListener(bD)
        hasShizuku = isShizukuReady()
        onDispose { Shizuku.removeRequestPermissionResultListener(pL); Shizuku.removeBinderReceivedListener(bR); Shizuku.removeBinderDeadListener(bD) }
    }

    val pickFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { try { ctx.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {} }
            updateFiles(fileList + uris)
        }
    }

    if (showPicker) {
        ShizukuFolderBrowserDialog(initialPath = targetPath, onDismiss = { showPicker = false }) { sel ->
            targetPath = sel; prefs.edit().putString("last_target_path", sel).apply(); showPicker = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D101D), Color(0xFF07090F)))).padding(18.dp).animateContentSize()) {
        TypewriterHeader {
            IconButton(onClick = onResetKey, modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(0.06f))) {
                Icon(Icons.Rounded.Logout, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = if (hasShizuku) Color(0xFF0D2822) else Color(0xFF2B121D)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, if (hasShizuku) Color(0xFF10B981).copy(0.5f) else Color(0xFFF43F5E).copy(0.5f), RoundedCornerShape(18.dp))
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (hasShizuku) Icons.Rounded.Verified else Icons.Rounded.GppBad, contentDescription = null, tint = if (hasShizuku) Color(0xFF10B981) else Color(0xFFF43F5E), modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (hasShizuku) "Shizuku: Đã kích hoạt" else "Shizuku: Chưa cấp quyền", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text(if (hasShizuku) "Quyền hệ thống sẵn sàng" else "Bấm Cấp quyền để kết nối", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                }
                if (!hasShizuku) {
                    Button(
                        onClick = { if (Shizuku.pingBinder()) Shizuku.requestPermission(101) else Toast.makeText(ctx, "Mở app Shizuku và Start trước!", Toast.LENGTH_LONG).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)), shape = RoundedCornerShape(12.dp)
                    ) { Text("Cấp quyền", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Surface(
            onClick = { pickFiles.launch(arrayOf("*/*")) }, shape = RoundedCornerShape(16.dp), color = Color(0xFF161B2E),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF8B5CF6).copy(0.45f), RoundedCornerShape(16.dp))
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.NoteAdd, contentDescription = null, tint = Color(0xFFA78BFA))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("1. Chọn / Thêm File nguồn", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Đang lưu ${fileList.size} file • Bấm để thêm tiếp", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
                Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, tint = Color(0xFFA78BFA))
            }
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            onClick = { if (hasShizuku) showPicker = true else Toast.makeText(ctx, "Hãy cấp quyền Shizuku trước!", Toast.LENGTH_SHORT).show() },
            shape = RoundedCornerShape(16.dp), color = Color(0xFF161B2E),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF06B6D4).copy(0.45f), RoundedCornerShape(16.dp))
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = Color(0xFF22D3EE))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("2. Thư mục đích (Bấm để duyệt)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(targetPath, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2)
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color(0xFF22D3EE))
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Hàng đợi file (${fileList.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            if (fileList.isNotEmpty()) {
                TextButton(onClick = { updateFiles(emptyList()) }) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Xóa hết", color = Color(0xFFF43F5E), fontSize = 12.sp)
                }
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF111522)).border(1.dp, Color.White.copy(0.06f), RoundedCornerShape(18.dp)).padding(12.dp)) {
            if (fileList.isEmpty()) {
                Text("Chưa có file nào\n(Giữ đè file khi chọn để tích nhiều file)", color = Color(0xFF64748B), fontSize = 12.sp, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn {
                    items(fileList, key = { it.toString() }) { uri ->
                        val name = DocumentFile.fromSingleUri(ctx, uri)?.name ?: uri.lastPathSegment ?: "File"
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.InsertDriveFile, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(name, color = Color.White, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f))
                            IconButton(onClick = { updateFiles(fileList - uri) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        val clean = targetPath.trim()
        val canRun = hasShizuku && fileList.isNotEmpty() && clean.startsWith("/") && !working
        val sc by animateFloatAsState(if (canRun) 1f else 0.98f, label = "sc")
        Button(
            enabled = canRun,
            onClick = { scope.launch { working = true; val d = transferViaShizuku(ctx, fileList, clean); delay(350); working = false; Toast.makeText(ctx, "Đã ghi đè $d/${fileList.size} file!", Toast.LENGTH_LONG).show() } },
            shape = RoundedCornerShape(18.dp), contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth().height(58.dp).scale(sc)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(if (canRun) Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4))) else Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B)))),
                contentAlignment = Alignment.Center
            ) {
                if (working) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("ĐANG CHÉP QUA SHIZUKU...", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.RocketLaunch, contentDescription = null, tint = if (canRun) Color.White else Color.Gray)
                        Spacer(Modifier.width(10.dp))
                        Text("BẮT ĐẦU CHUYỂN FILE", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = if (canRun) Color.White else Color.Gray)
                    }
                }
            }
        }
    }
}
