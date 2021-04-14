package org.fofaviewer.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogUtil {
    /**
     * 得到要记录的日志的路径及文件名称
     * @return
     */
    private static String getLogName() {
        return System.getProperty("user.dir") + System.getProperty("file.separator") + "log" +
                System.getProperty("file.separator") + "error.log";
    }

    /**
     * 配置Logger对象输出日志文件路径
     * @param logger
     * @throws SecurityException
     * @throws IOException
     */
    public static void setLogingProperties(Logger logger) {
        setLogingProperties(logger, Level.ALL);
    }

    /**
     * 配置Logger对象输出日志文件路径
     * @param logger
     * @param level 在日志文件中输出level级别以上的信息
     */
    public static void setLogingProperties(Logger logger, Level level) {
        FileHandler fh;
        try {
            fh = new FileHandler(getLogName(),true);
            logger.addHandler(fh);//日志输出文件
            //logger.setLevel(level);
            fh.setFormatter(new SimpleFormatter());//文本方式  XMLFormatter
            //logger.addHandler(new ConsoleHandler());//输出到控制台
        } catch (SecurityException e) {
            logger.log(Level.SEVERE, "安全性错误", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE,"读取文件日志错误", e);
        }
    }

}
