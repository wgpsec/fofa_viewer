package org.fofaviewer.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class LogUtil {
    /**
     * 得到要记录的日志的路径及文件名称
     */
    private static String getLogPath() {
        return System.getProperty("user.dir") + System.getProperty("file.separator") + "log";
    }

    /**
     * 配置Logger对象输出日志文件路径
     */
    public static void setLogingProperties(Logger logger) {
        setLogingProperties(logger, Level.ALL);
    }

    /**
     * 配置Logger对象输出日志文件路径
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
            fh = new FileHandler(logFile.getAbsolutePath(),0,1,true);
            logger.addHandler(fh); //日志输出文件
            fh.setFormatter(new SimpleFormatter());//文本方式
        } catch (SecurityException e) {
            System.out.println("安全性错误" + e.getMessage());
        } catch (IOException e) {
            System.out.println("读取文件日志错误" + e.getMessage());
        }
    }

    public static void log(String name, Exception e, Level level){
        Logger logger = Logger.getLogger(name);
        LogUtil.setLogingProperties(logger);
        logger.log(level,logger.getName() + "\n" + e.getMessage(), e);
        for(Handler h : logger.getHandlers()) {
            h.close();
        }
    }

    public static void log(String name, String info, Level level){
        Logger logger = Logger.getLogger(name);
        LogUtil.setLogingProperties(logger);
        logger.log(level, logger.getName() + "\n" + info);
        for(Handler h : logger.getHandlers()) {
            h.close();
        }
    }
}
