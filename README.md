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

runServer()
```

```groovy
import static graffias.*

get('/') {
    uri 'index.html'
}

post('/') { req ->
    "${req.parameterMap}"
}

get('/hello') { req ->
    setContentType 'text/html'
    setStatus 200
    """
    <html><body>
    <h1>
    Hello World!
    (filter = ${req.getAttribute('filter')})
    </h1>
    </body></html>
    """
}

filter('/hello') { req ->
    req.setAttributes(filter: 'on')
}

get('/gsp/*') {
    view 'hello.gsp'
}

get('/redirect') {
    sendRedirect '/'
}

error(404, view('404.html'))

runServer(8080)
```

Installation
============

Copy `graffias.groovy` to the same directory with your executable groovy file.
