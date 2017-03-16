'''
This code is written by Raghuveer Krishnamurthy Kanchibail | Formulation done by rkanchib and skeragod
'''

from collections import defaultdict
from collections import Counter
from utilities import *
import operator
import random

def emissionProbabilty(category,review,categoryReview):
    c = categoryReview[category][review]
    if c == 0:
        c = 0.0000001
    tC = sum(categoryReview[category].values())
    if tC == 0:
        return 0.000001
    eP = float(c)/tC
    return eP

def transitionProbability(curr,prev,prevCategory):
    cT = prevCategory[curr][prev]
    tC = sum(prevCategory[curr].values())
    if cT == 0:
        return 0.0000001
    return float(cT)/tC

def returnMaxinDict(dict1):
    if bool(dict1):
        return max(dict1.iteritems(), key=operator.itemgetter(1))[0],max(dict1.iteritems(), key=operator.itemgetter(1))[1]
    else:
        return 'No Info'


def hmm(d):
    category = Counter()
    categoryReview = defaultdict(Counter)
    prevCategory = defaultdict(Counter)
    initialProb = {}
    totalReviews = ['good','bad']
    #categoryList = ['American','Breakfast & Brunch','Italian','Chinese','Coffee & Tea', 'Mediterranean']
    iP = float(1)/len(categoriesMain)

    l = []
    for i,j in d.iteritems():
        for x in j:
            categoryReview[x[0]][x[1]] += 1
            category[x[0]] += 1
            l.append(x[1])
            if x[0] not in initialProb:
                initialProb[x[0]] = iP

    for i in range(1,len(l)):
        prevCategory[l[i]][l[i-1]] += 1

    alpha = defaultdict(Counter)
    good = defaultdict(dict)
    bad = defaultdict(dict)
    for i,j in d.iteritems():
        for c in totalReviews:
            alpha[0][c] = initialProb[j[0][0]] * emissionProbabilty(j[0][0],c, categoryReview)
        for x in range(1,len(j)+1):
            for c1 in totalReviews:
                for c2 in totalReviews:
                    alpha[x][c1] += alpha[x-1][c2] * transitionProbability(c2,c1, prevCategory) * emissionProbabilty(j[x-1][0],c1,categoryReview)
            for c3 in totalReviews:
                for c4 in categoriesMain:
                    if c3 == 'good':
                        good[c3][c4] = emissionProbabilty(c4,c3,categoryReview) * alpha[x-1][c3]
                    else:
                        bad[c3][c4] = emissionProbabilty(c4, c3, categoryReview) * alpha[x-1][c3]

    return good, bad

def calcAccuracy(results):
    correct = 0
    for p,q in results.iteritems():
        if q[0] == q[1]:
            correct += 1
    return float(correct)/len(results) * 100


def calcTop2Accuracy(results):
    correct = 0
    for p,q in results.iteritems():
        if q[0] in q[1]:
            correct += 1
    return float(correct)/len(results) * 100

if __name__ == '__main__':

    d = defaultdict(list)
    result = defaultdict()
    result2 = defaultdict()
    lst = []
    with open('HMM_extract_final.csv','r') as f:
        for i in f:
            i = i.strip('\n')
            lst.append(i)

    for i in lst:
        i = i.split(',')
        i[0] = i[0].strip()
        i[1] = i[1].strip()
        i[2] = i[2].strip()
        d[i[0]].append((i[1],i[2]))

    counter1 = 0
    counter2 = 0
    acc = 0
    for i,j in d.iteritems():
        diction = {}
        if len(j) == 1:
            actual = j[-1][0]
            prob = 0
            if j[0][1] == 'good':
                counter1 += 1
                counter2 += 1
                print 'The user with id:', i, 'is likely to give his next good review for', actual, 'with probability 1.00000000'
            else:
                counter2+=1
                prob = float(len(categoriesMain) - 1) / len(categoriesMain)
                prob = float(prob)/len(categoriesMain)
                r = random.randrange(0,len(categoriesMain))
                randomCategory = categoriesMain[r]
                print 'The user with id:', i, 'is likely to give his next good review for', randomCategory,'with', prob,' probability'
        else:
            actual = j[-1][0]
            diction[i] = j[:-1]
            g,b = hmm(diction)
            gCategory,v1 = returnMaxinDict(g['good'])

            top2Category = sorted(g['good'], key=g['good'].get, reverse=True)[:2]

            result[i] = (actual,gCategory)

            result2[i] = (actual,top2Category)


            if actual == gCategory:
                counter1 += 1
                counter2 += 1
            else:
                counter2 += 1
            print 'The user with id:',i,'is likely to give his next good review for',gCategory,'with', '{0:.10f}'.format(v1),'probability'

    acc = calcAccuracy(result)
    acc2 = calcTop2Accuracy(result2)
    print ''
    print 'Accuracy of the predicted value to be the correct value:',acc
    print 'Accuracy of actual value being in top 2 predicted value:', acc2