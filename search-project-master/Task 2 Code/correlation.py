'''
Code written by Supreeth Suryaprakash (skeragod@indiana.edu) | Formulation done by rkanchib and skeragod
'''

from utilities import *
from scipy.stats import spearmanr
import os

def findSpearmanRank(userMap, valueList):
	spearmanDict = dict()

	for idx, item in enumerate(valueList):
		for gdx, item1 in enumerate(valueList):
			if ((idx+1, gdx+1) not in spearmanDict.keys() and (gdx+1, idx+1) not in spearmanDict.keys()) and idx != gdx:
				if not userMap.get(idx+1)[0] in spearmanDict:
					spearmanDict[userMap.get(idx + 1)[0]] = []
				else:
					spearmanDict[userMap.get(idx+1)[0]].append((userMap.get(gdx)[0], abs(spearmanr(item, item1)[0])))

	iterator = 0
	for key, val in spearmanDict.items():
		val.sort(key=lambda tup: tup[1])
		possibleCombination = [x[0] for x in val[-25:]]
		iterator += 1
		a = set(userMap[iterator][1])
		b = set(possibleCombination)
		friends = b.intersection(a)
		for eachPossibleFan in val[-25:]:
			if eachPossibleFan[0] in friends:
				del eachPossibleFan #Since they are already friends.

	return spearmanDict


def printPossibleFollowers(dictionary, userIndexMap):
	somedict = dict()
	anotherdict = dict()
	for key, val in dictionary[0].items():
		print 'Recommended Friends for user ' + dictionary[1].get(key) + ' with ID ' + key + ' are'
		if userIndexMap[key][0] != '':
			array = [i[0] for i in val[:100]]
			arrayLen = int(len(userIndexMap[key]) * ( 3000 / (650000*1.0)) * 100)
			for eachVal in userIndexMap[key]:
				if eachVal in array:
					if key in somedict:
						somedict[key] += 1
					else:
						somedict[key] = 1
						anotherdict[key] = arrayLen
		for eachItem in val:
			print dictionary[1].get(eachItem[0]) + ', ',
		print '\n'
	return somedict, anotherdict


if __name__ == '__main__':
	list1 = list()
	#if not os.path.isfile('correlatedModelFile'):
	content = readFile('Data/yelp_academic_dataset_user.csv')
	userDict, userIndexMap, valList, userIDName, userFriendsMap = createUserDict(content)
	if not os.path.isfile('correlatedModelFile'):
		spearManDict = findSpearmanRank(userIndexMap, valList)
		makeModelFile('correlatedModelFile', (spearManDict, userIDName))
	userAndFollowers = getModelFile('correlatedModelFile')
	somedict, arrayLen = printPossibleFollowers(userAndFollowers, userFriendsMap)
	'''
	Evaluation Part
	'''
	for key, val in somedict.items():
		totalNumber = arrayLen[key] if arrayLen[key] > 0 else 1
		predicted = somedict[key]
		list1.append((predicted/(totalNumber * 1.0)) * 100)
	print 'Average Accuracy for top 100 Users is', calcAcc(list1)

