package org.apache.camel.component.file.remote;

import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;

public class FileToFtpsImplicitSSLWithClientAuthAndSSLContextParametersTest extends FileToFtpsImplicitSSLWithClientAuthTest {
    @Override
    protected JndiRegistry createRegistry() throws Exception {
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource("server.jks");
        ksp.setPassword("password");
        
        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyPassword("password");
        kmp.setKeyStore(ksp);
        
        TrustManagersParameters tmp = new TrustManagersParameters();
        tmp.setKeyStore(ksp);
        
        SSLContextParameters sslContextParameters = new SSLContextParameters();
        sslContextParameters.setSecureSocketProtocol("SSL");
        sslContextParameters.setKeyManagers(kmp);
        sslContextParameters.setTrustManagers(tmp);
        
        JndiRegistry registry = super.createRegistry();
        registry.bind("sslContextParameters", sslContextParameters);
        return registry;
    }
    
    protected String getFtpUrl() {
        return "ftps://admin@localhost:" + getPort() + "/tmp2/camel?password=admin&consumer.initialDelay=2000&disableSecureDataChannelDefaults=true"
                + "&isImplicit=true&sslContextParameters=#sslContextParameters&delete=true";
    }
}