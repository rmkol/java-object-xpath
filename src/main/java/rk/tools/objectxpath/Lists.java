package rk.tools.objectxpath;

import java.util.ArrayList;
import java.util.List;

public class Lists {
    public static <T> List<T> removeAll(List<T> list) {
        List<T> result = new ArrayList<>(list);
        list.clear();
        return result;
    }
}
