import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.commons.collections4.ListUtils;
import org.fofaviewer.bean.ExcelBean;
import org.fofaviewer.bean.TableBean;
import org.fofaviewer.main.FofaConfig;
import org.fofaviewer.main.ProxyConfig;
import org.fofaviewer.utils.RequestUtil;
import org.tinylog.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public void getCommonName(){
        String cert="";
        Pattern pattern = Pattern.compile("CommonName: ([\\w|\\.]+)\n\n");
        Matcher matcher = pattern.matcher(cert);
        if(matcher.find()){
            System.out.println(matcher.group(1));
        }

    }

    public void getSerialNumber(){
        String cert="";
        Pattern pattern = Pattern.compile("Serial Number: (\\d+)\n");
        Matcher matcher = pattern.matcher(cert);
        if(matcher.find()){
            System.out.println(matcher.group(1));
        }
    }

    public static void main(String[] args) {
//        Properties properties = new Properties();
//        properties.load(new FileInputStream("config.properties"));
//        FofaConfig client = FofaConfig.getInstance();
//        client.setKey(properties.getProperty("key").trim());
//        client.setEmail(properties.getProperty("email").trim());
//        client.setAPI(properties.getProperty("api"));
//        client.setSize(properties.getProperty("max_size"));
//        RequestUtil helper = RequestUtil.getInstance();
//        String a = "domain=\"baidu.com\"";
//        System.out.println(a);
//        String requestUrl = client.getParam("1", false) + helper.encode(a);
//        System.out.println(requestUrl);
//        System.out.println(helper.getHTML(requestUrl, 3000,5000));
//        Test test = new Test();
//        test.getCommonName();
//        test.getSerialNumber();
    }
}
