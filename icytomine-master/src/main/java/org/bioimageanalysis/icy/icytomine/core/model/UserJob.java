/**
 * 
 */
package org.bioimageanalysis.icy.icytomine.core.model;

import java.util.Date;
import java.util.Optional;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class UserJob extends Entity
{

    /**
     * @param client
     * @param model
     */
    public UserJob(CytomineClient client, be.cytomine.client.models.UserJob model)
    {
        super(client, model);
    }

    public Optional<String> getName()
    {
        return getStr("softwareName");
    }

    public User getUser()
    {
        Long userId = getLong("user").orElse(0L);
        return getClient().getUser(userId);
    }

    public Optional<Date> getDate()
    {
        Optional<Long> createdInstant = getLong("created");
        if (createdInstant.isPresent())
        {
            return Optional.ofNullable(new Date(createdInstant.get()));
        }
        else
        {
            return Optional.empty();
        }

    }

}
