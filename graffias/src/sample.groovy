import static graffias.*

get('/') {
    'Hello World!'
}

get('/hello') {
    setContentType 'text/html'
    """
    <html><body>
    <h1>Hello World!</h1>
    </body></html>
    """
}

runServer()
