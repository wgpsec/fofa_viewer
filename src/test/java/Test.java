import org.fofaviewer.bean.FofaBean;
import org.fofaviewer.utils.RequestHelper;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Test {

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("config.properties"));
        FofaBean client = new FofaBean(properties.getProperty("email").trim(), properties.getProperty("key").trim());
        client.setSize(properties.getProperty("maxSize"));
        RequestHelper helper = RequestHelper.getInstance();
        String a = "doamin=\"baidu.com\"";
        String requestUrl = client.getParam(null) + helper.encode(a);
        System.out.println(requestUrl);
        System.out.println(helper.getHTML(requestUrl, 3000,5000));
    }
}
