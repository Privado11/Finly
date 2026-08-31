import re

with open('app/src/main/java/co/privado/finly/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('@androidx.compose.material3.ExperimentalMaterial3Api', '@androidx.compose.material3.ExperimentalMaterial3Api\n@androidx.annotation.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)')

with open('app/src/main/java/co/privado/finly/MainActivity.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/co/privado/finly/ui/navigation/AppNavGraph.kt', 'r') as f:
    nav_content = f.read()
    
nav_content = nav_content.replace('@androidx.compose.material3.ExperimentalMaterial3Api', '@androidx.compose.material3.ExperimentalMaterial3Api\n@androidx.annotation.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)')

with open('app/src/main/java/co/privado/finly/ui/navigation/AppNavGraph.kt', 'w') as f:
    f.write(nav_content)

with open('app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt', 'r') as f:
    main_content = f.read()

main_content = main_content.replace('@androidx.compose.material3.ExperimentalMaterial3Api', '@androidx.compose.material3.ExperimentalMaterial3Api\n@androidx.annotation.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)')

with open('app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt', 'w') as f:
    f.write(main_content)
