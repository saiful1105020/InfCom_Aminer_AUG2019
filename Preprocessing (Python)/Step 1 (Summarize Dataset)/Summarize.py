import json
import re
import time
import retinasdk
import os

#Define Constants
LANG_EN_PATTERN = "^[a-zA-Z0-9$@$!%*:?&#^\-_\. +]+$"
#LANG_EN_PATTERN_2 ="[^\u0000-\u0080]+"
pattern = re.compile(LANG_EN_PATTERN)
START_FILE_IDX = 59
END_FILE_IDX = 154
DIR = "../../Dataset_Main/aminer_papers_2/"
OUT_DIR = "../../Dataset_Preprocessed/folder2/"

isascii = lambda x: len(x) == len(x.encode())

#Directory Validity Check
if not os.path.exists(DIR):
    print("Input Directory Missing")
    exit(0)
if not os.path.exists(OUT_DIR):
    os.makedirs(OUT_DIR)

#keywordMap = {}

#docs: count of extracted papers                
docs = 0

for fid in range(START_FILE_IDX,END_FILE_IDX+1):
    print("File #",fid)
    filename = DIR+"aminer_papers_"+str(fid)+".txt"
    writefilename = OUT_DIR+"summary_"+str(fid)+".txt"
    #data file
    f = open(filename,encoding='utf-8',mode='r')
    #file containing summary
    f2 = open(writefilename,encoding='utf-8',mode='w')

    for s in f:
        d = json.loads(s)
        data = {}

        #Discard papers of other languages
        if not(pattern.match(d["title"])):
            continue
        if d["lang"]!="en":
            continue

        #Summary tags
        try:
            n = len(d["keywords"])

            if n>0:
                docs = docs + 1
                data["id"] = d["id"]
                data["authors"] = d["authors"]
                data["keywords"] = d["keywords"]
                data["citations"] = d["n_citation"]

                json_data = json.dumps(data)
                f2.write(json_data)
                f2.write("\n")
        except Exception as e:
            pass

    s = str(fid)+":: total Documents with keywords: "+str(docs)+"\n"
    print(s)
    
    f.close()
    f2.close()
