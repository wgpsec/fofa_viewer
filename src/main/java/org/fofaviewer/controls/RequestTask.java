package org.fofaviewer.controls;

import javafx.concurrent.Task;
import org.fofaviewer.utils.RequestHelper;

import java.util.HashMap;

public class RequestTask extends Task<HashMap<String,String>> {
    String requestUrl;
    String tabTitle;
    RequestHelper helper = RequestHelper.getInstance();

    public RequestTask(String requestUrl, String tabTitle){
        super();
        this.requestUrl = requestUrl;
        this.tabTitle = tabTitle;
    }

    @Override
    protected HashMap<String,String> call() {
        return helper.getHTML(this.requestUrl, 10000, 10000);
    }

    public String getTabTitle() {
        return tabTitle;
    }
}
