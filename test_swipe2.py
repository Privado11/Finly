import re

with open('app/src/main/java/co/privado/finly/MainActivity.kt', 'r') as f:
    content = f.read()

content = re.sub(r'@androidx.compose.material3.ExperimentalMaterial3Api\n@androidx.compose.runtime.Composable\nfun TestSwipe\(\).*?\{.*?\}', '', content, flags=re.DOTALL)
content = content.replace('co.privado.finly.dumpSheetState()', '')

with open('app/src/main/java/co/privado/finly/MainActivity.kt', 'w') as f:
    f.write(content)
