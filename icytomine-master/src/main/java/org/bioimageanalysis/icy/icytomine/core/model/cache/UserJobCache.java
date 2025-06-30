package org.bioimageanalysis.icy.icytomine.core.model.cache;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.UserJob;

/**
 * Represents the cache manager for cytomine user job instances.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class UserJobCache extends EntityCache<Long, UserJob>
{

    /**
     * Creates the cache manager instance.
     * 
     * @param client
     *        The Cytomine client.
     * @return The created instance.
     */
    public static UserJobCache create(CytomineClient client)
    {
        return new UserJobCache(client);
    }

    /**
     * @param client
     */
    public UserJobCache(CytomineClient client)
    {
        super(client);
    }

    @Override
    protected Class<Long> getKeyClass()
    {
        return Long.class;
    }

    @Override
    protected Class<UserJob> getValueClass()
    {
        return UserJob.class;
    }

}
