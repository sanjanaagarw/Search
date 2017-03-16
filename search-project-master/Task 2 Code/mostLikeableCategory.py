'''
Code written by Supreeth Suryaprakash (skeragod@indiana.edu) | Formulation done by rkanchib and skeragod
'''

from utilities import *
import os
import operator

def makeUserDictionary(reviews, busID):
	userDict = dict()
	for review in reviews:
		if not review[3][1:].translate(None, "\\'") in userDict:
			userDict[review[3][1:].translate(None, "\\'")] = dict()

		if review[0][1:].translate(None, "\\'") in busID:
			if busID[review[0][1:].translate(None, "\\'")] in userDict[review[3][1:].translate(None, "\\'")]:
				userDict[review[3][1:].translate(None, "\\'")][busID[review[0][1:].translate(None, "\\'")]].append(
					int(review[4].strip()))
			else:
				userDict[review[3][1:].translate(None, "\\'")][busID[review[0][1:].translate(None, "\\'")]] = [
					int(review[4].strip())]

	return userDict


def findBestCategory(userDict):
	usersFavCuisine = dict()
	for key, val in userDict.items():
		best = -99999
		cuisine = 'Unknown'
		for keyCuisine, valCount in val.items():
			avg = sum(valCount) / len(valCount)
			if best < avg:
				cuisine = keyCuisine
				best = avg
		usersFavCuisine[key] = cuisine
	return usersFavCuisine


def printToFile(fileName, usersFavCuisine):
	file = open(fileName, 'w')
	print "Users most Favorite Categories"
	for key, val in usersFavCuisine.items():
		file.write(key + ',' + val)
		file.write(os.linesep)
		print key + " - " + val

if __name__ == '__main__':
	reviews = readFile('../Data/min.csv')
	businessIDMap = mapBusinessId('../Data/newBusiness.csv')
	userSpecDict = makeUserDictionary(reviews, businessIDMap)
	makeModelFile('userCategories', sumUpValues(userSpecDict, 'category'))
	userFavCategory = findBestCategory(userSpecDict)
	printToFile('userFavCusinies.txt', userFavCategory)