INPUT_FILE = "../../Dataset_Preprocessed/keywords.txt"
OUTPUT_FILE = "../../Dataset_Preprocessed/keywords_corrected.txt"
f = open(INPUT_FILE,encoding='utf-8',mode='r')
fw = open(OUTPUT_FILE,encoding='utf-8',mode='w')
for s in f:
    try:
        #keyword = s.split(":")[0].strip()
        substrs = s.split(":")
        #print(substrs)
        sub_len = len(substrs)
        #print(substrs[sub_len - 1])

        if(sub_len==2):
            fw.write(s)
        else:
            freq = substrs[sub_len-1]
            #print(freq)
            #exit(0)
            keyword = ""
            for i in(0,sub_len-1):
                keyword=keyword+" "+substrs[i].strip()
            keyword = keyword.strip()
            fw.write(keyword+" : "+freq)

    except Exception as e:
        print(e)
f.close()
fw.close()

