/**
 * 
 */
package org.bioimageanalysis.icy.icytomine.core.model;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;

/**
 * @author Daniel Felipe Gonzalez Obando
 *
 */
public class Job extends Entity
{

    /**
     * @param client
     * @param model
     */
    public Job(CytomineClient client, be.cytomine.client.models.Job model)
    {
        super(client, model);
    }

}
