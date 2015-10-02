package net.nueca.imonggosdk.tools;

import java.util.List;

/**
 * Created by gama on 8/17/15.
 */
public class ListTools {
    public static List partition(int nthPartition, List list, int size) {
        if(nthPartition < 0)
            throw new IllegalArgumentException("nthPartition can't be negative");
        if(size < 0)
            throw new IllegalArgumentException("size can't be negative");

        if(nthPartition > list.size()/size)
            throw new IndexOutOfBoundsException("can't create partition " + nthPartition + " of " + list.size()/size +
                    " allowed partitions for list with size " + list.size());

        int start = nthPartition * size;
        int end = Math.min(start + size, list.size());
        return list.subList(start, end);
    }
}
