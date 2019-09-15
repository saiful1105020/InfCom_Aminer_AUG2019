import json
import re
import time
import retinasdk

authors = {}
#Assign new IDs
auth_id = 1

output_filename = "../../Dataset_Preprocessed/"+"authors_fixed_all.txt"
fw = open(output_filename,encoding='utf-8',mode='w')

for i in range (0,3):
    print("File #"+str(i)+":\n")
    filename = "../../Dataset_Preprocessed/"+"authors_fixed_encoding_"+str(i)+".txt"
    f = open(filename,encoding='utf-8',mode='r')

    #the count variable is used for debugging
    count=0
    for s in f:
        count = count+1
        strs = s.split(" : ")
        auth = strs[0]
        try:
            aid = strs[1]
        except:
            #Error: Out of format
            print(count)
            print(s)
            time.sleep(5)
        if auth not in authors:
            authors[auth] = auth_id
            fw.write(auth+" : "+str(auth_id)+" : \n")
            auth_id = auth_id + 1

    f.close()
fw.close()
print("Total number of authors: "+str(auth_id-1))
