package org.fofaviewer.callback;

import org.fofaviewer.bean.RequestBean;

public interface HttpCallback {
    void onResponse(RequestBean bean);
}
