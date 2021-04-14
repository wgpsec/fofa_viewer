package org.fofaviewer.utils;

import javafx.scene.control.Alert;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.misc.BASE64Encoder;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHelper {
    private static RequestHelper request = null;
    private Logger logger = null;
    private static final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    private final String[] ua = new String[]{
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.41 Safari/537.36 Edg/88.0.705.22"
    };
    private final String path = System.getProperty("user.dir") +
            System.getProperty("file.separator") + "iconhash" + System.getProperty("file.separator");

    private RequestHelper() {
        this.logger = Logger.getLogger("RequestHelper");
        LogUtil.setLogingProperties(logger);
    }

    public static RequestHelper getInstance(){
        if(request==null){
            request = new RequestHelper();
        }
        return request;
    }

    private CloseableHttpResponse getResponse(String url){
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;
        HashMap<String,String> map = new HashMap<String,String>();
        try {
            URIBuilder builder = new URIBuilder(url);
            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setHeader("User-Agent", ua[(new SecureRandom()).nextInt(3)]);
            response = httpClient.execute(httpGet);
            return response;
        }catch (java.net.ConnectException e){
            logger.log(Level.WARNING, e.getMessage(), e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("网站访问异常！");
            alert.showAndWait();
            return null;
        }catch (Exception ex){
            logger.log(Level.WARNING, ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * 发起HTTP请求获取响应内容
     * @param url 请求url
     * @return 响应内容
     * 200 : 请求响应内容
     * other code : request error
     * error ：请求失败
     */
    public HashMap<String, String> getHTML(String url){
        CloseableHttpResponse response = getResponse(url);
        HashMap<String,String> result = new HashMap<>();
        if(response != null){
            int code = response.getStatusLine().getStatusCode();
            result.put("code", String.valueOf(code));
            try{
                if(code == 200){
                    HttpEntity httpEntity = response.getEntity();
                    result.put("msg", EntityUtils.toString(httpEntity,"utf8"));
                }else if(code == 401) {
                    result.put("msg", "请求错误状态码401，可能是没有在config中配置有效的email和key，或者您的账号权限不足无法使用api进行查询。");
                }else if(code == 502){
                    result.put("msg", "请求错误状态码502，可能是账号限制了每次请求的最大数量，建议尝试修改config中的maxSize为100");
                }else{
                    result.put("msg", "请求响应错误,状态码" + String.valueOf(code));
                }
                return result;
            }catch (Exception e){
                result.put("code", "error");
                result.put("msg", e.getMessage());
                logger.log(Level.WARNING, e.getMessage(), e);
                return result;
            }finally {
                try{
                    response.close();
                }catch (IOException ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
        result.put("code", "error");
        return result;
    }

    /**
     * 提取网站favicon 需要两步：
     * 1. 直接访问url根目录的favicon，若404则跳转至第2步
     * 2. 访问网站，获取html页面，获取head头中的 link标签的ico 路径
     * @param url
     * @return
     */
    public HashMap<String, String> getImageFavicon(String url){
        String filename =  this.path + "favicon.ico";
        File file = new File(filename);
        CloseableHttpResponse response = getResponse(url);
        HashMap<String,String> result = new HashMap<>();
        int cache = 10240;
        if(response !=null) {
            int code = response.getStatusLine().getStatusCode();
            result.put("code", String.valueOf(code));
            if (code == 200) {
                try {
                    if(response.getEntity().getContentLength() == 0){
                        logger.log(Level.FINE, url + "无响应内容");
                        return null;
                    }
                    FileOutputStream fileout = new FileOutputStream(file);
                    InputStream is = response.getEntity().getContent();
                    byte[] buffer = new byte[cache];
                    int ch = 0;
                    while ((ch = is.read(buffer)) != -1) {
                        fileout.write(buffer, 0, ch);
                    }
                    is.close();
                    fileout.flush();
                    fileout.close();
                }catch (Exception e){
                    result.put("code", "error");
                    result.put("msg", e.getMessage());
                    logger.log(Level.WARNING, e.getMessage(), e);
                    return result;
                }finally{
                    try{
                        response.close();
                    }catch (IOException ex) {
                        logger.log(Level.WARNING, ex.getMessage(), ex);
                    }
                }
                String hash = getIconHash(filename);
                if(hash != null) {
                    result.put("msg", hash.replaceAll("\n", ""));
                    return result;
                }
            }
        }
        return null;
    }

    public String getLinkIcon(String url){
        HashMap<String, String> result = getHTML(url);
        if(result.get("code").equals("200")){
            Document document = Jsoup.parse(result.get("msg"));
            Elements elements = document.getElementsByTag("link");
            if (elements.size() == 0){ // 没有link标签
                return null;
            }else{
                for(Element i : elements){
                    String rel = i.attr("rel");
                    if(rel.equals("icon") || rel.equals("shortcut icon")){
                        String href = i.attr("href");
                        if(href.startsWith("http")){ // link 显示完整url
                            return href;
                        }else if(href.startsWith("/")){  // link 显示相对路径
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
     * 使用iconhash计算favicon hash
     * @param f favicon的文件对象
     * @return favicon hash值
     */
    private String getIconHash(String f){
        String os = System.getProperty("os.name").toLowerCase(Locale.US);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.US);
        StringBuilder filepath = new StringBuilder();
        if(arch.equals("amd64") || arch.equals("x86_64")){
            if(os.contains("mac")){
                filepath.append(this.path).append("iconhash_darwin_amd64");
            }else if(os.contains("windows")){
                filepath.append(this.path).append("iconhash_windows_amd64.exe");
            }else {//linux
                filepath.append(this.path).append("iconhash_linux_amd64");
            }
            Process process = null;
            BufferedReader bufrIn = null;
            BufferedReader bufrError = null;
            StringBuilder result = new StringBuilder();
            try {
                process = Runtime.getRuntime().exec(new String[]{filepath.toString(), "-file", f});
                process.waitFor();
                bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
                String line;

                while ((line = bufrIn.readLine()) != null) {
                    result.append(line).append('\n');
                }
                while ((line = bufrError.readLine()) != null) {
                    result.append(line).append('\n');
                }
            }catch (Exception e){
                logger.log(Level.WARNING, e.getMessage(), e);
            }finally {
                try {
                    if (bufrIn != null) bufrIn.close();
                    if (bufrError != null) bufrIn.close();
                }catch (Exception ex){
                    logger.log(Level.FINER, ex.getMessage(), ex);
                }
                // 销毁子进程
                if (process != null) {
                    process.destroy();
                }
            }
            return result.toString();
        }else{
            return null;
        }
    }

    public String getCertSerialNum(String host) {
        try {
            URL url = new URL(host);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            TrustModifier.relaxHostChecking(conn);
            conn.connect();
            Certificate[] certs = conn.getServerCertificates();
            X509Certificate cert = (X509Certificate) certs[0];
            return "cert=\"" + cert.getSerialNumber().toString() + "\"";
        }catch (Exception e){
            logger.log(Level.FINER, e.getMessage(), e);
        }
        return null;
    }

    /**
     * base64编码字符串
     * @param str 字符串
     * @return 编码字符串
     */
    public static String encode(String str){
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(str.getBytes(StandardCharsets.UTF_8));
    }
}
