@Grapes([
    @Grab('org.eclipse.jetty.aggregate:jetty-server:8.1.13.v20130916'),
    @Grab('org.eclipse.jetty.aggregate:jetty-webapp:8.1.13.v20130916'),
    @Grab('javax.servlet:javax.servlet-api:3.0.1')
])
import java.util.regex.Pattern
import javax.servlet.*
import javax.servlet.http.*
import org.eclipse.jetty.http.ssl.SslContextFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector
import org.eclipse.jetty.servlet.*
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.websocket.*

class Config {
    static def get = [:], post = [:], put = [:], delete = [:]
    static def filter = [:], websocket = [:], error = [:]
}

static def get(String path, Closure closure) {
    register('get', path, closure)
}

static def get(Pattern pattern, Closure closure) {
    register('get', pattern, closure)
}

static def post(String path, Closure closure) {
    register('post', path, closure)
}

static def post(Pattern pattern, Closure closure) {
    register('post', pattern, closure)
}

static def put(String path, Closure closure) {
    register('put', path, closure)
}

static def put(Pattern pattern, Closure closure) {
    register('put', pattern, closure)
}

static def delete(String path, Closure closure) {
    register('delete', path, closure)
}

static def delete(Pattern pattern, Closure closure) {
    register('delete', pattern, closure)
}

static def filter(String path, Closure closure) {
    register('filter', path, closure)
}

static def filter(Pattern pattern, Closure closure) {
    register('filter', pattern, closure)
}

static def websocket(String path, Closure closure) {
    register('websocket', path, closure)
}

private static def register(method, route, closure) {
    Config[method] << [(route): closure]
}

static def error(int status, URI uri) {
    Config.error << [(status): uri.toString()]
}

static def uri(String path) {
    "/${path}".toURI()
}

static def view(String path) {
    "/WEB-INF/views/${path}".toURI()
}

static def runServer(int port = 8080, String contextPath = '/', Map<String, String> ssl = [:]) {
    def webServer = new WebServer(port, contextPath, ssl)
    webServer.start()
}

class WebServer {
    static {
        System.setProperty('groovy.source.encoding', 'UTF-8')
        graffias.defineExpandedMethods()
    }

    def jetty, webapp

    WebServer(int port, String contextPath, Map<String, String> ssl) {
        jetty = createServer(port, ssl)
        webapp = new WebAppContext(jetty, null, contextPath)
        webapp.resourceBase = 'public'
        webapp.setInitParameter('org.eclipse.jetty.servlet.Default.dirAllowed', 'false')
        webapp.start() // load web.xml
        webapp.addServlet(groovy.servlet.GroovyServlet, '*.groovy')
        webapp.addServlet(groovy.servlet.TemplateServlet, '*.gsp')
        registerFilter(new GraffiasFilter(), '/*')
        Config.websocket.each { path, closure -> registerWebSocket(path, closure) }
        Config.error.each { status, uri -> webapp.errorHandler.addErrorPage(status, uri) }
    }

    private def createServer(port, ssl) {
        if (ssl) {
            def server = new Server()
            def sslContextFactory = new SslContextFactory()
            ssl.each { method, value ->
                sslContextFactory."set${method.capitalize()}"(value)
            }
            def sslConnector = new SslSelectChannelConnector(sslContextFactory)
            sslConnector.port = port
            server.addConnector(sslConnector)
            return server
        } else {
            return new Server(port)
        }
    }

    def start() {
        jetty.start()
        jetty.join()
    }

    private def registerFilter(filter, path) {
        def dispatches = EnumSet.of(DispatcherType.REQUEST)
        webapp.addFilter(new FilterHolder(filter), path, dispatches)
    }

    private def registerWebSocket(path, closure) {
        def servlet = [
            doWebSocketConnect: { HttpServletRequest request, String protocol ->
                return WebSocketDSL.websocket(request, protocol, closure)
            }
        ] as WebSocketServlet
        webapp.addServlet(new ServletHolder(servlet), path)
    }
}

protected static def defineExpandedMethods() {
    def setAttributesMethod = {
        setAttributes { Map<String, Object> attrs ->
            for (attr in attrs)
                delegate.setAttribute(attr.key, attr.value)
        }
    }
    HttpServletRequest.metaClass.define(setAttributesMethod)
    HttpSession.metaClass.define(setAttributesMethod)
}

class GraffiasFilter implements Filter {
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        filter(request, response)
        doMainProcess(request, response, chain)
    }

    private def filter(request, response) {
        def (closure, matcher) = findClosure(request, Config.filter)
        if (closure)
            invoke(closure, matcher, request, response)
    }

    private def doMainProcess(request, response, chain) {
        if ('websocket'.equalsIgnoreCase(request.getHeader('Upgrade'))) {
            chain.doFilter(request, response)
            return
        }
        def (closure, matcher) = findHttpMethodClosure(request)
        if (closure)
            invoke(closure, matcher, request, response)
        else
            chain.doFilter(request, response)
    }

    private def findHttpMethodClosure(request) {
        def method = request.method.toLowerCase()
        switch (method) {
            case 'get':
            case 'post':
            case 'put':
            case 'delete':
                return findClosure(request, Config[method])
            default:
                return [null, null]
        }
    }

    private def findClosure(request, mapping) {
        def uri = request.requestURI - request.contextPath
        for (route in mapping.keySet()) {
            switch (route) {
                case String:
                    if (route == uri
                            || graffias.matchesWildcard(route, uri) { servletPath, pathInfo ->
                                request.metaClass.getServletPath = { servletPath }
                                request.metaClass.getPathInfo = { pathInfo }
                            }
                            || graffias.matchesNamedParameters(route, uri) { params ->
                                request.setAttributes(params)
                            })
                        return [mapping[route], null]
                    break
                case Pattern:
                    def matcher = route.matcher(uri)
                    if (matcher.matches())
                        return [mapping[route], matcher]
                    break
                default:
                    throw new AssertionError("This type is not supported: ${route.class.name}")
            }
        }
        return [null, null]
    }

    private def invoke(closure, matcher, request, response) {
        closure.delegate = response
        def result = (matcher == null) ? closure(request) : closure(request, matcher)
        switch (result) {
            case String:
            case GString:
                response.writer.write(result.toString())
                break
            case URI:
                def dispatcher = request.getRequestDispatcher(result.toString())
                dispatcher.forward(request, response)
                break
        }
    }

    void init(FilterConfig filterConfig) {}
    void destroy() {}
}

private static def matchesWildcard(path, uri, closure) {
    if (!path.endsWith('/*'))
        return false
    def matched = "${uri}/".startsWith(path[0..<-1])
    if (matched) {
        def servletPath = path[0..<-'/*'.size()]
        def pathInfo = (uri - servletPath) ?: null
        closure(servletPath, pathInfo)
    }
    return matched
}

private static def matchesNamedParameters(path, uri, closure) {
    if (path.indexOf(':') == -1)
        return false
    def pathParts = path.split('/')
    def uriParts = uri.split('/')
    if (pathParts.size() != uriParts.size())
        return false
    def params = [:]
    for (i in 0..<pathParts.size()) {
        if (!pathParts[i].isEmpty() && pathParts[i].charAt(0) == ':')
            params[pathParts[i].substring(1)] = uriParts[i]
        else if (pathParts[i] != uriParts[i])
            return false
    }
    closure(params)
    return true
}

class WebSocketDSL {
    static def websocket(HttpServletRequest request, String protocol, Closure closure) {
        def dsl = new WebSocketDSL()
        closure = closure.clone()
        closure.delegate = dsl
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure(request, protocol)
        return [
            onOpen: { WebSocket.Connection connection -> dsl.onopen(connection) },
            onClose: { int closeCode, String message -> dsl.onclose(closeCode, message) },
            onMessage: { String data -> dsl.onmessage(data) }
        ] as WebSocket.OnTextMessage
    }

    def onopen, onclose, onmessage

    def onopen(Closure onopen) {
        this.onopen = onopen
    }

    def onclose(Closure onclose) {
        this.onclose = onclose
    }

    def onmessage(Closure onmessage) {
        this.onmessage = onmessage
    }
}
