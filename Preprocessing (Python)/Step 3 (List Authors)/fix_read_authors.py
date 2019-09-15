from ftfy import fix_encoding

for i in range(0,3):
    FILE_NAME="../../Dataset_Preprocessed/"+"authors_"+str(i)+".txt";
    FIXED_FILE_NAME = "../../Dataset_Preprocessed/"+"authors_fixed_encoding_"+str(i)+".txt";
    f = open(FILE_NAME,mode='r',encoding='utf-8')
    fw = open(FIXED_FILE_NAME,mode='w',encoding='utf-8')

    for s in f:
        s = fix_encoding(s)
        fw.write(s)
    
    f.close()
    fw.close()
