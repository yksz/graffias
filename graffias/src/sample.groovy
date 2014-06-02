import static graffias.*

get('/') {
    'index'
}

get('/hello') {
    setContentType 'text/html'
    """
    <html><body>
    <h1>Hello World!</h1>
    </body></html>
    """
}

post('/hello') { req ->
    req.params.toString()
}

runServer()
