import json

with open('/home/walter/.gemini/antigravity-cli/brain/781087cf-ab4b-4af9-936d-7013071dfd22/.system_generated/logs/transcript_full.jsonl', 'r') as f:
    for line in f:
        data = json.loads(line)
        if data.get('step_index') == 36:
            content = data['content']
            lines = content.split('Output:\n')[1].split('\n')
            with open('restored2.kt', 'w') as out:
                for l in lines:
                    out.write(l.replace('\r', '') + '\n')
            break
