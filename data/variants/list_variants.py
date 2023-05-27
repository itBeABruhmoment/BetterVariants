import os
import json

ls = []

for path, currentDirectory, files in os.walk("."):
    for file in files:
    	ls.append(file.split(".")[0])
ls.sort()
for str in ls:
	print(str)
