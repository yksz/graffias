import static graffias.*

get('/') {
    'index'
}

get('/hello') {
    println 'get hello'
    setContentType 'text/html'
    """
    <html><body>
    <h1>Hello World!</h1>
    </body></html>
    """
}

filter('/hello') {
    println 'filter'
}

get('/hello/gsp') {
    view('hello.gsp')
}

get('/redirect') {
    sendRedirect('/hello')
}

post('/') { req ->
    "${req.parameterMap}"
}

error(404, view('404.html'))

runServer()
