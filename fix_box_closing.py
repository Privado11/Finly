import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# I replaced `) { /* empty content */ }`
# I should have replaced it with `) { /* empty content */ }\n        }` to close the Box!

content = content.replace(') { /* empty content */ }', ') { /* empty content */ }\n        }')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
