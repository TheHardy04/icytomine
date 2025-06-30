package danyfel80.common.stream;

import java.util.function.Consumer;

/**
 * A consumer that can throw exceptions during its execution.
 * 
 * @see Consumer
 * @author Daniel Felipe Gonzalez Obando
 * @param <T>
 *        Type of the parameter value.
 */
@FunctionalInterface
public interface CheckedConsumer<T>
{
    /**
     * Perform this operation on the given argument.
     * 
     * @param t
     *        The input argument.
     * @throws Exception
     *         If an unexpected behavior occurs during the execution of this method.
     */
    void apply(T t) throws Exception;
}
