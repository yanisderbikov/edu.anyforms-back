package ru.anyforms.edu.util;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Порядок в админке — просто число: поставили уроку 2 — он встал вторым,
 * остальные подвинулись. После каждого сохранения список выстраивается
 * заново в 1, 2, 3…, поэтому дырок и дублей в номерах не остаётся.
 */
public final class Ordering {

    private Ordering() {
    }

    /**
     * @param justSavedId элемент, который только что правили: при совпадении номеров
     *                    он встаёт первым, а «старый» сосед уезжает ниже
     */
    public static <T> List<T> reorder(List<T> items,
                                      Function<T, Integer> getOrd,
                                      Function<T, UUID> getId,
                                      UUID justSavedId,
                                      BiConsumer<T, Integer> setOrd) {
        List<T> sorted = items.stream()
                .sorted(Comparator
                        .comparingInt((T item) -> {
                            Integer ord = getOrd.apply(item);
                            return ord == null ? Integer.MAX_VALUE : ord;
                        })
                        .thenComparingInt(item -> getId.apply(item).equals(justSavedId) ? 0 : 1))
                .toList();

        int position = 1;
        for (T item : sorted) {
            setOrd.accept(item, position++);
        }
        return sorted;
    }
}
