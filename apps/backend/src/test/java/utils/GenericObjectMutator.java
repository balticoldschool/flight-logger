package utils;

import java.util.function.Consumer;

@FunctionalInterface
public interface GenericObjectMutator<T> extends Consumer<T> {
}
