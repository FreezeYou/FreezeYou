package cf.playhi.freezeyou;

import java.io.Serializable;

class SerializableClass implements Serializable {
    private Class<?> c;

    Class<?> getStoredClass() {
        return c;
    }

    SerializableClass setStoredClass(Class<?> cls) {
        c = cls;
        return this;
    }

}
