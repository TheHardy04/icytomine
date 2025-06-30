/**
 * 
 */
package org.bioimageanalysis.icy.icytomine.core.model.cache;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;

/**
 * The cache used for annotations of different types (user, algorithm, reviewed)
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class AbstractAnnotationCache extends EntityCache<Long, AbstractAnnotation>
{
    private static final long ANNOTATION_HEAP_SIZE = 2000;

    public static AbstractAnnotationCache create(CytomineClient client)
    {
        return new AbstractAnnotationCache(client);
    }

    public AbstractAnnotationCache(CytomineClient client)
    {
        super(client);
    }

    @Override
    protected Class<Long> getKeyClass()
    {
        return Long.class;
    }

    @Override
    protected Class<AbstractAnnotation> getValueClass()
    {
        return AbstractAnnotation.class;
    }

    @Override
    public long getHeapSize()
    {
        return ANNOTATION_HEAP_SIZE;
    }

}
