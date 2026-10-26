import javax.net.ssl.*;
import java.net.URL;
import java.security.cert.X509Certificate;

public class SSLInspector {
    public static void main(String[] args) throws Exception {
        // 1. 配置全局代理指向 mitmproxy
        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyPort", "8080");

        // 2. 创建一个“间谍” TrustManager，专门用来偷窥证书
        TrustManager[] spyTrustManager = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                // 这是验证服务器证书的核心回调
                public void checkServerTrusted(X509Certificate[] certs, String authType) throws java.security.cert.CertificateException {
                    System.out.println("\n--- 🕵️ 截获到服务器证书链 ---");
                    for (int i = 0; i < certs.length; i++) {
                        String issuer = certs[i].getIssuerX500Principal().getName();
                        String subject = certs[i].getSubjectX500Principal().getName();
                        System.out.println(String.format("证书 [%d] 主题 (Subject): %s", i, subject));
                        System.out.println(String.format("证书 [%d] 颁发 (Issuer) : %s", i, issuer));
                        
                        // 确认是否为 eMudhra 伪造
                        if (issuer.contains("eMudhra") || issuer.contains("emSign")) {
                            System.out.println("⚠️ 确凿证据：发现 eMudhra / emSign 签发痕迹！");
                        }
                    }
                    System.out.println("------------------------------\n");
                    
                    // 偷窥完毕后，主动抛出异常切断连接（模拟安全拦截）
                    throw new java.security.cert.CertificateException("测试完毕，已主动阻断不安全连接！");
                }
            }
        };

        // 3. 将间谍 TrustManager 注入到全局 SSL 上下文
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, spyTrustManager, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // 4. 发起请求
        try {
            HttpsURLConnection conn = (HttpsURLConnection) new URL("https://example.com").openConnection();
            conn.connect();
        } catch (Exception e) {
            System.out.println("拦截结果: " + e.getMessage());
        }
    }
}
