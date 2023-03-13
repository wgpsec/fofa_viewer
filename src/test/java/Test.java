import java.io.IOException;
import java.util.Properties;
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

    public static void main(String[] args) throws IOException {

    }
}
