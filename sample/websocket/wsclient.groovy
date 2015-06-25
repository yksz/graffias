@Grab(group='org.eclipse.jetty', module='jetty-websocket', version='8.1.7.v20120910')
import org.eclipse.jetty.websocket.*
import java.util.concurrent.TimeUnit

url = "ws://localhost:8080"

def factory = new WebSocketClientFactory()
factory.start()
def client = factory.newWebSocketClient()
def connection = client.open(new URI(url), new WebSocket.OnTextMessage() {
    void onOpen(WebSocket.Connection connection) {
    }

    void onClose(int closeCode, String message) {
    }

    void onMessage(String data) {
        println data
    }
}).get(5, TimeUnit.SECONDS)
connection.sendMessage("Hello World")
connection.close()
factory.stop()
