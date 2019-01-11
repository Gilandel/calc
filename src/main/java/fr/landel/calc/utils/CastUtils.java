/*
 * #%L
 * utils-commons
 * %%
 * Copyright (C) 2016 - 2018 Gilles Landel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package fr.landel.calc.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Utility class to cast classes.
 *
 * @since Nov 23, 2015
 * @author Gilles Landel
 *
 */
public final class CastUtils {

    private static final Logger LOGGER = new Logger(CastUtils.class);

    private static final Collector<CharSequence, ?, String> COMMA_JOINING_COLLECTOR = Collectors.joining(", ");

    /**
     * Simple cache to avoid reflect calls
     */
    private static ConcurrentMap<Class<?>, Constructor<?>[]> constructorsCache = new ConcurrentHashMap<>();

    /**
     * Simple cache to avoid re-analyzing
     */
    private static ConcurrentMap<Integer, Constructor<?>> constructorCache = new ConcurrentHashMap<>();

    /**
     * Hidden constructor.
     */
    private CastUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the class of the object ({@code null} safe).
     * 
     * @param object
     *            The object (required)
     * @param <T>
     *            The object type
     * @return The class of the object or null
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(final T object) {
        if (object != null) {
            return (Class<T>) object.getClass();
        }
        return null;
    }

    /**
     * Auto cast an object.
     * 
     * @param object
     *            The object (required)
     * @param <T>
     *            The object type
     * @return The casted object
     * @throws ClassCastException
     *             on cast failure
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(final Object object) {
        return (T) object;
    }

    /**
     * Cast an object into the specified class (null safe).
     * 
     * @param o
     *            The input object (required)
     * @param clazz
     *            The output class (required)
     * @return The casted object or {@code null}
     * @param <T>
     *            The type of the output
     */
    public static <T> T cast(final Object o, final Class<T> clazz) {
        if (o != null && clazz != null && clazz.isAssignableFrom(o.getClass())) {
            return clazz.cast(o);
        }
        return null;
    }

    /**
     * Cast an object into a typed {@link ArrayList}.
     * 
     * @param o
     *            The input object
     * @param clazz
     *            The output class
     * @return The casted object
     * @param <T>
     *            The type of the output
     */
    public static <T> List<T> getArrayList(final Object o, final Class<T> clazz) {
        final List<T> list = new ArrayList<>();

        list(list, o, clazz);

        return list;
    }

    /**
     * Cast an object into a typed {@link Vector}.
     * 
     * @param o
     *            The input object
     * @param clazz
     *            The output class
     * @return The casted object
     * @param <T>
     *            The type of the output
     */
    public static <T> List<T> getVector(final Object o, final Class<T> clazz) {
        final List<T> list = new Vector<>();

        list(list, o, clazz);

        return list;
    }

    /**
     * Cast an object into a typed {@link LinkedList} as {@link List}.
     * 
     * @param o
     *            The input object
     * @param clazz
     *            The output class
     * @return The casted object
     * @param <T>
     *            The type of the output
     */
    public static <T> List<T> getLinkedListAsList(final Object o, final Class<T> clazz) {
        final List<T> list = new LinkedList<>();

        list(list, o, clazz);

        return list;
    }

    /**
     * Cast an object into a typed {@link LinkedList} as {@link Queue}.
     * 
     * @param o
     *            The input object
     * @param clazz
     *            The output class
     * @return The casted object
     * @param <T>
     *            The type of the output
     */
    public static <T> Queue<T> getLinkedListAsQueue(final Object o, final Class<T> clazz) {
        final Queue<T> queue = new LinkedList<>();

        queue(queue, o, clazz, false);

        return queue;
    }

    /**
     * Cast an object into a typed {@link LinkedTransferQueue}.
     * 
     * @param o
     *            The input object
     * @param clazz
     *            The output class
     * @return The casted object
     * @param <T>
     *            The type of the output
     */
    public static <T> Queue<T> getLinkedTransferQueue(final Object o, final Class<T> clazz) {
        final Queue<T> queue = new LinkedTransferQueue<>();

        queue(queue, o, clazz, true);

        return queue;
    }

    /**
     * Cast an object into a typed {@link PriorityQueue}.
     * 
     * @param o
     *            The input object
     * @param clazz
     *            The output class
     * @return The casted object
     * @param <T>
     *            The type of the output
     */
    public static <T> Queue<T> getPriorityQueue(final Object o, final Class<T> clazz) {
        final Queue<T> queue = new PriorityQueue<>();

        queue(queue, o, clazz, true);

        return queue;
    }

    /**
     * Cast an object into a typed {@link LinkedBlockingQueue}.
     * 
     * @param o
     *            The input object
     * @param clazz
     *            The output class
     * @return The casted object
     * @param <T>
     *            The type of the output
     */
    public static <T> BlockingQueue<T> getLinkedBlockingQueue(final Object o, final Class<T> clazz) {
        final BlockingQueue<T> queue = new LinkedBlockingQueue<>();

        queue(queue, o, clazz, true);

        return queue;
    }

    /**
     * Cast an object into a typed {@link ArrayBlockingQueue}.
     * 
     * @param o
     *            The input object
     * @param clazz
     *            The output class
     * @param capacity
     *            The initial capacity
     * @return The casted object
     * @param <T>
     *            The type of the output
     */
    public static <T> BlockingQueue<T> getArrayBlockingQueue(final Object o, final Class<T> clazz, final int capacity) {
        final BlockingQueue<T> queue = new ArrayBlockingQueue<>(capacity);

        queue(queue, o, clazz, true);

        return queue;
    }

    /**
     * Cast an object into a typed {@link PriorityBlockingQueue}.
     * 
     * @param o
     *            The input object
     * @param clazz
     *            The output class
     * @return The casted object
     * @param <T>
     *            The type of the output
     */
    public static <T> BlockingQueue<T> getPriorityBlockingQueue(final Object o, final Class<T> clazz) {
        final BlockingQueue<T> queue = new PriorityBlockingQueue<>();

        queue(queue, o, clazz, true);

        return queue;
    }

    /**
     * Cast an object into a typed {@link HashMap}.
     * 
     * @param o
     *            The input object
     * @param classKey
     *            The class of the key
     * @param classValue
     *            The class of the value
     * @return The casted object
     * @param <K>
     *            The type of key
     * @param <V>
     *            The type of the value
     */
    public static <K, V> Map<K, V> getHashMap(final Object o, final Class<K> classKey, final Class<V> classValue) {
        final Map<K, V> map = new HashMap<>();

        map(map, o, classKey, classValue, false);

        return map;
    }

    /**
     * Cast an object into a typed {@link TreeMap}.
     * 
     * @param o
     *            The input object
     * @param classKey
     *            The class of the key
     * @param classValue
     *            The class of the value
     * @return The casted object
     * @param <K>
     *            The type of key
     * @param <V>
     *            The type of the value
     */
    public static <K, V> Map<K, V> getTreeMap(final Object o, final Class<K> classKey, final Class<V> classValue) {
        final Map<K, V> map = new TreeMap<>();

        map(map, o, classKey, classValue, true);

        return map;
    }

    /**
     * Cast an object into a typed {@link TreeMap}. Comparator has to be null
     * safe.
     * 
     * @param o
     *            The input object
     * @param classKey
     *            The class of the key
     * @param classValue
     *            The class of the value
     * @param comparator
     *            The tree map comparator
     * @return The casted object
     * @param <K>
     *            The type of key
     * @param <V>
     *            The type of the value
     */
    public static <K, V> SortedMap<K, V> getTreeMap(final Object o, final Class<K> classKey, final Class<V> classValue, final Comparator<K> comparator) {
        final SortedMap<K, V> map = new TreeMap<>(comparator);

        map(map, o, classKey, classValue, false);

        return map;
    }

    /**
     * Cast an object into a typed {@link Hashtable}.
     * 
     * @param o
     *            The input object
     * @param classKey
     *            The class of the key
     * @param classValue
     *            The class of the value
     * @return The casted object
     * @param <K>
     *            The type of key
     * @param <V>
     *            The type of the value
     */
    public static <K, V> Map<K, V> getHashtable(final Object o, final Class<K> classKey, final Class<V> classValue) {
        final Map<K, V> map = new Hashtable<>();

        map(map, o, classKey, classValue, true);

        return map;
    }

    /**
     * Cast an object into a typed {@link LinkedHashMap}.
     * 
     * @param o
     *            The input object
     * @param classKey
     *            The class of the key
     * @param classValue
     *            The class of the value
     * @return The casted object
     * @param <K>
     *            The type of key
     * @param <V>
     *            The type of the value
     */
    public static <K, V> Map<K, V> getLinkedHashMap(final Object o, final Class<K> classKey, final Class<V> classValue) {
        final Map<K, V> map = new LinkedHashMap<>();

        map(map, o, classKey, classValue, true);

        return map;
    }

    /**
     * Cast an object into a typed {@link HashSet}.
     * 
     * @param o
     *            The input object
     * @param classElement
     *            The class of the element in the set
     * @return The casted object
     * @param <T>
     *            The type of the element
     */
    public static <T> Set<T> getHashSet(final Object o, final Class<T> classElement) {
        final Set<T> set = new HashSet<>();

        set(set, o, classElement);

        return set;
    }

    /**
     * Cast an object into a typed {@link TreeSet}.
     * 
     * @param o
     *            The input object
     * @param classElement
     *            The class of the element in the set
     * @return The casted object
     * @param <T>
     *            The type of the element
     */
    public static <T> Set<T> getTreeSet(final Object o, final Class<T> classElement) {
        final Set<T> set = new TreeSet<>();

        set(set, o, classElement);

        return set;
    }

    /**
     * Cast an object into a typed {@link TreeSet}.
     * 
     * @param o
     *            The input object
     * @param classElement
     *            The class of the element in the set
     * @param comparator
     *            The comparator to keep the sort
     * @return The casted object
     * @param <T>
     *            The type of the element
     */
    public static <T> SortedSet<T> getTreeSet(final Object o, final Class<T> classElement, final Comparator<T> comparator) {
        final SortedSet<T> set = new TreeSet<>(comparator);

        set(set, o, classElement);

        return set;
    }

    /**
     * Set the map value.
     * 
     * @param map
     *            the map
     * @param key
     *            the key
     * @param obj
     *            the object
     * @param classValue
     *            the class value
     * @param removeNull
     *            If null has to be removed
     * @param <K>
     *            The type of key
     * @param <V>
     *            The type of the value
     * 
     */
    private static <K, V> void setMapValue(final Map<K, V> map, final K key, final Entry<?, ?> obj, final Class<V> classValue, final boolean removeNull) {
        if (obj.getValue() != null && classValue.isAssignableFrom(obj.getValue().getClass())) {
            map.put(key, classValue.cast(obj.getValue()));
        } else if (obj.getValue() == null && !removeNull) {
            map.put(key, null);
        }
    }

    /**
     * Set the list value.
     * 
     * @param list
     *            the list (required)
     * @param obj
     *            the object (nullable)
     * @param clazz
     *            the class (required)
     * @param <T>
     *            The type of the element
     */
    private static <T> void setListValue(final List<T> list, final Object obj, final Class<T> clazz) {
        if (obj != null && clazz.isAssignableFrom(obj.getClass())) {
            list.add(clazz.cast(obj));
        } else if (obj == null) {
            list.add(null);
        }
    }

    /**
     * Set the queue value.
     * 
     * @param queue
     *            the queue
     * @param obj
     *            the object
     * @param clazz
     *            the class
     * @param removeNull
     *            If null has to be removed
     * @param <T>
     *            The type of the element
     */
    private static <T> void setQueueValue(final Queue<T> queue, final Object obj, final Class<T> clazz, final boolean removeNull) {
        if (obj != null && clazz.isAssignableFrom(obj.getClass())) {
            queue.add(clazz.cast(obj));
        } else if (obj == null && !removeNull) {
            queue.add(null);
        }
    }

    /**
     * Cast the object and feel the map.
     * 
     * @param map
     *            The output map
     * @param o
     *            The input object
     * @param classKey
     *            The class of the map key
     * @param classValue
     *            The class of the map value
     * @param removeNull
     *            If null has to be removed
     */
    private static <K, V> void map(final Map<K, V> map, final Object o, final Class<K> classKey, final Class<V> classValue, final boolean removeNull) {
        if (o != null && Map.class.isAssignableFrom(o.getClass()) && classKey != null && classValue != null) {
            Map<?, ?> mObj = (Map<?, ?>) o;
            for (Entry<?, ?> obj : mObj.entrySet()) {
                if (obj.getKey() != null && classKey.isAssignableFrom(obj.getKey().getClass())) {
                    final K key = classKey.cast(obj.getKey());
                    setMapValue(map, key, obj, classValue, removeNull);
                } else if (obj.getKey() == null && !removeNull) {
                    setMapValue(map, null, obj, classValue, removeNull);
                }
            }
        }
    }

    /**
     * Cast the object and feel the queue.
     * 
     * @param queue
     *            The output queue
     * @param o
     *            The input object
     * @param classElement
     *            The typed class
     * @param removeNull
     *            If null has to be removed
     */
    private static <T> void queue(final Queue<T> queue, final Object o, final Class<T> clazz, final boolean removeNull) {
        if (o != null && Queue.class.isAssignableFrom(o.getClass())) {
            Queue<?> mObj = (Queue<?>) o;
            for (Object obj : mObj) {
                setQueueValue(queue, obj, clazz, removeNull);
            }
        }
    }

    /**
     * Cast the object and feel the list.
     * 
     * @param list
     *            The output list
     * @param o
     *            The input object
     * @param classElement
     *            The typed class
     */
    private static <T> void list(final List<T> list, final Object o, final Class<T> clazz) {
        if (o != null && clazz != null && List.class.isAssignableFrom(o.getClass())) {
            List<?> mObj = (List<?>) o;
            for (Object obj : mObj) {
                setListValue(list, obj, clazz);
            }
        }
    }

    /**
     * Cast the object and feel the set.
     * 
     * @param set
     *            The output set
     * @param o
     *            The input object
     * @param classElement
     *            The typed class
     */
    private static <T> void set(final Set<T> set, final Object o, final Class<T> classElement) {
        if (o != null && classElement != null && Set.class.isAssignableFrom(o.getClass())) {
            Set<?> mObj = (Set<?>) o;
            for (Object obj : mObj) {
                if (obj != null && classElement.isAssignableFrom(obj.getClass())) {
                    set.add(classElement.cast(obj));
                }
            }
        }
    }

    /**
     * Cast an object into a typed iterator.
     * 
     * @param o
     *            The input object
     * @param classElement
     *            The class of the element in the iterator
     * @return The casted object
     * @param <T>
     *            The type of the element
     */
    public static <T> Iterator<T> getIterator(final Object o, final Class<T> classElement) {
        final List<T> list = new ArrayList<>();

        if (o != null && classElement != null && Iterator.class.isAssignableFrom(o.getClass())) {
            Iterator<?> mObj = (Iterator<?>) o;
            while (mObj.hasNext()) {
                Object obj = mObj.next();
                if (obj == null) {
                    list.add(null);
                } else if (classElement.isAssignableFrom(obj.getClass())) {
                    final T element = classElement.cast(obj);
                    list.add(element);
                }
            }
        }

        return list.iterator();
    }

    /**
     * Instantiate some objects.
     * 
     * @param logLevelInfo
     *            log level info
     * @param constructor
     *            constructor (required, not null)
     * @param objects
     *            list of objects (required, not null)
     * @return the instantiated class
     * @param <T>
     *            The type of the element
     */
    @SuppressWarnings("unchecked")
    protected static <T> T instantiate(final boolean logLevelInfo, final Constructor<?> constructor, final Object... objects) {
        T result = null;
        try {
            result = (T) constructor.newInstance(objects);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            final String arguments = Arrays.stream(objects).map(String::valueOf).collect(COMMA_JOINING_COLLECTOR);
            if (logLevelInfo) {
                LOGGER.info("Cannot instantiate {} with arguments {}", constructor.getName(), arguments);
            } else {
                LOGGER.error("Cannot instantiate {} with arguments {}", constructor.getName(), arguments);
            }
        }
        return result;
    }

    /**
     * Get a constructor for a class and put the result in cache, to increase
     * speed for the next call.
     * 
     * @param instantiableClass
     *            The class to instanciate
     * @param classes
     *            The parameter classes to search
     * @param <T>
     *            The type of class and constructor
     * @return The list of constructors
     */
    @SuppressWarnings("unchecked")
    protected static <T> Constructor<T> getConstructor(final Class<T> instantiableClass, final Class<?>... classes) {
        final int hashCode = instantiableClass.hashCode() ^ Arrays.hashCode(classes);

        if (constructorCache.containsKey(hashCode)) {
            return (Constructor<T>) constructorCache.get(hashCode);
        } else {
            if (!constructorsCache.containsKey(instantiableClass)) {
                constructorsCache.put(instantiableClass, instantiableClass.getDeclaredConstructors());
            }

            boolean mismatch;

            for (Constructor<?> constructor : constructorsCache.get(instantiableClass)) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == classes.length) {
                    mismatch = false;
                    // only take the first matching constructor
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (classes[i] != null && !parameterTypes[i].isAssignableFrom(classes[i])) {
                            mismatch = true;
                            break;
                        }
                    }
                    if (!mismatch) {
                        constructorCache.put(hashCode, constructor);
                        return (Constructor<T>) constructor;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Instantiate a typed constructor.
     * 
     * @param instantiableClass
     *            instantiable class (required, not null)
     * @param classes
     *            classes (required, not null)
     * @param objects
     *            objects
     * @return the instantiated constructor
     * @param <T>
     *            The type of the element
     */
    protected static <T> T instantiateConstructor(final Class<T> instantiableClass, final Class<?>[] classes, final Object[] objects) {
        T result = null;

        final Constructor<T> typedConstructor = getConstructor(instantiableClass, classes);

        if (typedConstructor != null) {
            result = instantiate(true, typedConstructor, objects);
        } else {
            final String arguments = Arrays.stream(objects).map(String::valueOf).collect(COMMA_JOINING_COLLECTOR);
            LOGGER.info("Cannot map [{}] into {}, constructor signature not found", arguments, instantiableClass.getName());
        }

        return result;
    }

    /**
     * Map a list of object into a instantiable.
     * 
     * @param instantiableClass
     *            The instantiable
     * @param objects
     *            The objects to pass through the constructor
     * @return The new instance
     * @param <T>
     *            The result class
     */
    public static <T> T map(final Class<T> instantiableClass, final Object... objects) {
        T result = null;

        if (instantiableClass != null && objects != null) {
            Class<?>[] classes = new Class<?>[objects.length];

            for (int i = 0; i < objects.length; i++) {
                classes[i] = CastUtils.getClass(objects[i]);
            }

            result = instantiateConstructor(instantiableClass, classes, objects);
        }

        return result;
    }

    /**
     * Get the generic {@link List} class.
     * 
     * @param instantiableClass
     *            The item class
     * @param <T>
     *            The item type
     * @return The iterable typed
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<List<T>> getTypedListClass(final Class<T> instantiableClass) {
        final List<T> list = new ArrayList<>();

        if (instantiableClass != null) {
            try {
                list.add(instantiableClass.getDeclaredConstructor().newInstance());

            } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
                LOGGER.error("Errors occurred in CastUtils#getTypedListClass()", e);
            }
        }

        return (Class<List<T>>) list.getClass();
    }

    /**
     * Get the generic {@link Set} class.
     * 
     * @param instantiableClass
     *            The item class
     * @param <T>
     *            The item type
     * @return The iterable typed
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<Set<T>> getTypedSetClass(final Class<T> instantiableClass) {
        final Set<T> set = new HashSet<>();

        if (instantiableClass != null) {
            try {
                set.add(instantiableClass.getDeclaredConstructor().newInstance());
            } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
                LOGGER.error("Errors occurred in CastUtils#getTypedSetClass()", e);
            }
        }

        return (Class<Set<T>>) set.getClass();
    }

    /**
     * Get the generic {@link Queue} class.
     * 
     * @param instantiableClass
     *            The item class
     * @param <T>
     *            The item type
     * @return The iterable typed
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<Queue<T>> getTypedQueueClass(final Class<T> instantiableClass) {
        final Queue<T> queue = new LinkedList<>();

        if (instantiableClass != null) {
            try {
                queue.add(instantiableClass.getDeclaredConstructor().newInstance());

            } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
                LOGGER.error("Errors occurred in CastUtils#getTypedQueueClass()", e);
            }
        }

        return (Class<Queue<T>>) queue.getClass();
    }

    /**
     * Get the generic {@link Map} class.
     * 
     * @param instantiableKeyClass
     *            The instantiable key class
     * @param instantiableValueClass
     *            The instantiable value class
     * @param <K>
     *            The key type
     * @param <V>
     *            The value type
     * @return The map typed
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Class<Map<K, V>> getTypedMapClass(final Class<K> instantiableKeyClass, final Class<V> instantiableValueClass) {
        final Map<K, V> map = new HashMap<>();

        if (instantiableKeyClass != null && instantiableValueClass != null) {
            try {
                map.put(instantiableKeyClass.getDeclaredConstructor().newInstance(), instantiableValueClass.getDeclaredConstructor().newInstance());

            } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
                LOGGER.error("Errors occurred in CastUtils#getTypedMapClass()", e);
            }
        }

        return (Class<Map<K, V>>) map.getClass();
    }
}
