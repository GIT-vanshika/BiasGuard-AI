package com.example.solutionchallenge.ui.screens.upload

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.solutionchallenge.ui.components.BiasGuardButton
import com.example.solutionchallenge.ui.components.BiasGuardTextField
import com.example.solutionchallenge.ui.viewmodel.UploadViewModelSample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun UploadScreen(onNavigateResults: (String, String, String) -> Unit, viewModel: UploadViewModelSample) {
    val uploadState by viewModel.uploadState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    
    var targetColumn by remember { mutableStateOf("") }
    var protectedAttribute by remember { mutableStateOf("") }
    var predictionColumn by remember { mutableStateOf("") }

    // 1. Android Native File Chooser Launcher
    // Utilizing "*/*" because some devices fail to recognize text/csv intents reliably
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
        uri?.let {
            fileName = getFileName(context, it) ?: "selected_file.csv"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Run Live Bias Audit", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Text("1. Select Dataset", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            BiasGuardButton(
                text = "Browse Device",
                onClick = { launcher.launch("*/*") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = fileName ?: "No file selected",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        BiasGuardButton(
            text = if (isUploading) "Uploading Dataset..." else "Upload Dataset to Backend",
            onClick = {
                selectedUri?.let { uri ->
                    isUploading = true
                    // Offload file copying to IO thread to avoid UI freezing
                    coroutineScope.launch(Dispatchers.IO) {
                        val tempFile = copyUriToCacheFile(context, uri, fileName ?: "temp_dataset.csv")
                        if (tempFile != null) {
                            // Bridging to our existing UploadViewModelSample
                            viewModel.uploadDataset(tempFile)
                        } else {
                            isUploading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedUri != null && !isUploading && uploadState == null
        )

        // Observe ViewModel State Updates
        if (uploadState != null) {
            isUploading = false // Turn off loading state once successful

            Spacer(modifier = Modifier.height(16.dp))
            Text("Success: ${uploadState!!.message}", color = MaterialTheme.colorScheme.primary)
            Text("Columns detected: ${uploadState!!.columns_detected.joinToString()}")
            Spacer(modifier = Modifier.height(16.dp))

            Text("2. Model Configuration", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            BiasGuardTextField(value = targetColumn, onValueChange = { targetColumn = it }, label = "Target Column (from detected cols)")
            Spacer(modifier = Modifier.height(8.dp))
            BiasGuardTextField(value = protectedAttribute, onValueChange = { protectedAttribute = it }, label = "Protected Attribute (from detected cols)")
            Spacer(modifier = Modifier.height(8.dp))
            BiasGuardTextField(value = predictionColumn, onValueChange = { predictionColumn = it }, label = "Prediction Column")

            Spacer(modifier = Modifier.height(32.dp))
            BiasGuardButton(
                text = "Run Audit",
                onClick = { onNavigateResults(targetColumn, protectedAttribute, predictionColumn) },
                modifier = Modifier.fillMaxWidth(),
                enabled = targetColumn.isNotEmpty() && protectedAttribute.isNotEmpty() && predictionColumn.isNotEmpty()
            )
        }
    }
}

/**
 * Helper function to parse the true file name from the Content Resolver's Uri.
 */
private fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

/**
 * Helper function converting Android Native URI -> java.io.File 
 * Required because Retrofit Multipart Requests need strong File streams.
 */
private fun copyUriToCacheFile(context: Context, uri: Uri, fileName: String): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File(context.cacheDir, fileName)
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
