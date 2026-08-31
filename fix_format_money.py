import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('co.privado.finly.util.formatMoney', 'formatMoney')

if 'fun formatMoney(' not in content:
    content += """
private fun formatMoney(value: Double): String = "$" + java.text.NumberFormat.getNumberInstance(
    java.util.Locale("es", "CO")
).format(value)
"""

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
