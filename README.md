# SKYNET
We are the future 


SG Carpark rate API
import urllib
url = 'https://data.gov.sg/api/action/datastore_search?resource_id=e2468b11-6cac-42e4-8891-145c4fc1cba2&limit=5&q=title:jones'
fileobj = urllib.urlopen(url)
print fileobj.read()
