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
    @ExcelProperty(value="host", index=0)
    private String host;

    @ColumnWidth(75)
    @ExcelProperty(value="网页标题", index=1)
    private String title;

    @ColumnWidth(25)
    @ExcelProperty(value="域名", index=2)
    private String domain;

    @ColumnWidth(25)
    @ExcelProperty(value="证书绑定域名", index=3)
    private String certCN;

    @ColumnWidth(18)
    @ExcelProperty(value="ip", index=4)
    private String ip;

    @ExcelProperty(value="端口", index=5)
    private Integer port;

    @ExcelProperty(value="协议", index=6)
    private String protocol;

    @ColumnWidth(20)
    @ExcelProperty(value="banner", index=7)
    private String server;

    @ColumnWidth(35)
    @ExcelProperty(value="fid", index=8)
    private String fid;

    public ExcelBean(String host, String title, String ip, String domain, Integer port, String protocol, String server, String fid, String certCN) {
        this.host = host;
        this.title = title;
        this.ip = ip;
        this.domain = domain;
        this.port = port;
        this.protocol = protocol;
        this.server = server;
        this.fid = fid;
        this.certCN = certCN;
    }
}
