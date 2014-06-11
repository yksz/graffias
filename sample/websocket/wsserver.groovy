import static graffias.*

websocket('/') { protocol ->
    def connection
    onopen { conn ->
        connection = conn
    }
    onclose { code ->
        if (connection)
            connection.close()
    }
    onmessage { msg ->
        connection.sendMessage(msg)
    }
}

runServer()
