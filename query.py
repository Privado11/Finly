import urllib.request
import json
url = "https://html.duckduckgo.com/html/?q=jetpack+compose+ModalBottomSheet+custom+peek+height"
req = urllib.request.Request(
    url, 
    data=None, 
    headers={
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }
)
try:
    response = urllib.request.urlopen(req)
    html = response.read().decode('utf-8')
    print("Fetched")
    # basic parse
    import re
    snippets = re.findall(r'<a class="result__snippet[^>]*>(.*?)</a>', html, re.IGNORECASE | re.DOTALL)
    for s in snippets:
        print(re.sub('<[^<]+>', '', s).strip())
except Exception as e:
    print(e)
