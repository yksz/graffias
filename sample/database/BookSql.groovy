import groovy.sql.Sql

class BookSql {
    static def createBook(sql) {
        sql.withTransaction {
            sql.execute '''
                CREATE TABLE IF NOT EXISTS book (
                    id int NOT NULL PRIMARY KEY,
                    title varchar(50) NOT NULL,
                    author varchar(50) NOT NULL
                )
            '''
            if (countBooks(sql) == 0) {
                // default books
                saveBook(sql, [1, 'abc', '123'])
                saveBook(sql, [2, 'def', '456'])
            }
        }
    }

    static def countBooks(sql) {
        def rows = sql.rows '''
            SELECT COUNT(*) FROM book
        '''
        rows[0]['COUNT(*)']
    }

    static def findBook(sql, id) {
        sql.firstRow '''
            SELECT * FROM book
            WHERE id = ?
        ''', [id]
    }

    static def findAllBooks(sql) {
        sql.rows '''
            SELECT * FROM book
        '''
    }

    static def saveBook(sql, params) {
        sql.execute '''
            INSERT INTO book (id, title, author)
            VALUES (?, ?, ?)
        ''', params
    }
}
