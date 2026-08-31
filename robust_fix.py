lines = []
with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if '// Destino (si es transferencia)' in line:
        skip = True
        continue
    
    if skip:
        if '}' in line and '// Categoría' in lines[i+2]:
            skip = False
        continue
    
    if 'viewModel.save(amount, type, account?.id, destination?.id, category?.id, merchant, onSaved)' in line:
        line = line.replace('destination?.id, ', '')
    
    if 'var destination by remember' in line:
        continue
        
    if 'TransactionType.transfer to "Transferencia",' in line:
        continue
        
    if 'destination = null' in line:
        continue
        
    if 'TransactionType.transfer -> "Ej. Transferencia' in line:
        continue

    new_lines.append(line)

content = "".join(new_lines)
with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
