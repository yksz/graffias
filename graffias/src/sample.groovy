import static graffias.*

dataSource('mydb') {
    def datasource = "datasource"
    return datasource
}

get('/') {
    'Hello World!'
}
