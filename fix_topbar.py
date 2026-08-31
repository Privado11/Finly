import re

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'r') as f:
    content = f.read()

old_topbar = """                item {
                    // Topbar
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = "FINLY",
                            style = TypographyEyebrow,
                            color = ColorBrass,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Tu resumen",
                            style = TextStyle(
                                fontFamily = Fraunces,
                                fontWeight = FontWeight.Medium,
                                fontSize = 26.sp,
                                color = ColorBone
                            )
                        )
                    }
                }"""

new_topbar = """                item {
                    // Topbar
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "FINLY",
                                style = TypographyEyebrow,
                                color = ColorBrass,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Tu resumen",
                                style = TextStyle(
                                    fontFamily = Fraunces,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 26.sp,
                                    color = ColorBone
                                )
                            )
                        }
                        IconButton(onClick = { isVisible = !isVisible }) {
                            Icon(
                                imageVector = if (isVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                contentDescription = "Mostrar balances",
                                tint = ColorSlate
                            )
                        }
                    }
                }"""

if old_topbar in content:
    content = content.replace(old_topbar, new_topbar)
else:
    print("Could not find old topbar block!")

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'w') as f:
    f.write(content)
