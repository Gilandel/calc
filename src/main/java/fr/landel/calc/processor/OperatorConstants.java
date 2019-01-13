package fr.landel.calc.processor;

import java.util.SortedSet;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import fr.landel.calc.utils.DateUtils;

public interface OperatorConstants {

    // VALIDATORS

    BiPredicate<Integer, Integer> ALL_EXCEPT_LAST = (index, end) -> index > -1 && index < end;
    BiPredicate<Integer, Integer> ALL_EXCEPT_FIRST_AND_LAST = (index, end) -> index > 0 && index < end;

    BiPredicate<Entity, Entity> IS_LEFT_NUMBER_AND_RIGHT_NOT_UNITY = (a, b) -> UnityType.NUMBER.equals(a.getUnityType()) && !b.isUnity() && !b.isDate();
    BiPredicate<Entity, Entity> IS_LEFT_NOT_UNITY_AND_RIGHT_NUMBER = (a, b) -> !a.isUnity() && !a.isDate() && UnityType.NUMBER.equals(b.getUnityType());
    BiPredicate<Entity, Entity> IS_ANY_NUMBER = IS_LEFT_NUMBER_AND_RIGHT_NOT_UNITY.or(IS_LEFT_NOT_UNITY_AND_RIGHT_NUMBER);
    BiPredicate<Entity, Entity> CHECK_ADD = (a, b) -> {
        return !a.isUnity() && a.getUnityType().equals(b.getUnityType())
                && (!UnityType.DATE.equals(a.getUnityType()) || (a.isDate() != b.isDate()) || (a.isDuration() && b.isDuration()));
    };
    BiPredicate<Entity, Entity> CHECK_SUBSTRACT = (a, b) -> {
        return !a.isUnity() && a.getUnityType().equals(b.getUnityType()) && (!UnityType.DATE.equals(a.getUnityType()) || (a.isDuration() && b.isDuration()) || a.isDate());
    };
    BiPredicate<Entity, Entity> IS_CONVERTIBLE = (a, b) -> !a.isUnity() && b.isUnity() && a.getUnityType().equals(b.getUnityType());

    // FUNCTIONS

    BiFunction<Entity, Entity, Entity> FUN_ADD = (a, b) -> {
        final SortedSet<Unity> unities = Unity.merge(a.getUnities(), b.getUnities());

        if (!UnityType.DATE.equals(a.getUnityType())) {
            return new Entity(a.getIndex(), a.getValue() + b.getValue(), unities);
        } else {
            return DateUtils.add(a, b, unities.last());
        }
    };
    BiFunction<Entity, Entity, Entity> FUN_SUBSTRACT = (a, b) -> {
        final SortedSet<Unity> unities = Unity.merge(a.getUnities(), b.getUnities());

        if (!UnityType.DATE.equals(a.getUnityType())) {
            return new Entity(a.getIndex(), a.getValue() - b.getValue(), unities);
        } else {
            return DateUtils.subtract(a, b, unities.last());
        }
    };
    BiFunction<Entity, Entity, Entity> FUN_MULTIPLY = (a, b) -> {
        final double r = a.toUnityOrValue() * b.toUnityOrValue();

        if (a.hasUnity()) {
            return new Entity(a.getIndex(), a.fromUnity(r), a.getUnities());
        } else if (b.hasUnity()) {
            return new Entity(a.getIndex(), b.fromUnity(r), b.getUnities());
        } else {
            return new Entity(a.getIndex(), r, b.getUnities());
        }
    };
    BiFunction<Entity, Entity, Entity> FUN_DEVIDE = (a, b) -> {
        if (a.hasUnity()) {
            return new Entity(a.getIndex(), a.fromUnity(a.toUnity() / b.getValue()), a.getUnities());
        } else {
            return new Entity(a.getIndex(), a.getValue() / b.getValue(), a.getUnities());
        }
    };
    BiFunction<Entity, Entity, Entity> FUN_MODULO = (a, b) -> new Entity(a.getIndex(), a.fromUnity(a.toUnity() % b.getValue()), a.getUnities());
    BiFunction<Entity, Entity, Entity> FUN_POWER = (a, b) -> new Entity(a.getIndex(), a.fromUnity(Math.pow(a.toUnity(), b.getValue())), a.getUnities());
    BiFunction<Entity, Entity, Entity> FUN_CONVERT = (a, b) -> a.setUnities(b.getUnities());
}
