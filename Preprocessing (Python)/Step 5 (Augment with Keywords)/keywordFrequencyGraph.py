import matplotlib.pyplot as plt
import matplotlib.pylab as pylab
import math
import numpy

from numpy.ma import array

INPUT_FILE = "../../Dataset_Preprocessed/keywords_corrected.txt"
OUTPUT_FILE = "../../Dataset_Preprocessed/keywordIdMap.txt"

f = open(INPUT_FILE,encoding='utf-8',mode='r')
x = []
y = []

i = 0

for s in f:
    try:
        #keyword = s.split(":")[0].strip()
        freq = (int)(s.split(":")[1].strip())
        x.append(math.log((i+1),10))
        y.append(freq/1000)
        i = i + 1
        #print(keyword)
    except Exception as e:
        print(e)
        print(s)
        freq = (int)(s.split(":")[2].strip())
        x.append(math.log(i, 10))
        y.append(freq/1000)
        i = i + 1

# mark_x = [math.log(10000,10)]
# mark_y = [y[19999]]
plt.plot(x, y, color='g')
# plt.plot(mark_x,mark_y, ls="", marker="o", label="points")
plt.xlabel('Number of top keywords', fontsize=10)
plt.ylabel('Frequency(x1K)', fontsize=10)

params = {'axes.labelsize': 16,
         'axes.titlesize':'x-large',
         'xtick.labelsize':'x-large',
         'ytick.labelsize':'x-large'}
pylab.rcParams.update(params)
locs, labels = plt.xticks()            # Get locations and labels
newLabels = []
for item in locs:
    #item = math.pow(10,(int)(item))
    newLabels.append('$10^{'+(str)((int)(item))+'}$')
plt.xticks(locs[1:], newLabels[1:])  # Set locations and labels
plt.show()