package com.example.solutionchallenge.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

@Composable
fun BiasBarChart(
    demographicParity: Float,
    equalOpportunity: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setPinchZoom(false)
                setDrawGridBackground(false)
                
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f
                axisLeft.axisMaximum = 1f
                axisLeft.textColor = textColor
                
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                xAxis.textColor = textColor
                
                legend.textColor = textColor
            }
        },
        update = { chart ->
            val entries = listOf(
                BarEntry(1f, demographicParity),
                BarEntry(2f, equalOpportunity)
            )

            val dataSet = BarDataSet(entries, "Bias Metrics (1=Dem. Parity, 2=Eq. Opp.)").apply {
                colors = listOf(primaryColor, secondaryColor)
                valueTextSize = 12f
                valueTextColor = textColor
            }

            chart.data = BarData(dataSet).apply {
                barWidth = 0.5f
            }
            chart.invalidate()
        }
    )
}
