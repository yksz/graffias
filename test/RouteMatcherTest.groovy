import static graffias.*
import groovy.util.GroovyTestCase

class RouteMatcherTest extends GroovyTestCase {
    void testMatchesWildcard() {
        assert matchesWildcard('/abc/*', '/abc') { servletPath, pathInfo ->
            assert servletPath == '/abc'
            assert pathInfo == null
        }
        assert matchesWildcard('/abc/*', '/abc/') { servletPath, pathInfo ->
            assert servletPath == '/abc'
            assert pathInfo == '/'
        }
        assert matchesWildcard('/abc/*', '/abc/def') { servletPath, pathInfo ->
            assert servletPath == '/abc'
            assert pathInfo == '/def'
        }
        assert matchesWildcard('/abc/*', '/abc/def/ghi') { servletPath, pathInfo ->
            assert servletPath == '/abc'
            assert pathInfo == '/def/ghi'
        }
        assert matchesWildcard('/abc//*', '/abc//def') { servletPath, pathInfo ->
            assert servletPath == '/abc/'
            assert pathInfo == '/def'
        }
        assert !matchesWildcard('/abc', '/abc') {}
        assert !matchesWildcard('/abc//*', '/abc/def') {}
        assert !matchesWildcard('/abc/**', '/abc/def') {}
    }

    void testMatchesNamedParams() {
        assert matchesNamedParameters('/abc/:', '/abc/def') { params ->
            assert params[''] == 'def'
        }
        assert matchesNamedParameters('/abc/:x', '/abc/def') { params ->
            assert params.x == 'def'
        }
        assert matchesNamedParameters('/abc/:x/:y', '/abc/def/ghi') { params ->
            assert params.x == 'def'
            assert params.y == 'ghi'
        }
        assert matchesNamedParameters('/abc/:x/ghi', '/abc/def/ghi') { params ->
            assert params.x == 'def'
        }
        assert matchesNamedParameters('/abc/:x/ghi/:y', '/abc/def/ghi/jkl') { params ->
            assert params.x == 'def'
            assert params.y == 'jkl'
        }
        assert !matchesNamedParameters('/abc', '/abc') {}
        assert !matchesNamedParameters('/abc/:x', '/abc//def') {}
    }
}
