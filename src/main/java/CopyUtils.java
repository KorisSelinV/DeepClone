import java.util.*;
import java.lang.reflect.*;

public class CopyUtils {

    public static <T> T deepCopy(T obj) {
        try {
            return deepCopy(obj, new IdentityHashMap<>());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при копировании объекта", e);
        }
    }

    private static <T> T deepCopy(T obj, Map<Object, Object> copies) throws Exception {
        if (obj == null) {
            return null;
        }

        if (copies.containsKey(obj)) {
            return (T) copies.get(obj);
        }

        Class<?> classObj = obj.getClass();
        // Обработка массивов
        if (classObj.isArray()) {
            int length = Array.getLength(obj);
            T copyArr = (T) Array.newInstance(classObj.getComponentType(), length);
            copies.put(obj, copyArr);
            for (int i = 0; i < length; i++) {
                Array.set(copyArr, i, deepCopy(Array.get(obj, i), copies));
            }
            return copyArr;
        }
        // Обработка коллекций
        if (obj instanceof Collection<?> collection) {
            Collection<Object> copy = collection instanceof List ? new ArrayList<>() :
                    collection instanceof Set ? new HashSet<>() :
                            new LinkedList<>();
            copies.put(obj, copy);
            for (Object item : collection) {
                copy.add(deepCopy(item, copies));
            }
            return (T) copy;
        }
        // Обработка Map
        if (obj instanceof Map) {
            Map<Object, Object> copy = obj instanceof SortedMap ? new TreeMap<>() :
                    obj instanceof LinkedHashMap ? new LinkedHashMap<>() :
                            new HashMap<>();
            copies.put(obj, copy);
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                Object keyCopy = deepCopy(entry.getKey(), copies);
                Object valueCopy = deepCopy(entry.getValue(), copies);
                copy.put(keyCopy, valueCopy);
            }
            return (T) copy;
        }
        // Примитивные типы и строки возвращаем как есть
        if (classObj.isPrimitive() || obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Character) {
            return obj;
        }
        // Создаем новый экземпляр объекта
        T copy = (T) classObj.getDeclaredConstructor().newInstance();
        copies.put(obj, copy); // Сохраняем объект в Map копий

        // Копируем все поля объекта
        while (classObj != null) {
            for (Field field : classObj.getDeclaredFields()) {
                field.setAccessible(true);
                Object fieldValue = field.get(obj);
                field.set(copy, deepCopy(fieldValue, copies));
            }
            classObj = classObj.getSuperclass();
        }

        return copy;
    }

    public static void main(String[] args) {
        Man original = new Man("Ivan", 30, Arrays.asList("Book1", "Book2"));
        Man copy = deepCopy(original);
        copy.setAge(55);

        System.out.printf("Оригинал: %s, %d, %s%n", original.getName(), original.getAge(), original.getFavoriteBooks());
        System.out.printf("Копия: %s, %d, %s%n", copy.getName(), copy.getAge(), copy.getFavoriteBooks());

        // Проверка независимости копии от оригинала
        copy.setName("Dima");
        copy.setAge(25);
        copy.getFavoriteBooks().add("Book3");

        System.out.printf("Измененный оригинал: %s, %d, %s%n", original.getName(), original.getAge(), original.getFavoriteBooks());
        System.out.printf("Измененная копия: %s, %d, %s%n", copy.getName(), copy.getAge(), copy.getFavoriteBooks());
    }
}
