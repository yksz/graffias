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
