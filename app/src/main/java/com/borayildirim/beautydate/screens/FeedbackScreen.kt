package com.borayildirim.beautydate.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borayildirim.beautydate.viewmodels.FeedbackViewModel

/**
 * Feedback screen for user to send feedback
 * Features form validation and submission handling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onNavigateBack: () -> Unit,
    feedbackViewModel: FeedbackViewModel = hiltViewModel()
) {
    val uiState by feedbackViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Geri Bildirim",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Feedback,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Görüşleriniz Bizim İçin Önemli",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            text = "Uygulamamızı daha iyi hale getirmek için fikirlerinizi paylaşın",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Subject Field
            OutlinedTextField(
                value = uiState.subject,
                onValueChange = feedbackViewModel::updateSubject,
                label = { Text("Konu") },
                placeholder = { Text("Geri bildirim konusunu yazın") },
                isError = !uiState.isSubjectValid,
                supportingText = uiState.subjectError?.let { error ->
                    { Text(error) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )
            
            // Message Field
            OutlinedTextField(
                value = uiState.message,
                onValueChange = feedbackViewModel::updateMessage,
                label = { Text("Mesaj") },
                placeholder = { Text("Detaylı mesajınızı yazın...") },
                isError = !uiState.isMessageValid,
                supportingText = {
                    uiState.messageError?.let { error ->
                        Text(error)
                    } ?: Text(
                        text = "${uiState.messageCharacterCount}/300 karakter",
                        color = if (uiState.remainingCharacters < 50) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                minLines = 4,
                maxLines = 8,
                enabled = !uiState.isLoading
            )
            
            // Submit Button
            Button(
                onClick = { 
                    if (uiState.canSubmit) {
                        feedbackViewModel.showConfirmDialog()
                    } else {
                        feedbackViewModel.validateForm()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gönderiliyor...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gönder",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            
            // Clear Button
            if (uiState.subject.isNotEmpty() || uiState.message.isNotEmpty()) {
                OutlinedButton(
                    onClick = { feedbackViewModel.clearForm() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text("Temizle")
                }
            }
            
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "📝 Not",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Geri bildiriminiz direkt olarak geliştirici ekibine iletilir. " +
                                "Kişisel bilgileriniz güvende tutulur ve gizlilik politikamız çerçevesinde işlenir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
    
    // Confirmation Dialog
    if (uiState.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { feedbackViewModel.hideConfirmDialog() },
            title = {
                Text("Geri Bildirim Gönder")
            },
            text = {
                Text("Geri bildiriminizi göndermek istediğinizden emin misiniz?")
            },
            confirmButton = {
                TextButton(
                    onClick = { feedbackViewModel.submitFeedback() }
                ) {
                    Text("Gönder")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { feedbackViewModel.hideConfirmDialog() }
                ) {
                    Text("İptal")
                }
            }
        )
    }
    
    // Success message
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            feedbackViewModel.clearSuccess()
            // Navigate back to "Diğer" menu after successful submission
            onNavigateBack()
        }
    }
    
    // Error message
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            feedbackViewModel.clearError()
        }
    }
} 