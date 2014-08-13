package util;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigReader {
    private Map<String, List<String>> map = new LinkedHashMap<>();

    public ConfigReader(String path, String encoding)
            throws ConfigReaderIOException, ConfigReaderParseException {

        File file = new File(this.getClass().getResource(path).getFile());

        try (
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, encoding);
            BufferedReader in = new BufferedReader(isr)
        ) {
            String line = null;

            while ((line = in.readLine()) != null) {
                parseLine(line);
            }
        } catch (IOException e) {
            throw new ConfigReaderIOException(e);
        }
    }

    private void parseLine(String line) throws ConfigReaderParseException {
        String[] kv = line.split("=");

        if (kv.length != 2)
            throw new ConfigReaderParseException(line);

        String key = kv[0].trim().replaceAll("\\\\u0020", " ");
        String value = kv[1].trim().replaceAll("\\\\u0020", " ");

        if (!map.containsKey(key))
            map.put(key, new ArrayList<>());

        map.get(key).add(value);
    }

    public String getString(String key) {
        return map.get(key).get(0);
    }

    public List<String> getList(String key) {
        List<String> res = map.get(key);
        if (res == null)
            return new ArrayList<>();
        return res;
    }
}
