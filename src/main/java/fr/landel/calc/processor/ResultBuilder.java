package fr.landel.calc.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import fr.landel.calc.config.I18n;
import fr.landel.calc.utils.StringUtils;

public class ResultBuilder {

    // private static final Logger LOGGER = new Logger(Result.class);

    public static final String FIRST_ID = idBuilder(0);

    // TODO correct missing operator
    private static final char[] NUMBER = "0123456789".toCharArray();
    static {
        Arrays.sort(NUMBER);
    }

    private final StringBuilder formula = new StringBuilder();
    private final Map<String, Entity> entities = new HashMap<>();
    private int id;
    private int length;

    public ResultBuilder() {
    }

    private static String idBuilder(final int id) {
        return new StringBuilder(StringUtils.ID_OPEN).append(id).append(StringUtils.ID_CLOSE).toString();
    }

    public ResultBuilder append(final String text, final ResultBuilder input) throws ProcessorException {
        if (!text.isEmpty()) {
            return from(this, text, input);
        }
        return this;
    }

    public ResultBuilder append(final String text) throws ProcessorException {
        if (!text.isEmpty()) {
            if (formula.length() > 0 && Arrays.binarySearch(NUMBER, formula.charAt(formula.length() - 1)) > -1
                    && Arrays.binarySearch(NUMBER, text.charAt(0)) > -1) {
                throw new ProcessorException(I18n.ERROR_RESULT_EVAL, formula, text);
            }
            formula.append(text);
            length += text.length();
        }
        return this;
    }

    public ResultBuilder append(final Entity entity) throws ProcessorException {
        if (formula.length() > 0 && Arrays.binarySearch(NUMBER, formula.charAt(formula.length() - 1)) > -1) {
            throw new ProcessorException(I18n.ERROR_RESULT_EVAL, formula, entity);
        }
        final String id = idBuilder(this.id++);
        entities.put(id, entity);
        formula.append(id);
        return this;
    }

    public int lastIndexOf(final char character) {
        return formula.lastIndexOf(String.valueOf(character));
    }

    public int indexOf(final String text) {
        return formula.indexOf(text);
    }

    public int indexOf(final char character, final int fromIndex) {
        return formula.indexOf(String.valueOf(character), fromIndex);
    }

    public String substring(final int start) {
        return formula.substring(start);
    }

    public String substring(final int start, final int end) {
        return formula.substring(start, end);
    }

    public Optional<Entity> subEntity(final int start, final int end) {
        return Optional.ofNullable(entities.get(substring(start, end)));
    }

    public int length() {
        return formula.length();
    }

    public int innerLength() {
        return length;
    }

    public boolean hasEntities() {
        return !this.entities.isEmpty();
    }

    public int getEntitiesSize() {
        return this.entities.size();
    }

    public Optional<Entity> firstEntity() {
        return getEntity(FIRST_ID);
    }

    public Optional<Entity> getEntity(final String key) {
        return Optional.ofNullable(this.entities.get(key));
    }

    public String getFormula() {
        return this.formula.toString();
    }

    @Override
    public String toString() {
        String result = formula.toString();
        for (Entry<String, Entity> entry : this.entities.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue().toString());
        }

        return result;
    }

    public static ResultBuilder from(final String text) throws ProcessorException {
        return new ResultBuilder().append(text);
    }

    public static ResultBuilder from(final Entity entity) throws ProcessorException {
        return new ResultBuilder().append(entity);
    }

    public static ResultBuilder from(final String block, final ResultBuilder input) throws ProcessorException {
        return from(new ResultBuilder(), block, input);
    }

    public static ResultBuilder from(final ResultBuilder result, final String block, final ResultBuilder input) throws ProcessorException {

        if (block.indexOf(StringUtils.ID_OPEN) < 0 || !input.hasEntities()) {
            return result.append(block);
        }

        final SortedMap<Integer, Entry<String, Entity>> positionsEntity = new TreeMap<>(Integer::compareTo);
        int start;
        for (Entry<String, Entity> entry : input.entities.entrySet()) {
            start = block.indexOf(entry.getKey());
            if (start > -1) {
                positionsEntity.put(start, entry);
            }
        }

        if (!positionsEntity.isEmpty()) {
            start = 0;
            for (Entry<Integer, Entry<String, Entity>> entry : positionsEntity.entrySet()) {
                if (entry.getKey() > start) {
                    result.append(block.substring(start, entry.getKey()));
                }

                result.append(entry.getValue().getValue());
                start += entry.getKey() + entry.getValue().getKey().length();
            }
            if (start < block.length()) {
                result.append(block.substring(start));
            }
        } else {
            result.append(block);
        }

        return result;
    }
}
