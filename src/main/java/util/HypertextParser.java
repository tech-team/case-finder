package util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HypertextParser {
    public static List<HypertextNode> parse(String hypertext) {
        List<HypertextNode> list = new ArrayList<>();

        Pattern regex = Pattern.compile("https?:\\/\\/\\S*", Pattern.CASE_INSENSITIVE);
        Matcher regexMatcher = regex.matcher(hypertext);

        int lastPos = 0;

        while (regexMatcher.find()) {
            int start = regexMatcher.start();
            int end = regexMatcher.end();

            if (lastPos != start) {
                String value = hypertext.substring(lastPos, start);
                list.add(new HypertextNode(HypertextNode.Type.TEXT, value));
            }

            String value = hypertext.substring(start, end);
            list.add(new HypertextNode(HypertextNode.Type.LINK, value));

            lastPos = end;
        }

        if (lastPos != hypertext.length()) {
            String value = hypertext.substring(lastPos, hypertext.length());
            list.add(new HypertextNode(HypertextNode.Type.TEXT, value));
        }

        return list;
    }
}