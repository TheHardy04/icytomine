package danyfel80.common.stream;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utilitary class providing functions to be used when using streams.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class StreamUtils
{
    /**
     * This wrapper allows to capture exceptions thrown by a {@link CheckedFunction}, which are not commonly allowed when using calls to map in streams. For
     * this, the wrapper encapsulates the thrown exception inside a {@link RuntimeException} that is thrown and captured by the map function.
     * 
     * @param <T>
     *        Type of parameter of the function.
     * @param <R>
     *        Type of return of the function.
     * @param checkedFunction
     *        The checked function being wrapped.
     * @return The function wrapping the input checked function. The wrapper throws a {@link RuntimeException} if any kind of exception is thrown by the
     *         parameter function.
     */
    public static <T, R> Function<T, R> wrapFunction(CheckedFunction<T, R> checkedFunction) throws RuntimeException
    {
        return t -> {
            try
            {
                return checkedFunction.apply(t);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * This wrapper allows to capture exceptions thrown by a {@link CheckedConsumer}, which are not commonly allowed when using calls to forEach in streams. For
     * this, the wrapper encapsulates the thrown exception inside a {@link RuntimeException} that is thrown and captured by the forEach function.
     * 
     * @param <T>
     *        Type of parameter of the consumer.
     * @param checkedConsumer
     *        The checked consumer being wrapped.
     * @return The consumer wrapping the input checked consumer. The wrapper throws a {@link RuntimeException} if any kind of exception is thrown by
     *         the parameter consumer.
     */
    public static <T> Consumer<T> wrapConsumer(CheckedConsumer<T> checkedConsumer) throws RuntimeException
    {
        return t -> {
            try
            {
                checkedConsumer.apply(t);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        };
    }

    public static <R> Supplier<R> wrapSupplier(CheckedSupplier<R> checkedSupplier) throws RuntimeException
    {
        return () -> {
            try
            {
                return checkedSupplier.apply();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        };
    }
}
