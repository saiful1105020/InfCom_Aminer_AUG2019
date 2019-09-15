import time
import multiprocessing as mp
from multiprocessing import Pool
import json
import nltk
import numpy
from gensim.models import Word2Vec
from nltk import WordNetLemmatizer
from nltk.corpus import stopwords
from scipy import spatial
import os
import sys

pos_filter_passed = lambda pos: (pos[:2] == 'NN' or pos[:2] == 'JJ' or pos == 'VBG')
model = Word2Vec.load("word2vec_lower_tokenized_v3.model")
DIR = "../../Dataset_Preprocessed/author_name_replaced/"
OUT_DIR = "../../Dataset_Preprocessed/Augmented_20K/"
allJsonObjects = []
file_index = 0
keywordMap = {}
INPUT_FILE = "../../Dataset_Preprocessed/keywords_corrected.txt"
similarKeywordsMap = {}
wordnet_lemmatizer = WordNetLemmatizer()
stop_words = set(stopwords.words('english'))
# For each keyword, we keep the top similar words vector in a map
keyword_topNWords = {}
keywords = []
#Keyword Matching Parametres
mapping_threshold = 0.30
num_keywords = 20000
topK = 10
TOP_N = 10

#Given a keyword, find lemmatized words
def preprocess(keyword):

    # tokenize
    tokenized = nltk.word_tokenize(keyword)

    # remove stopwords
    filtered_sentence = [w for w in tokenized if not w in stop_words]

    # extract nouns, adjective, gerunds plus lemmatize
    lemmatized_words = []
    for (word, pos) in nltk.pos_tag(filtered_sentence):
        if pos_filter_passed(pos):
            if pos == 'VBG':
                lemmatized_words.append(wordnet_lemmatizer.lemmatize(word, 'v'))
            elif pos=='JJ':
                lemmatized_words.append(wordnet_lemmatizer.lemmatize(word, 'a'))
            else:
                lemmatized_words.append(wordnet_lemmatizer.lemmatize(word, 'n'))

    return lemmatized_words

#print(preprocess("a very intelligent automated database design"))
#exit(0)

#Given a keyword, find N_TOP most similar words
def getSimilarVector(keyword, k = TOP_N):

    # tokenize
    tokenized = nltk.word_tokenize(keyword)

    # remove stopwords
    filtered_sentence = [w for w in tokenized if not w in stop_words]

    # extract nouns, adjective, gerunds plus lemmatize
    lemmatized_words = []
    for (word, pos) in nltk.pos_tag(filtered_sentence):
        if pos_filter_passed(pos):
            if pos == 'VBG':
                lemmatized_words.append(wordnet_lemmatizer.lemmatize(word, 'v'))
            elif pos=='JJ':
                lemmatized_words.append(wordnet_lemmatizer.lemmatize(word, 'a'))
            else:
                lemmatized_words.append(wordnet_lemmatizer.lemmatize(word, 'n'))

    similar_words = []
    try:
        similar_words = model.wv.most_similar(positive = lemmatized_words, topn=k)
    except:
        #The given keyword is not found in the dictionary
        pass
    return similar_words

#Given a keyword like "machine learning", outputs the embedded vector
def getVectorForKeyword(keyword):
    # tokenize
    tokenized = nltk.word_tokenize(keyword)

    # remove stopwords
    filtered_sentence = [w for w in tokenized if not w in stop_words]

    # extract nouns and adjective plus lemmatize
    lemmatized_words = []
    for (word, pos) in nltk.pos_tag(filtered_sentence):
        if pos_filter_passed(pos):
            if pos=='VBG':
                lemmatized_words.append(wordnet_lemmatizer.lemmatize(word, 'v'))
            elif pos=='JJ':
                lemmatized_words.append(wordnet_lemmatizer.lemmatize(word, 'a'))
            else:
                lemmatized_words.append(wordnet_lemmatizer.lemmatize(word, 'n'))

    X_vec = []
    for x in lemmatized_words:
        try:
            X_vec.append(model.wv.get_vector(x))
        except:
            pass

    if(len(X_vec)==0):
        #Either the words are not in vocabulary or they are not detected as nouns or adjectives
        #How to improve this part?
        return numpy.zeros(model.wv.vector_size)

    X_vec = numpy.mean(X_vec, axis=0)

    return  X_vec

def nTopSimilarity2(X_vec, Y_vec):
    if (len(X_vec) == 0 or len(Y_vec) == 0):
        return 0.0

    sum_sim = 0
    for x in X_vec:
        max_sim = 0
        vec1 = model.wv.get_vector(x[0])
        for y in Y_vec:
            vec2 = model.wv.get_vector(y[0])
            sim = 1-spatial.distance.cosine(vec1,vec2)
            if sim>max_sim:
                max_sim = sim
        sum_sim = sum_sim+max_sim

    for y in Y_vec:
        max_sim = 0
        vec1 = model.wv.get_vector(y[0])
        for x in X_vec:
            vec2 = model.wv.get_vector(x[0])
            sim = 1-spatial.distance.cosine(vec1,vec2)
            if sim>max_sim:
                max_sim = sim
        sum_sim = sum_sim+max_sim

    return sum_sim/(len(X_vec)+len(Y_vec))

#Given two vectors containing words. Calculate similarity
def nTopSimilarity(X_vec, Y_vec):
    #We need a dictionary which covers all the words of X_vec and Y_vec
    dict = {}
    #An index is assigned to each word in the dictionary
    index = 0

    #Build the combined dictionary
    for x in X_vec:
        if x[0] not in dict:
            dict[x[0]] = index
            index = index + 1
    for y in Y_vec:
        if y[0] not in dict:
            dict[y[0]] = index
            index = index + 1

    #Initialize the vectors which will be used to calculate cosine similarity
    #vec1 corresponds to X_vec, vec2 to Y_vec
    vec1 = [0.0]*len(dict)
    vec2 = [0.0]*len(dict)

    for x in X_vec:
        vec1[dict[x[0]]] = x[1]
    for y in Y_vec:
        vec2[dict[y[0]]] = y[1]

    result = 1 - spatial.distance.cosine(vec1, vec2)
    return result

def process_article(s,q,keyword_topNWords,count):
    #print("Processing article")
    #print(s)

    d = json.loads(s)
    data = {}
    data["id"] = d["id"]
    data["authors"] = d["authors"]
    data["citations"] = d["citations"]
    similar_keywords = []

    #print("process_article: "+str(data["id"]))
    # Get annotated keywords
    for x in d["keywords"]:
        try:
            input_term = x.strip()

            input_topNWords = getSimilarVector(input_term, k=TOP_N)
            #print(input_topNWords)
            similarity = {}
            #print(len(keywords))
            for y in keyword_topNWords.keys():
                similarity[y] = nTopSimilarity(input_topNWords, keyword_topNWords[y])
                #print("similarity: "+str(similarity[y]))

            #print("Processing Done")
            for item in sorted(similarity.items(), key=lambda kv: float('-inf')
            if numpy.math.isnan(kv[1]) else kv[1], reverse=True)[0:topK]:
                if (item[1] > mapping_threshold):
                    kId = item[0]
                    if kId not in similar_keywords:
                        similar_keywords.append(kId)
                    else:
                        break
        except Exception as e:
            print("Exception in Pool Function: "+e)

    data["keywords"] = json.dumps(similar_keywords)
    #json_data = json.dumps(data)
    #print(json_data)
    q.append(data)
    if count%100==0:
        print(count)

def writeFile(fn,q):
    print("WRITE FILE STARTED")
    with open(fn, 'w') as f:
        for item in q:
            json_data = json.dumps(item)
            f.write(json_data)
            f.write("\n")
            f.flush()
    f.close()

q = []

if __name__ == '__main__':
    if not os.path.exists(OUT_DIR):
        os.makedirs(OUT_DIR)
    #Read Keywords
    keywordId = 1
    f = open(INPUT_FILE, encoding='utf-8', mode='r')
    for s in f:
        try:
            keyword = s.split(":")[0].strip()
            keywordMap[keyword] = keywordId
            keywordId = keywordId + 1
        except Exception as e:
            print(e)
    f.close()
    print("Stage 2: Keyword ID Mapping Done")

    # List of top keywords
    i = 0
    for x in keywordMap.keys():
        if (i == num_keywords):
            break
        keywords.append(x)
        i = i + 1

    print(len(keywords))
    print("Stage 3: Top Keywords Loaded")

    for keyword in keywords:
        keyword_topNWords[keywordMap[keyword]] = getSimilarVector(keyword, k=TOP_N)
    print("Stage 4: Word Embedding Done")

    while (file_index <= 154):
        print("File #", file_index)
        filename = DIR + "summary_auth_id_" + str(file_index) + ".txt"
        writefilename = OUT_DIR + "summary_all_id_" + str(file_index) + ".txt"

        f = open(filename, encoding='utf-8', mode='r')
        starttime = time.time()

        with mp.Manager() as manager:
            q = manager.list()
            pool = Pool(processes=4)

            #writer = pool.apply_async(writeFile, (writefilename,q))
            count=1
            for s in f:
                proc = pool.apply_async(process_article,(s,q,keyword_topNWords,count))
                count = count+1
            pool.close()
            pool.join()
            q = list(q)

        print('Processing File#'+str(file_index)+' took {} seconds'.format(time.time() - starttime))
        writeFile(writefilename,q)
        f.close()

        file_index = file_index + 1

    print("Finished successfully.")