package jp.co.syslinks.sscce.java.http;

import java.util.Arrays;

import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;

public class HttpClient5 {

    public static void main(String[] args) throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            ClassicHttpRequest httpGet = ClassicRequestBuilder.get("http://httpbin.org/get").build();
            // The underlying HTTP connection is still held by the response object
            // to allow the response content to be streamed directly from the network socket.
            // In order to ensure correct deallocation of system resources
            // the user MUST call CloseableHttpResponse#close() from a finally clause.
            // Please note that if response content is not fully consumed the underlying
            // connection cannot be safely re-used and will be shut down and discarded
            // by the connection manager.
            httpclient.execute(httpGet, response -> {
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                System.out.println(response.getLocale());
                final HttpEntity entity1 = response.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                EntityUtils.consume(entity1);
                return null;
            });

            ClassicHttpRequest httpPost = ClassicRequestBuilder.post("http://httpbin.org/post") //
                    .setEntity(new UrlEncodedFormEntity(Arrays.asList( //
                            new BasicNameValuePair("username", "vip") //
                            , new BasicNameValuePair("password", "secret")) //
                    )).build();
            httpclient.execute(httpPost, response -> {
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                System.out.println(response.getLocale());
                final HttpEntity entity2 = response.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                EntityUtils.consume(entity2);
                return null;
            });
        }
    }

}
