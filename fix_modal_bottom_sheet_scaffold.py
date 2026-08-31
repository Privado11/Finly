import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Let's inspect the imports needed
imports = """import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.SheetValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalConfiguration
"""
if 'import androidx.compose.material3.BottomSheetScaffold' not in content:
    content = content.replace('import androidx.compose.material3.ModalBottomSheet', imports + 'import androidx.compose.material3.ModalBottomSheet')

# I will replace CategoryPicker completely as a test. If it works, I'll do AccountPicker too.
