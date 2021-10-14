package org.fofaviewer.bean;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@HeadFontStyle(fontHeightInPoints = 16)
@ContentFontStyle(fontHeightInPoints = 14)
public class ExcelBean extends BaseBean {
    @ColumnWidth(35)
    @ExcelProperty("host")
    private String host;

    @ColumnWidth(75)
    @ExcelProperty("网页标题")
    private String title;

    @ColumnWidth(25)
    @ExcelProperty("域名")
    private String domain;

    @ColumnWidth(18)
    @ExcelProperty("ip")
    private String ip;

    @ExcelProperty("端口")
    private Integer port;

    @ExcelProperty("协议")
    private String protocol;

    @ColumnWidth(20)
    @ExcelProperty("banner")
    private String server;

    @ColumnWidth(35)
    @ExcelProperty("fid")
    private String fid;

    public ExcelBean(String host, String title, String ip, String domain, Integer port, String protocol, String server, String fid) {
        this.host = host;
        this.title = title;
        this.ip = ip;
        this.domain = domain;
        this.port = port;
        this.protocol = protocol;
        this.server = server;
        this.fid = fid;
    }
}
