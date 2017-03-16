import csv

with open('business_extract.csv','r+', encoding="utf8") as file:
    reader = csv.reader(file)
    bus1 = [row for row in reader]
    print(bus1[:10])
    #data = list(reader)
    #print(len(data))
    #for i in range(2):
        #print(reader.__next__())

target = []
with open('newBusiness.csv', 'r+', encoding="utf8") as csvfile1:
    with open ("HMM_extract.csv", "r+", encoding="utf8") as csvfile2:
        bus = csv.reader(csvfile1)
        hmm = csv.reader(csvfile2)
        bus1 = [row for row in bus]
        hmm1 = [row for row in hmm]
        for r in hmm1:
            review = []
            for b in bus1:
                if r[1] == b[0]:
                    #print(r[0] + b[1])
                    review.append(r[0])
                    review.append(r[2])
                    review.append(b[1])
                    review.append(r[3])
                    target.append(review)

print("Reading done")

with open('HMM_extract_final.csv', 'w', newline='', encoding="utf-8") as fp:
    a = csv.writer(fp, delimiter=',')
    a.writerows(target)

print("Writing done, final csv is ready")