with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    lines = f.readlines()

# The bad part is from line 348 to 370
# Let's find the `fun FinlyPickerField` end, which is at line 347.
# Then `AccountPicker` starts around line 372.
# We can just delete lines 348 to 371.

start_del = -1
end_del = -1
for i, line in enumerate(lines):
    if line.strip() == ',' and 'text = {' in lines[i+1]:
        if start_del == -1:
            start_del = i
    if line.strip() == '@Composable' and 'private fun AccountPicker' in lines[i+1]:
        end_del = i
        break

if start_del != -1 and end_del != -1:
    del lines[start_del:end_del]

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.writelines(lines)
