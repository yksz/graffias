@Grapes([
    @Grab('org.eclipse.jetty.aggregate:jetty-server:8.1.15.v20140411'),
    @Grab('org.eclipse.jetty.aggregate:jetty-webapp:8.1.15.v20140411'),
    @Grab('javax.servlet:javax.servlet-api:3.0.1')
])
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.*
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.websocket.*
import org.eclipse.jetty.websocket.WebSocket.Connection
import javax.servlet.*
import javax.servlet.http.*
import groovy.servlet.*

class Config {
    static def mappings = [], errors = []
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

private static def register(method, path, closure) {
    Config.mappings << [method: method, path: path, closure: closure]
}

static def error(int status, uri) {
    Config.errors << [status: status, uri: uri.toString()]
}

static def uri(String path) {
    "/${path}".toURI()
}

static def view(String path) {
    "/WEB-INF/views/${path}".toURI()
}

static def runServer(int port = 8080, String contextPath = '/') {
    def server = new WebServer(port, contextPath, Config.mappings, Config.errors)
    server.start()
}

class WebServer {
    static {
        System.setProperty('groovy.source.encoding', 'utf-8')
        GraffiasMethod.expand()
    }

    def jetty, webapp, servlets = [:]

    WebServer(int port, String contextPath, List<Map> mappings, List<Map> errors) {
        jetty = new Server(port)
        webapp = new WebAppContext(jetty, null, contextPath)
        webapp.resourceBase = 'public'
        webapp.setInitParameter('org.eclipse.jetty.servlet.Default.dirAllowed', 'false')
        webapp.addServlet(GroovyServlet, '*.groovy')
        webapp.addServlet(TemplateServlet, '*.gsp')
        mappings.each { registerMapping(it) }
        errors.each { registerErrorPage(it) }
    }

    def start() {
        jetty.start()
        jetty.join()
    }

    private def registerMapping(mapping) {
        if (mapping.path == '/')
            mapping.path = ''
        if (mapping.method == 'filter')
            registerFilter(mapping)
        else if (mapping.method == 'websocket')
            registerWebSocket(mapping)
        else
            registerServlet(mapping)
    }

    private def registerServlet(mapping) {
        def servlet = servlets[mapping.path]
        if (!servlet) {
            servlet = new GraffiasServlet()
            servlets[mapping.path] = servlet
        }
        servlet[mapping.method] = mapping.closure
        webapp.addServlet(new ServletHolder(servlet), mapping.path)
    }

    private def registerFilter(mapping) {
        def filter = new GraffiasFilter(closure: mapping.closure)
        def dispatches = EnumSet.of(DispatcherType.REQUEST)
        webapp.addFilter(new FilterHolder(filter), mapping.path, dispatches)
    }

    private def registerWebSocket(mapping) {
        def servlet = [
            doWebSocketConnect: { HttpServletRequest request, String protocol ->
                WebSocketDSL.websocket(protocol, mapping.closure)
            }
        ] as WebSocketServlet
        webapp.addServlet(new ServletHolder(servlet), mapping.path)
    }

    private def registerErrorPage(error) {
        webapp.errorHandler.addErrorPage(error.status, error.uri)
    }
}

class GraffiasServlet extends HttpServlet {
    def get, post, put, delete

    void doGet(HttpServletRequest request, HttpServletResponse response) {
        execute(request, response, get)
    }

    void doPost(HttpServletRequest request, HttpServletResponse response) {
        execute(request, response, post)
    }

    void doPut(HttpServletRequest request, HttpServletResponse response) {
        execute(request, response, put)
    }

    void doDelete(HttpServletRequest request, HttpServletResponse response) {
        execute(request, response, delete)
    }

    private def execute(request, response, closure) {
        if (!closure) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
            return
        }
        closure.delegate = response
        def result = closure(request)
        switch (result) {
            case String:
            case GString:
                response.writer.write(result.toString())
                break
            case URI:
                def dispatcher
                if (result.toString() == request.requestURI - request.contextPath)
                    dispatcher = servletContext.getNamedDispatcher('default')
                else
                    dispatcher = request.getRequestDispatcher(result.toString())
                dispatcher.forward(request, response)
                break
        }
    }
}

class GraffiasFilter implements Filter {
    def closure

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        if (closure) {
            closure.delegate = response
            closure(request)
        }
        chain.doFilter(request, response)
    }

    void init(FilterConfig filterConfig) {}
    void destroy() {}
}

class GraffiasMethod {
    static def expand() {
        def method = {
            setAttributes { Map<String, Object> attrs ->
                attrs.each { key, value ->
                    delegate.setAttribute(key, value)
                }
            }
        }
        HttpServletRequest.metaClass.define(method)
        HttpSession.metaClass.define(method)
    }
}

class WebSocketDSL {
    static def websocket(String protocol, Closure closure) {
        def dsl = new WebSocketDSL()
        def c = closure.clone()
        c.delegate = dsl
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c(protocol)
        [
            onOpen: { Connection connection -> dsl.onopen(connection) },
            onClose: { int closeCode, String message -> dsl.onclose(closeCode) },
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
