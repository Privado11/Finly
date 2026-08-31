import sys

with open("app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt", "r") as f:
    content = f.read()

target = """Text("Asistente Inteligente", fontFamily = co.privado.finly.ui.theme.Inter, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = co.privado.finly.ui.theme.ColorBone)
                            Spacer(Modifier.height(4.dp))
                            Text("Dicta, toma una foto o escribe", fontFamily = co.privado.finly.ui.theme.Inter, fontSize = 14.sp, color = co.privado.finly.ui.theme.ColorSlate)"""

replacement = """Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Asistente Inteligente", fontFamily = co.privado.finly.ui.theme.Inter, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = co.privado.finly.ui.theme.ColorBone)
                                Spacer(Modifier.width(8.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF2A323C)) {
                                    Text("PRÓXIMAMENTE", fontFamily = co.privado.finly.ui.theme.Inter, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = co.privado.finly.ui.theme.ColorSlate, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Dicta, toma una foto o escribe", fontFamily = co.privado.finly.ui.theme.Inter, fontSize = 14.sp, color = co.privado.finly.ui.theme.ColorSlate)"""

content = content.replace(target, replacement)

with open("app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt", "w") as f:
    f.write(content)
