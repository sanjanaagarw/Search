import csv

'''
Code written by Suhas Jagadish (jagadiss@iu.edu)
'''

'''
with open('business_extract.csv','r+', encoding="utf8") as file:
    reader = csv.reader(file)
    bus1 = [row for row in reader]
    print(bus1[:10])
    #data = list(reader)
    #print(len(data))
    #for i in range(2):
        #print(reader.__next__())
'''
target = []
with open('newBusiness.csv', 'r+', encoding="utf8") as csvfile1:
    with open ("review_extract.csv", "r+", encoding="utf8") as csvfile2:
        bus = csv.reader(csvfile1)
        rev = csv.reader(csvfile2)
        bus1 = [row for row in bus]
        rev1 = [row for row in rev]
        for r in rev1:
            review = []
            for b in bus1:
                if r[1] == b[0]:
                    #print(r[0] + b[1])
                    review.append(r[0])
                    review.append(b[1])
                    target.append(review)

print("Reading done")

with open('final_csv.csv', 'w', newline='', encoding="utf-8") as fp:
    a = csv.writer(fp, delimiter=',')
    a.writerows(target)

print("Writing done, final csv is ready")

'''
target = []
with open('review_extract.csv','r+', encoding="utf8") as csv_file:
    reader = csv.reader(csv_file, delimiter=",", quotechar='"')
    with open('business_extract.csv','r+', encoding="utf8") as csv_file1:
        reader1 = csv.reader(csv_file1, delimiter=",", quotechar='"')
        for row in reader:
            review = []
            for col in reader1:
                print(col[1])
                if col[0] == row[1]:
                    print(row[0] + "->" + col[1])
                    review.append(row[0])
                    review.append(col[1])
                    target.append(review)
                    break
                else:
                    continue


print("Reading done")

with open('final_csv.csv', 'w', newline='', encoding="utf-8") as fp:
    a = csv.writer(fp, delimiter=',')
    a.writerows(target)

print("Writing done, final csv is ready")
'''