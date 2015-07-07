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
    "path=${req.pathInfo}"
}

get('/:named/params') { req ->
    setContentType 'text/plain'
    "named=${req.getAttribute('named')}"
}

filter('/filter') { req ->
    req.setAttributes(filter: 'on')
}

get('/filter') { req ->
    setContentType 'text/plain'
    "filter=${req.getAttribute('filter')}"
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
