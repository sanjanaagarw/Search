'''
Code written by Supreeth Suryaprakash (skeragod@indiana.edu) | Formulation done by rkanchib and skeragod
'''

from utilities import *
import os

def makeDict(fileName):
	businessDict = dict()
	businesses = readFile(fileName)
	for business in businesses:
		neighborhood = ''.join(list(business[4].translate(None, "[]'")))
		if len(neighborhood) < 1:
			neighborhood = business[3]
		businessDict[business[0]] = [business[1], business[2].translate(None, ','), business[3], neighborhood]
	return businessDict


def userNeighborhood(reviews, businessDict):
	userDict = dict()
	for review in reviews:
		if not review[3][1:].translate(None, "\\'") in userDict:
			userDict[review[3][1:].translate(None, "\\'")] = dict()

		if review[0][1:].translate(None, "\\'") in businessDict:
			if businessDict[review[0][1:].translate(None, "\\'")][3] in userDict[review[3][1:].translate(None, "\\'")]:
				if businessDict[review[0][1:].translate(None, "\\'")][3] == businessDict[review[0][1:].translate(None, "\\'")][2]:
					downtownCity = businessDict[review[0][1:].translate(None, "\\'")][3] + ',' +businessDict[review[0][1:].translate(None, "\\'")][0]
				else:
					downtownCity = businessDict[review[0][1:].translate(None, "\\'")][3] + ',' + businessDict[review[0][1:].translate(None, "\\'")][2] +','+businessDict[review[0][1:].translate(None, "\\'")][0]
				userDict[review[3][1:].translate(None, "\\'")][downtownCity] += 1
			else:
				if businessDict[review[0][1:].translate(None, "\\'")][3] == businessDict[review[0][1:].translate(None, "\\'")][2]:
					downtownCity = businessDict[review[0][1:].translate(None, "\\'")][3] + ',' + businessDict[review[0][1:].translate(None, "\\'")][0]
				else:
					downtownCity = businessDict[review[0][1:].translate(None, "\\'")][3] + ',' + businessDict[review[0][1:].translate(None, "\\'")][2] + ',' + businessDict[review[0][1:].translate(None, "\\'")][0]
				if not downtownCity in userDict[review[3][1:].translate(None, "\\'")]:
					userDict[review[3][1:].translate(None, "\\'")][downtownCity] = 1
				else:
					userDict[review[3][1:].translate(None, "\\'")][downtownCity] += 1

	return userDict

def printUserPrefNeighborhood(userDict, fileName):
	file = open(fileName, 'w')
	for key, val in userDict.items():
		file.write(key + ',' + returnMaxinDict(val))
		file.write(os.linesep)
		print key + " - " + returnMaxinDict(val)

if __name__ == '__main__':
	completeBusInfo = readFile('../Data/yelp_academic_dataset_business.csv')
	if not os.path.isfile('businessFile.csv'):
		printBusinessNecessaryInfo(completeBusInfo, 'businessFile.csv')
	businessInfoDict = makeDict('businessFile.csv')
	reviews = readFile('../Data/yelp_academic_dataset_review.csv')
	userSpecDict = userNeighborhood(reviews, businessInfoDict)
	makeModelFile('userNeighborhood', sumUpValues(userSpecDict, 'neighborhood'))
	printUserPrefNeighborhood(userSpecDict, 'userFavNeighborhood.csv')
