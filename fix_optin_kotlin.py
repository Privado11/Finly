import re
import glob

for filename in glob.glob('app/src/main/java/**/*.kt', recursive=True):
    with open(filename, 'r') as f:
        content = f.read()
    
    if 'androidx.annotation.OptIn' in content:
        content = content.replace('androidx.annotation.OptIn', 'kotlin.OptIn')
        with open(filename, 'w') as f:
            f.write(content)
