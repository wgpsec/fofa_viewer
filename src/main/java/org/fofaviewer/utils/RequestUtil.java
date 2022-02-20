package org.fofaviewer.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.gv7.woodpecker.requests.*;
import me.gv7.woodpecker.requests.exception.RequestsException;
import org.fofaviewer.main.FofaConfig;
import org.fofaviewer.main.ProxyConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import javax.net.ssl.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.hash.Hashing;
import org.tinylog.Logger;

public class RequestUtil {
    private static RequestUtil request = null;
    private ProxyConfig config;
    private final String[] ua = new String[]{
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Safari/605.1.15",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.41 Safari/537.36 Edg/88.0.705.22"
    };
    Pattern cnPattern = Pattern.compile("CommonName: ([\\w|\\.]+)\n\n");
    Pattern snPattern = Pattern.compile("Serial Number: (\\d+)\n");

    private RequestUtil() {
        config = ProxyConfig.getInstance();
    }

    public static RequestUtil getInstance() {
        if (request == null) {
            request = new RequestUtil();
        }
        return request;
    }

    private RequestBuilder getBuilder(String url){
        return config.getStatus() ? Requests.get(url).proxy(config.getProxy()) : Requests.get(url);
    }

    /**
     * 发起HTTP请求获取响应内容
     *
     * @param url 请求url
     * @return 响应内容
     * 200 : 请求响应内容
     * other code : request error
     * error ：请求失败
     */
    public HashMap<String, String> getHTML(String url, int connectTimeout, int socksTimeout) {
        RawResponse response;
        HashMap<String, String> result = new HashMap<>();
        try {
            response = getBuilder(url)
                    .headers(new HashMap<String, String>() {{ put("User-Agent", ua[(new SecureRandom()).nextInt(3)]); }})
                    .connectTimeout(connectTimeout)
                    .socksTimeout(socksTimeout)
                    .send();
        }catch (Exception e){
            Logger.warn(e);
            result.put("code", "error");
            result.put("msg", e.getMessage());
            return result;
        }
        if (response != null) {
            int code = response.statusCode();
            result.put("code", String.valueOf(code));
            try {
                if (code == 200) {
                    String body = response.readToText(); // 默认使用utf-8编码
                    result.put("msg", body);
                } else if (code == 401) {
                    result.put("msg", "请求错误状态码401，可能是没有在config中配置有效的email和key，或者您的账号权限不足无法使用api进行查询。");
                } else if (code == 502) {
                    result.put("msg", "请求错误状态码502，可能是账号限制了每次请求的最大数量，建议尝试修改config中的maxSize为100");
                } else {
                    result.put("msg", "请求响应错误,状态码" + code);
                }
                return result;
            } catch (Exception e) {
                Logger.warn(e);
                result.put("code", "error");
                result.put("msg", e.getMessage());
                return result;
            }
        }
        result.put("code", "error");
        return result;
    }

    /**
     * 提取网站favicon 需要两步：
     * 1. 直接访问url根目录的favicon，若404则跳转至第2步
     * 2. 访问网站，获取html页面，获取head中的 link标签的ico 路径
     *
     */
    public HashMap<String, String> getImageFavicon(String url) {
        Response<byte[]> response = null;
        HashMap<String, String> result = new HashMap<>();
        try {
            response = getBuilder(url)
                    .headers(new HashMap<String, String>() {{
                        put("User-Agent", ua[(new SecureRandom()).nextInt(3)]);
                    }})
                    .verify(false) // 忽略证书校验
                    .send()
                    .toBytesResponse();
        }catch (RequestsException e){
            Logger.warn(e);
            result.put("code", "error");
            result.put("msg", e.getMessage());
        }
        if (response != null) {
            int code = response.statusCode();
            result.put("code", String.valueOf(code));
            if (code == 200) {
                try {
                    byte[] resp1 = response.body();
                    if (resp1.length == 0) {
                        Logger.warn(url + "无响应内容");
                        return null;
                    }
                    String encoded = Base64.getMimeEncoder().encodeToString(resp1);
                    String hash = getIconHash(encoded);
                    result.put("msg", "icon_hash=\"" + hash + "\"");
                    return result;
                } catch (Exception e) {
                    result.put("code", "error");
                    result.put("msg", e.getMessage());
                    Logger.warn(e);
                    return result;
                }
            }
        }
        return result;
    }

    public String getLinkIcon(String url) {
        HashMap<String, String> result = getHTML(url, 10000,10000);
        if (result.get("code").equals("200")) {
            Document document = Jsoup.parse(result.get("msg"));
            Elements elements = document.getElementsByTag("link");
            if (elements.size() == 0) { // 没有link标签
                return null;
            } else {
                for (Element i : elements) {
                    String rel = i.attr("rel");
                    if (rel.equals("icon") || rel.equals("shortcut icon")) {
                        String href = i.attr("href");
                        if (href.startsWith("http")) { // link 显示完整url
                            return href;
                        } else if (href.startsWith("/")) {  // link 显示相对路径
                            return url + href;
                        }
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 计算favicon hash
     *
     * @param f favicon的文件对象
     * @return favicon hash值
     */
    private String getIconHash(String f) {
        int murmu = Hashing
                .murmur3_32()
                .hashString(f.replaceAll("\r","" )+"\n", StandardCharsets.UTF_8)
                .asInt();
        return String.valueOf(murmu);
    }

    private X509Certificate getX509Certificate(String host) throws Exception {
        URL url = new URL(host);
        HttpsURLConnection conn;
        if(config.getStatus()){
            conn = (HttpsURLConnection) url.openConnection(config.getProxy());
        }else{
            conn = (HttpsURLConnection) url.openConnection();
        }
        TrustModifier.relaxHostChecking(conn);
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);
        conn.connect();
        Certificate[] certs = conn.getServerCertificates();
        return (X509Certificate) certs[0];
    }

    /**
     * 获取证书编号
     * @param host 域名
     * @return 证书编号
     */
    public String getCertSerialNum(String host) {
        try {
            X509Certificate cert = getX509Certificate(host);
            return "cert=\"" + cert.getSerialNumber().toString() + "\"";
        } catch (Exception e) {
            Logger.warn(e);
            return null;
        }
    }

    /**
     * 从https证书中获取域名
     */
    public String getCertSubjectDomain(String host){
        try {
            X509Certificate cert = getX509Certificate(host);
            String subjectCN = cert.getSubjectDN().toString().split(",")[0];
            int i = subjectCN.lastIndexOf(".");
            int j = subjectCN.indexOf(".");
            return i==j ? subjectCN.substring(3) : subjectCN.substring(j+1);
        } catch (Exception e) {
            Logger.warn(e, host);
            return "";
        }
    }

    /**
     * 自动提示
     * @param key query content
     * @return hint
     */
    public Map<String,String> getTips(String key) {
        try {
            key = java.net.URLEncoder.encode(key, "UTF-8");
            HashMap<String, String> result = getHTML(FofaConfig.TIP_API + key, 3000, 5000);
            if (result.get("code").equals("200")) {
                JSONObject obj = JSON.parseObject(result.get("msg"));
                if(obj.getString("message").equals("ok")){
                    Map<String,String> data = new HashMap();
                    JSONArray objs = obj.getJSONArray("data");
                    for (Object o : objs) {
                        JSONObject tmp = (JSONObject) o;
                        data.put(tmp.getString("name") + "--" + tmp.getString("company"), "app=\""+tmp.getString("name")+"\"");
                    }
                    return data;
                }
            }
            return null;
        }catch (Exception e){
            Logger.warn(e);
            return null;
        }

    }

    /**
     * base64编码字符串
     *
     * @param str 字符串
     * @return 编码字符串
     */
    public String encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从fofa API 获取的证书信息中提取CommonName
     */
    public String getCertSubjectDomainByFoFa(String cert){
        Matcher matcher = cnPattern.matcher(cert);
        if(matcher.find()){
            return matcher.group(1);
        }
        return "";
    }

    /**
     * 从fofa API 获取的证书信息中提取Serial Number
     */
    public String getCertSerialNumberByFoFa(String cert){
        Matcher matcher = snPattern.matcher(cert);
        if(matcher.find()){
            return matcher.group(1);
        }
        return "";
    }
}
