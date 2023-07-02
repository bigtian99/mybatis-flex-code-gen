package club.bigtian.mf.plugin.core.functions;

@FunctionalInterface
public interface SimpleFunction<T> {

    void apply(T t);
}
