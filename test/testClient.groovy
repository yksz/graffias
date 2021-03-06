@Grapes([
    @Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1'),
    @Grab('org.eclipse.jetty:jetty-websocket:8.1.7.v20120910')
])
import static groovy.test.GroovyAssert.*
import static groovyx.net.http.ContentType.*
import groovy.util.GroovyTestCase
import groovyx.net.http.*
import java.util.concurrent.TimeUnit
import org.eclipse.jetty.websocket.*

class GraffiasTest extends GroovyTestCase {
    static final def HOST = 'localhost'
    static final def PORT = 55555

    def http = new HTTPBuilder("http://${HOST}:${PORT}")

    void testGet_Override() {
        http.get(path: '/', contentType: TEXT) { resp, reader ->
            assert resp.contentType == 'text/html'
            assert reader.text == '<html><h1>INDEX</h1></html>\n'
        }
    }

    void testGet_SameURI() {
        http.get(path: '/stackoverflow.html', contentType: TEXT) { resp, reader ->
            assert resp.contentType == 'text/html'
            assert reader.text == '<html><h1>stackoverflow</h1></html>\n'
        }
    }

    void testPost_RequestParameters() {
        http.post(path: '/', query: [key:'value']) { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'key=value'
        }
    }

    void testGet_WildcardPath01() {
        http.get(path: '/wildcard/foo') { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'servletPath=/wildcard, pathInfo=/foo'
        }
    }

    void testGet_WildcardPath02() {
        http.get(path: '/wildcard') { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'servletPath=/wildcard, pathInfo=null'
        }
    }

    void testGet_NamedParameters() {
        http.get(path: '/foo/params') { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'name=foo'
        }
    }

    void testGet_Pattern() {
        http.get(path: '/pattern/foo') { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'name=foo'
        }
    }

    void testFilter() {
        http.get(path: '/filter') { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'filter=on'
        }
    }

    void testFilter_Wildcard() {
        http.get(path: '/filter/wildcard/foo') { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'servletPath=/filter/wildcard, pathInfo=/foo'
        }
    }

    void testFilter_NamedParameters() {
        http.get(path: '/filter/foo/params') { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'name=foo'
        }
    }

    void testFilter_Pattern() {
        http.get(path: '/filter/pattern/foo') { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'name=foo'
        }
    }

    void testGet_Groovy() {
        http.get(path: '/groovy') { resp, html ->
            assert resp.contentType == 'text/html'
            assert html.BODY.P.size() == 3
            html.BODY.P.each {
                assert it == 'groovy'
            }
        }
    }

    void testGet_Gsp() {
        http.get(path: '/gsp') { resp, html ->
            assert resp.contentType == 'text/html'
            assert html.BODY.P.size() == 3
            html.BODY.P.each {
                assert it == 'gsp'
            }
        }
    }

    void testError_404() {
        def e = shouldFail(HttpResponseException) {
            http.get(path: '/notfound', contentType: TEXT)
        }
        assert e.statusCode == 404
        assert e.message == 'Not Found'
    }

    void testWebXml_Classes() {
        http.get(path: '/classes') { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'TestServlet'
        }
    }

    void testWebSocket() {
        def url = "ws://${HOST}:${PORT}/websocket"
        def subProtocol = 'sub'
        def sendMessage = 'Hello World'

        def factory = new WebSocketClientFactory()
        factory.start()
        def client = factory.newWebSocketClient()
        client.protocol = subProtocol

        def webSocket = new WebSocket.OnTextMessage() {
            void onOpen(WebSocket.Connection connection) {
            }

            void onClose(int closeCode, String message) {
                assert closeCode == 1000
                assert message == null
            }

            void onMessage(String data) {
                assert data == "${subProtocol}:${sendMessage}"
            }
        }
        def connection
        try {
            connection = client.open(new URI(url), webSocket, 5, TimeUnit.SECONDS)
            connection.sendMessage(sendMessage)
            Thread.sleep(10)
        } finally {
            connection?.close()
            factory.stop()
        }
    }
}
