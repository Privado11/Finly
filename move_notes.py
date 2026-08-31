with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove the note from Card 2
old_card2 = """                        DetailRow("Origen de datos", tx.source.label())
                        
                        if (!tx.description.isNullOrBlank()) {
                            Divider()
                            DetailRow("Nota", tx.description)
                        }
                    }"""
                    
new_card2 = """                        DetailRow("Origen de datos", tx.source.label())
                    }"""

content = content.replace(old_card2, new_card2)

# 2. Add the note as a new Card right after Card 2 and before Card 3 (Raw notification)
old_card3 = """                    // Card 3: Raw notification (if present)"""

new_card_note = """                    // Notas del usuario
                    if (!tx.description.isNullOrBlank()) {
                        DetailCard {
                            Text(
                                text = "NOTA",
                                style = TypographyEyebrow,
                                color = ColorBrass,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ColorSurfaceHi)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = tx.description,
                                    style = TextStyle(fontFamily = Inter, fontSize = 14.sp, color = ColorBone, lineHeight = 20.sp)
                                )
                            }
                        }
                    }

                    // Card 3: Raw notification (if present)"""

content = content.replace(old_card3, new_card_note)

with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailScreen.kt', 'w') as f:
    f.write(content)
