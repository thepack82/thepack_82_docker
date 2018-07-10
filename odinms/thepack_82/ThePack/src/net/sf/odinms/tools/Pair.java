package net.sf.odinms.tools;

import java.io.Serializable;

public class Pair<E, F> implements Serializable {

    static final long serialVersionUID = 9179541993413738569L;
    private E left;
    private F right;

    public Pair(E left, F right) {
        this.left = left;
        this.right = right;
    }

    public E getLeft() {
        return left;
    }

    public F getRight() {
        return right;
    }

    @Override
    public String toString() {
        return left.toString() + ":" + right.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((left == null) ? 0 : left.hashCode());
        result = prime * result + ((right == null) ? 0 : right.hashCode());
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair other = (Pair) obj;
        if (left == null) {
            if (other.left != null) {
                return false;
            }
        } else if (!left.equals(other.left)) {
            return false;
        }
        if (right == null) {
            if (other.right != null) {
                return false;
            }
        } else if (!right.equals(other.right)) {
            return false;
        }
        return true;
    }
}
