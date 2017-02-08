package com.iboxpay.settlement.gateway.common.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;

/**
 * jdk的PriorityBlockingQueue无法实现有序的iterator，自行实现一个基于ArrayList的队列。只需要实现add, poll, size, iterator几个方法即可。非线程安全。
 * @author: jianbo_chen
 * @since: 2015年7月23日
 * @version:
 */
public class PriorityTaskQueue<E extends Comparable<E>> implements Queue<E>{
    
    private ArrayList<E> queue;
    
    public PriorityTaskQueue(){
        this.queue = new ArrayList<E>();
    }
    
    @Override
    public boolean add(E e) {
//        Returns: the index of the search key, if it is contained in the list; otherwise, (-(insertion point) - 1). 
        int position = Collections.binarySearch(queue, e);
        if(position >= 0){
            for (int i = position, maxPosition = queue.size() - 1; i <= maxPosition; i++) {//已有相同存在时，放到最后面
                if(queue.get(i).compareTo(e) != 0){//找到不同的，回溯一个
                    position = i - 1;
                    break;
                }
                if(i >= maxPosition)//找到最后没有不同的
                    position = maxPosition;
            }
            position += 1;//一般来说是放到后面一个就行
        }else{
            position = -(position + 1);
        }
        queue.add(position, e);
        return true;
    }
    

    @Override
    public E poll() {
        if(queue.size() > 0)
            return queue.remove(0);
        
        return null;
    }

    @Override
    public int size() {
        return queue.size();
    }
    
    @Override
    public Iterator<E> iterator() {
        return queue.iterator();
    }
    

    ///////////////////////////////////////////////////////////////////////////////
    
    @Override
    public E remove() {
        throw new UnsupportedOperationException();
    }
    @Override
    public E element() {
        throw new UnsupportedOperationException();
    }
    @Override
    public E peek() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }
    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }
}

	