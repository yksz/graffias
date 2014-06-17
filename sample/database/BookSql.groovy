import groovy.sql.Sql

class BookSql {
    static def createBook(sql) {
        sql.withTransaction {
            sql.execute '''
                create table if not exists BOOK (
                    id int not null primary key,
                    title nvarchar(50) not null unique,
                    author nvarchar(50),
                )
            '''
            if (countBook(sql) == 0) {
                // default books
                saveBook(sql, [1, 'abc', '123'])
                saveBook(sql, [2, 'def', '456'])
            }
        }
    }

    static def countBook(sql) {
        def rows = sql.rows '''
            select count(*) from BOOK
        '''
        rows[0]['COUNT(*)']
    }

    static def findBook(sql, id) {
        sql.firstRow '''
            select * from BOOK
            where id = ?
        ''', [id]
    }

    static def findAllBooks(sql) {
        sql.rows '''
            select * from BOOK
        '''
    }

    static def saveBook(sql, params) {
        sql.execute '''
            insert into BOOK (id, title, author)
            values (?, ?, ?)
        ''', params
    }
}
