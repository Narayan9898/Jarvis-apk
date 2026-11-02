package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkflowItem
import com.example.ui.JarvisViewModel
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun WorkflowsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val workflows by viewModel.workflows.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AUTOMATION PROTOCOLS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Automated Workflows (${workflows.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(workflows, key = { it.id }) { workflow ->
                    WorkflowCard(
                        workflow = workflow,
                        onRun = { viewModel.runWorkflow(workflow) },
                        onToggle = { viewModel.toggleWorkflow(workflow) },
                        onDelete = { viewModel.deleteWorkflow(workflow) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_workflow_fab"),
            containerColor = JarvisCyan,
            contentColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Automation")
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var triggerPhrase by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create Automation Protocol", color = JarvisTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Routine Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = triggerPhrase,
                        onValueChange = { triggerPhrase = it },
                        label = { Text("Voice Trigger (e.g. 'Work Mode')") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && triggerPhrase.isNotBlank()) {
                            val defaultSteps = """[{"actionId":"summarize_notifications","parameters":{}},{"actionId":"search_web","parameters":{"query":"top tech news"}}]"""
                            viewModel.addWorkflow(title, triggerPhrase, description, defaultSteps)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
                ) {
                    Text("Save Protocol")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = JarvisCyan)
                }
            },
            containerColor = JarvisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun WorkflowCard(
    workflow: WorkflowItem,
    onRun: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Protocol",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = workflow.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                }

                Switch(
                    checked = workflow.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .background(JarvisSurfaceVariant, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "TRIGGER: \"${workflow.triggerPhrase}\"",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = workflow.description,
                fontSize = 12.sp,
                color = JarvisTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRun,
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Execute Routine", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = JarvisError.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
