package com.inspur.util;

import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HttpUtils {

    public static final int tokenPort = 18444;
    private static Map<String, String> tokenMap = new HashMap<>();
    private static Map<String, Long> expiresinMap = new HashMap<>();

    public static String doGet(String url, Map<String, String> params) throws Exception {
        HttpGet httpGet;
        if (params != null && !params.isEmpty()) {
            String newUrl = getNewUrl(url, params);
            httpGet = new HttpGet(newUrl);
        } else {
            httpGet = new HttpGet(url);
        }
        setAuthHeader(httpGet);
        String result = "";
        try (final CloseableHttpClient httpClient = createCloseableHttpClient(url);
             final CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
        }
        return result;
    }

    public static String doDelete(String url, Map<String, String> params) throws Exception {
        HttpDelete httpDelete;
        if (!params.isEmpty()) {
            String newUrl = getNewUrl(url, params);
            httpDelete = new HttpDelete(newUrl);
        } else {
            httpDelete = new HttpDelete(url);
        }
        setAuthHeader(httpDelete);
        try (final CloseableHttpClient httpClient = createCloseableHttpClient(url);
             final CloseableHttpResponse response = httpClient.execute(httpDelete)) {
            return getResponseStr(response);
        }
    }

    private static String getNewUrl(String url, Map<String, String> params) {
        StringBuilder builder = new StringBuilder(url);
        builder.append("?");
        params.forEach((k, v) -> builder.append(k).append("=").append(v).append("&"));
        builder.deleteCharAt(builder.lastIndexOf("&"));
        return builder.toString();
    }


    public static String doJsonPost(String url, Map<String, Object> params) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        setAuthHeader(httpPost);
        String jsonStr = JSONObject.fromObject(params).toString();
        StringEntity stringEntity = new StringEntity(jsonStr, "UTF-8");
        httpPost.addHeader("Content-type", "application/json");
        httpPost.setEntity(stringEntity);
        try (final CloseableHttpClient httpClient = createCloseableHttpClient(url);
             final CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return getResponseStr(response);
        }
    }

    public static String doFilePost(String url, Map<String, String> headers, Map<String, String> params, Map<String, File> files) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        setAuthHeader(httpPost);

        if (headers != null) {
            headers.forEach(httpPost::addHeader);
        }

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        files.forEach((k, v) -> builder.addPart(k, new FileBody(v)));
        if (null != params) {
            params.forEach(builder::addTextBody);
        }

        httpPost.setEntity(builder.build());
        try (final CloseableHttpClient httpClient = createCloseableHttpClient(url);
             final CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return getResponseStr(response);
        }
    }


    public static String doFormPost(String url, Map<String, String> params) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        setAuthHeader(httpPost);

        List<NameValuePair> paramsList = convertNameValue(params);

        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(paramsList);
        httpPost.setEntity(formEntity);

        httpPost.addHeader("Content-type", "application/x-www-form-urlencoded");

        try (final CloseableHttpClient httpClient = createCloseableHttpClient(url);
             final CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return getResponseStr(response);
        }
    }

    public static String doJsonPut(String url, Map<String, Object> params) throws Exception {
        HttpPut httpPut = new HttpPut(url);
        setAuthHeader(httpPut);
        httpPut.addHeader("Content-Type", "application/json; charset=utf-8");
        if (params != null && !params.isEmpty()) {
            String jsonStr = JSONObject.fromObject(params).toString();
            StringEntity stringEntity = new StringEntity(jsonStr, "UTF-8");
            httpPut.setEntity(stringEntity);
        }
        try (final CloseableHttpClient httpClient = createCloseableHttpClient(url);
             final CloseableHttpResponse response = httpClient.execute(httpPut)) {
            return getResponseStr(response);
        }
    }

    private static String getResponseStr(CloseableHttpResponse response) throws IOException {
        String result = "";
        if (String.valueOf(response.getStatusLine().getStatusCode()).startsWith("20")) {
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
        }
        return result;
    }

    private static void setAuthHeader(HttpRequestBase request) {
        Map<String, String> header = getHeaderMap(EnvUtils.getAuthHost(),
                EnvUtils.getAuthUser(), EnvUtils.getAuthPwd(), EnvUtils.getREALM());
        header.forEach(request::addHeader);
    }


    private static List<NameValuePair> convertNameValue(Map<String, String> map) {
        List<NameValuePair> convertedList = new LinkedList<>();
        map.forEach((key, value) -> convertedList.add(new BasicNameValuePair(key, value)));
        return convertedList;
    }

    public static CloseableHttpClient createCloseableHttpClient(String url) throws Exception {
        if (!url.toLowerCase().startsWith("https")) {
            return HttpClients.createDefault();
        }
        SSLContext sslcontext = createIgnoreVerifySSL();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connManager.setMaxTotal(100);
        return HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .setConnectionManager(connManager)
                .build();
    }


    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLS");
        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

    public static Map<String, String> getHeaderMap(String authHostIp, String user, String pwd, String realm) {
        Map<String, String> headers = new HashMap<>();
        String token = getToken(authHostIp, realm, user, pwd, false);
        String cookie = generateCookie(token);
        headers.put("Cookie", cookie);
        return headers;
    }

    public static String generateCookie(String token) {
        StringBuilder cookieBuilder = new StringBuilder();
        cookieBuilder.append(token);
        return cookieBuilder.toString();
    }


    public static synchronized String getToken(String authHostIp, String realmname, String user, String pwd, boolean noCache) {
        if (noCache || !expiresinMap.containsKey(realmname) || expiresinMap.get(realmname) - System.currentTimeMillis() < 200000L) {
            String url = "http://" + authHostIp + ":" + tokenPort + "/gateway/default/knoxtoken/api/v1/token";
            String np = EnvUtils.getNifiUsername() + "-" + realmname + ":" + EnvUtils.getNifiPassword();
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + org.apache.commons.codec.binary.Base64.encodeBase64String(np.getBytes()));
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            //System.out.println(execPost(url, headers, ""));
            JSONObject access = JSONObject.fromObject(execPost(url, headers, ""));

            expiresinMap.put(realmname, Long.valueOf(access.getString("expires_in")));
            tokenMap.put(realmname, realmname + "-jwt=" + access.getString("access_token"));
        }
        return tokenMap.get(realmname);
    }

    public static String execPost(String url, Map<String, String> headers, String parameters) {
        String str = "{error:404}";
        HttpPost httpPost = new HttpPost(url);
        if (StringUtil.isNotEmpty(headers)) {
            for (String key : headers.keySet()) {
                httpPost.addHeader(key, headers.get(key));
            }
        }
        if (StringUtil.isNotEmpty(parameters)) {
            httpPost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));
        }
        try (final CloseableHttpClient httpclient = createCloseableHttpClient(url);
             final CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            str = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
        } catch (Exception e) {
            System.out.println("POST method failed to access URL，URL：" + url + "\n" + e);
        }
        return str;
    }

    public static synchronized String getCookie() {
        //TODO 代码获取
        return "clustername=cluster1; JSESSIONID=837EF466BD584F1E661A800E24AFAEF6; session=.eJwNjd1uwiAYQF9l4QloHRd6aSzdcKDl5yv0Di1JI7Qjpplbje--XpyLk5zkPNH8HcOEdk_0dkE71Nl09DX7cGU6e5MZ2My4IVlZwS7gsMOCiVgY0EyERN-1zRAqcujLNAB2D9Xmn9VNa7MIMS4aJOV1J79GIGra-wbHcm2Ax23lNv0ZannXVO6F2VbKypOsIm7b-OtNYY1mHGr-WB80GGJ8nbuQ0kaDOJ4Uubmx_xNA_TVm3I_sUybRNDjfmwQrcfYFTGoR5HpwRdBi5jAouQw39Hr9AwwDUlE.EBAMCg.K8J_dSxZgAUJrJTMZfv2Ak3BVcU; username=admin; tenantrealm=realm; manage-sg-user=sysadmin-realm; dev-sg-user=sysadmin-realm; clustername=cluster1; hadoop-jwt=eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJLTk9YU1NPIiwic3ViIjoic3lzYWRtaW4iLCJleHAiOjE1NjM0NTY3MzN9.LfXUwDGUmej0m3KenPtWUFfLxtnirY9mxcvng2HdneaQhMmRmaMPPIlIyM9txwSZ3Px7NKb9SqANjDPTaZmV1Riqi8uK7xQDy2yU4MnH4m0FpN78csijzwvefuvUzJIiZdQ_lQ9KzwDtQgjuwLgUsqEybb37_Jswh11E58bpQZGW_jmf_HjFNDMDTF4VrZm8z_o389OwRCrxbxnV49Z3HPmKyIUagLfj_5YzCqDjmgtTpGjH5fk8M3PnWlOqahSJ7tUMC3-UrOFLwDG4-Ds1tTjiYlYjZ7VXU_64Qcnf0IcoMyBXHYJFb1hibWJy25Um_NRnJxwF5uTijwJYavFfhA; AMBARISESSIONID=awsgr9zzzclbg6n120dhtti; realm-jwt=eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJLTk9YU1NPIiwic3ViIjoic3lzYWRtaW4iLCJleHAiOjE1NjM0NTc5NTl9.A5_04DrmpRmHtSP5-J_EtD4-sRDNt8_cHpyvtnRYorWf_i8WpfCZC4xzW2f0tdDJ14d02IZB9urK3cpTx2QJlAaAjU4SoEz1msw4jGl5UlL0653pNDAgZnkQfucWnm54X-8at-hv-aW4M44o1TkGwKzGN02VqId-ZhxZHBUcNY8l9S8igFpA2mvZXGP0XF9FGDt8CDSvll2IJ6kOiMJz8dOMKsiZvzmXVKJ5JMJK9NGxMILoFSWllK2J_3VOUG7GhzTFEUQZesOZ7ak7eD7ucX8TWd9MrG_t2PnZcg-VBF6n6WsTnacOXXVlBZb4a38DMNV8EICfZYuO2U2XFhN-WQ";
    }
}
