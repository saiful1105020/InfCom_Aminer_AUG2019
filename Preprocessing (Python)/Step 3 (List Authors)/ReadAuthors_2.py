import json
import re
import os
import time
import retinasdk

LANG_EN_PATTERN = "^[a-zA-Z0-9$@$!%*:?&#^\-_\. +]+$"
pattern = re.compile(LANG_EN_PATTERN)
isascii = lambda x: len(x) == len(x.encode())

authorMap = {}
IN_DIR = "../../Dataset_Preprocessed/fixed_encoding_folder2/"
OUT_DIR = "../../Dataset_Preprocessed/"
writefilename = OUT_DIR+"authors_2.txt"

if not os.path.exists(IN_DIR):
    print("Input Directory Missing")
    exit(0)
                
auth_id = 1
f2 = open(writefilename,encoding='utf-8',mode='w')

for fid in range(59,155):
    print("File #",fid)
    filename = IN_DIR+"summary_fixed_"+str(fid)+".txt"
    #data file
    f = open(filename,encoding='utf-8',mode='r')
    
    for s in f:
        try:
            d = json.loads(s)
        except:
            continue
        
        #Summary tags
        try:
            n = len(d["keywords"])

            if n>0:
                authors = d["authors"]
                for auth in authors:
                    auth = auth['name']
                    auth = auth.replace('\n',' ').replace('\r','')
                    #print(auth)
                    if auth not in authorMap:
                        authorMap[auth] = auth_id
                        f2.write(auth+" : "+str(auth_id))
                        f2.write("\n")
                        auth_id = auth_id+1
                        
        except Exception as e:
            pass
    
    f.close()
f2.close()

print("Number of authors: "+str(auth_id-1))