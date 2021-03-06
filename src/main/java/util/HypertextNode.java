package util;

public class HypertextNode {
    public enum Type {TEXT, LINK}

    private Type type;
    private String value;

    public HypertextNode(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
