package org.bioimageanalysis.icy.icytomine.core.model;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Map;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.junit.Before;
import org.junit.Test;

public class AnnotationSpec
{

    private CytomineClient client;

    @Before
    public void setUpBefore() throws Exception
    {
        URL host = new URL("https://bigpicture.demo.cytomine.com");
        // String username = "dgonzalez_obando";
        String publicKey = "96ca4ea2-3434-44fc-98e2-ae11172f6bd5";
        String privateKey = "860f227f-98ad-489f-a672-20ad661145fd";
        client = CytomineClient.create(host, publicKey, privateKey);
    }

    @Test
    public void givenImageWithNoUserAnnotationsWhenGetUserAnnotationsThenIsEmpty()
    {
        Image image = client.getImageInstance(80624);
        Map<Long, UserAnnotation> annotations = client.getImageUserAnnotations(image);
        assertTrue("User annotations is not empty", annotations.isEmpty());
    }

    @Test
    public void givenImageWithUserAnnotationsWhenGetUserAnnotationsThenIsNotEmpty()
    {
        Image image = client.getImageInstance(80540);
        Map<Long, UserAnnotation> annotations = client.getImageUserAnnotations(image);
        assertTrue("User annotations is empty", !annotations.isEmpty());
    }

    @Test
    public void givenImageWithNoAlgorithmAnnotationsWhenGetAlgorithmAnnotationsThenIsEmpty()
    {
        Image image = client.getImageInstance(104450);
        Map<Long, AlgorithmAnnotation> annotations = client.getImageAlgorithmAnnotations(image);
        assertTrue("Algo annotations is not empty", annotations.isEmpty());
    }

    @Test
    public void givenImageWithAlgorithmAnnotationsWhenGetAlgorithmAnnotationsThenIsNotEmpty()
    {
        Image image = client.getImageInstance(80540);
        Map<Long, AlgorithmAnnotation> annotations = client.getImageAlgorithmAnnotations(image);
        assertTrue("Algo annotations is empty", !annotations.isEmpty());
    }

    @Test
    public void givenImageWithNoReviewedAnnotationsWhenGetReviewedAnnotationsThenIsEmpty()
    {
        Image image = client.getImageInstance(108337);
        Map<Long, ReviewedAnnotation> annotations = client.getImageReviewedAnnotations(image);
        assertTrue("Reviewed annotations is not empty", annotations.isEmpty());
    }

    @Test
    public void givenImageWithReviewedAnnotationsWhenGetReviewedAnnotationsThenIsNotEmpty()
    {
        Image image = client.getImageInstance(104450);
        Map<Long, ReviewedAnnotation> annotations = client.getImageReviewedAnnotations(image);
        assertTrue("Reviewed annotations is empty", !annotations.isEmpty());
    }

}
