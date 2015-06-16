import static graffias.*

get('/') {
    'ignore'
}

get('/') { // override
    uri 'index.html'
}

post('/') { req ->
    setContentType 'text/plain'
    "key=${req.getParameter('key')}"
}

error(404, view('404.html'))

runServer(55555)
