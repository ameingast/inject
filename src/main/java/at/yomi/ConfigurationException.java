package at.yomi;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String msg, String fqnClass, String field) {
        super("Unable to configure '" + fqnClass + "." + field + "':" + msg);
    }

    public ConfigurationException(String msg) {
        super(msg);
    }
}
