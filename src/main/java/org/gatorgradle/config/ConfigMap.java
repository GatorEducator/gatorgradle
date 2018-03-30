package org.gatorgradle.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigMap {
    // FIXME: is this how we actually want to represent data?

    private class Node<E> {
        private E stored;
        private String name;

        protected Node(String name, E stored) {
            this.name   = name;
            this.stored = stored;
        }

        protected Node(String name) {
            this.name = name;
        }

        protected E get() {
            return stored;
        }

        protected String getName() {
            return name;
        }
    }

    private class ListNode<I> extends Node<List<I>> {
        protected ListNode(String name, List<I> value) {
            super(name, value);
        }

        protected ListNode(String name) {
            super(name);
        }

        protected void add(I item) {
            get().add(item);
        }

        protected I get(int index) {
            return get().get(index);
        }
    }

    private class NodeListNode<N> extends Node<Map<String, Node<N>>> {
        protected NodeListNode(String name, Map<String, Node<N>> value) {
            super(name, value);
        }

        protected NodeListNode(String name) {
            super(name, new HashMap<String, Node<N>>());
        }

        protected void add(Node<N> node) {
            get().put(node.getName(), node);
        }

        protected Node<N> get(String name) {
            return get().get(name);
        }
    }

    public static final String SEP      = "[/.]";
    public static final String SEP_CHAR = "/";

    private Map<String, String> header;
    // private NodeListNode<Node> body;

    public ConfigMap() {
        header = new HashMap<>();
        // body   = new NodeListNode<>("root");
    }

    public String getHeader(String name) {
        return header.get(name);
    }

    /**
     * Searches for the given address in the map.
     *
     * @param  address the address to search for
     * @return         true if there is a value associated with the address
     */
    public boolean contains(String... address) {
        // List<String> namePath = new ArrayList<>();
        // for (String str : address) {
        //     for (String name : str.replaceAll(SEP, SEP_CHAR).split(SEP_CHAR)) {
        //         namePath.add(name);
        //     }
        // }

        // TODO: actual calculation
        return false;
    }

    public static class ConfigMapCursor {
        // Node<?> current;

        // TODO: design and implement traverser for ConfigMap
    }
}
