import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Add imports
if 'import androidx.compose.foundation.layout.statusBarsPadding' not in content:
    content = content.replace('import androidx.compose.foundation.layout.padding', 'import androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.statusBarsPadding')


# For AccountPicker and CategoryPicker
# We need to wrap BottomSheetScaffold in a Box and move the scrim.

def patch_picker(content, picker_name):
    # This is tricky with regex because of nested braces.
    # Let's just find the Dialog block and replace it.
    
    # We can split by `BottomSheetScaffold(`
    # But it's easier to just do text replacement for the specific pattern.
    
    # Find:
    #     androidx.compose.ui.window.Dialog(...) {
    #         androidx.compose.material3.BottomSheetScaffold(
    #             scaffoldState = scaffoldState, sheetPeekHeight = peek, sheetContainerColor = ColorInk, containerColor = Color.Transparent,
    
    # Replace with:
    #     androidx.compose.ui.window.Dialog(...) {
    #         Box(Modifier.fillMaxSize()) {
    #             Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { dismiss() })
    #             androidx.compose.material3.BottomSheetScaffold(
    #                 modifier = Modifier.fillMaxSize().statusBarsPadding(),
    #                 scaffoldState = scaffoldState, sheetPeekHeight = peek, sheetContainerColor = ColorInk, containerColor = Color.Transparent,
    
    search_pattern = r'androidx\.compose\.ui\.window\.Dialog\([^)]*\)\s*\{\s*androidx\.compose\.material3\.BottomSheetScaffold\(\s*scaffoldState = scaffoldState,\s*sheetPeekHeight = peek,\s*sheetContainerColor = ColorInk,\s*containerColor = Color\.Transparent,'
    
    def repl(m):
        dialog_decl = m.group(0).split('{')[0] + "{"
        return dialog_decl + """
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { dismiss() })
            androidx.compose.material3.BottomSheetScaffold(
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
                scaffoldState = scaffoldState, sheetPeekHeight = peek, sheetContainerColor = ColorInk, containerColor = Color.Transparent,"""
                
    content = re.sub(search_pattern, repl, content)
    
    # Now we need to remove the old scrim from inside BottomSheetScaffold's content block.
    # It looks like this:
    # ) { Box(Modifier.fillMaxSize().clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { dismiss() }) { Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) } }
    
    old_scrim = r'\) \{ Box\(Modifier\.fillMaxSize\(\)\.clickable\(interactionSource = remember \{ androidx\.compose\.foundation\.interaction\.MutableInteractionSource\(\) \}, indication = null\) \{ dismiss\(\) \}\) \{ Box\(Modifier\.fillMaxSize\(\)\.background\(Color\.Black\.copy\(alpha = 0\.5f\)\)\) \} \}'
    content = re.sub(old_scrim, r') { /* empty content */ }', content)
    
    # My first regex had `) { Box(Modifier...` but I also have `) { Box(Modifier.fillMaxSize().clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { dismiss() }) { Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) } }`
    return content

content = patch_picker(content, "AccountPicker")
# category picker uses the same pattern, so `re.sub` should have caught both since I didn't restrict it to 1 occurrence.
# Let's verify by printing how many times we replaced it.

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
