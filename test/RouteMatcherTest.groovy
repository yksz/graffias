import static graffias.*
import groovy.util.GroovyTestCase

class RouteMatcherTest extends GroovyTestCase {
    void testMatchesWildcard() {
        assert matchesWildcard('/abc/*', '/abc/def') { assert it == 'def' }
        assert matchesWildcard('/abc/*', '/abc/def/ghi') { assert it == 'def/ghi' }
        assert matchesWildcard('/abc//*', '/abc//def') { assert it == 'def' }
        assert !matchesWildcard('/abc', '/abc') {}
        assert !matchesWildcard('/abc//*', '/abc/def') {}
        assert !matchesWildcard('/abc/**', '/abc/def') {}
    }

    void testMatchesNamedParams() {
        assert matchesNamedParameters('/abc/:', '/abc/def') { assert it[''] == 'def' }
        assert matchesNamedParameters('/abc/:1', '/abc/def') { assert it['1'] == 'def' }
        assert matchesNamedParameters('/abc/:1/:2', '/abc/def/ghi') { assert it['1'] == 'def'; assert it['2'] == 'ghi' }
        assert matchesNamedParameters('/abc/:1/ghi', '/abc/def/ghi') { assert it['1'] == 'def' }
        assert matchesNamedParameters('/abc/:1/ghi/:2', '/abc/def/ghi/jkl') { assert it['1'] == 'def'; assert it['2'] == 'jkl' }
        assert !matchesNamedParameters('/abc', '/abc') {}
        assert !matchesNamedParameters('/abc/:1', '/abc//def') {}
    }
}

