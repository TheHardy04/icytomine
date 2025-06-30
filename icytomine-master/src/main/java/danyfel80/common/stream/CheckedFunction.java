package danyfel80.common.stream;

import java.util.function.Function;

/**
 * A function that can throw exceptions during its execution.
 * 
 * @see Function
 * @author Daniel Felipe Gonzalez Obando
 * @param <T>
 *        Type of the parameter value.
 * @param <R>
 *        Type of the return value.
 */
@FunctionalInterface
public interface CheckedFunction<T, R>
{

    /**
     * Applies this function to the given argument.
     * 
     * @param t
     *        The input argument.
     * @return The output value.
     * @throws Exception
     *         If an unexpected behavior occurs during the execution of this method.
     */
    R apply(T t) throws Exception;
}
