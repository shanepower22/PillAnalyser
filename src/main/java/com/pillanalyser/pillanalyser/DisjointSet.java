package com.pillanalyser.pillanalyser;

public class DisjointSet {
    public static int find(int[] a, int id) {
        if (a[id] != id) {
            a[id] = find(a, a[id]);
        }
        return a[id];
    }

    public static void union(int[] a, int p, int q) {
        int rootP = find(a, p);
        int rootQ = find(a, q);
        if (rootP != rootQ) {
            a[rootQ] = rootP;
        }
    }

}
