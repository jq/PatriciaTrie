Simple in-memory prefix search
=============

This is an experimental project.
It's a simple http server on top of LimeWire's PatriciaTrie implementation.
more info at: http://code.google.com/p/google-collections/issues/detail?id=5

The goal is to provide an http interface for prefix searches (aka type-head, aka auto-complete)

URL's etc
===

The whole thing's "REST" based & does stuff based on the HTTP verb you use.
Currently GET and PUT/POST are the only ones i've bothered with.

FYI: There's no config yet & this thing's hard coded to listen on http://localhost:8666

PUT
---

This will add a string to the PatriciaTrie:

`curl localhost:8666 -d key="some string"`


You can `PUT` multiple strings at once by specifying the `key` parameter multiple times:

`curl localhost:8666 -d key="some string" -d key="some other string"`

The server will spit out some JSON telling you what prefixes it extracted for each `key` you sent it.

GET
---

This will get strings from the server for a given prefix:

`curl localhost:8666/?key=some`

output's gonna be...

`{
     "some string": [
         "some string"
     ],
     "some other string": [
         "some other string",
         "other string"
     ]
 }`