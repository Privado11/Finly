import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Fix Account to AccountBalance
content = content.replace('mutableStateOf<Account?>', 'mutableStateOf<AccountBalance?>')
content = content.replace('List<Account>', 'List<AccountBalance>')
content = content.replace('select: (Account) -> Unit', 'select: (AccountBalance) -> Unit')

# Fix unresolved reference `name` and `id` for AccountBalance
# Wait, AccountBalance has `name` and `id` right? Let's check.
# Yes, it has id: String, name: String.

# Let's fix the CategoryPicker compiler errors from before I reverted.
# The error was: 
# e: ...TransactionsScreen.kt:488:21 @Composable invocations can only happen from the context of a @Composable function
# This happened because I inserted Box and Text inside LazyColumn block!

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
