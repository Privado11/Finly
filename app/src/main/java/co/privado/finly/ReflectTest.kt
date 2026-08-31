package co.privado.finly

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.memberFunctions

@OptIn(ExperimentalMaterial3Api::class)
fun dumpSheetState() {
    println("==== SheetState Methods ====")
    SheetState::class.memberFunctions.forEach { println(it.name) }
    println("==== SheetState Properties ====")
    SheetState::class.memberProperties.forEach { println(it.name) }
}
