@Grapes([
    @Grab('org.eclipse.jetty.aggregate:jetty-server:8.1.7.v20120910'),
    @Grab('org.eclipse.jetty.aggregate:jetty-webapp:8.1.7.v20120910'),
    @Grab('javax.servlet:javax.servlet-api:3.0.1')
])
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.*
import org.eclipse.jetty.webapp.WebAppContext
import javax.servlet.*
import javax.servlet.http.*
import groovy.servlet.*

class Config {
    static def viewsPath = '/WEB-INF/views'
    static def mappings = []
    static def errors = []
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

private static def register(method, path, closure) {
    Config.mappings << [method: method, path: path, closure: closure]
}

static def error(int status, uri) {
    Config.errors << [status: status, uri: uri.toString()]
}

static def view(String path) {
    new URI("${Config.viewsPath}/${path}")
}

static def runServer(int port = 8080, String root = 'public') {
    def server = new WebServer(port, root, Config.mappings, Config.errors)
    server.start()
}

class WebServer {
    static {
        System.setProperty('groovy.source.encoding', 'utf-8')
    }
    def jetty
    def webapp
    def servlets = [:]

    WebServer(int port, String root, List<Map> mappings, List<Map> errors) {
        jetty = new Server(port)
        webapp = new WebAppContext(jetty, null, '/')
        webapp.resourceBase = root
        webapp.setInitParameter('org.eclipse.jetty.servlet.Default.dirAllowed', 'false')
        webapp.addServlet(GroovyServlet, '*.groovy')
        webapp.addServlet(TemplateServlet, '*.gsp')
        mappings.each {
            registerMapping(it)
        }
        errors.each {
            registerErrors(it)
        }
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
        webapp.addFilter(new FilterHolder(filter), mapping.path, EnumSet.of(DispatcherType.REQUEST))
    }

    private def registerErrors(error) {
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
                def path = result.toString()
                def dispatcher = request.getRequestDispatcher(path)
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
