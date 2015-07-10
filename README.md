What is Graffias?
=================

Graffias is a lightweight and micro web framework for Groovy inspired by
[Graffiti](https://github.com/webdevwilson/graffiti)
and Sinatra.

Graffias is distributed as a single file,
and it's source code is only about 300 lines.  
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
@Grab(group='commons-lang', module='commons-lang', version='2.6')
import static graffias.*
import org.apache.commons.lang.StringEscapeUtils

get('/') {
    uri 'index.html' // public/index.html
}

post('/') { req -> // req => javax.servlet.http.HttpServletRequest
    setContentType 'text/plain'
    "${req.parameterMap}"
}

get('/hello') { req ->
    setContentType 'text/html' // HttpServletResponse.setContentType()
    setStatus 200 // HttpServletResponse.setStatus()
    """
    <html><body>
    <h1>
    Hello World (filter=${req.getAttribute('filter')})
    </h1>
    </body></html>
    """
}

get('/hello/:name') { req ->
    setContentType 'text/plain'
    "Hello ${req.getAttribute('name')} (filter=${req.getAttribute('filter')})"
}

filter('/hello/*') { req ->
    req.setAttributes(filter: 'on') // an expanded method
}

get(~/^\/goodbye\/(?<name>.+)/) { req, m -> // m => java.util.regex.Matcher
    setContentType 'text/plain'
    "Goodbye ${m.group('name')}"
}

get('/security') { req ->
    setContentType 'text/html'
    StringEscapeUtils.escapeHtml("${req.parameterMap}") // XSS prevention
}

get('/groovy') {
    view 'hello.groovy' // public/WEB-INF/views/hello.groovy
}

get('/gsp') {
    view 'hello.gsp' // public/WEB-INF/views/hello.gsp
}

get('/redirect') {
    sendRedirect '/' // HttpServletResponse.sendRedirect()
}

error(404, view('404.html')) // public/WEB-INF/views/404.html

runServer(8080, '/')
```

Example - WebSocket
===================

```groovy
import static graffias.*

websocket('/') { req, protocol ->
    def connection
    onopen { conn ->
        connection = conn
    }
    onclose { code, msg ->
        if (connection)
            connection.close()
    }
    onmessage { msg ->
        connection.sendMessage(msg)
    }
}

runServer()
```

Installation
============

Copy `graffias.groovy` to the same directory with your executable groovy file.
