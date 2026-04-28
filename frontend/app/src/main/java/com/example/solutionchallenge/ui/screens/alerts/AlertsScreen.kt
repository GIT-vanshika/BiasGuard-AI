package com.example.solutionchallenge.ui.screens.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.solutionchallenge.ui.theme.WarningColor

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import com.example.solutionchallenge.ui.viewmodel.MonitoringViewModelSample

@Composable
fun AlertsScreen(viewModel: MonitoringViewModelSample) {
    val monitorState by viewModel.monitorData.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMonitoringData()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Text("System Alerts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (monitorState != null) {
            val driftStatus = monitorState!!.drift_status
            
            if (driftStatus.alert) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("CRITICAL: Drift Detected", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(driftStatus.message, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("System Healthy", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No critical drifts or bias threshold violations detected.")
                    }
                }
            }
        } else {
            Text("Fetching real-time monitoring data...")
        }
    }
}
