import static graffias.*

get('/') {
    'index.html'.toURI() // public/index.html
}

post('/') { req ->
    "${req.parameterMap}" // HttpServletRequest.getParameterMap()
}

get('/hello') { req ->
    setContentType 'text/html' // HttpServletResponse.setContentType()
    setStatus 200 // HttpServletResponse.setStatus()
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
    req.setAttributes(filter: 'on') // an expanded method
}

get('/gsp/*') {
    view 'hello.gsp' // public/WEB-INF/views/hello.gsp
}

get('/redirect') {
    sendRedirect '/' // HttpServletResponse.sendRedirect()
}

error(404, view('404.html')) // public/WEB-INF/views/404.html

runServer(8080)