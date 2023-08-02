package club.bigtian.mf.plugin.core.function;

@FunctionalInterface
public interface BigFunction<T, U,B, R> {

    R apply(T t, U u,B b  );
}
