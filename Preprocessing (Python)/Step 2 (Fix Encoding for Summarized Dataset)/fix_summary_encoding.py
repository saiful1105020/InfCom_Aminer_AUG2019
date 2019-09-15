from ftfy import fix_encoding
import os

file_count = [30,29,96]
for i in range(0,3):
    DIR_NAME="../../Dataset_Preprocessed/folder"+str(i)+"\\"
    OUT_DIR_NAME = "../../Dataset_Preprocessed/fixed_encoding_folder" + str(i) + "\\"
    print(DIR_NAME+"\n")

    if not os.path.exists(DIR_NAME):
        print("Input Directory Missing")
        exit(0)
    if not os.path.exists(OUT_DIR_NAME):
        os.makedirs(OUT_DIR_NAME)

    count = 0
    for j in range(0,i):
        count = count + file_count[j]
    
    for j in range(0,file_count[i]):
        FILE_NAME = "summary_"+str(count+j)+".txt"
        FIXED_FILE_NAME = "summary_fixed_"+str(count+j)+".txt"
        print(FILE_NAME+"\n")

        f = open(DIR_NAME+FILE_NAME,mode='r',encoding='utf-8')
        fw = open(OUT_DIR_NAME+FIXED_FILE_NAME,mode='w',encoding='utf-8')

        for s in f:
            s = fix_encoding(s)
            fw.write(s)
    
        f.close()
        fw.close()
    

#f = open("authors_all.txt",'r',encoding='utf-8')
#fw = open("authors_encoding_fixed.txt",'w',encoding='utf-8')
#for s in f:
#    s = fix_encoding(s)
#    fw.write(s)
    
#f.close()
#fw.close()
