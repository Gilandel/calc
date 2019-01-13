package fr.landel.calc.processor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import fr.landel.calc.utils.StringUtils;

public class FunctionsTree {
    private FunctionsTree[] trees;
    private Functions function;

    public static final Character[] CHARS;
    public static final FunctionsTree[] TREE;
    static {
        final Set<Character> chars = new HashSet<>();
        final FunctionsTree tree = new FunctionsTree();
        for (Functions function : Functions.values()) {
            final Character[] functionChars = StringUtils.toChars(function.getFunction());
            addTree(tree, functionChars, 0, function);
            chars.addAll(Arrays.asList(functionChars));
        }
        TREE = tree.trees;
        CHARS = chars.toArray(Character[]::new);
        Arrays.sort(CHARS);
    }

    public FunctionsTree[] getTrees() {
        return this.trees;
    }

    public Functions getFunction() {
        return this.function;
    }

    static void addTree(final FunctionsTree tree, final Character[] chars, final int index, final Functions function) {
        if (chars.length > index) {
            final Character c = chars[index];
            if (tree.trees == null) {
                tree.trees = new FunctionsTree[c + 1];
                final FunctionsTree sub = new FunctionsTree();
                tree.trees[c] = sub;
                addTree(sub, chars, index + 1, function);

            } else if (tree.trees.length <= c || tree.trees[c] == null) {
                FunctionsTree sub = new FunctionsTree();
                if (tree.trees.length <= c) {
                    FunctionsTree[] tmp = new FunctionsTree[c + 1];
                    System.arraycopy(tree.trees, 0, tmp, 0, tree.trees.length);
                    tree.trees = tmp;
                }
                tree.trees[c] = sub;
                addTree(sub, chars, index + 1, function);

            } else if (chars.length > index) {
                addTree(tree.trees[c], chars, index + 1, function);
            }
        } else if (index > 0 && chars.length == index) {
            tree.function = function;
        }
    }

    public static Optional<Functions> check(final char[] array) {
        return check(TREE, array, 0);
    }

    private static Optional<Functions> check(final FunctionsTree[] validator, final char[] array, final int index) {
        if (array.length > index) {
            char c = array[index];
            if (validator.length > c && validator[c] != null) {
                if (validator[c].getTrees() != null) {
                    final Optional<Functions> function = check(validator[c].getTrees(), array, index + 1);
                    if (function.isPresent()) {
                        return function;
                    } else {
                        return Optional.ofNullable(validator[c].getFunction());
                    }
                } else if (array.length == index + 1) {
                    return Optional.of(validator[c].getFunction());
                }
            }
        }
        return Optional.empty();
    }
}
