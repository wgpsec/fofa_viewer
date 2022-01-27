package org.fofaviewer.utils;

import org.fofaviewer.bean.RequestBean;

public interface HttpCallback {
    void onResponse(RequestBean bean);
}
