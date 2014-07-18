package util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class HypertextParserTest {
    @Test
    public void testParseNoLinks() throws Exception {
        List<HypertextNode> desiredList = new ArrayList<>();
        desiredList.add(
                new HypertextNode(HypertextNode.Type.TEXT,"no links here"));

        actualTest(desiredList);
    }

    @Test
    public void testParseJustLink() throws Exception {
        List<HypertextNode> desiredList = new ArrayList<>();
        desiredList.add(
                new HypertextNode(HypertextNode.Type.LINK, "http://google.com"));

        actualTest(desiredList);
    }

    @Test
    public void testParseAllTogetherOne() throws Exception {
        List<HypertextNode> desiredList = new ArrayList<>();
        desiredList.add(new HypertextNode(HypertextNode.Type.TEXT, "some text "));
        desiredList.add(new HypertextNode(HypertextNode.Type.LINK, "http://google.com"));
        desiredList.add(new HypertextNode(HypertextNode.Type.TEXT, " some other text"));

        actualTest(desiredList);
    }

    @Test
    public void testParseAllTogetherTwo() throws Exception {
        List<HypertextNode> desiredList = new ArrayList<>();
        desiredList.add(new HypertextNode(HypertextNode.Type.LINK, "http://google.com"));
        desiredList.add(new HypertextNode(HypertextNode.Type.TEXT, " some text "));
        desiredList.add(new HypertextNode(HypertextNode.Type.LINK, "http://google.com"));
        desiredList.add(new HypertextNode(HypertextNode.Type.TEXT, " some other text"));
        desiredList.add(new HypertextNode(HypertextNode.Type.LINK, "https://google.com"));

        actualTest(desiredList);
    }

    @Test
    public void testParseAllTogetherThree() throws Exception {
        List<HypertextNode> desiredList = new ArrayList<>();
        desiredList.add(new HypertextNode(HypertextNode.Type.TEXT, "some text "));
        desiredList.add(new HypertextNode(HypertextNode.Type.LINK, "http://google.com"));
        desiredList.add(new HypertextNode(HypertextNode.Type.TEXT, "\n"));
        desiredList.add(new HypertextNode(HypertextNode.Type.LINK, "https://google.com"));

        actualTest(desiredList);
    }

    private void actualTest(List<HypertextNode> desiredList) {
        String hypertext = desiredList.stream()
                .map(HypertextNode::getValue)
                .reduce("", (a, b) -> a + b);

        List<HypertextNode> list = HypertextParser.parse(hypertext);

        assert list.size() == desiredList.size() : "Lists' sizes not equal";

        for (int i = 0; i < list.size(); ++i) {
            assert list.get(i).getType() == desiredList.get(i).getType()
                    : "types not equal";

            assert list.get(i).getValue().equals(desiredList.get(i).getValue())
                    : "values not equal";
        }
    }
}