package com.sjl;

public class Main {
    public static void main(String... anArgs) throws Exception {
        new Main().start();
    }

    private WebServer server;

    public Main() {
        String webPort = System.getenv("PORT");

        if(webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        server = new WebServer(WebServerConfig.Factory.newDevelopmentConfig("server", Integer.parseInt(webPort), "localhost"));
    }

    public void start() throws Exception {
        server.start();
        server.join();
    }
}
