package org.bioimageanalysis.icy.icytomine.core.model;

import java.util.Map;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;

/**
 * Model holding information for reviewed annotations.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class ReviewedAnnotation extends AbstractAnnotation
{

    /**
     * @param client
     *        The client connecting with the servers.
     * @param model
     *        The model The annotation Cytomine model.
     */
    public ReviewedAnnotation(CytomineClient client, be.cytomine.client.models.Annotation model)
    {
        super(client, model);
    }

    /**
     * Retrieves all reviewed annotations present on the given image.
     * 
     * @param image
     *        The image annotations belong to.
     * @return The list of reviewed annotations.
     */
    public static Map<Long, ReviewedAnnotation> getReviewedAnnotationsInImage(Image image)
    {
        return image.getClient().getImageReviewedAnnotations(image);
    }
    
    public static Map<Long, ReviewedAnnotation> getReviewedAnnotationsInImage(Image image, Set<Long> reviewUserIds)
    {
        return image.getClient().getImageReviewedAnnotations(image, reviewUserIds);
    }

}
