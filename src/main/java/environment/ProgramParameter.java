package environment;

/**
 * Класс определяет набор свойств, необходимых для хранения параметра, заданного в командной строке.
 */

public class ProgramParameter {
    private String value; // Parameter value.
    private  boolean require; // Is required.

    public String getValue() {
        return value;
    }

    public boolean isRequire() {
        return require;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public void setRequire(boolean value) {
        this.require = value;
    }

    public ProgramParameter(String value, boolean require) {
        this.value = value;
        this.require = require;
    }
}
