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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Psychology
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
import com.example.data.model.MemoryItem
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
fun MemoryScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val memories by viewModel.memories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

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
                        text = "COGNITIVE ARCHIVES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Personal Memory (${memories.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                }

                if (memories.isNotEmpty()) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All Memories",
                            tint = JarvisTextSecondary
                        )
                    }
                }
            }

            if (memories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "No memories",
                            tint = JarvisCyan.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Learned Preferences",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Teach JARVIS facts by saying 'Remember that my hometown is Hyderabad' or tap + to add facts manually.",
                            fontSize = 13.sp,
                            color = JarvisTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(memories, key = { it.id }) { item ->
                        MemoryCard(item = item, onDelete = { viewModel.deleteMemory(item) })
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_memory_fab"),
            containerColor = JarvisCyan,
            contentColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Memory")
        }
    }

    if (showAddDialog) {
        var keyName by remember { mutableStateOf("") }
        var valueStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Store Memory Item", color = JarvisTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = keyName,
                        onValueChange = { keyName = it },
                        label = { Text("Fact Key (e.g., favorite_music, office_address)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { valueStr = it },
                        label = { Text("Value / Details") },
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
                        if (keyName.isNotBlank() && valueStr.isNotBlank()) {
                            viewModel.saveMemory(keyName.trim(), valueStr.trim())
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
                ) {
                    Text("Store Fact")
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

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Purge All Stored Memories?", color = JarvisTextPrimary) },
            text = { Text("This will delete all saved user facts and preferences permanently.", color = JarvisTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearMemories()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = JarvisError)
                ) {
                    Text("Purge All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = JarvisCyan)
                }
            },
            containerColor = JarvisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun MemoryCard(
    item: MemoryItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .background(JarvisSurfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.keyName.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.valueStr,
                    fontSize = 14.sp,
                    color = JarvisTextPrimary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Memory",
                    tint = JarvisError.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
