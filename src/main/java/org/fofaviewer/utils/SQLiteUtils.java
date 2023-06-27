package org.fofaviewer.utils;

import javafx.scene.control.Alert;
import org.tinylog.Logger;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SQLiteUtils {
    public static void init() {
        Connection connection;
        if(!checkDbFile()){
            System.out.println("db文件不存在，程序将自动创建！");
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite://" + getPath() + "rules.db");
                // 创建表
                setTable(connection);
                connection.close();
            } catch ( Exception e ) {
                Logger.error(e);
            }
        }
    }

    public static String getPath() {
        try{
            String jarPath = java.net.URLDecoder.decode(DataUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile(), String.valueOf(StandardCharsets.UTF_8));
            return jarPath.substring(0, jarPath.lastIndexOf(System.getProperty("file.separator")) + 1);
        }catch (Exception e){
            Logger.error(e);
        }
        return "";
    }

    public static boolean checkDbFile(){
        return (new File( getPath() + "rules.db")).exists();
    }

    /**
     * 初始化rules表
     */
    private static void setTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE rules(" +
                "rule_name TEXT PRIMARY KEY," +
                "query_text TEXT NOT NULL,"+
                "description TEXT)";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        ArrayList<String> list = new ArrayList<String>(){{
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('ip', 'ip=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('app', 'app=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('title', 'title=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('header', 'header=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('body', 'body=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('domain', 'domain=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('host', 'host=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('cert', 'cert=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('cert.subject', 'cert.subject=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('region', 'region=', NULL);");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('server', 'server=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('icp', 'icp=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('org', 'org=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('asn', 'asn=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('ip_country', 'ip_country=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('city', 'city=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('country', 'country=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('cert.is_valid', 'cert.is_valid=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('after', 'after=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('before', 'before=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('is_domain', 'is_domain=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('banner', 'banner=', NULL)");
            add("INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES ('fid', 'fid=', NULL)");
        }};
        for(String i : list){
            stmt.executeUpdate(i);
        }
        stmt.close();
    }

    public static Map<String,String> matchRule(String queryTxt){
        Connection connection;
        PreparedStatement stmt;
        HashMap<String,String> res = new HashMap<>();
        try{
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite://" + getPath() + "rules.db");
            String sql = "select rule_name,query_text from rules where rule_name like ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, "%" + queryTxt + "%");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                res.put(rs.getString("rule_name"), rs.getString("query_text"));
            }
            connection.close();
            stmt.close();
            return res;
        }catch ( Exception e ) {
            Logger.error(e);
        }
        return res;
    }

    public static boolean insertRule(Map<String,String> rule){
        Connection connection;
        PreparedStatement stmt;
        try{
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite://" + getPath() + "rules.db");
            String sql = "INSERT INTO \"rules\" (\"rule_name\", \"query_text\", \"description\") VALUES (?,?,?)";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, rule.get("rule_name"));
            stmt.setString(2, rule.get("query_text"));
            stmt.setString(3, rule.get("rule_description"));
            stmt.executeUpdate();
            connection.close();
            stmt.close();
            return true;
        }catch ( Exception e ) {
            Logger.error(e);
            DataUtil.showAlert(Alert.AlertType.ERROR, null, e.getMessage()).showAndWait();
            return false;
        }
    }
}
