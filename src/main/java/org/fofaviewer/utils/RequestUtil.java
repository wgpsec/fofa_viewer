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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Timestamp;
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
    private final String appId = "9e9fb94330d97833acfbc041ee1a76793f1bc691";
    private final String privateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC/TGN5+4FMXo7H3jRmostQUUEO1NwH10B8ONaDJnYDnkr5V0ZzUvkuola7JGSFgYVOUjgrmFGITG+Ne7AgR53Weiunlwp15MsnCa8/IWBoSHs7DX1O72xNHmEfFOGNPyJ4CsHaQ0B2nxeijs7wqKGYGa1snW6ZG/ZfEb6abYHI9kWVN1ZEVTfygI+QYqWuX9HM4kpFgy/XSzUxYE9jqhiRGI5f8SwBRVp7rMpGo1HZDgfMlXyA5gw++qRq7yHA3yLqvTPSOQMYJElJb12NaTcHKLdHahJ1nQihL73UwW0q9Zh2c0fZRuGWe7U/7Bt64gV2na7tlA62A9fSa1Dbrd7lAgMBAAECggEAPrsbB95MyTFc2vfn8RxDVcQ/dFCjEsMod1PgLEPJgWhAJ8HR7XFxGzTLAjVt7UXK5CMcHlelrO97yUadPAigHrwTYrKqEH0FjXikiiw0xB24o2XKCL+EoUlsCdg8GqhwcjL83Mke84c6Jel0vQBfdVQ+RZbetMCxqv1TpqpwW+iswlDY0+OKNxcDSnUyVkBko4M7bCqJ19DjzuHHLRmSuJhWLjX2PzdrVwIrRChxeJRR5AzrNE2BC/ssKasWjZfgkTOW6MS96q+wMLgwFGCQraU0f4AW5HA4Svg8iWT2uukcDg7VXXc/eEmkfmDGzmgsszUJZYb1hYsvjgbMP1ObwQKBgQDw1K0xfICYctiZ3aHS7mOk0Zt6B/3rP2z9GcJVs0eYiqH+lteLNy+Yx4tHtrQEuz16IKmM1/2Ghv8kIlOazpKaonk3JEwm1mCEXpgm4JI7UxPGQj/pFTCavKBBOIXxHJVSUSg0nKFkJVaoJiNy0CKwQNoFGdROk2fSYu8ReB/WlQKBgQDLWQR3RioaH/Phz8PT1ytAytH+W9M4P4tEx/2Uf5KRJxPQbN00hPnK6xxHAqycTpKkLkbJIkVWEKcIGxCqr6iGyte3xr30bt49MxIAYrdC0LtBLeWIOa88GTqYmIusqJEBmiy+A+DudM/xW4XRkgrOR1ZsagzI3FUVlei9DwFjEQKBgG8JH3EZfhDLoqIOVXXzA24SViTFWoUEETQAlGD+75udD2NaGLbPEtrV5ZmC2yzzRzzvojyVuQY1Z505VmKhq2YwUsLhsVqWrJlbI7uI/uLrQsq98Ml+Q5KUNS7c6KRqEU6KrIbVUHPj4zhTnTRqUhQBUoPXjNNNkyilBKSBReyhAoGAd3xGCIPdB17RIlW/3sFnM/o5bDmuojWMcw0ErvZLPCl3Fhhx3oNod9iw0/T5UhtFRV2/0D3n+gts6nFk2LbA0vtryBvq0C85PUK+CCX5QzR9Y25Bmksy8aBtcu7n27ttAUEDm1+SEuvmqA68Ugl7efwnBytFed0lzbo5eKXRjdECgYAk6pg3YIPi86zoId2dC/KfsgJzjWKVr8fj1+OyInvRFQPVoPydi6iw6ePBsbr55Z6TItnVFUTDd5EX5ow4QU1orrEqNcYyG5aPcD3FXD0Vq6/xrYoFTjZWZx23gdHJoE8JBCwigSt0KFmPyDsN3FaF66Iqg3iBt8rhbUA8Jy6FQA==";
    Pattern cnPattern = Pattern.compile("CommonName:\\s([-|\\*|\\w|\\.|\\s]+)\n\nSubject Public");
    Pattern snPattern = Pattern.compile("Serial Number:\\s(\\d+)\n");

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
     * ??????HTTP????????????????????????
     *
     * @param url ??????url
     * @return ????????????
     * 200 : ??????????????????
     * other code : request error
     * error ???????????????
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
            Logger.warn(url + e.getMessage());
            result.put("code", "error");
            result.put("msg", e.getMessage());
            return result;
        }
        if (response != null) {
            int code = response.statusCode();
            result.put("code", String.valueOf(code));
            try {
                if (code == 200) {
                    String body = response.readToText(); // ????????????utf-8??????
                    result.put("msg", body);
                } else if (code == 401) {
                    result.put("msg", "?????????????????????401?????????????????????config??????????????????email???key?????????????????????????????????????????????api???????????????");
                } else if (code == 502) {
                    result.put("msg", "?????????????????????502???????????????????????????????????????????????????????????????????????????config??????maxSize???100");
                } else {
                    result.put("msg", "??????????????????,?????????" + code);
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
     * ????????????favicon ???????????????
     * 1. ????????????url????????????favicon??????404???????????????2???
     * 2. ?????????????????????html???????????????head?????? link?????????ico ??????
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
                    .verify(false) // ??????????????????
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
                        Logger.warn(url + "???????????????");
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
        HashMap<String, String> result = getHTML(url, 60000,60000);
        if (result.get("code").equals("200")) {
            Document document = Jsoup.parse(result.get("msg"));
            Elements elements = document.getElementsByTag("link");
            if (elements.size() == 0) { // ??????link??????
                return null;
            } else {
                for (Element i : elements) {
                    String rel = i.attr("rel");
                    if (rel.equals("icon") || rel.equals("shortcut icon")) {
                        String href = i.attr("href");
                        if (href.startsWith("http")) { // link ????????????url
                            return href;
                        } else if (href.startsWith("/")) {  // link ??????????????????
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
     * ??????favicon hash
     *
     * @param f favicon???????????????
     * @return favicon hash???
     */
    public String getIconHash(String f) {
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
     * ??????????????????
     * @param host ??????
     * @return ????????????
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
     * ???https?????????????????????
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
     * ????????????
     * @param key query content
     * @return hint
     */
    public Map<String,String> getTips(String key) {
        try {
            String ts = String.valueOf((new Timestamp(System.currentTimeMillis())).getTime());
            String singParam = "q" + key + "ts" + ts;
            String params = URLEncoder.encode(key,"UTF-8") + "&ts=" + ts + "&sign=" + URLEncoder.encode(getInputSign(singParam), "utf-8") + "&app_id=" + this.appId;
            HashMap<String, String> result = getHTML(FofaConfig.TIP_API + params, 5000, 10000);
            if (result.get("code").equals("200")) {
                JSONObject obj = JSON.parseObject(result.get("msg"));
                if(obj.getInteger("code") == 0){
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
     * base64???????????????
     *
     * @param str ?????????
     * @return ???????????????
     */
    public String encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * ???fofa API ??????????????????????????????CommonName
     */
    public String getCertSubjectDomainByFoFa(String cert){
        Matcher matcher = cnPattern.matcher(cert);
        if(matcher.find()){
            return matcher.group(1);
        }
        return "";
    }

    /**
     * ???fofa API ??????????????????????????????Serial Number
     */
    public String getCertSerialNumberByFoFa(String cert){
        Matcher matcher = snPattern.matcher(cert);
        if(matcher.find()){
            return matcher.group(1);
        }
        return "";
    }

    /**
     * ??????SHA256withRSA??????
     * @param inputString ??????????????? q + ??????????????? + ts + ?????????
     */
    private String getInputSign(String inputString){
        try {
            byte[] data = inputString.getBytes();
            byte[] keyBytes = Base64.getDecoder().decode(this.privateKey);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey priKey = KeyFactory.getInstance("RSA").generatePrivate(pkcs8KeySpec);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(priKey);
            signature.update(data);
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            Logger.error(e);
            return "";
        }
    }
}
