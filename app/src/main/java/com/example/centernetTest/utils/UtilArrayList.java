package com.example.centernetTest.utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Function;

public class UtilArrayList<E> extends ArrayList<E> {
    private int maxSize; // -1 means infinite
    private POP popmethod = POP.FIFO;


    //todo clean up ths class

    public UtilArrayList(Collection<? extends E> c, int maxSize) {
        super(c);
        this.maxSize = maxSize;
    }

    public UtilArrayList(int maxSize, POP popmethod) {
        super();
        this.maxSize = maxSize;
        this.popmethod = popmethod;
    }

    public UtilArrayList(int maxSize) {
        super();
        this.maxSize = maxSize;
    }

    public UtilArrayList() {
        super();
        maxSize = -1;
    }

    private void pop() {
        switch (popmethod) {
            case FIFO:
                remove(0);
                break;
            case LIFO:
                remove(size() - 1);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + popmethod);
        }
    }

    public void popcheck() {
        //check whether we should pop an element
        while (maxSize != -1 && size() == maxSize) {
            pop();
        }
    }

    @Override
    public void add(int idx, E e) {
        popcheck();
        super.add(idx, e);
    }

    @Override
    public boolean add(E e) {
        popcheck();
        add(size(), e);
        return true;
    }

    public void synchronizedAdd(E e) {
        synchronized (this) {
            add(e);
        }
    }

    synchronized public void synchronizedAdd(int idx, E e) {
        synchronized (this) {
            add(idx, e);
        }
    }

    public E getMax(Comparator<E> comp) {
        //assert E implements Comparable
        if (size() == 0) return null;
        return (E) Collections.max((ArrayList) this, comp);
    }

    public E getMax() {
        //assert E implements Comparable
        if (size() == 0) return null;
        return (E) Collections.max((ArrayList) this);
    }

    public E getMin(Comparator<E> comp) {
        //assert E implements Comparable
        if (size() == 0) return null;
        return (E) Collections.min((ArrayList) this, comp);
    }

    public E getMin() {
        //assert E implements Comparable
        if (size() == 0) return null;
        return (E) Collections.min((ArrayList) this);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public E get(int index) {
        if (maxSize != -1 && size() > maxSize)
            throw new IllegalStateException("Size cannot be: " + size() + " if maxsize: " + maxSize);
        return super.get(index);
    }

    public E synchronizedGet(int index) {
        synchronized (this) {
            return super.get(index);
        }
    }

    public E getLast() {
        //todo might throw index out of bounds exception, maybe we should syncrhonize
        if (maxSize != -1 && size() > maxSize)
            throw new IllegalStateException("size cannot be: " + size() + " if maxsize: " + maxSize);
        if (size() == 0) return null;
        return get(size() - 1);
    }

    public JSONArray getAsFlatJsonArray(Function<E, ?> function) {
        //returns a flat json array for one list fields
        JSONArray jsonArray = new JSONArray();
        stream().map(function).forEach(jsonArray::put);
        return jsonArray;
    }

    public enum POP {
        FIFO,
        LIFO
    }
}
