package co.privado.finly.ui.navigation

import androidx.lifecycle.ViewModel
import co.privado.finly.ui.state.TransactionUpdateNotifier
import co.privado.finly.ui.state.GlobalMessageNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val transactionUpdateNotifier: TransactionUpdateNotifier,
    val globalMessageNotifier: GlobalMessageNotifier
) : ViewModel()
