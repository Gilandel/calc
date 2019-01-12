package fr.landel.calc.processor;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface FunctionConstants {

    Function<Supplier<Double>, Function<Entity[], Entity>> NO_PARAM = f -> e -> new Entity(0, f.get());
    BiFunction<Supplier<Double>, Unity, Function<Entity[], Entity>> NO_PARAM_UNITY = (f, u) -> e -> new Entity(0, f.get(), u);
    Function<Function<Double, Double>, Function<Entity[], Entity>> ONE_PARAM = f -> e -> new Entity(e[0].getIndex(), f.apply(e[0].getValue()), e[0].getUnities());
    BiFunction<Function<Double, Double>, Unity, Function<Entity[], Entity>> ONE_PARAM_UNITY = (f, u) -> e -> new Entity(e[0].getIndex(), f.apply(e[0].getValue()), u);
    Function<BiFunction<Double, Double, Double>, Function<Entity[], Entity>> TWO_PARAM = f -> e -> new Entity(e[0].getIndex(), f.apply(e[0].getValue(), e[1].getValue()),
            e[0].getUnities());
    BiFunction<BiFunction<Double, Double, Double>, Unity, Function<Entity[], Entity>> TWO_PARAM_UNITY = (f,
            u) -> e -> new Entity(e[0].getIndex(), f.apply(e[0].getValue(), e[1].getValue()), u);
}
