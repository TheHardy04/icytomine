package org.bioimageanalysis.icy.icytomine.core.model.cache;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.Project;

/**
 * Represents the cache manager for cytomine project instances.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class ProjectCache extends EntityCache<Long, Project>
{

    /**
     * Creates the cache manager instance.
     * 
     * @param client
     *        The Cytomine client.
     * @return The created instance.
     */
    public static ProjectCache create(CytomineClient client)
    {
        return new ProjectCache(client);
    }

    private ProjectCache(CytomineClient client)
    {
        super(client);
    }

    @Override
    protected Class<Long> getKeyClass()
    {
        return Long.class;
    }

    @Override
    protected Class<Project> getValueClass()
    {
        return Project.class;
    }

}
