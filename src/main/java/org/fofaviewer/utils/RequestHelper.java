package org.fofaviewer.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.gv7.woodpecker.requests.RawResponse;
import me.gv7.woodpecker.requests.Requests;
import me.gv7.woodpecker.requests.Response;
import me.gv7.woodpecker.requests.exception.RequestsException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.misc.BASE64Encoder;
import org.apache.commons.codec.binary.Base64;
import javax.net.ssl.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.common.hash.Hashing;

public class RequestHelper {
    private static RequestHelper request = null;
    private Logger logger = null;
    private final String[] ua = new String[]{
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.41 Safari/537.36 Edg/88.0.705.22"
    };

    private RequestHelper() {
        this.logger = Logger.getLogger("RequestHelper");
        LogUtil.setLogingProperties(logger);
    }

    public static RequestHelper getInstance() {
        if (request == null) {
            request = new RequestHelper();
        }
        return request;
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
        RawResponse response = null;
        HashMap<String, String> result = new HashMap<>();
        try {
            response = Requests.get(url)
                    .headers(new HashMap<String, String>() {{ put("User-Agent", ua[(new SecureRandom()).nextInt(3)]); }})
                    .connectTimeout(connectTimeout)
                    .socksTimeout(socksTimeout)
                    .send();
        }catch (Exception e){
            logger.log(Level.WARNING, e.getMessage(), e);
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
                    result.put("msg", "请求响应错误,状态码" + String.valueOf(code));
                }
                return result;
            } catch (Exception e) {
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
     * @param url
     * @return
     */
    public HashMap<String, String> getImageFavicon(String url) {
        Response<byte[]> response = null;
        HashMap<String, String> result = new HashMap<>();
        try {
            response = Requests.get(url)
                    .headers(new HashMap<String, String>() {{
                        put("User-Agent", ua[(new SecureRandom()).nextInt(3)]);
                    }})
                    .verify(false) // 忽略证书校验
                    .send()
                    .toBytesResponse();
        }catch (RequestsException e){
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
                        logger.log(Level.FINE, url + "无响应内容");
                        return null;
                    }
                    String encoded = new BASE64Encoder().encode(resp1);
                    String hash = getIconHash(encoded);
                    result.put("msg", "icon_hash=\"" + hash + "\"");
                    return result;
                } catch (Exception e) {
                    result.put("code", "error");
                    result.put("msg", e.getMessage());
                    logger.log(Level.WARNING, e.getMessage(), e);
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
                .hashString(f.replaceAll("\r","" )+ "\n", StandardCharsets.UTF_8)
                .asInt();
        return String.valueOf(murmu);
    }

    /**
     * 获取证书编号
     * @param host 域名
     * @return 证书编号
     */
    public String getCertSerialNum(String host) {
        try {
            URL url = new URL(host);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            TrustModifier.relaxHostChecking(conn);
            conn.connect();
            Certificate[] certs = conn.getServerCertificates();
            X509Certificate cert = (X509Certificate) certs[0];
            return "cert=\"" + cert.getSerialNumber().toString() + "\"";
        } catch (Exception e) {
            logger.log(Level.FINER, e.getMessage(), e);
        }
        return null;
    }

    /**
     * 自动提示
     * @param key
     * @return
     */
    public List<String> getTips(String key) {
        try {
            key = java.net.URLEncoder.encode(key, "UTF-8");
            HashMap<String, String> result = getHTML("https://api.fofa.so/v1/search/tip?q=" + key, 3000, 5000);
            if (result.get("code").equals("200")) {
                JSONObject obj = JSON.parseObject(result.get("msg"));
                if(obj.getString("message").equals("ok")){
                    List<String> data = new ArrayList<>();
                    JSONArray objs = obj.getJSONArray("data");
                    for (Object o : objs) {
                        JSONObject tmp = (JSONObject) o;
                        data.add(tmp.getString("name") + "--" + tmp.getString("company"));
                    }
                    return data;
                }
            }
            return null;
        }catch (Exception e){
            logger.log(Level.WARNING, e.getMessage(), e);
            return null;
        }

    }

    /**
     * base64编码字符串
     *
     * @param str 字符串
     * @return 编码字符串
     */
    public static String encode(String str) {
        return Base64.encodeBase64String(str.getBytes(StandardCharsets.UTF_8));
    }
}
