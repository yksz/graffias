What is Graffias?
=================

Graffias is a lightweight and micro web framework for Groovy inspired by
[Graffiti](https://github.com/webdevwilson/graffiti)
and Sinatra.

Graffias is distributed as a single file,
and it's source code is only about 200 lines.  
So, please customize it freely!


Example
=======

```groovy
import static graffias.*

get('/') {
    'Hello World!'
}

// /hello?name=You
get('/hello') { req ->
    "Hello '${req.getParameter('name')}'"
}

runServer()
```

Installation
============

Copy `graffias.groovy` to the same directory with your executable groovy file.
