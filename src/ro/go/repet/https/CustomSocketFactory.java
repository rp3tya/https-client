package ro.go.repet.https;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Wraps an SSLSocketFactory and creates sockets with configured protocols only
 */
public class CustomSocketFactory extends SSLSocketFactory {

	/** factory */
	private SSLSocketFactory factory = null;

	/** protocols */
	private String[] protocols = null;

	/**
	 * @param factory
	 */
	public CustomSocketFactory(SSLSocketFactory factory, String... protocol) {
		this.factory = factory;
		this.protocols = protocol;
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return this.factory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return this.factory.getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		return this.forceProtocol(this.factory.createSocket(s, host, port, autoClose));
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return this.forceProtocol(this.factory.createSocket(host, port));
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return this.forceProtocol(this.factory.createSocket(host, port));
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException,
			UnknownHostException {
		return this.forceProtocol(this.factory.createSocket(host, port, localHost, localPort));
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
			throws IOException {
		return this.forceProtocol(this.factory.createSocket(address, port, localAddress, localPort));
	}

	/**
	 * @param socket
	 * @return
	 */
	private Socket forceProtocol(Socket socket) {
		//
		if (socket instanceof SSLSocket) {
			SSLSocket sslSocket = (SSLSocket) socket;
			SSLParameters sslParams = sslSocket.getSSLParameters();
			//
			sslParams.setProtocols(this.protocols);
			sslSocket.setSSLParameters(sslParams);
		}
		//
		return socket;
	}

}
