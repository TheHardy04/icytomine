package org.bioimageanalysis.icy.icytomine.core.model;

import java.util.Map;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;

/**
 * Model holding information for algorithm annotations.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class AlgorithmAnnotation extends AbstractAnnotation
{

    /**
     * @param client
     *        The client connecting with the servers.
     * @param model
     *        The model The annotation Cytomine model.
     */
    public AlgorithmAnnotation(CytomineClient client, be.cytomine.client.models.Annotation model)
    {
        super(client, model);
    }

    /**
     * Retrieves all algorithm annotations present on the given image.
     * 
     * @param image
     *        The image annotations belong to.
     * @return The list of algorithm annotations.
     */
    public static Map<Long, AlgorithmAnnotation> getAlgorithmAnnotationsInImage(Image image)
    {
        return image.getClient().getImageAlgorithmAnnotations(image);
    }

    public static Map<Long, AlgorithmAnnotation> getAlgorithmAnnotationsInImage(Image image, Set<Long> userJobIds)
    {
        return image.getClient().getImageAlgorithmAnnotations(image, userJobIds);
    }

    @Override
    public User getUser()
    {
        return getUserJob().getUser();
    }

    public UserJob getUserJob()
    {
        return getClient().getUserJob(getInternalAnnotation().getLong("user"));
    }

}
