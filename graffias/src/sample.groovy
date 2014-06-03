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

get('/hello/gsp') {
    render('hello.gsp')
}

post('/') { req ->
    "${req.params}"
}

runServer()
