package ch.njol.util.coll.iterator;

import org.eclipse.jdt.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ARTHUR on 20/03/2017.
 */
@SuppressWarnings("unused")
public class BiPeekingIterator<E> implements Iterator<E> {
    private List<E> internal = new ArrayList<>();
    private int index = 0;

    public BiPeekingIterator(List<E> list) {
        this.internal = list;
    }

    public BiPeekingIterator(List<E> list, int index) {
        this.internal = list;
        this.index = index;
    }

    @Override
    public void remove() {
        internal.remove(index);
    }

    public void set(E e) {
        internal.set(index, e);
    }

    public void add(E e) {
        internal.add(index, e);
    }

    public E peekPrevious() {
        return internal.get(index - 1);
    }

    public E peekNext() {
        return internal.get(index + 1);
    }

    @Override
    public boolean hasNext() {
        return index + 1 < internal.size();
    }

    @Override
    public E next() {
        return internal.get(++index);
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public E previous() {
        return internal.get(--index);
    }

    public int nextIndex() {
        return index + 1;
    }

    public int previousIndex() {
        return index - 1;
    }
}
