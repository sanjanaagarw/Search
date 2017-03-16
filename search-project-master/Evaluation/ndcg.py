import utilities as utils
import numpy as np
import random
import os

""" Reference from https://gist.github.com/bwhite/3726239 for ndcg_at_k and dcg_at_k
Code written by Sanjana Agarwal (sa14593@indiana.edu)
"""

ndcgSum = 0.0
listLength = 0


def printRanking(RankDict, i):
    for key, value in RankDict.items():
        ndcgDict = dict()
        ndcgList = dict()
        r = []
        list = RankDict.get(key)
        if i == 1:
            print('Recommendations of neighborhood ' + str(RankDict.get(key)) + ' for user with ID ' + str(key))
        elif i == 2:
            print('Recommendations of categories' + str(RankDict.get(key)) + ' for user with ID ' + str(key))
        random.shuffle(list)
        for item in list:
            r.append(item[1])

        for neighborhood in list:
            ndcg = ndcg_at_k(r, len(list), method=0)
            ndcgDict[neighborhood[0]] = ndcg
            ndcgList[key] = ndcg
        # print("Outside list is", ndcgList)
        if i == 1:
            printToFile('NeighborhoodNDCG.txt', ndcgList, list)
        elif i == 2:
            printToFile('CategoryNDCG.txt', ndcgList, list)
    print("NDCG Mean is", ndcgSum / listLength)


def printToFile(filename, ndcgList, list):
    global ndcgSum, listLength
    # print("Length ", len(ndcgList))
    # print("NDCG SCORES FOR USERS NEIGHBORHOODS")
    file = open(filename, 'a+')
    # with open('neighborhoodNDCG.txt') as file:
    for key, value in ndcgList.items():
        file.write(str(key) + "\t" + str(list) + "\t" + str(value))
        file.write(os.linesep)
        ndcgSum += value
        listLength += 1
        # print("Sum is", ndcgSum)


def dcg_at_k(r, k, method=0):
    r = np.asfarray(r)[:k]
    if r.size:
        if method == 0:
            return r[0] + np.sum(r[1:] / np.log2(np.arange(2, r.size + 1)))
        elif method == 1:
            return np.sum(r / np.log2(np.arange(2, r.size + 2)))
        else:
            raise ValueError('method must be 0 or 1.')
    return 0.


def ndcg_at_k(r, k, method=0):
    dcg_max = dcg_at_k(sorted(r, reverse=True), k, method)
    if not dcg_max:
        return 0.
    return dcg_at_k(r, k, method) / dcg_max


if __name__ == '__main__':
    neighborhoodRanking = utils.getModelFile('userNeighborhood')
    printRanking(neighborhoodRanking, 1)
    categoryRanking = utils.getModelFile('userCategories')
    printRanking(categoryRanking, 2)
