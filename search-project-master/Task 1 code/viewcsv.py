

import csv

'''with open('yelp_academic_dataset_business.csv','r+', encoding="utf8") as file:
    reader = csv.reader(file)
    for i in range(2):
        print(reader.__next__())'''


target = []
with open('yelp_academic_dataset_business.csv','r+', encoding="utf8") as file:
    reader = csv.reader(file)
    for row in reader:
        review = []
        review.append(row[15])
        review.append(row[20])
        target.append(review)

print("Reading done")

with open('business_extract.csv', 'w', newline='', encoding="utf-8") as fp:
    a = csv.writer(fp, delimiter=',')
    a.writerows(target)

print("Writing done")

'''colnames = ['business_id', 'categories']
data = pandas.read_csv('yelp_academic_dataset_business.csv', names=colnames)
bus = data.business_id.tolist()
cat = data.categories.tolist()'''