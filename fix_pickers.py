with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# 1. Update AccountPicker
account_picker_end = """            androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                items(items) { acc ->
                    Row(Modifier.fillMaxWidth().clickable { select(acc) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(ColorSurfaceHi), contentAlignment = Alignment.Center) {
                            Icon(androidx.compose.material.icons.Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = co.privado.finly.ui.theme.ColorBrass, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(acc.name, fontFamily = co.privado.finly.ui.theme.Inter, fontSize = 16.sp, color = co.privado.finly.ui.theme.ColorBone)
                            Text(formatMoney(acc.balance), fontFamily = co.privado.finly.ui.theme.IbmPlexMono, fontSize = 13.sp, color = co.privado.finly.ui.theme.ColorSlate)
                        }
                    }
                }
            }
        }
    }
}"""
new_account_picker_end = """            androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                items(items) { acc ->
                    Row(Modifier.fillMaxWidth().clickable { select(acc) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(ColorSurfaceHi), contentAlignment = Alignment.Center) {
                            Icon(androidx.compose.material.icons.Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = co.privado.finly.ui.theme.ColorBrass, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(acc.name, fontFamily = co.privado.finly.ui.theme.Inter, fontSize = 16.sp, color = co.privado.finly.ui.theme.ColorBone)
                            Text(formatMoney(acc.balance), fontFamily = co.privado.finly.ui.theme.IbmPlexMono, fontSize = 13.sp, color = co.privado.finly.ui.theme.ColorSlate)
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(1.dp).background(ColorHair))
            Text(
                text = "+ Agregar cuenta",
                style = TextStyle(color = ColorBone, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.fillMaxWidth().clickable { }.padding(bottom = 24.dp)
            )
        }
    }
}"""
content = content.replace(account_picker_end, new_account_picker_end)

# 2. Update CategoryPicker
category_picker_bs = """    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = dismiss,
        containerColor = co.privado.finly.ui.theme.ColorInk,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {"""
new_category_picker_bs = """    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = dismiss,
        containerColor = co.privado.finly.ui.theme.ColorInk,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false),
        modifier = Modifier.fillMaxHeight(0.6f)
    ) {"""
content = content.replace(category_picker_bs, new_category_picker_bs)

category_picker_end = """                        }
                    }
                }
            }
        }
    }
}"""
new_category_picker_end = """                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(1.dp).background(ColorHair))
            Text(
                text = "+ Agregar categoría",
                style = TextStyle(color = ColorBone, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.fillMaxWidth().clickable { }.padding(bottom = 24.dp)
            )
        }
    }
}"""
content = content.replace(category_picker_end, new_category_picker_end)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
