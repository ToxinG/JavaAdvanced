package ru.ifmo.ctddev.maltsev.arrayset;

import java.util.*;

/**
 * Created by Антон on 20.02.2017.
 */
public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {

    private List<E> list = null;
    private Comparator<? super E> comparator = null;

    public ArraySet() {
        this((Comparator<E>) null);
    }

    public ArraySet(Collection<E> collection) {
        this(collection, null);
    }

    public ArraySet(Comparator<? super E> comparator) {
        list = Collections.emptyList();
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        this.comparator = comparator;
        TreeSet<E> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        list = new ArrayList<>(treeSet);
    }

    private ArraySet(List<E> list, Comparator<? super E> comparator, boolean flag) {
        this.list = list;
        this.comparator = comparator;
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(list, (E) o, comparator) >= 0;
    }

    private int indexOfElement(E e) {
        int index = Collections.binarySearch(list, e, comparator);
        return index < 0 ? ~index : index;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    @Override
    public int size() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        int fromIndex = indexOfElement(fromElement);
        int toIndex = indexOfElement(toElement);
        if (fromIndex > toIndex) {
            fromIndex = toIndex;
        }
        return new ArraySet<E>(list.subList(fromIndex, toIndex), comparator, true);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int toIndex = indexOfElement(toElement);
        return new ArraySet<E>(list.subList(0, toIndex), comparator, true);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int fromIndex = indexOfElement(fromElement);
        return new ArraySet<E>(list.subList(fromIndex, list.size()), comparator, true);
    }

    @Override
    public E first() {
        if (list.isEmpty()) {
            throw new NoSuchElementException("Could not get first element: sorted set is empty");
        }
        return list.get(0);
    }

    @Override
    public E last() {
        if (list.isEmpty()) {
            throw new NoSuchElementException("Could not get last element: sorted set is empty");
        }
        return list.get(list.size() - 1);
    }

}
