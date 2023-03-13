package org.fofaviewer.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import org.controlsfx.control.StatusBar;
import org.fofaviewer.bean.RequestBean;
import org.fofaviewer.bean.TabDataBean;
import org.fofaviewer.bean.TableBean;
import org.fofaviewer.callback.HttpCallback;
import org.fofaviewer.callback.MainControllerCallback;
import org.fofaviewer.callback.RequestCallback;
import org.fofaviewer.controls.MyTableView;
import org.fofaviewer.utils.*;
import org.tinylog.Logger;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Request {
    private final ArrayList<RequestBean> queryList;
    private final RequestCallback<Request> callback;
    private final MainControllerCallback mainControllerCallback;
    private AtomicInteger succeeded;
    private Semaphore semaphore; // 设置线程最大数量
    private CountDownLatch latch;

    public Request(ArrayList<RequestBean> queryList, RequestCallback<Request> callback, MainControllerCallback mainControllerCallback){
        this.callback = Objects.requireNonNull(callback);
        this.queryList = queryList;
        this.mainControllerCallback = mainControllerCallback;
    }

    @SuppressWarnings("unchecked")
    public void query(int connection){
        semaphore = new Semaphore(connection);
        succeeded = new AtomicInteger();
        latch = new CountDownLatch(queryList.size());
        // 设置延时任务在滚动条界面渲染结束后进行事件绑定
        ThreadPoolUtil.submit(() -> {
            try {
                launchRequest(queryList, bean -> {
                    bean.setRequestStatus(RequestStatus.RUNNING);
                    TabDataBean _tmp = new TabDataBean();
                    Platform.runLater(() -> this.callback.before(_tmp, bean));
                    HashMap<String, String> res = RequestUtil.getInstance().getHTML(bean.getRequestUrl(), 120000, 120000);
                    bean.setResult(res);
                    if (res.get("code").equals("error") || !res.get("code").equals("200")) {
                        bean.setRequestStatus(RequestStatus.FAILED);
                        Platform.runLater(() -> this.callback.failed(res.get("msg"), bean));
                    } else {
                        JSONObject obj = JSON.parseObject(bean.getResult().get("msg"));
                        if (obj.getBoolean("error")) {
                            Platform.runLater(() -> this.callback.failed(obj.getString("errmsg"), bean));
                            semaphore.release();
                            succeeded.incrementAndGet();
                            latch.countDown();
                            return;
                        }
                        bean.setRequestStatus(RequestStatus.SUCCEEDED);
                        if (obj.getInteger("size") < Integer.parseInt(bean.getSize())) {
                            _tmp.hasMoreData = false;
                        }
                        StatusBar bar = new StatusBar();
                        _tmp.total = obj.getInteger("size");
                        Label totalLabel = new Label(ResourceBundleUtil.getResource().getString("QUERY_TIPS1") +
                                obj.getString("size") + ResourceBundleUtil.getResource().getString("QUERY_TIPS2"));
                        Label countLabel = new Label(String.valueOf(obj.getJSONArray("results").size()));
                        bar.getRightItems().addAll(new ArrayList<Label>() {{
                            add(totalLabel);
                            add(countLabel);
                            add(new Label(ResourceBundleUtil.getResource().getString("QUERY_TIPS3")));
                        }});
                        bar.getLeftItems().add(new Label());
                        BorderPane tablePane = new BorderPane();
                        TableView<TableBean> view = new TableView<>();
                        List<TableBean> values = (List<TableBean>) DataUtil.loadJsonData(_tmp, obj, null, null, false);
                        view.setItems(FXCollections.observableArrayList(values));
                        new MyTableView(view, mainControllerCallback);
                        tablePane.setCenter(view);
                        tablePane.setBottom(bar);
                        // 设置延时任务在滚动条界面渲染结束后进行事件绑定
                        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                        pause.setOnFinished(event -> mainControllerCallback.addSBListener(view));
                        pause.playFromStart();
                        Platform.runLater(() -> this.callback.succeeded(tablePane, bar, bean));
                    }
                    semaphore.release();
                    succeeded.incrementAndGet();
                    latch.countDown();
                });
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        });
    }

    private void launchRequest(ArrayList<RequestBean> queryList, HttpCallback callback) throws InterruptedException {
        for(RequestBean bean : queryList) {
            query(bean, callback);
        }
        latch.await();
    }

    private void query(RequestBean bean, HttpCallback callback) throws InterruptedException {
        semaphore.acquire();
        callback.onResponse(bean);
    }


}
