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

private static register(method, path, closure) {
    def mapping = [method: method, path: path, closure: closure]
    Graffias.config.mappings << mapping
}

static def dataSource(String name, Closure closure) {
    def database = closure()
    Graffias.config.databases[name] = database
}
