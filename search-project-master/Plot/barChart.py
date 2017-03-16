'''
Written by Supreeth

Code taken by python official website
'''
import numpy as np
import matplotlib.pyplot as plt

label = tuple()

list1 = [33.33333333333333, 66.66666666666666, 25.0, 66.66666666666666, 100.0, 33.33333333333333, 50.0, 100.0, 50.0, 20.0, 100.0, 21.428571428571427, 30.0, 100.0, 100.0, 66.66666666666666, 33.33333333333333, 33.33333333333333, 20.0, 50.0, 50.0, 33.33333333333333, 37.5, 100.0, 50.0]
N = len(list1)

for i in range(N):
	label = label + ('u' + str(i) ,)

ind = np.arange(N)  # the x locations for the groups
width = 0.35       # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind, list1, width, color='r')

# add some text for labels, title and axes ticks
ax.set_ylabel('Accuracy')
ax.set_title('Users')
ax.set_xticks(ind + width)
ax.set_xticklabels(label)



def autolabel(rects):
    # attach some text labels
    for rect in rects:
        height = rect.get_height()
        ax.text(rect.get_x() + rect.get_width()/2., 1.05*height,
                '%d' % int(height),
                ha='center', va='bottom')

autolabel(rects1)
plt.show()