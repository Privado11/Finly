import re

with open('app/src/main/java/co/privado/finly/ui/screens/history/HistoryScreen.kt', 'r') as f:
    content = f.read()

# Replace the text element with a Row
old_header = """                                Text(
                                    text = dateHeader,
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        color = ColorSlate
                                    ),
                                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
                                )"""
new_header = """                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateHeader,
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            color = ColorSlate
                                        )
                                    )
                                    val dailyTotal = transactions.sumOf { if (it.type == TransactionType.income) it.amount else -it.amount }
                                    val prefix = if (dailyTotal > 0) "+" else if (dailyTotal < 0) "−" else ""
                                    val color = if (dailyTotal > 0) ColorMoss else if (dailyTotal < 0) ColorClay else ColorSlate
                                    Text(
                                        text = "$prefix${formatMoney(kotlin.math.abs(dailyTotal))}",
                                        style = TextStyle(
                                            fontFamily = IbmPlexMono,
                                            fontSize = 13.sp,
                                            color = color
                                        )
                                    )
                                }"""
content = content.replace(old_header, new_header)

with open('app/src/main/java/co/privado/finly/ui/screens/history/HistoryScreen.kt', 'w') as f:
    f.write(content)
