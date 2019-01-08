package fr.landel.calc.processor;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public interface OperatorConstants {
    BiPredicate<Entity, Entity> IS_LEFT_NUMBER_AND_RIGHT_NOT_UNITY = (a, b) -> Unity.Type.NUMBER.equals(a.getUnityType()) && !b.isUnity();
    BiPredicate<Entity, Entity> IS_LEFT_NOT_UNITY_AND_RIGHT_NUMBER = (a, b) -> !a.isUnity() && Unity.Type.NUMBER.equals(b.getUnityType());
    BiPredicate<Entity, Entity> IS_ANY_NUMBER = IS_LEFT_NUMBER_AND_RIGHT_NOT_UNITY.or(IS_LEFT_NOT_UNITY_AND_RIGHT_NUMBER);
    BiPredicate<Entity, Entity> IS_SAME_TYPE_AND_NOT_UNITY = (a, b) -> !a.isUnity() && a.getUnityType().equals(b.getUnityType());
    BiPredicate<Entity, Entity> IS_CONVERTIBLE = (a, b) -> !a.isUnity() && b.isUnity() && a.getUnityType().equals(b.getUnityType());

    BiFunction<Entity, Entity, Entity> FUN_MULTIPLY = (a, b) -> {
        if (a.isNumber()) {
            return new Entity(a.getIndex(), a.fromUnity(a.toUnity() * b.getValue()), a.getUnities());
        } else {
            return new Entity(a.getIndex(), b.fromUnity(b.toUnity() * a.getValue()), b.getUnities());
        }
    };
    BiFunction<Entity, Entity, Entity> FUN_DEVIDE = (a, b) -> new Entity(a.getIndex(), a.fromUnity(a.toUnity() / b.getValue()), a.getUnities());
}
