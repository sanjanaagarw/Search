'''
This file contains all the utility functions that can be used in this project.

Code written by Supreeth Suryaprakash (skeragod@indiana.edu)
'''

import csv
import pickle
import os
import operator

categoriesMain = ['Indian', 'Chinese', 'Japanese', 'American', 'Thai', 'Italian', 'Pakistani', 'Mediterranean', 'Delis', 'Ethiopian', 'Mexican', 'Vietnamese', 'Caribbean', 'Greek', 'Coffee & Tea', 'Breakfast & Brunch', 'Korean']

map_cuisine = dict()
map_cuisine['American'] = ['Beer', 'Bars', 'Burgers', 'Pubs', 'Bakeries', 'Sandwiches', 'Smoothies', 'Barbeque', 'Steakhouses', 'Donuts', 'Fast Food']
map_cuisine['Italian'] = ['Pizza']
map_cuisine['Japanese'] = ['Noodles']
k = 20

def readFile(fileName):
	fileContents = []
	with open(fileName, 'rb') as f:
		reader = csv.reader(f, dialect='excel', delimiter=',')
		for row in reader:
			fileContents.append(row)
	f.close()
	return fileContents[1:len(fileContents)] #Restricting it to 1000 for testing purpose

'''
Adds all the compliment values
'''
def addComplimentValues(userItem):
	return int(userItem[1]) if userItem[1] != '' else 0 + int(userItem[4]) if userItem[4] != '' else 0 + int(userItem[5]) if userItem[5] != '' else 0 + int(userItem[7]) if userItem[7] != '' else 0 + int(userItem[9]) if userItem[9] != '' else 0 + int(userItem[10]) if userItem[10] != '' else 0 + int(userItem[11]) if userItem[11] != '' else 0 + int(userItem[13]) if userItem[13] != '' else 0 +int(userItem[18]) if userItem[18] != '' else 0 + int(userItem[21]) if userItem[21] != '' else 0
'''
Adds all the vote values
'''
def addVoteValues(userItem):
	return int(userItem[17]) if userItem[17] != '' else 0 + int(userItem[19]) if userItem[19] != '' else 0 + int(userItem[22]) if userItem[22] != '' else 0

def createUserDict(listOfUsers):
	userDict = dict()
	userMap = dict()
	valList = list()
	userIdwithName = dict()
	userFriendsMap = dict()
	index = 0
	for eachrow in listOfUsers:
		index += 1
		complimentVals = addComplimentValues(eachrow)
		voteValues = addVoteValues(eachrow)
		friendsList = eachrow[3].translate(None, "'\\").translate(None, '[').translate(None, ']').split(',')
		friendsList = map(str.strip, friendsList)
		userDict[eachrow[16]] = [int(eachrow[0].split('-')[0]), complimentVals, int(eachrow[2]), len(friendsList), int(eachrow[6]), float(eachrow[12]), voteValues]
		userMap[index] = [eachrow[16], friendsList]
		userFriendsMap[eachrow[16]] = friendsList
		userIdwithName[eachrow[16]] = eachrow[15].strip()
		valList.append(userDict[eachrow[16]])
	return userDict, userMap, valList, userIdwithName, userFriendsMap


def makeModelFile(filename, content):
	with open(filename, 'wb') as file:
		pickle.dump(content, file)
		file.close()


def getModelFile(fileName):
	with open(fileName, 'rb') as file:
		a = pickle.load(file)
		file.close()
		return a


def consolidateCusines(fileName, newFileName):
    categoryList = {}
    finalCategory = []
    with open(fileName, 'rb') as file:
        categories = csv.reader(file)
        for row in categories:
            category = row[1].translate(None, "'\\").translate(None, '[').translate(None, ']').split(',')
            category = map(str.strip, category)
            categoryList[row[0].strip()] = category

    with open(newFileName, 'wb') as file2:
        writer = csv.writer(file2)
        for key, val in categoryList.items():
            items = []
            for cuisines in val:
                if cuisines in categoriesMain:
                    val = cuisines
                else:
                    if cuisines in map_cuisine['American']:
                        val = 'American'
                    elif cuisines in map_cuisine['Italian']:
                        val = 'Italian'
                    elif cuisines in map_cuisine['Japanese']:
                        val = 'Japanese'
            items.append(key)
            if type(val) != list:
                items.append(val)
                finalCategory.append(items)
        writer.writerows(finalCategory)
    file2.close()


def mapBusinessId(fileName):
	eachRow = {}
	with open(fileName, 'rb') as f:
		reader = csv.reader(f, dialect='excel', delimiter=',')
		for row in reader:
			eachRow[row[0].strip()] = row[1].strip()
	f.close()
	return eachRow

def printBusinessNecessaryInfo(businesses, fileName):
	file = open(fileName, 'w')
	for everyBusiness in businesses:
		file.write(everyBusiness[15] + ',' + everyBusiness[38] + ',' + everyBusiness[44].strip().translate(None, '\n').translate(None, ',') + ',' + everyBusiness[58] + ',' + everyBusiness[93])
		file.write(os.linesep)

def returnMaxinDict(dict1):
	if bool(dict1):
		return max(dict1.iteritems(), key=operator.itemgetter(1))[0]
	else:
		return 'No Info'


def calcAcc(list1):
	list1 = [i for i in list1 if i >= k]
	print list1
	return sum(list1) / len(list1)


def sumUpValues(userDict, mode):
	for key, val in userDict.items():
		for key1, val1 in val.items():
			if mode != 'neighborhood':
				userDict[key][key1] = sum(val1)
		userDict[key] = sorted(val.items(), key=operator.itemgetter(1), reverse=True)
	return userDict