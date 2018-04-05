package rk.tools.objectxpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Commons {
    public static void print(Object msg) {
        System.out.println(msg);
    }

    public static <T> List<T> arrayListOf(T... items) {
        if (items == null || items.length == 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(items));
    }

    public static <T, R> List<R> transformList(List<T> list, Function<T, R> mapper) {
        return list.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
}
