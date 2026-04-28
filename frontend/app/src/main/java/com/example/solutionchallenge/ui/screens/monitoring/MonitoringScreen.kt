package com.example.solutionchallenge.ui.screens.monitoring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.solutionchallenge.ui.components.MetricCard
import com.example.solutionchallenge.ui.theme.WarningColor
import com.example.solutionchallenge.ui.viewmodel.MonitoringViewModelSample

@Composable
fun MonitoringScreen(viewModel: MonitoringViewModelSample) {
    val monitorState by viewModel.monitorData.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMonitoringData()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Text("System Monitoring (Live)", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (monitorState != null) {
            val driftStatus = monitorState!!.drift_status
            val currentScore = monitorState!!.history.lastOrNull()?.dp_score ?: 0.0

            Text("Current Drift Score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            MetricCard(
                title = "Drift Metric", 
                value = "$currentScore ${if (driftStatus.alert) "(Warning)" else "(Stable)"}"
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Latest Alerts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (driftStatus.alert) "Drift Alert!" else "System Log", fontWeight = FontWeight.Bold, color = if (driftStatus.alert) WarningColor else MaterialTheme.colorScheme.onSurface)
                    Text(driftStatus.message)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Previous Audits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            monitorState!!.history.forEach { point ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(point.timestamp, fontWeight = FontWeight.Bold)
                        Text("Demographic Parity Score: ${point.dp_score}")
                    }
                }
            }

        } else {
            CircularProgressIndicator(modifier = Modifier.padding(vertical = 32.dp))
            Text("Fetching real-time backend metrics from server...")
        }
    }
}
