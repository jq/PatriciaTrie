Simple in-memory pseudo-prefix search
=============

This is an experimental project.
It's a simple http server on top of LimeWire's PatriciaTrie implementation.
more info at: http://code.google.com/p/google-collections/issues/detail?id=5

The goal is to provide an http interface for pseudo-prefix searches (aka type-head, aka auto-complete).

I say "pseudo-prefix" b/c we break the initial string into chunks (chop off previous token over & over again) to enable kinda-sorta-partial-pseudo-prefix type searches.
Essentially: for a string like "A Nightmare on Elm Street" we want queries such as:

- elm
- on elm

etc to return "A Nightmare on Elm Street" as a result

Quick Start
===

Run it and add stuff
---

    $ make run

*switch to an other terminal window*

    $ cd etc
    $ ./put "A Nightmare on Elm Street"
    $ ./put "30 Days of Night"
    $ ./put "Silent Night Deadly Night"
    $ ./put "The Nightmare Before Christmas"

Get stuff out
---

    $ ./get night
    {
         "night": [
             "A Nightmare on Elm Street",
             "Silent Night Deadly Night",
             "The Nightmare Before Christmas"
         ]
    }

    $ ./get elm
    {
         "elm": [
             "A Nightmare on Elm Street"
         ]
    }

    $ ./get e
    {
        "e": [
            "A Nightmare on Elm Street"
        ]
    }

URL's etc
===

The whole thing's "REST" based & does stuff based on the HTTP verb you use (the path doesn't matter at all).
Currently GET and PUT/POST are the only ones i've bothered with.

FYI: There's no config yet & this thing's hard coded to listen on http://localhost:8666

PUT
---

This will add a string to the PatriciaTrie:

`curl localhost:8666/ -d s="some string"`


You can `PUT` multiple strings at once by specifying the `s` parameter multiple times:

`curl localhost:8666/ -d s="some string" -d s="some other string"`

The server will spit out some JSON telling you what prefixes it extracted for each `s` you sent it.

    {
         "some string": [
             "some string"
         ],
         "some other string": [
             "some other string",
             "other string"
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


GET
---

This will get strings from the server for a given prefix:

`curl localhost:8666/?s=some`

output's gonna be...

     [
         "some other string",
         "some string"
     ]

Test Data - Free of Charge
===

`etc/data/movies` has a ton of movie names in it. I copy pasted a shit ton of stuff from wikipedia;
as such there's some garbage or mangled titles in there but it's good enough.

To load the movie titles just do this:

    $ cd etc/data/movies
    $ cat *.txt | python import.py


Web UI for Testing
===

Cruise on over to http://localhost:8666/webui/ for a simple web ui. Just start typing to see auto-complete suggestions.
If you're just fucking around you'll probably wanna load in the movie data
(instructions in the previous section).


Useful stuff
===

- `make run` runs it on http://localhost:8666
- `make jar` assembles an exeutable jar
- `etc/get` is a convenience script to perform a query
- `etc/put` is a convenience script to add strings


Licence
===
Shit's free...

I have no clue what the official license is for LimeWire's code.
On http://code.google.com/p/google-collections/issues/detail?id=5 they state:

    The files can be licensed as necessary (we own the copyright and can
    change/transfer the license).  I'm not sure what license, if any, these
    would need to be for inclusion.

As far as Jeraff, Inc's concerned have at it but don't blame us if shit breaks, is slow, sucks or looks ugly