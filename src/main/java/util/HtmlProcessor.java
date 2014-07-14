package util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;

public class HtmlProcessor {
    public HtmlProcessor() throws IOException {
        File input = new File("input.html");
        Document d = Jsoup.parse(input, "UTF-8");
    }

    public static void main(String[] args) throws IOException {
        HtmlProcessor h = new HtmlProcessor();
    }

}
