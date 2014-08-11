package util;

public class ConfigReaderParseException extends Exception {
    public ConfigReaderParseException(String line) {
        super(line);
    }
}
