with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailScreen.kt', 'r') as f:
    content = f.read()

# I will replace `amountColor` assignment
content = content.replace(
    'val amountColor = if (isInc) ColorMoss else if (isExp) ColorClay else ColorBone',
    'val amountColor = if (isInc) ColorMoss else ColorBone'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailScreen.kt', 'w') as f:
    f.write(content)
