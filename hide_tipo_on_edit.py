import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Find the block containing the "TIPO" label and Segmented Control
pattern = r'(                // Tipo\n                Text\(\n                    text = "TIPO".*?\n                        \}\n                    \}\n                \})'
match = re.search(pattern, content, re.DOTALL)

if match:
    old_block = match.group(1)
    # Just wrap it in if (state.initialTransaction == null)
    new_block = '                if (state.initialTransaction == null) {\n'
    for line in old_block.split('\n'):
        if line:
            new_block += '    ' + line + '\n'
        else:
            new_block += '\n'
    new_block += '                }'
    
    content = content.replace(old_block, new_block)

    with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
        f.write(content)
else:
    print("Pattern not found!")
