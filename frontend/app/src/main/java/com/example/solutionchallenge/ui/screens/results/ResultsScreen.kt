package com.example.solutionchallenge.ui.screens.results

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
import com.example.solutionchallenge.ui.components.BiasBarChart
import com.example.solutionchallenge.ui.components.BiasGuardButton
import com.example.solutionchallenge.ui.components.MetricCard
import com.example.solutionchallenge.ui.theme.FairColor
import com.example.solutionchallenge.ui.theme.WarningColor
import com.example.solutionchallenge.ui.viewmodel.ResultsViewModelSample

@Composable
fun ResultsScreen(target: String, protectedAttribute: String, predictionColumn: String, onViewExplanation: (String) -> Unit, viewModel: ResultsViewModelSample) {
    val auditState by viewModel.auditResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(target, protectedAttribute, predictionColumn) {
        viewModel.runAudit(listOf(protectedAttribute), predictionColumn, target)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            Text("Calculating fairness metrics across your dataset dynamically...")
        } else if (auditState != null) {
            val metrics = auditState!!.metrics
            val dpScore = metrics.demographic_parity?.score ?: 0.0
            val eoScore = metrics.equal_opportunity?.score ?: 0.0
            val isBiased = metrics.demographic_parity?.is_biased == true || metrics.equal_opportunity?.is_biased == true

            Text("Live Audit Results", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Status: ${if (isBiased) "Unfair / Biased" else "Fair"}", 
                color = if (isBiased) WarningColor else FairColor, 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            MetricCard(title = "Demographic Parity Disparity", value = String.format("%.2f", dpScore))
            Spacer(modifier = Modifier.height(8.dp))
            MetricCard(title = "Equal Opportunity Disparity", value = String.format("%.2f", eoScore))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Intersectional Maximum Bias: ${auditState!!.intersectional_max_bias}", fontWeight = FontWeight.Light)
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Visualization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            BiasBarChart(demographicParity = dpScore.toFloat(), equalOpportunity = eoScore.toFloat(), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.weight(1f))
            BiasGuardButton(
                text = "View Detailed Explanation",
                onClick = { onViewExplanation("audit_id_live") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text("Could not calculate metrics, backend disconnected.", color = WarningColor)
        }
    }
}
