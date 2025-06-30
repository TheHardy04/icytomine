package org.bioimageanalysis.icy.icytomine.core.model;

import java.util.Map;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;

/**
 * Model holding information for user annotations.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class UserAnnotation extends AbstractAnnotation
{

    /**
     * @param client
     *        The client connecting with the servers.
     * @param model
     *        The model The annotation Cytomine model.
     */
    public UserAnnotation(CytomineClient client, be.cytomine.client.models.Annotation model)
    {
        super(client, model);
    }

    /**
     * Retrieves all user annotations present on the given image.
     * 
     * @param image
     *        The image annotations belong to.
     * @return The list of user annotations.
     */
    public static Map<Long, UserAnnotation> getUserAnnotationsInImage(Image image)
    {
        return image.getClient().getImageUserAnnotations(image);
    }

    public static Map<Long, UserAnnotation> getUserAnnotationsInImage(Image image, Set<Long> userIds)
    {
        return image.getClient().getImageUserAnnotations(image, userIds);
    }

}
