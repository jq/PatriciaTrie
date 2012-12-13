import json
import urllib
import urllib2
import sys
from subprocess import call


for line in sys.stdin:
	s = line.strip()

	if not bool(s):
		continue
		
	l = s.replace("[edit]", "").rstrip(':')
	return_code = call(["curl", "localhost:8666/", "-d", ("key=%s" % l)])
	print return_code