import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Remove the broken imports at the very beginning if they exist before package
if content.startswith('import androidx.compose'):
    # remove the first two lines which I added
    lines = content.split('\n')
    # find the package line
    pkg_index = next(i for i, line in enumerate(lines) if line.startswith('package'))
    
    # move everything before package to after package
    before_pkg = lines[:pkg_index]
    rest = lines[pkg_index:]
    
    new_lines = [rest[0]] + before_pkg + rest[1:]
    content = '\n'.join(new_lines)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
