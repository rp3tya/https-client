package ro.go.repet.https;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * A factory for SSLContexts. Builds an SSLContext with custom KeyStore and TrustStore, to work with a client cert
 * signed by a self-signed CA cert.
 */
public class CustomContextFactory {

	/** instance */
	private static CustomContextFactory instance = null;

	/** */
	private CustomContextFactory() {
		//
	}

	/**
	 * @return instance
	 */
	public static CustomContextFactory getInstance() {
		if (instance == null) {
			instance = new CustomContextFactory();
		}
		//
		return instance;
	}

	/**
	 * Creates an SSLContext with the client and server certificates
	 * 
	 * @param clientCert
	 *            A stream containing the client certificate
	 * @param clientCertPwd
	 *            Password for the client certificate
	 * @param serverCert
	 *            A stream containing the server certificate
	 * @return An initialized SSLContext
	 * @throws Exception
	 */
	public SSLContext makeContext(InputStream clientCert, String clientCertPwd, InputStream serverCert)
			throws Exception {
		final KeyStore keyStore = this.loadPKCS12KeyStore(clientCert, clientCertPwd);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
		kmf.init(keyStore, clientCertPwd.toCharArray());
		KeyManager[] keyManagers = kmf.getKeyManagers();
		//
		final KeyStore trustStore = this.loadPEMTrustStore(serverCert);
		TrustManager[] trustManagers = { new CustomTrustManager(trustStore) };
		//
		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(keyManagers, trustManagers, null);
		//
		return sslContext;
	}

	/**
	 * Produces a KeyStore from a PKCS12 (.p12) certificate file, typically the client certificate
	 * 
	 * @param certStream
	 *            A file containing the client certificate
	 * @param certPassword
	 *            Password for the certificate
	 * @return A KeyStore containing the certificate from the certificateFile
	 * @throws Exception
	 */
	private KeyStore loadPKCS12KeyStore(InputStream certStream, String certPassword) throws Exception {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		//
		keyStore.load(certStream, certPassword.toCharArray());
		//
		return keyStore;
	}

	/**
	 * Produces a KeyStore from a String containing a PEM certificate (typically, the server's CA certificate)
	 * 
	 * @param certificateString
	 *            A String containing the PEM-encoded certificate
	 * @return a KeyStore (to be used as a trust store) that contains the certificate
	 * @throws Exception
	 */
	private KeyStore loadPEMTrustStore(InputStream certStream) throws Exception {
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(certStream);
		String alias = certificate.getSubjectX500Principal().getName();
		//
		trustStore.load(null);
		trustStore.setCertificateEntry(alias, certificate);
		//
		return trustStore;
	}

}
