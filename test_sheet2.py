import re

with open('app/src/main/java/co/privado/finly/MainActivity.kt', 'r') as f:
    content = f.read()

if 'dumpSheetState()' not in content:
    content = content.replace('super.onCreate(savedInstanceState)', 'super.onCreate(savedInstanceState)\n        co.privado.finly.dumpSheetState()')

with open('app/src/main/java/co/privado/finly/MainActivity.kt', 'w') as f:
    f.write(content)
