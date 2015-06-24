@Grapes([
    @Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')
])
import groovy.util.GroovyTestCase
import static groovy.test.GroovyAssert.*
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*

class GraffiasTest extends GroovyTestCase {
    def http = new HTTPBuilder('http://localhost:55555')

    void testGet_Override() {
        http.get(path: '/', contentType: TEXT) { resp, reader ->
            assert resp.contentType == 'text/html'
            assert reader.text == '<html><h1>INDEX</h1></html>\n'
        }
    }

    void testPost_Parameter() {
        http.post(path: '/', query: [key:'value']) { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'key=value'
        }
    }

    void testGet_WildcardPath() {
        http.get(path: '/wildcard/foobar') { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'path=/foobar'
        }
    }

    void testFilter() {
        http.get(path: '/filter') { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'filter=on'
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

    void testWebXml() {
        http.get(path: '/classes') { resp, reader ->
            assert resp.contentType == 'text/plain'
            assert reader.text == 'TestServlet'
        }
    }
}
