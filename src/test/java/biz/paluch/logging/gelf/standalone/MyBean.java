package biz.paluch.logging.gelf.standalone;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 31.07.14 09:02
 */
public class MyBean {
    public String getValue() {
        return "value field";
    }

    public String getValue2(String arg) {
        return "value field";
    }

    public void getVoid() {

    }

    public boolean isBoolean() {
        return true;
    }

    public Object getObject() {
        return new Object();
    }

}
