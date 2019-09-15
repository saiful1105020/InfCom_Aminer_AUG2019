import json
import re
import time
import retinasdk
import os

f = open("../../Dataset_Preprocessed/authors_fixed_all.txt",'r',encoding='utf-8')
name_to_id = {}
for s in f:
    try:
        temp = s.split(" : ")
        name_to_id[temp[0]] = temp[1]
        #print(temp[1])
    except:
        print(s+">>"+temp[0])
        pass
print(len(name_to_id))
f.close()

docs=0
file_count = [30,29,96]
for i in range(0,3):
    IN_DIR = "../../Dataset_Preprocessed/fixed_encoding_folder"+str(i)+"/"
    OUT_DIR = "../../Dataset_Preprocessed/author_name_replaced"+str(i)+"/"

    if not os.path.exists(IN_DIR):
        print("Input Directory Missing")
        exit(0)
    if not os.path.exists(OUT_DIR):
        os.makedirs(OUT_DIR)

    count = 0
    for j in range(0, i):
        count = count + file_count[j]

    for j in range(0, file_count[i]):
        FILE_NAME = "summary_fixed_" + str(count + j) + ".txt"
        OUT_FILE_NAME = "summary_auth_id_"+str(count + j) + ".txt"
        print(FILE_NAME + "\n")

        f = open(IN_DIR + FILE_NAME, mode='r', encoding='utf-8')
        fw = open(OUT_DIR + OUT_FILE_NAME, mode='w', encoding='utf-8')

        for s in f:
            d = json.loads(s)
            data = {}

            try:
                n = len(d["keywords"])
                if n > 0:
                    docs = docs + 1

                    authors = []
                    for auth in d["authors"]:
                        auth = auth["name"]
                        #print(name_to_id[auth])
                        authors.append(int(name_to_id[auth]))

                    data["id"] = d["id"]
                    data["authors"] = json.dumps(authors)
                    data["keywords"] = d["keywords"]
                    data["citations"] = d["citations"]

                    json_data = json.dumps(data)

                    fw.write(json_data)
                    fw.write("\n")
                    fw.flush()
            except Exception as e:
                pass

        f.close()
        fw.close()
