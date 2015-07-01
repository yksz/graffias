@Grapes([
    @Grab('org.eclipse.jetty.aggregate:jetty-server:8.1.13.v20130916'),
    @Grab('org.eclipse.jetty.aggregate:jetty-webapp:8.1.13.v20130916'),
    @Grab('javax.servlet:javax.servlet-api:3.0.1')
])
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.*
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.websocket.*
import javax.servlet.*
import javax.servlet.http.*

class Config {
    static def get = [:], post = [:], put = [:], delete = [:]
    static def filter = [:], websocket = [:], error = [:]
}

static def get(String path, Closure closure) {
    register('get', path, closure)
}

static def post(String path, Closure closure) {
    register('post', path, closure)
}

static def put(String path, Closure closure) {
    register('put', path, closure)
}

static def delete(String path, Closure closure) {
    register('delete', path, closure)
}

static def filter(String path, Closure closure) {
    register('filter', path, closure)
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

static def runServer(int port = 8080, String contextPath = '/') {
    def server = new WebServer(port, contextPath)
    server.start()
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

class WebServer {
    static {
        System.setProperty('groovy.source.encoding', 'UTF-8')
        graffias.defineExpandedMethods()
    }

    def jetty, webapp

    WebServer(int port, String contextPath) {
        jetty = new Server(port)
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

class GraffiasFilter implements Filter {
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        filter(request, response)
        doMainProcess(request, response, chain)
    }

    private def filter(request, response) {
        def closure = findClosure(request, Config.filter)
        if (closure)
            invoke(closure, request, response)
    }

    private def doMainProcess(request, response, chain) {
        if ('websocket'.equalsIgnoreCase(request.getHeader('Upgrade'))) {
            chain.doFilter(request, response)
            return
        }
        def closure = findHttpMethodClosure(request)
        if (closure)
            invoke(closure, request, response)
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
        }
    }

    private def findClosure(request, mapping) {
        def uri = request.requestURI - request.contextPath
        for (route in mapping.keySet()) {
            switch (route) {
                case String:
                    if (route == uri || matchesWildcard(route, uri, request))
                        return mapping[route]
                    break
            }
        }
    }

    private def matchesWildcard(route, uri, request) {
        if (!route.endsWith('/*'))
            return false
        def index = route.size() - '/*'.size()
        def prefix = route[0..<index]
        def matched = uri.startsWith(prefix)
        if (matched)
            request.metaClass.getPathInfo = { request.requestURI - prefix }
        return matched
    }

    private def invoke(closure, request, response) {
        closure.delegate = response
        def result = closure(request)
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

class WebSocketDSL {
    static def websocket(HttpServletRequest request, String protocol, Closure closure) {
        def dsl = new WebSocketDSL()
        def clonedClosure = closure.clone()
        clonedClosure.delegate = dsl
        clonedClosure.resolveStrategy = Closure.DELEGATE_FIRST
        clonedClosure(request, protocol)
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
