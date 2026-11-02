package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionPlan
import com.example.data.model.RiskLevel
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarning

@Composable
fun ActionConfirmationCard(
    plan: ActionPlan,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val riskColor = when (plan.riskLevel) {
        RiskLevel.LOW -> JarvisCyan
        RiskLevel.MEDIUM -> JarvisWarning
        RiskLevel.HIGH -> JarvisError
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, riskColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .testTag("action_confirmation_card"),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Risk Warning",
                        tint = riskColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACTION VERIFICATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor,
                        letterSpacing = 1.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(riskColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${plan.riskLevel.name} RISK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = plan.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = JarvisTextPrimary
            )

            Text(
                text = plan.description,
                fontSize = 13.sp,
                color = JarvisTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )

            if (plan.parameters.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "EXTRACTED PARAMETERS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JarvisCyan,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    plan.parameters.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = key, fontSize = 12.sp, color = JarvisTextSecondary)
                            Text(
                                text = value,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = JarvisTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cancel_action_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisError)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reject")
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("confirm_action_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirm", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Authorize", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
