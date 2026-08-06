package com.apptimemachine.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.apptimemachine.app.ui.theme.MonitoringActiveAccent
import com.apptimemachine.app.ui.theme.MonitoringInactiveAccent

/** Small pill showing whether background monitoring is currently running — spec's "Monitoring Status". */
@Composable
fun MonitoringStatusBadge(isActive: Boolean, modifier: Modifier = Modifier) {
    val color = if (isActive) MonitoringActiveAccent else MonitoringInactiveAccent
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(8.dp).clip(CircleShape).background(color)
        )
        Text(
            text = if (isActive) "Monitoring Active" else "Monitoring Off",
            style = MaterialTheme.typography.labelLarge,
            color = color,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}
