package it.unipi.erasmusnest.dbconnectors;

public class ConnectionManager {

    private final String host;
    private final int port;

    public ConnectionManager(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
    	return host;
    }

    public int getPort() {
    	return port;
    }

}
