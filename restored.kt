package co.privado.finly.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.domain.model.AccountBalance
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import co.privado.finly.util.toIcon
import co.privado.finly.domain.model.Category
import co.privado.finly.domain.model.TransactionType
import co.privado.finly.ui.theme.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun TransactionsScreen(initialType: TransactionType = TransactionType.expense, onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var amount by rememberSaveable { mutableStateOf("") }
