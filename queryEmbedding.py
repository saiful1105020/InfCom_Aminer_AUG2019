import json
import time
from distutils.command.config import config

import nltk
import numpy
from gensim.models import Word2Vec
from nltk import WordNetLemmatizer
from nltk.corpus import stopwords
from scipy import spatial
import os
import sys
import multiprocessing

queryTerms = []

#Check File ID
num_keywords = (int) (sys.argv[1])
mapping_threshold = (float)(sys.argv[2])
numberOfTerms = (int)(sys.argv[3])
if(len(sys.argv)!=numberOfTerms+4):
    print("Number of arguments does not match")
    exit(0)

#Assign ID to each keyword
keywordMap = {}
keywords = []
keywordId = 1
INPUT_FILE = "../Dataset_Preprocessed/keywords_corrected.txt"

f = open(INPUT_FILE,encoding='utf-8',mode='r')

for s in f:
    try:
        keyword = s.split(":")[0].strip()
        keywordMap[keyword] = keywordId
        keywordId = keywordId+1
        keywords.append(keyword)
        if(keywordId>num_keywords):
            break
    except Exception as e:
        print(e)
f.close()

#print("Stage 1: Keyword ID Mapping Done")

#Match relevant keywords to a term
topK = 10
TOP_N = 15

pos_filter_passed = lambda pos: (pos[:2] == 'NN' or pos[:2] == 'JJ' or pos == 'VBG')

wordnet_lemmatizer = WordNetLemmatizer()
stop_words = set(stopwords.words('english'))

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

def writeFile(filename,content):
    fw = open(filename, encoding='utf-8', mode='w')
    fw.write(json.dumps(content))
    fw.flush()
    fw.close()

if __name__ == '__main__':

    #For each keyword, we keep the top similar words vector in a map
    keyword_topNWords = {}

    #print(len(keywords))
    #print("Top Keywords Loaded")

    #Load pre-trained word2vec model
    model = Word2Vec.load("word2vec_lower_tokenized_v3.model")
    for keyword in keywords:
        keyword_topNWords[keyword] = getSimilarVector(keyword, k=TOP_N)
    #print("Word2Vec Model Loaded")

    similar_keywords = {}
    for queryTerm in sys.argv[4:]:
        #print(queryTerm+"\n-------\n")
        input_term = queryTerm.strip()
        input_topNWords = getSimilarVector(input_term, k=TOP_N)

        similarity = {}
        for y in keywords:
            similarity[y] = nTopSimilarity(input_topNWords, keyword_topNWords[y])
        sim_key_ids = []
        for item in sorted(similarity.items(), key=lambda kv: float('-inf')
        if numpy.math.isnan(kv[1]) else kv[1], reverse=True)[0:topK]:
            if (item[1] > mapping_threshold):
                kId = keywordMap[item[0]]
                #print(item[0])
                sim_key_ids.append(kId)
            else:
                break
        similar_keywords[queryTerm]=sim_key_ids

    #print("\n----------\n----------\n")
    writeFile("query_response.txt",similar_keywords)
