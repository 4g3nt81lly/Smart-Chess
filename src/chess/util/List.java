package chess.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public final class List<T> extends ArrayList<T> {

    @SafeVarargs
    public static <T> List<T> of(T... elements) {
        return new List<>(elements);
    }

    @SafeVarargs
    public List(T... elements) {
        super();
        super.addAll(Arrays.asList(elements));
    }

    public List(List<T> list) {
        super(list);
    }

    public List(Collection<? extends T> collection) {
        super(collection);
    }

    public List<T> copy() {
        return new List<>(this);
    }

    public List<T> reversed() {
        List<T> list = this.copy();
        Collections.reverse(list);
        return list;
    }

    public Optional<T> findFirst(Predicate<? super T> predicate) {
        for (T element : this) {
            if (predicate.test(element)) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }

    public boolean contains(Predicate<? super T> predicate) {
        return this.findFirst(predicate).isPresent();
    }

    public int count(Predicate<? super T> predicate) {
        int count = 0;
        for (T element : this) {
            if (predicate.test(element)) {
                count++;
            }
        }
        return count;
    }

    public void filter(Predicate<? super T> predicate) {
        this.removeIf(predicate.negate());
    }

    public Optional<T> removeFirst(T element) {
        return this.findFirst(item -> item.equals(element)).map(itemToRemove -> {
            this.remove(itemToRemove);
            return itemToRemove;
        });
    }

    public List<T> filtered(Predicate<? super T> predicate) {
        List<T> list = this.copy();
        list.filter(predicate);
        return list;
    }

    public <U> List<U> map(Function<? super T, ? extends U> transform) {
        List<U> transformed = new List<>();
        for (T element : this) {
            transformed.add(transform.apply(element));
        }
        return transformed;
    }

    public boolean andMap(Predicate<? super T> predicate) {
        return this.stream().allMatch(predicate);
    }

    public boolean orMap(Predicate<? super T> predicate) {
        return this.stream().anyMatch(predicate);
    }

    public Optional<T> at(int index) {
        try {
            return Optional.of(this.get(index));
        } catch (IndexOutOfBoundsException err) {
            return Optional.empty();
        }
    }

}