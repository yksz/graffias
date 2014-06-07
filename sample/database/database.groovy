@Grab(group='com.h2database', module='h2', version='1.4+')
import groovy.sql.Sql
import static graffias.*

def db = [
    url: 'jdbc:h2:file:db/testDB',
    rser: 'sa',
    password: '',
    driver: 'org.h2.Driver'
]

def sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
Book.updateTable(sql)

get('/book') { req ->
    def books = []
    def id = req.getParameter('id')
    if (id)
        books = Book.find(sql, id)
    else
        books = Book.findAll(sql)
    req.setAttributes(books: books)
    setContentType 'text/html'
    view 'book.gsp'
}

post('/book') { req ->
    def id = req.getParameter('id')
    def title = req.getParameter('title')
    def author = req.getParameter('author')
    try {
        Book.save(sql, [id, title, author])
        setStatus 200
    } catch (e) {
        setStatus 400
    }
}

runServer()
