How To Create and Install a Selfsigned SSL Certificate for use in Java
======================================================================

Overview
--------
  - We use the Java tool keytool to create and store a selfsigned certificate which can be used to secure https communication

Generate Root CA KeyPair and Store
----------------------------------
```bash
keytool -genkeypair \
  -keystore root.jks -storepass psssst \
  -keypass psssst \
  -alias 'My-Root-CA' \
  -dname "CN=My-Root-CA, OU=TS, O=Hewlett-Packard, L=Palo Alto, S=California, C=US" \
  -ext 'BasicConstraints=CA:true' \
  -validity 3650 \
  -v \
  -keyalg RSA \
  -keysize 4096
```

Export Root CA Key
------------------
```bash
keytool -exportcert \
  -keystore root.jks -storepass psssst \
  -alias 'My-Root-CA' \
  -rfc > root.pem
```

Import Root CA Key into Java Trusted Key Store
----------------------------------------------
```bash
keytool -importcert \
  -keystore /etc/ssl/certs/java/cacerts \
  -storepass changeit \
  -alias 'My-Root-CA' \
  -trustcacerts \
  -file root.pem
```

Generate Host KeyPair and Store
-------------------------------
```bash
keytool -genkeypair \
  -keystore host.jks -storepass psssst \
  -keypass psssst \
  -alias 'My-Host' \
  -dname "CN=myhost.emea.hpqcorp.net, OU=TS, \
  O=Hewlett-Packard, L=Palo Alto, S=California, C=US" \
  -ext 'san=dns:myhost.emea.hpqcorp.net,dns:myhost,ip:16.57.99.99' \
  -validity 3650 -v
```

Sign Host Key
-------------
```bash
keytool -certreq \
  -keystore host.jks -storepass psssst \
  -alias 'My-Host' |
keytool -gencert \
  -keystore root.jks -storepass psssst \
  -alias 'My-Root-CA' \
  -ext KeyUsage:critical=digitalSignature,keyEncipherment \
  -rfc > host-signed.pem
```

Import Signed Server Key into Server Keystore
---------------------------------------------
```bash
cat root.pem host-signed.pem > root+host.pem

keytool -importcert \
  -keystore host.jks -storepass psssst \
  -alias 'My-Host' \
  -trustcacerts \
  -file root+host.pem
```
