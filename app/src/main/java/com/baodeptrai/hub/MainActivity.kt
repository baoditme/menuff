package com.baodeptrai.hub

import android.content.Context
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF7C4DFF),
                    background = Color(0xFF0B0D14),
                    surface = Color(0xFF151926)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Key,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(Modifier.height(20.dp))
        Text("BAODEPTRAI HUB", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text("Nhập key bản quyền để tiếp tục", fontSize = 14.sp, color = Color(0xFF8E95A9))
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = keyText,
            onValueChange = {
                keyText = it
                errorMsg = false
            },
            label = { Text("Nhập Key") },
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
                text = "Key sai rồi! Vui lòng nhập lại.",
                color = Color(0xFFFF5252),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = {
                if (keyText.trim() == "baodeptrai") {
                    Toast.makeText(context, "Đã lưu key & mở khóa!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    errorMsg = true
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("XÁC THỰC", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun MainHubScreen(onResetKey: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("baodeptrai_prefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var hasShizuku by remember { mutableStateOf(isShizukuReady()) }
    var fileList by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var targetPath by remember {
        mutableStateOf(
            prefs.getString("last_target_path", "/storage/emulated/0/Android/data/")
                ?: "/storage/emulated/0/Android/data/"
        )
    }
    var isProcessing by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = Shizuku.OnRequestPermissionResultListener { _, result ->
            hasShizuku = result == PackageManager.PERMISSION_GRANTED
        }
        Shizuku.addRequestPermissionResultListener(listener)
        onDispose { Shizuku.removeRequestPermissionResultListener(listener) }
    }

    val pickFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) fileList = uris
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .animateContentSize()
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("CONTROL HUB", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("Shizuku Android/data Injector", fontSize = 12.sp, color = Color(0xFF8E95A9))
            }
            IconButton(onClick = onResetKey) {
                Icon(Icons.Rounded.Logout, contentDescription = "Đăng xuất key", tint = Color(0xFF8E95A9))
            }
        }

        Spacer(Modifier.height(14.dp))

        // Shizuku Status Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (hasShizuku) Color(0xFF122B22) else Color(0xFF2D151C)
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (hasShizuku) Color(0xFF00E676).copy(alpha = 0.4f) else Color(0xFFFF5252).copy(alpha = 0.4f),
                    RoundedCornerShape(18.dp)
                )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (hasShizuku) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = if (hasShizuku) Color(0xFF00E676) else Color(0xFFFF5252),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (hasShizuku) "Shizuku: Đã cấp quyền" else "Shizuku: Chưa sẵn sàng",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (hasShizuku) "Sẵn sàng ghi vào Android/data" else "Bật Shizuku rồi bấm Cấp quyền",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
                if (!hasShizuku) {
                    Button(
                        onClick = {
                            if (Shizuku.pingBinder()) {
                                Shizuku.requestPermission(101)
                            } else {
                                Toast.makeText(context, "Mở app Shizuku và Start dịch vụ trước!", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cấp quyền", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 1. Nút chọn Multi-file
        FilledTonalButton(
            onClick = { pickFilesLauncher.launch(arrayOf("*/*")) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Rounded.FileCopy, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text("1. Chọn nhiều File nguồn (${fileList.size} file)", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(12.dp))

        // 2. Ô nhập đường dẫn thư mục đích Android/data
        OutlinedTextField(
            value = targetPath,
            onValueChange = {
                targetPath = it
                prefs.edit().putString("last_target_path", it).apply()
            },
            label = { Text("2. Thư mục đích (Android/data/...)") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.FolderSpecial, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Danh sách file đã chọn
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF151926))
                .padding(12.dp)
        ) {
            if (fileList.isEmpty()) {
                Text(
                    text = "Chưa chọn file nguồn nào...",
                    color = Color(0xFF5A627A),
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn {
                    items(fileList) { uri ->
                        val name = DocumentFile.fromSingleUri(context, uri)?.name ?: uri.lastPathSegment ?: "Unknown"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Description, contentDescription = null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(name, color = Color.White, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f))
                            IconButton(onClick = { fileList = fileList - uri }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Xóa", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 3. Nút chính thực thi chép qua Shizuku
        val cleanPath = targetPath.trim()
        val canRun = hasShizuku && fileList.isNotEmpty() && cleanPath.startsWith("/") && !isProcessing
        val btnScale by animateFloatAsState(targetValue = if (canRun) 1f else 0.98f, label = "BtnScale")

        Button(
            enabled = canRun,
            onClick = {
                scope.launch {
                    isProcessing = true
                    val done = transferViaShizuku(context, fileList, cleanPath)
                    delay(350)
                    isProcessing = false
                    Toast.makeText(context, "Đã chép $done/${fileList.size} file vào đích!", Toast.LENGTH_LONG).show()
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .scale(btnScale)
        ) {
            AnimatedContent(targetState = isProcessing, label = "TransferState") { working ->
                if (working) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("ĐANG CHÉP QUA SHIZUKU...", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.RocketLaunch, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(10.dp))
                        Text("BẮT ĐẦU CHUYỂN FILE", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

fun isShizukuReady(): Boolean {
    return try {
        Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (_: Exception) {
        false
    }
}

suspend fun transferViaShizuku(context: Context, sources: List<Uri>, destDirPath: String): Int = withContext(Dispatchers.IO) {
    var success = 0
    val normalizedDest = destDirPath.trimEnd('/')

    // Tạo sẵn thư mục đích bằng quyền Shizuku nếu chưa tồn tại
    runShizukuShell("mkdir -p \"$normalizedDest\"")

    for (uri in sources) {
        try {
            val doc = DocumentFile.fromSingleUri(context, uri)
            val fileName = (doc?.name ?: "file_${System.currentTimeMillis()}").replace("\"", "\\\"")

            // 1. Copy file nguồn ra cache tạm của app để shell Shizuku đọc được đường dẫn thật
            val tempFile = File(context.cacheDir, "temp_transfer_${System.nanoTime()}")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            // 2. Dùng lệnh cp của Shizuku đẩy thẳng vào Android/data và xóa file tạm
            val targetFilePath = "$normalizedDest/$fileName"
            val exitCode = runShizukuShell("cp -f \"${tempFile.absolutePath}\" \"$targetFilePath\" && chmod 660 \"$targetFilePath\"")
            tempFile.delete()

            if (exitCode == 0) success++
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    success
}

private fun runShizukuShell(command: String): Int {
    return try {
        val method = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        method.isAccessible = true
        val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
        process.waitFor()
    } catch (e: Exception) {
        e.printStackTrace()
        -1
    }
}
