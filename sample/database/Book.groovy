import groovy.sql.Sql

class Book {
    static def createTable(sql) {
        sql.withTransaction {
            sql.execute '''
                create table BOOK (
                    id int not null primary key,
                    title nvarchar(50) not null unique,
                    author nvarchar(50),
                )
            '''
            // default books
            save(sql, [1, 'abc', '123'])
            save(sql, [2, 'def', '456'])
        }
    }

    static def updateTable(sql) {
        try {
            count(sql)
        } catch (e) {
            createTable(sql)
        }
    }

    static def count(sql) {
        def rows = sql.rows '''
            select count(*) from BOOK
        '''
        rows[0]['COUNT(*)']
    }

    static def find(sql, id) {
        sql.firstRow '''
            select * from BOOK
            where id = ?
        ''', [id]
    }

    static def findAll(sql) {
        sql.rows '''
            select * from BOOK
        '''
    }

    static def save(sql, params) {
        sql.execute '''
            insert into BOOK (id, title, author)
            values (?, ?, ?)
        ''', params
    }
}
