import static graffias.*

get('/') {
    'ignore'
}

get('/') { // override
    uri 'index.html'
}

get('/stackoverflow.html') {
    uri 'stackoverflow.html'
}

post('/') { req ->
    setContentType 'text/plain'
    "key=${req.getParameter('key')}"
}

get('/wildcard/*') { req ->
    setContentType 'text/plain'
    "servletPath=${req.servletPath}, pathInfo=${req.pathInfo}"
}

get('/:name/params') { req ->
    setContentType 'text/plain'
    "name=${req.getAttribute('name')}"
}

get(~/^\/pattern\/(?<name>.+)/) { req, m ->
    setContentType 'text/plain'
    "name=${m.group('name')}"
}

filter('/filter') { req ->
    req.setAttributes(filter: 'on')
}

get('/filter') { req ->
    setContentType 'text/plain'
    "filter=${req.getAttribute('filter')}"
}

filter('/filter/wildcard/*') { req ->
    setContentType 'text/plain'
    "servletPath=${req.servletPath}, pathInfo=${req.pathInfo}"
}

filter('/filter/:name/params') { req ->
    setContentType 'text/plain'
    "name=${req.getAttribute('name')}"
}

filter(~/^\/filter\/pattern\/(?<name>.+)/) { req, m ->
    setContentType 'text/plain'
    "name=${m.group('name')}"
}

get('/filter/*') { req ->
}

get('/groovy') {
    view 'test.groovy'
}

get('/gsp') {
    view 'test.gsp'
}

error(404, view('404.html'))

websocket('/websocket') { req, protocol ->
    def connection
    onopen { conn ->
        connection = conn
    }
    onclose { code, msg ->
        if (connection)
            connection.close()
    }
    onmessage { msg ->
        def sub = protocol ?: ''
        connection.sendMessage("${sub}:${msg}")
    }
}

runServer(55555)
