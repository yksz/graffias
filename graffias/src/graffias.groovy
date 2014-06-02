@Grapes([
    @Grab('org.eclipse.jetty.aggregate:jetty-server:8.1.7.v20120910'),
    @Grab('org.eclipse.jetty.aggregate:jetty-servlet:8.1.7.v20120910'),
    @Grab('javax.servlet:javax.servlet-api:3.0.1')
])
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import javax.servlet.http.*

class Graffias {
    static def mappings = []
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

private static def register(method, path, closure) {
    Graffias.mappings << [method: method, path: path, closure: closure]
}

static def runServer(int port = 8080, String root = 'public') {
    def server = new WebServer(port, root, Graffias.mappings)
    server.start()
}

class WebServer {
    def jetty
    def context
    def servlets = [:]

    WebServer(int port, String root, List<Map> mappings) {
        jetty = new Server(port)
        context = new ServletContextHandler(jetty, '/', ServletContextHandler.SESSIONS)
        context.resourceBase = root
        mappings.each {
            registerServlet(it)
        }
    }

    def start() {
        jetty.start()
        jetty.join()
    }

    private def registerServlet(mapping) {
        def servlet = servlets[mapping.path]
        if (!servlet) {
            servlet = new GraffiasServlet()
            servlets[mapping.path] = servlet
        }
        servlet[mapping.method] = mapping.closure
        context.addServlet(new ServletHolder(servlet), mapping.path)
    }
}

class GraffiasServlet extends HttpServlet {
    def get, post, put, delete

    void doGet(HttpServletRequest request, HttpServletResponse response) {
        exec(request, response, get)
    }

    void doPost(HttpServletRequest request, HttpServletResponse response) {
        exec(request, response, post)
    }

    void doPut(HttpServletRequest request, HttpServletResponse response) {
        exec(request, response, put)
    }

    void doDelete(HttpServletRequest request, HttpServletResponse response) {
        exec(request, response, delete)
    }

    private def exec(request, response, closure) {
        if (closure) {
            closure.delegate = response
            def result = closure(request)
            if (result instanceof String || result instanceof GString) {
                response.writer.write(result.toString())
            }
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
        }
    }
}
