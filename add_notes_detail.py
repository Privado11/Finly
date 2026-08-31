with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailScreen.kt', 'r') as f:
    content = f.read()

old_details = """                        Divider()
                        DetailRow("Origen de datos", tx.source.label())
                    }"""

new_details = """                        Divider()
                        DetailRow("Origen de datos", tx.source.label())
                        
                        if (!tx.description.isNullOrBlank()) {
                            Divider()
                            DetailRow("Nota", tx.description)
                        }
                    }"""
content = content.replace(old_details, new_details)

with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailScreen.kt', 'w') as f:
    f.write(content)
