import json

with open('/home/walter/.gemini/antigravity-cli/brain/781087cf-ab4b-4af9-936d-7013071dfd22/.system_generated/logs/transcript_full.jsonl', 'r') as f:
    for line in f:
        data = json.loads(line)
        if data.get('step_index') == 86:
            content = data['content']
            # The output has "Created At... \nOutput:\n" followed by `cat -n` output
            lines = content.split('Output:\n')[1].split('\n')
            with open('restored.kt', 'w') as out:
                for l in lines:
                    if l.strip() == '': continue
                    # remove line numbers like `     1\t`
                    try:
                        clean_line = l.split('\t', 1)[1]
                        # Fix carriage returns
                        clean_line = clean_line.replace('\r', '')
                        out.write(clean_line + '\n')
                    except IndexError:
                        pass
            break
