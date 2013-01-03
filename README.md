Simple in-memory pseudo-prefix search
=============

An experimental auto-complete, prefix search server...

The goal is to provide an http interface for fast prefix searches (aka type-head, aka auto-complete).

I say "pseudo-prefix" b/c we break the initial string into chunks (chop off previous token over & over again) to enable kinda-sorta-partial-pseudo-prefix type searches.
Essentially: for a string like "A Nightmare on Elm Street" we want a successful match for queries such as:

- elm
- on elm

So far there's no persistence at all; server goes down == need to re-import the data. 

Quick Start
===

Run it and add stuff
---

    $ make run

*switch to an other terminal window*

    $ ./bin/put "A Nightmare on Elm Street"
    $ ./bin/put "30 Days of Night"
    $ ./bin/put "Silent Night Deadly Night"
    $ ./bin/put "The Nightmare Before Christmas"

Get stuff out
---

    $ ./bin/get night
    [
        "A Nightmare on Elm Street",
        "Silent Night Deadly Night",
        "The Nightmare Before Christmas"
    ]

    $ ./bin/get elm
    [
        "A Nightmare on Elm Street"
    ]

    $ ./bin/get n
	[
		"A Nightmare on Elm Street",
	 	"Silent Night Deadly Night",
	 	"The Nightmare Before Christmas"
	]

HTTP Methods, URL's etc
===

The whole thing's "REST" based & does stuff based on the HTTP verb you use.
Currently GET and PUT/POST are the only ones i've bothered with.


PUT
---

This will add a string to the PatriciaTrie:

`curl localhost:8666/api/ -d s="some string"`

You can `PUT` multiple strings at once by specifying the `s` parameter multiple times:

`curl localhost:8666/api/ -d s="some string" -d s="some other string"`

The server will spit out some JSON telling you what "prefixes" it extracted for each `s` you sent it.

    {
         "some string": [
             "some string",
  			 "string"
         ],
         "some other string": [
             "some other string",
             "other string"
  			 "string"
         ]
    }

So with this example the following "prefix" queries would return "some other string":

- s
- so
- som
- some
- some o
- some ot
- some oth
- some othe
- some other
- some other s
- some other st
- some other str
- some other stri
- some other strin
- some other string
- o
- ot
- oth
- othe
- other
- other s
- other st
- other str
- other stri
- other strin
- other string
- s
- st
- str
- stri
- strin
- string


GET
---

This will get strings from the server for a given prefix:

`curl localhost:8666/api/?s=some`

output's gonna be...

     [
         "some other string",
         "some string"
     ]

HEAD
---

Send a HEAD request to see how many matches there would be for a given prefix...
Assuming you started with an empty trie

	$ ./bin/put "A Nightmare on Elm Street"
	$ ./bin/put "30 Days of Night"
	$ ./bin/put "Silent Night Deadly Night"
	$ ./bin/put "The Nightmare Before Christmas"
	
Check how many total prefixes are stored
	
	$ curl --head localhost:8666/api/
	
	HTTP/1.1 200 OK
	X-Patricia-Prefix-Count: 20
	Content-Length: 0
	Server: Jetty(8.1.7.v20120910)
		
Check how many matches there are for a specific prefix

	$ curl --head localhost:8666/api/?s=night
	
	HTTP/1.1 200 OK
	X-Patricia-Prefix-Count: 4
	Content-Length: 0
	Server: Jetty(8.1.7.v20120910)
	

Test Data - Free of Charge
===

`etc/data/movies` has a ton of movie names in it. I copy pasted a shit ton of stuff from wikipedia;
as such there's some garbage or mangled titles in there but it's good enough.

To load the movie titles just do this:

    $ cat etc/data/movies/*.txt | ./bin/import_stdin


Web UI for Testing
===

Add stuff
---
http://localhost:8666/add gives you an interface to add strings to the trie. The ui has a single textfield;
new line separate the strings you want to add.

Test Auto-Complete
---
Cruise on over to http://localhost:8666/ to try out the auto-complete. Just start typing & suggestions will pop down.

Basic Status
---
http://localhost:8666/status has some bare bones info; number of strings, first key, last key, up time etc

Config
===

On the command line you can specify a config file via system property named "patricia.conf"

    java -Dpatricia.conf=~/xxxxxx/PatriciaTrie/etc/conf/local.json -jar /path/to/patricia.jar

Config file's in json... Here's the most basic one you could have...

    {
        "connector": {
            "port": 8666,
        }
    }

The config will use reflection to set stuff up on the connector. So in essence given the config file above the code will
do do the following:

    connector.setPort(8666)

Obviously, if you specified more values then those will get set as well. Check out
http://wiki.eclipse.org/Jetty/Howto/Configure_Connectors for a list of `connector` options.

There's a second layer to the config as well... anything in the config file will be overridden by the value of
system properties who's names begin with `patricia.`.

Going with the example above:

    java -Dpatricia.conf=~/xxxxxx/PatriciaTrie/etc/conf/local.json -Dpatricia.connector.port=8111 -jar /path/to/patricia.jar

With a line like that the app will first load in all the values from `local.json` then layer on the values
from any system properties named `patricia.*`. So in this example `connector.port` is first set to 8666 from the config,
then that value is overridden with 8111 due to `-Dpatricia.connector.port=8111`.

Hope that makes sense... long story short; any system prop who's key starts with `patricia.` wins the battle.

Useful stuff
===

- `make run` by default runs on http://localhost:8666
- `make jar` assembles an exeutable jar
- `bin/get` is a convenience script to perform a query
- `bin/put` is a convenience script to add strings
- `bin/import_stdin` is a convenience script to import lines from stdin


Licence
===

This project makes use of LimeWire's PatriciaTrie implementation.
http://code.google.com/p/google-collections/issues/detail?id=5

I have no clue what the official license is for LimeWire's code but on that page they state:

    The files can be licensed as necessary (we own the copyright and can
    change/transfer the license).  I'm not sure what license, if any, these
    would need to be for inclusion.

As far as Jeraff, Inc's concerned have at it but don't blame us if shit breaks, is slow, sucks or looks ugly