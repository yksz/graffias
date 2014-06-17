@Grab(group='com.h2database', module='h2', version='1.3.+')
import groovy.sql.Sql
import static graffias.*

def db = [
    url: 'jdbc:h2:file:./db/testDB;MVCC=TRUE;LOCK_TIMEOUT=10000',
    rser: 'sa',
    password: '',
    driver: 'org.h2.Driver'
]

def sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
Sql.mixin(BookSql)
sql.createBook()

get('/book') { req ->
    def books = []
    def id = req.getParameter('id')
    if (id)
        books << sql.findBook(id)
    else
        books = sql.findAllBooks()
    req.setAttributes(books: books)
    setContentType 'text/html'
    view 'book.gsp'
}

post('/book') { req ->
    def id = req.getParameter('id')
    def title = req.getParameter('title')
    def author = req.getParameter('author')
    try {
        sql.saveBook([id, title, author])
        setStatus 200
    } catch (e) {
        setStatus 400
    }
}

runServer()
