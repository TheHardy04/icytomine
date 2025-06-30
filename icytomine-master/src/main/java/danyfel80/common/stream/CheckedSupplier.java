/**
 * 
 */
package danyfel80.common.stream;

import java.util.function.Supplier;

/**
 * A supplier that can throw exceptions during its execution.
 * 
 * @see Supplier
 * @author Daniel Felipe Gonzalez Obando
 * @param <R>
 *        Type of the return value.
 */
@FunctionalInterface
public interface CheckedSupplier<R>
{
    /**
     * Perform this operation returning a value of type {@link R}.
     * 
     * @throws Exception
     *         If an unexpected behavior occurs during the execution of this method.
     */
    R apply() throws Exception;
}
