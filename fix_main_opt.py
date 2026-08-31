with open('app/src/main/java/co/privado/finly/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('class MainActivity', '@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\nclass MainActivity')

with open('app/src/main/java/co/privado/finly/MainActivity.kt', 'w') as f:
    f.write(content)
