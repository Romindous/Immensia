package ru.immensia.utils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import ru.immensia.Main;


public class ClassUtil {

    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';

    public static <T extends Enum<?>> T rotateEnum(T t) {
        try {
            final Method values = t.getClass().getMethod("values");
            @SuppressWarnings("unchecked")
            final T[] vals = (T[]) values.invoke(t);
            if (t.ordinal() == vals.length - 1)
                return vals[0];
            else return vals[t.ordinal() + 1];
        } catch (Exception ex) {
            return t;
        }
    }

    public static <G> G[] scale(final G[] arr, final int width, final int height) {
        if (arr.length == width * height) return arr;
        final float min = Math.min(width, height);
        final float min_w = width / min, min_h = height / min;
        int i = 1;
        final float arl = arr.length;
        while (i * i * min_w * min_h < arl) i++;
        final int far_w = (int) (min_w * i);
        final int arr_w = Math.min(far_w, width),
            arr_h = Math.min((int) (min_h * i), height);
        final G[] far = Arrays.copyOf(arr, width * height);
        Arrays.fill(far, null);
        for (int h = 0; h != arr_h; h++) {
            for (int w = 0; w != arr_w; w++) {
                final int pos = far_w * h + w;
                far[width * h + w] = pos < arr.length ? arr[pos] : null;
            }
        }
        return far;
    }

    @Nullable
    public static <G, T extends G> T cast(final G obj, final Class<T> cls) {
        return cls.isInstance(obj) ? cls.cast(obj) : null;
    }

    @SuppressWarnings("unchecked")
    public static <G> G rndElmt(final G... arr) {
        if (arr.length == 0) return null;
        return arr[Main.srnd.nextInt(arr.length)];
    }

    public static <G> G[] shuffle(final G[] ar) {
        int chs = ar.length >> 2;
        if (chs == 0) {
            if (ar.length > 1) {
                final G ne = ar[0];
                ar[0] = ar[ar.length - 1];
                ar[ar.length - 1] = ne;
            }
            return ar;
        }
        for (int i = ar.length - 1; i > chs; i--) {
            final int ni = Main.srnd.nextInt(i);
            final G ne = ar[ni];
            ar[ni] = ar[i];
            ar[i] = ne;
            chs += ((chs - ni) >> 31) + 1;
        }
        return ar;
    }

    public static <T> boolean check(final T[] split, final int length, final boolean extra) {
        if (split.length < length) {
            Main.log_err("Tried parsing " + Arrays.toString(split) + ", len-" + split.length + " < " + length);
            return false;
        }
        if (!extra && split.length > length) {
            Main.log_err("Tried parsing " + Arrays.toString(split) + ", len-" + split.length + " > " + length + " (no extras)");
            return false;
        }
        return true;
    }

    public static <G> boolean equal(final G o1, final G o2, final Function<G, ?> by) {
        return o1 == null ? o2 == null : o2 != null && Objects.equals(by.apply(o1), by.apply(o2));
    }
}
