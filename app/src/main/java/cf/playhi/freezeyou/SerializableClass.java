package cf.playhi.freezeyou;

import java.io.Serializable;

public class SerializableClass implements Serializable {
    private Class<?> c;

    public Class<?> getStoredClass() {
        return c;
    }

    public SerializableClass setStoredClass(Class<?> cls) {
        c = cls;
        return this;
    }

}
