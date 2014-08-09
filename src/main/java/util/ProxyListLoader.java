package util;

import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyListLoader {
    private final WebView webView = new WebView();
    private final WebEngine webEngine = webView.getEngine();
    private final Consumer<List<String>> callback;

    public ProxyListLoader(Consumer<List<String>> onLoadedCallback) {
        callback = onLoadedCallback;
    }

    public void load() {
        Stage stage = new Stage();
        webView.setVisible(false);
        stage.setScene(new Scene(webView, 1, 1));
        stage.setTitle("Loading proxy list...");

        webEngine.setUserAgent("Opera/9.80 (Windows NT 6.0) Presto/2.12.388 Version/12.14");
        webEngine.getLoadWorker().stateProperty().addListener(
                (ov, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        String content = webEngine.executeScript("document.getElementById('listable').innerText").toString();

                        List<String> proxyList = new ArrayList<>();
                        Pattern regex = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
                        Matcher regexMatcher = regex.matcher(content);
                        while (regexMatcher.find()) {
                            proxyList.add(regexMatcher.group());
                        }

                        callback.accept(proxyList);
                        stage.close();
                    }
                });

        webEngine.load("http://proxylist.hidemyass.com/search-1311573#listable");

        stage.show();
    }
}
