with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    lines = f.readlines()

# The compiler error says:
# e: file:///.../TransactionsScreen.kt:432:1 Syntax error: Expecting a top level declaration.
# e: file:///.../TransactionsScreen.kt:506:1 Syntax error: Expecting a top level declaration.
# We will just empty out those lines. (0-indexed, so lines 431 and 505).

lines[431] = '\n'
lines[505] = '\n'

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.writelines(lines)
