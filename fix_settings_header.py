with open('app/src/main/java/co/privado/finly/ui/screens/settings/SettingsScreen.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.ui.text.TextStyle" not in content:
    content = content.replace("import androidx.compose.ui.text.font.FontWeight", "import androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.TextStyle")

old_header = """            Text(
                text = "CONFIGURACIÓN",
                style = TypographyEyebrow,
                color = ColorBrass,
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp, top = 24.dp)
            )"""

new_header = """            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)) {
                Text(
                    text = "CONFIGURACIÓN",
                    style = TypographyEyebrow,
                    color = ColorBrass,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Tus ajustes",
                    style = TextStyle(
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Medium,
                        fontSize = 26.sp,
                        color = ColorBone
                    )
                )
            }"""

content = content.replace(old_header, new_header)

with open('app/src/main/java/co/privado/finly/ui/screens/settings/SettingsScreen.kt', 'w') as f:
    f.write(content)
