class Graffias {
    static def config = [
        port: 8080,
        root: 'public',
        mappings: [],
        databases: [:],
    ]
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
    def mapping = [method: method, path: path, closure: closure]
    Graffias.config.mappings << mapping
}

static def dataSource(String name, Closure closure) {
    def database = closure()
    Graffias.config.databases[name] = database
}

static def runServer() {
    def server = new WebServer(Graffias.config)
    server.start()
}

class WebServer {
    def jetty
    def webapp

    WebServer(config) {
        jetty = new Server(config.port)
        webapp = new WebAppContext()
        webapp.resourceBase = root
    }

    def start() {
        jetty.start()
    }

    def map(mapping) {
        webapp.addServlet(servlet, path)
    }
}

class Server {
    Server(int port) { println "port=$port" }
    def start() { println "server start" }
}

