import os
import json

ls = []

for path, currentDirectory, files in os.walk("."):
    for file in files:
    	name = file.split(".")
    	if name[1] == 'variant':
    		ls.append(name[0])
ls.sort()
for str in ls:
	print(str)
