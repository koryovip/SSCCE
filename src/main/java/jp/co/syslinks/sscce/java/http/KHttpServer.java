package jp.co.syslinks.sscce.java.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Date;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * @see https://qiita.com/niwasawa/items/05ec2ab756d6b291791a
 */
@SuppressWarnings("restriction")
public class KHttpServer implements Closeable {

    private HttpServer server;

    public void start(int port) throws IOException {
        if (server != null) {
            return;
        }
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/", new MyHandler());
        System.out.println("MyServer wakes up: port=" + port);
        server.start();
    }

    @Override
    public void close() throws IOException {
        if (server == null) {
            return;
        }
        System.out.println("MyServer Stopping...");
        server.stop(0); // 最大待機時間
    }

    private static class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            {
                final Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("date", new Date().toString());
                responseHeaders.set("server", "nginx");
                responseHeaders.set("content-type", ContentTypeMng.me.get("html"));
            }
            exchange.sendResponseHeaders(200, 2);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write("ok".getBytes());
            }
        }

    }
}
