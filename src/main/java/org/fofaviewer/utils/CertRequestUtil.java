package org.fofaviewer.utils;

import javafx.scene.control.TableView;
import org.fofaviewer.bean.ExcelBean;
import org.fofaviewer.bean.TableBean;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CertRequestUtil {
    public TableView<TableBean> view;
    public ExecutorService threadPool = Executors.newCachedThreadPool();
    public static RequestUtil helper = RequestUtil.getInstance();

    public CertRequestUtil(TableView<TableBean> view) {
        this.view = view;
    }

    public void getCertDomain(HashMap<String,?> map, boolean isExport) throws InterruptedException {
        for(String i : map.keySet()){
            if(i.startsWith("https://")){
                if(isExport){
                    ExcelBean bean = (ExcelBean) map.get(i);
                    if(bean.getDomain().isEmpty()){
                        threadPool.execute(() -> {
                            String domain = helper.getCertSubjectDomain(bean.getHost());
                            if(!domain.isEmpty()){
                                bean.setDomain(domain);
                            }
                        });
                    }
                }else{
                    TableBean bean = (TableBean) map.get(i);
                    if(bean.domain.getValue().isEmpty()){
                        threadPool.execute(() -> {
                            String domain = helper.getCertSubjectDomain(bean.host.getValue());
                            if(!domain.isEmpty()){
                                bean.setDomain(domain);
                            }
                        });
                    }
                }
            }
        }
        if(!isExport){
            threadPool.shutdown();
            while(!threadPool.isTerminated()){
                view.refresh();
            }
        }
    }

}


