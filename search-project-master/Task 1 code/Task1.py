'''
Code written by Suhas Jagadish (jagadiss@iu.edu)
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
from sklearn.feature_selection import VarianceThreshold
from sklearn.decomposition import PCA
from sklearn.feature_selection import SelectKBest
from sklearn.feature_selection import mutual_info_classif


wordCount1 = Counter()

documentMatrix = dict()
documentMatrix1 = dict()

specialCharacters = ['\n','\t', ' ', '\r', ',', '']

stopWords = ['a','about', 'above', 'after', 'again', 'against', 'all', 'am', 'an', 'and', 'any', 'are', 'arent', 'as',
			 'at', 'because', 'be', 'been', 'before', 'being', 'below', 'between', 'both', 'but', 'by', 'cant', 'cannot',
			 'couldnt', 'did', 'didnt', 'do', 'does','doesnt', 'doing', 'dont', 'down', 'during', 'each', 'few', 'for', 'from',
			 'further', 'had', 'hadnt', 'has', 'hasnt', 'have', 'havent', 'having', 'he', 'hed','hell','hes', 'her', 'hers', 'here',
			 'heres', 'herself', 'him', 'himself', 'his', 'how', 'hows', 'i', 'id', 'ill', 'im', 'ive', 'if', 'in', 'into', 'is', 'isnt',
			 'it', 'its', 'itself', 'lets', 'more', 'me', 'most', 'mustnt', 'my', 'myself', 'no', 'nor', 'not', 'of', 'off', 'on', 'once',
			 'only', 'or', 'other', 'ought', 'our', 'ours', 'ourselves', 'out', 'over', 'own', 'same', 'shant', 'she', 'shell','shed', 'shes',
			 'should', 'shouldnt', 'so', 'some', 'such', 'than', 'that', 'thats', 'the', 'their', 'theirs', 'them', 'themselves', 'then',
			 'there', 'theres', 'they', 'theyd', 'theyll','theyre', 'theyve', 'this', 'those', 'though', 'through', 'to', 'too', 'under',
			 'until', 'up', 'very', 'was', 'wasnt', 'we', 'wed', 'well', 'were', 'weve', 'werent', 'what', 'whats', 'when', 'whens', 'where',
			 'wheres', 'which', 'while', 'who', 'whos', 'whom', 'why', 'whys', 'with', 'wont', 'would', 'wouldnt', 'you', 'youd', 'youll',
			 'youre', 'youve', 'your', 'yours', 'yourself', 'yourselves', 'font', 'html', 'table', 'br', 'will', 'img']

#with open('yelp_academic_dataset_review.csv','r') as file:
#    reader = csv.reader(file,delimiter = ',')
#    for i in range(2):
#        print(reader.next())

def evaluate_model(target_true,target_predicted):
    print(classification_report(target_true,target_predicted))
    print("The accuracy score is {:.2%}".format(accuracy_score(target_true,target_predicted)))

def createBOW(lines):
    c = 0
    for line in lines:
        word = line.split(' ')
        for w in word:
            w = w.lower()
            w = ''.join(e for e in w if e.isalnum())
            if w not in stopWords and w.isalpha() and w not in wordCount1:
                wordCount1[w] = c
                c += 1

def createMatrix(lines, fileName):
    wordCount = Counter()
    valCount = []
    documentMatrix[fileName] = []
    word = lines.split(' ')
    for w in word:
        w = w.lower()
        words = ''.join(e for e in w if e.isalnum())
        wordCount[words] += 1

    for word in wordCount1:
        if word in wordCount.keys():
            valCount.append(wordCount[word])
        else:
            valCount.append(0)

    return documentMatrix[fileName].append(valCount)

def createMatrix1(lines, fileName):
    wordCount = Counter()
    valCount = []
    documentMatrix1[fileName] = []
    word = lines.split(' ')
    for w in word:
        w = w.lower()
        words = ''.join(e for e in w if e.isalnum())
        wordCount[words] += 1

    for word in wordCount1:
        if word in wordCount.keys():
            valCount.append(wordCount[word])
        else:
            valCount.append(0)

    return documentMatrix1[fileName].append(valCount)

def train_model():
    cc=0
    val = []
    row = []
    col = []

    createBOW(X_train)

    for line in X_train:
        createMatrix(line, cc)
        cc+=1

    #print(documentMatrix)

    for k in documentMatrix.keys():
        for i in range(len(documentMatrix.get(k)[0])):
            val.append(documentMatrix.get(k)[0][i])
            row.append(k)
            col.append(i)

    coo = coo_matrix((val,(row,col)))
    return csr_matrix(coo)

def test_model():
    cc=0
    val = []
    row = []
    col = []

    #createBOW(X_test)

    for line in X_test:
        createMatrix1(line, cc)
        cc+=1

    #print(documentMatrix1)

    for k in documentMatrix1.keys():
        for i in range(len(documentMatrix1.get(k)[0])):
            val.append(documentMatrix1.get(k)[0][i])
            row.append(k)
            col.append(i)

    coo = coo_matrix((val,(row,col)))
    return csr_matrix(coo)


with open('final_csv.csv') as csv_file:
    reader = csv.reader(csv_file, delimiter=",", quotechar='"')
    data = []
    target = []
    for row in reader:
        if row[0] and row[1]:
            data.append(row[0])
            target.append(row[1])

X_train, X_test, y_train, y_test = train_test_split(data, target, test_size=0.3, random_state=0)

tfidf_transformer = TfidfTransformer()

print("Considering the entire feature space")

#knn
neigh = KNeighborsClassifier(n_neighbors=5)
data_final = tfidf_transformer.fit_transform(train_model())
#print(data_final.shape[1])
k = int(0.75 * data_final.shape[1])
#X_new = SelectKBest(mutual_info_classif, k).fit_transform(data_final, y_train)
#print(X_new.shape)
neigh.fit(data_final, y_train)
data_testing = tfidf_transformer.transform(test_model())
#print(data_testing.shape)

#X_new1 = SelectKBest(mutual_info_classif, k).fit_transform(data_testing, y_test)
#print(X_new1.shape)

predicted = neigh.predict(data_testing)
evaluate_model(y_test,predicted)

#svm - use LinearSVC which implements “one-vs-the-rest” multi-class strategy
clf = svm.LinearSVC()
clf.fit(data_final, y_train)
predicted1 = clf.predict(data_testing)
#print(predicted1)
evaluate_model(y_test,predicted1)

print("After dimensinality reduction")

#knn
neigh = KNeighborsClassifier(n_neighbors=5)
data_final = tfidf_transformer.fit_transform(train_model())
#print(data_final.shape)
X_new = SelectKBest(mutual_info_classif, k).fit_transform(data_final, y_train)
#print(X_new.shape)
neigh.fit(X_new, y_train)
data_testing = tfidf_transformer.transform(test_model())
#print(data_testing.shape)
X_new1 = SelectKBest(mutual_info_classif, k).fit_transform(data_testing, y_test)
#print(X_new1.shape)

predicted = neigh.predict(X_new1)
evaluate_model(y_test,predicted)

#svm - use LinearSVC which implements “one-vs-the-rest” multi-class strategy
clf = svm.LinearSVC()
clf.fit(X_new, y_train)
predicted1 = clf.predict(X_new1)
#print(predicted1)
evaluate_model(y_test,predicted1)

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