package fr.landel.calc.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import fr.landel.calc.utils.StringUtils;

public class ResultBuilder {

    // private static final Logger LOGGER = new Logger(Result.class);

    private static final String FIRST_ID = idBuilder(0);

    private static final String ERROR_CONCAT = "cannot evaluate: {} and {}";

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

    public ResultBuilder append(final String text) throws ProcessorException {
        if (formula.length() > 0 && Arrays.binarySearch(NUMBER, formula.charAt(formula.length() - 1)) > -1 && Arrays.binarySearch(NUMBER, text.charAt(0)) > -1) {
            throw new ProcessorException(ERROR_CONCAT, formula, text);
        }
        formula.append(text);
        length += text.length();
        return this;
    }

    public ResultBuilder append(final Entity entity) throws ProcessorException {
        if (formula.length() > 0 && Arrays.binarySearch(NUMBER, formula.charAt(formula.length() - 1)) > -1) {
            throw new ProcessorException(ERROR_CONCAT, formula, entity);
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

    public Entity firstEntity() {
        return this.entities.get(FIRST_ID);
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
}
