openssl pkcs12 -export -in server-crt.pem -inkey server-key.pem -out server.p12 -name "certificate"

keytool -importkeystore -srckeystore server.p12 -srcstoretype pkcs12 -destkeystore cert.jks
keytool -importcert -file ca-crt.pem -keystore trustedCerts.jks