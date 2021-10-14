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
    private static String getLogPath() {
        return System.getProperty("user.dir") + System.getProperty("file.separator") + "log";
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
            // 文件不存在时自动创建
            File logPath = new File(getLogPath());
            if(!logPath.exists()){
                logPath.mkdir();
            }
            File logFile = new File(getLogPath() + System.getProperty("file.separator") + "error.log");
            if(!logFile.exists()){
                logFile.createNewFile();
            }
            fh = new FileHandler(logFile.getAbsolutePath(),true);
            logger.addHandler(fh); //日志输出文件
            fh.setFormatter(new SimpleFormatter());//文本方式
        } catch (SecurityException e) {
            logger.log(Level.SEVERE, "安全性错误", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE,"读取文件日志错误", e);
        }
    }

    public static void log(String name, Exception e, Level level){
        Logger logger = Logger.getLogger(name);
        LogUtil.setLogingProperties(logger);
        logger.log(level, e.getMessage(), e);
    }

    public static void log(String name, String info, Level level){
        Logger logger = Logger.getLogger(name);
        LogUtil.setLogingProperties(logger);
        logger.log(level, info);
    }
}
