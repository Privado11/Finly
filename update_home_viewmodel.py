import re

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeViewModel.kt', 'r') as f:
    content = f.read()

# Add import
content = content.replace(
    'import co.privado.finly.domain.repository.AccountRepository',
    'import co.privado.finly.domain.repository.AccountRepository\nimport co.privado.finly.data.local.SessionDataStore'
)

# Inject DataStore
content = content.replace(
    'private val transactionUpdateNotifier: TransactionUpdateNotifier',
    'private val transactionUpdateNotifier: TransactionUpdateNotifier,\n    private val sessionDataStore: SessionDataStore'
)

# Add flows
insert_flows = """
    val isBalancesVisible: StateFlow<Boolean> = sessionDataStore.balancesVisibleState
    
    fun toggleBalancesVisibility() = viewModelScope.launch {
        sessionDataStore.setBalancesVisible(!sessionDataStore.balancesVisibleState.value)
    }

    init {"""
content = content.replace('    init {', insert_flows.lstrip('\n'))

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeViewModel.kt', 'w') as f:
    f.write(content)
