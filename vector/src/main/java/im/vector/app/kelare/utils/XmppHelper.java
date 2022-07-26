package im.vector.app.kelare.utils;

import android.text.TextUtils;


import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.SslContextFactory;
import org.jivesoftware.smack.util.TLSUtils;
import org.jxmpp.stringprep.XmppStringprepException;
import org.minidns.dnsname.DnsName;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * author : Jason
 * date   : 3/24/22 2:27 PM
 * desc   :
 */
public class XmppHelper {

    /**
     *
     * @return XMPPConnection是否连接并登陆
     */
    public static boolean isXMPPConnectedAndLogin(XMPPTCPConnection connection) {
        return connection != null && connection.isConnected() && connection.isAuthenticated();
    }

    public XMPPTCPConnection initXmppConfig(String address, String proxyAdd){

        XMPPTCPConnection connection = null;

        //trustAllHosts();
        XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
        //设置连接超时的最大时间
        builder.setConnectTimeout(10000);
        //设置登录openfire的用户名和密码
        builder.setUsernameAndPassword("", "");
        //设置安全模式
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible);
//        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
//        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);


        /**
         * very very important, this will verify certificate
         */
        TLSUtils.acceptAllCertificates(builder);

        //设置服务器名称
//        builder.setServiceName(address);
        try {
            builder.setXmppDomain(address);
            if (!TextUtils.isEmpty(proxyAdd)) {
                //set proxy
                builder.setHost(proxyAdd);
            }
            //builder.setCompressionEnabled(false);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        //是否查看debug日志
        builder.enableDefaultDebugger();
//        builder.setDebuggerEnabled(true);

        //设置端口号
        builder.setPort(5222);

        connection = new XMPPTCPConnection(builder.build());

//        connection.setUseStreamManagement(true);
//        connection.setUseStreamManagementResumption(true);

        return connection;
    }

    public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android use X509 cert
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }


}
