import json
import re
import os
import time
import retinasdk
import nltk
from nltk import WordNetLemmatizer
from nltk.corpus import stopwords

LANG_EN_PATTERN = "^[a-zA-Z0-9$@$!%*:?&#^\-_\. +]+$"
pattern = re.compile(LANG_EN_PATTERN)
isascii = lambda x: len(x) == len(x.encode())

wordnet_lemmatizer = WordNetLemmatizer()
stop_words = set(stopwords.words('english'))
pos_filter_passed = lambda pos: (pos[:2] == 'NN' or pos[:2] == 'JJ' or pos == 'VBG')

#Given a keyword, join lemmatized words
def preprocess(keyword):

    # tokenize
    tokenized = nltk.word_tokenize(keyword)

    # remove stopwords
    filtered_sentence = [w for w in tokenized if not w in stop_words]

    # extract nouns, adjective, gerunds plus lemmatize
    lemmatized_words = ""
    for (word, pos) in nltk.pos_tag(filtered_sentence):
        if pos_filter_passed(pos):
            if pos == 'VBG':
                lemmatized_words = lemmatized_words+wordnet_lemmatizer.lemmatize(word, 'v')
            elif pos=='JJ':
                lemmatized_words = lemmatized_words+wordnet_lemmatizer.lemmatize(word, 'a')
            else:
                lemmatized_words = lemmatized_words+wordnet_lemmatizer.lemmatize(word, 'n')

    return lemmatized_words

keywordMap = {}
keywordFullMap = {}

global_index=0
total_files = [30,29,96]

for dir_index in range(0,3):
    DIR = "../../Dataset_Preprocessed/author_name_replaced"+str(dir_index)+"/"
                    
    for fid in range(global_index,global_index+total_files[dir_index]):
        print("File #",fid)
        filename = DIR+"summary_auth_id_"+str(fid)+".txt"
        writefilename = "../../Dataset_Preprocessed/keywords_"+str(fid)+".txt"
        #data file
        f = open(filename,encoding='utf-8',mode='r')
        #file containing list of keywords
        f2 = open(writefilename,encoding='utf-8',mode='w')

        for s in f:
            d = json.loads(s)

            #Get annotated keywords
            try:
                n = len(d["keywords"])
                for x in d["keywords"]:
                    try:
                        #Ignore non-ascii keywords
                        if isascii(x)==False:
                            continue

                        #Remove affects of whitespaces and uppercase letters
                        x = x.lower()
                        x_spaced = x

                        #regex = re.compile('[^a-z]')
                        #x = regex.sub('', x)
                        x = preprocess(x)
                        #Put keyword x into map        
                        if x in keywordMap:
                            keywordMap[x]=keywordMap[x]+1
                        else:
                            keywordFullMap[x] = x_spaced
                            keywordMap[x]=1
                    except Exception as e:
                        print(e)
            except Exception as e:
                pass
        
        #Write extracted keywords to file
        #Written incrementally just to handle unexpected interrupts or exceptions
        #The last file contains the final output
        for x in sorted(keywordMap, key=keywordMap.get, reverse=True):
                freq = keywordMap[x]
                if freq>2:
                    s = str(keywordFullMap[x])+" : "+str(keywordMap[x])+"\n"
                    f2.write(s)
        f.close()
        f2.close()
    global_index = global_index + total_files[dir_index]

global_index = global_index-1
os.rename("../../Dataset_Preprocessed/keywords_"+str(global_index)+".txt", "../../Dataset_Preprocessed/keywords.txt")

for i in range(0,global_index):
    os.remove("../../Dataset_Preprocessed/keywords_"+str(i)+".txt")