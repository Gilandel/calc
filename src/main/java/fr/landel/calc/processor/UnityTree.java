package fr.landel.calc.processor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import fr.landel.calc.utils.StringUtils;

public class UnityTree {
    private UnityTree[] trees;
    private Unity unity;

    public static final Character[] CHARS;
    public static final UnityTree[] TREE;
    static {
        final Set<Character> chars = new HashSet<>();
        final UnityTree tree = new UnityTree();
        for (Unity unity : Unity.values()) {
            for (String symbol : unity.getSymbols()) {
                final Character[] functionChars = StringUtils.toChars(symbol);
                addTree(tree, functionChars, 0, unity);
                chars.addAll(Arrays.asList(functionChars));
            }
        }
        TREE = tree.trees;
        CHARS = chars.toArray(Character[]::new);
        Arrays.sort(CHARS);
    }

    public UnityTree[] getTrees() {
        return this.trees;
    }

    public Unity getUnity() {
        return this.unity;
    }

    public UnityType getUnityType() {
        if (this.unity != null) {
            return this.unity.getType();
        }
        return null;
    }

    static void addTree(final UnityTree tree, final Character[] chars, final int index, final Unity unity) {
        if (chars.length > index) {
            final Character c = chars[index];
            if (tree.trees == null) {
                tree.trees = new UnityTree[c + 1];
                final UnityTree sub = new UnityTree();
                tree.trees[c] = sub;
                addTree(sub, chars, index + 1, unity);

            } else if (tree.trees.length <= c || tree.trees[c] == null) {
                UnityTree sub = new UnityTree();
                if (tree.trees.length <= c) {
                    UnityTree[] tmp = new UnityTree[c + 1];
                    System.arraycopy(tree.trees, 0, tmp, 0, tree.trees.length);
                    tree.trees = tmp;
                }
                tree.trees[c] = sub;
                addTree(sub, chars, index + 1, unity);

            } else if (chars.length > index) {
                addTree(tree.trees[c], chars, index + 1, unity);
            }
        } else if (index > 0 && chars.length == index) {
            tree.unity = unity;
        }
    }

    static Optional<Unity> check(final char[] array, final int index, final UnityType requiredType) {
        return check(TREE, array, index, requiredType);
    }

    static Optional<Unity> check(final UnityTree[] validator, final char[] array, final int index, final UnityType requiredType) {
        if (array.length > index) {
            char c = array[index];
            if (validator.length > c && validator[c] != null) {
                Optional<Unity> unity = Optional.empty();
                if (validator[c].getTrees() != null) {
                    unity = check(validator[c].getTrees(), array, index + 1, requiredType);
                }
                if (unity.isPresent() && (requiredType == null || requiredType.equals(unity.get().getType()))) {
                    return unity;
                } else if (requiredType == null || requiredType.equals(validator[c].getUnityType())) {
                    return Optional.ofNullable(validator[c].getUnity());
                }
            }
        }
        return Optional.empty();
    }
}