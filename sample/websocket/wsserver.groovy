import static graffias.*

websocket('/') { req, protocol ->
    def connection
    onopen { conn ->
        connection = conn
    }
    onclose { code, msg ->
        if (connection)
            connection.close()
    }
    onmessage { msg ->
        connection.sendMessage(msg)
    }
}

runServer()
