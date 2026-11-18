package com.baodeptrai.hub

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

@Composable
fun ShizukuFolderBrowserDialog(
    initialPath: String,
    onDismiss: () -> Unit,
    onSelectFolder: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentPath by remember { mutableStateOf(initialPath.trimEnd('/').ifEmpty { "/storage/emulated/0" }) }
    var subFolders by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showNewFolderInput by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    fun loadFolders(path: String) {
        scope.launch {
            isLoading = true
            subFolders = listDirectoriesViaShizuku(path)
            isLoading = false
        }
    }

    LaunchedEffect(currentPath) { loadFolders(currentPath) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111522),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chọn thư mục đích", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row {
                        IconButton(onClick = { currentPath = "/storage/emulated/0/Android/data" }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Android, contentDescription = null, tint = Color(0xFF10B981))
                        }
                        IconButton(onClick = { showNewFolderInput = !showNewFolderInput }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.CreateNewFolder, contentDescription = null, tint = Color(0xFF22D3EE))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(currentPath, fontSize = 12.sp, color = Color(0xFF22D3EE), maxLines = 2)
            }
        },
        text = {
            Column(modifier = Modifier.height(330.dp)) {
                if (showNewFolderInput) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            placeholder = { Text("Tên thư mục...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(6.dp))
                        Button(onClick = {
                            val clean = newFolderName.trim()
                            if (clean.isNotEmpty()) {
                                scope.launch {
                                    runShizukuShell("mkdir -p \"$currentPath/$clean\"")
                                    newFolderName = ""
                                    showNewFolderInput = false
                                    loadFolders(currentPath)
                                }
                            }
                        }) { Text("Tạo", fontSize = 12.sp) }
                    }
                }
                if (currentPath != "/storage/emulated/0" && currentPath != "/") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                val p = currentPath.substringBeforeLast('/')
                                currentPath = if (p.isEmpty()) "/storage/emulated/0" else p
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.ArrowUpward, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(".. (Quay lại)", color = Color(0xFFFBBF24), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                }
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF8B5CF6))
                    }
                } else if (subFolders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Không có thư mục con", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(subFolders) { folder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { currentPath = "$currentPath/$folder" }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Folder, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(folder, color = Color.White, fontSize = 14.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSelectFolder(currentPath) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))) {
                Text("CHỌN THƯ MỤC NÀY", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy", color = Color.Gray) } }
    )
}

fun saveUris(prefs: SharedPreferences, uris: List<Uri>) {
    val arr = JSONArray()
    uris.forEach { arr.put(it.toString()) }
    prefs.edit().putString("saved_file_uris", arr.toString()).apply()
}

fun loadSavedUris(prefs: SharedPreferences): List<Uri> {
    val raw = prefs.getString("saved_file_uris", null) ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        List(arr.length()) { i -> Uri.parse(arr.getString(i)) }
    } catch (_: Exception) { emptyList() }
}

fun isShizukuReady(): Boolean = try {
    Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
} catch (_: Exception) { false }

suspend fun listDirectoriesViaShizuku(dirPath: String): List<String> = withContext(Dispatchers.IO) {
    val res = mutableListOf<String>()
    try {
        val m = Shizuku::class.java.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
        m.isAccessible = true
        val p = m.invoke(null, arrayOf("sh", "-c", "ls -1p \"$dirPath\" 2>/dev/null | grep '/$'"), null, null) as Process
        BufferedReader(InputStreamReader(p.inputStream)).forEachLine { line ->
            val c = line.trimEnd('/').trim()
            if (c.isNotEmpty() && c != "." && c != "..") res.add(c)
        }
        p.waitFor()
    } catch (_: Exception) {}
    res.sortedBy { it.lowercase() }
}

suspend fun transferViaShizuku(context: Context, sources: List<Uri>, destDirPath: String): Int = withContext(Dispatchers.IO) {
    var ok = 0
    val dest = destDirPath.trimEnd('/')
    runShizukuShell("mkdir -p \"$dest\"")

    // Dùng externalCacheDir (/storage/emulated/0/Android/data/...) để Shizuku shell đọc được không bị SELinux chặn
    val sharedCache = context.externalCacheDir ?: context.cacheDir

    for (uri in sources) {
        try {
            val name = (DocumentFile.fromSingleUri(context, uri)?.name ?: "file_${System.currentTimeMillis()}").replace("\"", "\\\"")
            val tmp = File(sharedCache, "tmp_${System.nanoTime()}")

            val opened = context.contentResolver.openInputStream(uri)?.use { inp ->
                FileOutputStream(tmp).use { out -> inp.copyTo(out) }
                true
            } ?: false

            if (!opened || !tmp.exists()) continue

            val exit = runShizukuShell("cp -f \"${tmp.absolutePath}\" \"$dest/$name\"")
            tmp.delete()
            if (exit == 0) ok++
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    ok
}


fun runShizukuShell(command: String): Int = try {
    val m = Shizuku::class.java.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
    m.isAccessible = true
    (m.invoke(null, arrayOf("sh", "-c", command), null, null) as Process).waitFor()
} catch (_: Exception) { -1 }
