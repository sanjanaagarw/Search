'''
Code written by Suhas Jagadish (jagadiss@iu.edu) and Sanjana Agarwal(sa14593@indiana.edu)
'''

import csv
import collections, re
from sklearn.neighbors import KNeighborsClassifier
from sklearn.neighbors import NearestNeighbors
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.metrics import classification_report
from sklearn.metrics import accuracy_score
from sklearn import svm
from collections import Counter
from scipy.sparse import csr_matrix, coo_matrix
from sklearn.model_selection import train_test_split
import math

wordCount1 = Counter()

documentMatrix = dict()
documentMatrix1 = dict()

specialCharacters = ['\n', '\t', ' ', '\r', ',', '', '!', '.', '?']

stopWords = ['a', 'about', 'above', 'after', 'again', 'against', 'all', 'am', 'an', 'and', 'any', 'are', 'arent', 'as',
             'at', 'because', 'be', 'been', 'before', 'being', 'below', 'between', 'both', 'but', 'by', 'cant',
             'cannot', 'couldnt', 'did', 'didnt', 'do', 'does', 'doesnt', 'doing', 'dont', 'down', 'during', 'each',
             'few', 'for', 'from', 'further', 'had', 'hadnt', 'has', 'hasnt', 'have', 'havent', 'having', 'he', 'hed',
             'hell', 'hes', 'her', 'hers', 'here', 'heres', 'herself', 'him', 'himself', 'his', 'how', 'hows', 'i',
             'id', 'ill', 'im', 'ive', 'if', 'in', 'into', 'is', 'isnt', 'it', 'its', 'itself', 'lets', 'more', 'me',
             'most', 'mustnt', 'my', 'myself', 'no', 'nor', 'not', 'of', 'off', 'on', 'once', 'only', 'or', 'other',
             'ought', 'our', 'ours', 'ourselves', 'out', 'over', 'own', 'same', 'shant', 'she', 'shell', 'shed', 'shes',
             'should', 'shouldnt', 'so', 'some', 'such', 'than', 'that', 'thats', 'the', 'their', 'theirs', 'them',
             'themselves', 'then', 'there', 'theres', 'they', 'theyd', 'theyll', 'theyre', 'theyve', 'this', 'those',
             'though', 'through', 'to', 'too', 'under', 'until', 'up', 'very', 'was', 'wasnt', 'we', 'wed', 'well',
             'were', 'weve', 'werent', 'what', 'whats', 'when', 'whens', 'where', 'wheres', 'which', 'while', 'who',
             'whos', 'whom', 'why', 'whys', 'with', 'wont', 'would', 'wouldnt', 'you', 'youd', 'youll', 'youre',
             'youve', 'your', 'yours', 'yourself', 'yourselves', 'font', 'html', 'table', 'br', 'will', 'img']


# with open('yelp_academic_dataset_review.csv','r') as file:
#    reader = csv.reader(file,delimiter = ',')
#    for i in range(2):
#        print(reader.next())

def evaluate_model(target_true, target_predicted):
    print(classification_report(target_true, target_predicted))
    print("The accuracy score is {:.2%}".format(accuracy_score(target_true, target_predicted)))


def createBOW(lines):
    c = 0
    for line in lines:
        # print("Line", line)
        line = line.lower().strip().replace(',', "").replace(".", "")
        word = line.split(' ')
        # print("word", word)
        cleanListOfWords(word)
        for w in word:
            # print("w is", w)
            # print("Beginning wordcount1 is", wordCount1)
            if w not in stopWords and w.isalpha() and w not in wordCount1:
                wordCount1[w] = c
                # print("wordCount1 in BOW", wordCount1)  # Total number of words
                c += 1
    # print("wordCount1 in BOW", wordCount1)


def createMatrix(lines, fileName, totalNoOfDocuments, kt, documentMatrix):
    tf = dict()
    wordCount = Counter()
    valCount = []
    documentMatrix[fileName] = []
    lines = lines.strip()
    word = lines.split(' ')
    # print("List of words", word)
    # print("Total number of documents", totalNoOfDocuments)

    '''Made a small change in the calculation of wordCount. Instead of counting all the words like the stopwords,
    counted only the words that we will need since, we need to use wordCount for tf'''
    for w in word:
        # print("w is", w)
        w = w.lower().strip().replace(',', "").replace(".", "")
        words = ''.join(e for e in w if e.isalnum())
        # print("WORDS", words)
        if w not in stopWords and w.isalpha():
            wordCount[words] += 1
            # print("wordCount[", words, "] ", wordCount[words])

    lengthOfDoc = len(wordCount)
    # print("Length of doc", lengthOfDoc)

    idf = dict()
    tfidf = dict()

    '''Calculating the TF-IDF of a particular sentence'''
    for word in wordCount.keys():
        # print("While tfidf word is", word)
        tf[word] = wordCount[word] / lengthOfDoc
        # print("tf of", word, " is", tf[word])
        if word not in kt.keys():
            idf[word] = math.log(totalNoOfDocuments / 1)
        else:
            idf[word] = math.log(1 + (totalNoOfDocuments / kt[word]))
        # print("idf of", word, " is", idf[word])
        tfidf[word] = tf[word] * idf[word]

    # print("TF is:", tf)
    # print(("IDF is", idf))
    # print("TF-IDF is", tfidf)

    for word in wordCount1:
        if word in wordCount.keys():
            valCount.append(tfidf[word])
            # print("valcount in if", valCount, "for", wordCount[word])
        else:
            valCount.append(0)
            # print("valcount in else", valCount, "for", wordCount[word])

    return documentMatrix[fileName].append(valCount)


def cleanListOfWords(listOfWords):
    setOfSPlChars = set(specialCharacters)
    for index, word in enumerate(listOfWords):
        listOfWords[index] = ''.join(e for e in word if e.isalnum())
        listOfWords[index] = ''.join([c for c in word if c not in setOfSPlChars])
    return listOfWords


def calculateKT(kt, X):
    """ Calculating the k(t) that is the total number of documents that a particular term appears in. """
    review = 1
    for w in wordCount1.keys():
        c = 1
        # print("w is", w)
        for line in X:
            # print("X is", X)
            # print("LINE", line)
            line = line.lower().strip().replace(',', "").replace(".", "")
            listOfWords = line.split(' ')
            cleanListOfWords(listOfWords)
            # print("listofwords", listOfWords)
            # print("w in this brace", w)
            if w not in stopWords and w.isalpha() and w in listOfWords:
                # print("*****Successfully added*****", w, "from the line", line)
                kt[w] = c
                # print("**kt is***", kt)
                c += 1
                # print("kt size", len(kt))
                # print("KT is", kt)
        # print("Review number in test", review)
        review += 1
    return kt


def createMatr(docMatrix, val, row, col):
    # print("Doc matrix", docMatrix)
    for k in docMatrix.keys():
        for i in range(len(docMatrix.get(k)[0])):
            # print("Len is", len(docMatrix.get(k)[0]))
            val.append(docMatrix.get(k)[0][i])
            # print("Val in k keys", val)  # count matrix.
            row.append(k)
            # print("row is", row)
            col.append(i)

    coo = coo_matrix((val, (row, col)))
    return coo


def train_model():
    cc = 0
    val = []
    row = []
    col = []
    createBOW(X_train)
    kt = dict()
    #print(wordCount1)
    # print("Calling from train")
    calculateKT(kt, X_train)
    # print("Kt in train", kt)
    for line in X_train:
        # print("Line in xtrain")
        # print(line)
        # print('cc', cc)
        totalNoOfDocuments = len(X_train)
        # print("Total number of docs", totalNoOfDocuments)
        createMatrix(line, cc, totalNoOfDocuments, kt, documentMatrix)
        cc += 1

    # print("On train model")
    # print(documentMatrix)
    coo = createMatr(documentMatrix, val, row, col)
    return csr_matrix(coo)


def test_model():
    cc = 0
    val = []
    row = []
    col = []

    kt = dict()
    # print("Calling from test")
    calculateKT(kt, X_test)
    # print("Kt in test", kt)
    for line in X_test:
        totalNoOfDocuments = len(X_train)
        createMatrix(line, cc, totalNoOfDocuments, kt, documentMatrix1)
        cc += 1

# print("On test model")
# print(documentMatrix1)
    coo = createMatr(documentMatrix1, val, row, col)
    return csr_matrix(coo)

with open('final_csv.csv') as csv_file:
    reader = csv.reader(csv_file, delimiter=",", quotechar='"')
    data = []
    target = []
    for row in reader:
        if row[0] and row[1]:
            data.append(row[0])
            target.append(row[1])

X_train, X_test, y_train, y_test = train_test_split(data, target, test_size=0.1, random_state=0)

# print("Xtrain")
# print(X_train)
# print("Xtest")
# print(X_test)


# knn
neigh = KNeighborsClassifier(n_neighbors=4)
neigh.fit(train_model(), y_train)
predicted = neigh.predict(test_model())
print(predicted)
evaluate_model(y_test, predicted)


# svm
clf = svm.SVC(decision_function_shape='ovo')
clf.fit(train_model(), y_train)
predicted1 = clf.predict(test_model())
print(predicted1)
evaluate_model(y_test, predicted1)


'''
#Bag of words representation
count_vectorizer = CountVectorizer()
data1 = count_vectorizer.fit_transform(data)
#print(count_vectorizer.get_feature_names())
#print(count_vectorizer.get_stop_words())

#print(data1.shape)
#print(count_vectorizer.vocabulary_.get(u'food'))
#print(count_vectorizer.vocabulary_.get(u'the'))

#Calculate tf-idf
tfidf_transformer = TfidfTransformer()
data_final = tfidf_transformer.fit_transform(data1)
#print(data_final)
#print(data1)
#print(data_final.shape)

#BOW and tf-idf for test data
data2 = count_vectorizer.transform(test_data)
data_testing = tfidf_transformer.transform(data2)
#print(data_testing)

#knn
neigh = KNeighborsClassifier(n_neighbors=1)
neigh.fit(data_final, target)
predicted = neigh.predict(data_testing)
print(predicted)
evaluate_model(test_target,predicted)

#svm
clf = svm.SVC(decision_function_shape='ovo')
clf.fit(data_final, target)
predicted1 = clf.predict(data_testing)
print(predicted1)
print(clf.decision_function(data_testing))
evaluate_model(test_target,predicted1)
'''