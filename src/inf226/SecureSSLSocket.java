package inf226;

import java.io.FileInputStream;
import java.security.*;
import javax.net.ssl.*;
import com.sun.net.ssl.internal.ssl.Provider;

public class SecureSSLSocket {
    private final int port;
    private final String serverName;
    private SSLContext sslContext;

    /**
     * This class should be split into 2 classes to ensure security
     * create Server Socket
     * @param port
     */
    public SecureSSLSocket(int port, String serverName) {
        this.serverName = serverName;
        this.port = port;
        try {
            Security.addProvider(new Provider());

            //Specifying the Keystore details
            //System.setProperty("javax.net.ssl.keyStore","keystore.pfx");
            //truststore
            //System.setProperty("javax.net.ssl.trustStore", "keystore.pfx");
            System.setProperty("javax.net.ssl.keyStorePassword","password");


            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            FileInputStream is = new FileInputStream("keystore.pfx");
            keyStore.load(is, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
            is.close();
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());

            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        } catch (Exception e) {
            System.err.println(e.getCause().toString());
        }
    }


    public SSLServerSocket createServerSocket() {
        // Registering the JSSE provider
        Security.addProvider(new Provider());

        //Specifying the Keystore details
        System.setProperty("javax.net.ssl.keyStore","keystore.pfx");
        //truststore
        System.setProperty("javax.net.ssl.trustStore", "keystore.pfx");
        System.setProperty("javax.net.ssl.keyStorePassword","password");

        // Enable debugging to view the handshake and communication which happens between the SSLClient and the SSLServer
        // System.setProperty("javax.net.debug","all");
        //keystore
        //keymanagerfactory -> keymanager
        //keymanager -> SSLContext.init(keymng, trustmanager/0, new secure random/0)

        try {

            // Initialize the Server Socket
            SSLServerSocketFactory sslServerSocketfactory = sslContext.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket)sslServerSocketfactory.createServerSocket(port);
            return sslServerSocket;
        } catch(Exception exp) {
            System.err.println("Failed to create socket");
        }
        return null; //TODO dont return null
    }

    public SSLSocket createClientSocket() {
        if (serverName == null)
            return null;
        String strServerName = serverName;
        // Registering the JSSE provider
        Security.addProvider(new Provider());

        try {

            // Creating Client Sockets
            SSLSocketFactory sslsocketfactory = sslContext.getSocketFactory();
            return (SSLSocket)sslsocketfactory.createSocket(strServerName, port);
        } catch(Exception exp) {
            System.err.println("Failed to create socket");
        }
        return null; //TODO dont return null
    }
}