import static graffias.*

get('/') {
    'Hello World!'
}

// http://wiki.eclipse.org/Jetty/Howto/Configure_SSL
def ssl = [
    keyStore: './etc/keystore',
    keyStorePassword: 'OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4',
    keyManagerPassword: 'OBF:1u2u1wml1z7s1z7a1wnl1u2g',
    trustStore: './etc/keystore',
    trustStorePassword: 'OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4'
]
runServer(8443, '/', ssl)
