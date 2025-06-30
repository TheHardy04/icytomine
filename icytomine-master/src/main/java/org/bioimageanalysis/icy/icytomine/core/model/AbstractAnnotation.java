package org.bioimageanalysis.icy.icytomine.core.model;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

import be.cytomine.client.models.Annotation;

/**
 * Represents an abstract annotation that can be of three types:
 * <ul>
 * <li>{@link UserAnnotation}</li>
 * <li>{@link AlgorithmAnnotation}</li>
 * <li>{@link ReviewedAnnotation}</li>
 * </ul>
 * This class holds common functionalities for these annotations.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public abstract class AbstractAnnotation extends Entity
{

    /**
     * @param client
     *        The client connecting with the servers.
     * @param model
     *        The model The annotation Cytomine model.
     */
    public AbstractAnnotation(CytomineClient client, be.cytomine.client.models.Annotation model)
    {
        super(client, model);
    }

    /**
     * Retrieves all annotations belonging to a given image.
     * 
     * @param image
     *        The image annotations belong to.
     * @return A list of annotations of the three main annotation types.
     */
    public static List<AbstractAnnotation> getAnnotationsInImage(Image image)
    {
        List<AbstractAnnotation> foundAnnotations = new ArrayList<>();
        foundAnnotations.addAll(UserAnnotation.getUserAnnotationsInImage(image).values());
        foundAnnotations.addAll(AlgorithmAnnotation.getAlgorithmAnnotationsInImage(image).values());
        foundAnnotations.addAll(ReviewedAnnotation.getReviewedAnnotationsInImage(image).values());
        return foundAnnotations;
    }

    /**
     * @return The internal annotation model of this instance.
     */
    public Annotation getInternalAnnotation()
    {
        return (Annotation) getModel();
    }

    /**
     * @return The identifier of the image instance associated with this annotation.
     */
    public Optional<Long> getImageInstanceId()
    {
        return getLong("image");
    }

    /**
     * @return The identifier of the user associated with this annotation.
     */
    public Optional<Long> getUserId()
    {
        return getLong("user");
    }

    private Geometry latestSimplifiedGeometry;
    private int latestSimplifiedGeometryResolution;

    /**
     * Computes the simplified geometry of this annotation for a given resolution level of the image.
     * 
     * @param resolution
     *        The target resolution simplification.
     * @return The simplified geometry.
     * @throws CytomineClientException
     *         If the geometry cannot be retrieved.
     */
    public Geometry getSimplifiedGeometryForResolution(int resolution) throws CytomineClientException
    {
        double pixelTolerance = 1;
        if (resolution > 0)
            for (int i = 0; i < resolution; i++)
                pixelTolerance *= 2d;
        else if (resolution < 0)
            for (int i = 0; i < -resolution; i++)
                pixelTolerance /= 2d;

        if (geometry == null || latestSimplifiedGeometry == null || latestSimplifiedGeometryResolution != resolution)
        {
            latestSimplifiedGeometry = getSimplifiedGeometry(pixelTolerance);
            latestSimplifiedGeometryResolution = resolution;
        }
        return latestSimplifiedGeometry;
    }

    private Rectangle2D adjustedApproximativeBounds;

    /**
     * Computes the approximative bounds of this annotation, adjusting the y axis coordinates (i.e. inverting the y coordinates with respect to the image
     * bounds).
     * 
     * @return The adjusted approximative annotation bounds.
     * @throws CytomineClientException
     */
    public Rectangle2D getYAdjustedApproximativeBounds() throws CytomineClientException
    {
        if (adjustedApproximativeBounds == null)
        {
            Rectangle2D approximativeBounds = getApproximativeBounds();
            adjustedApproximativeBounds = new Rectangle2D.Double(approximativeBounds.getMinX(),
                    getImage().getSizeY().get() - approximativeBounds.getMaxY(), approximativeBounds.getWidth(),
                    approximativeBounds.getHeight());
            if (adjustedApproximativeBounds.isEmpty())
            {
                adjustedApproximativeBounds = new Rectangle2D.Double(
                        adjustedApproximativeBounds.getX() - Double.MIN_VALUE,
                        adjustedApproximativeBounds.getY() - Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
            }
        }
        return adjustedApproximativeBounds;
    }

    private Rectangle2D approximativeBounds;

    /**
     * Approximates the bounds of the annotation.
     * 
     * @return The approximative annotation bounds.
     * @throws CytomineClientException
     *         If the geometry of the annotation cannot be retrieved.
     */
    public Rectangle2D getApproximativeBounds() throws CytomineClientException
    {
        if (approximativeBounds == null)
        {
            Geometry simplifiedGeometry = getSimplifiedGeometry(10);
            Envelope envelope = simplifiedGeometry.getEnvelopeInternal();
            approximativeBounds = new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(),
                    envelope.getHeight());
        }
        return approximativeBounds;
    }

    private Geometry getSimplifiedGeometry(double pixelTolerance) throws CytomineClientException
    {
        pixelTolerance = pixelTolerance > 0 ? pixelTolerance : 0;
        Geometry baseGeometry = getGeometryAtZeroResolution(false);
        if (baseGeometry == null)
            throw new CytomineClientException(String.format("Null base geometry (annotation id=%d)", getId()));
        TopologyPreservingSimplifier simplifier = new TopologyPreservingSimplifier(baseGeometry);
        simplifier.setDistanceTolerance(pixelTolerance);
        return simplifier.getResultGeometry();
    }

    private Rectangle2D bounds;

    public Rectangle2D getBounds()
    {
        if (bounds == null)
        {
            Envelope envelope = getGeometryAtZeroResolution(false).getEnvelopeInternal();
            bounds = new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(),
                    envelope.getWidth(), envelope.getHeight());
            if (bounds.isEmpty())
            {
                bounds = new Rectangle2D.Double(bounds.getX() - Double.MIN_VALUE,
                        bounds.getY() - Double.MIN_VALUE, 2d * Double.MIN_VALUE, 2d * Double.MIN_VALUE);
            }
        }
        return bounds;
    }

    private java.awt.geom.Rectangle2D adjustedBounds;

    /**
     * Computes the y-axis adjusted bounds of the annotation at resolution 0 (i.e. with y coordinates inverted taking into account the size of the image)
     * 
     * @return The adjusted annotation bounds.
     */
    public Rectangle2D getYAdjustedBounds()
    {
        if (adjustedBounds == null)
        {
            Envelope envelope = getGeometryAtZeroResolution(false).getEnvelopeInternal();
            adjustedBounds =
                    new Rectangle2D.Double(envelope.getMinX(), getImage().getSizeY().get() - envelope.getMaxY(),
                            envelope.getWidth(), envelope.getHeight());
            if (adjustedBounds.isEmpty())
            {
                adjustedBounds = new Rectangle2D.Double(adjustedBounds.getX() - Double.MIN_VALUE,
                        adjustedBounds.getY() - Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
            }
        }
        return adjustedBounds;
    }

    private Geometry geometry;

    /**
     * Retrieves this annotation's geometry. If the geometry is not yet present is is requested to the server. Otherwise, if {@code recompute} is set to false,
     * the cached geometry is used as response.
     * 
     * @param recompute
     *        If true, geometries are requested to the server. Otherwise, the cached ones are used.
     * @return Geometry of the annotation without any simplification.
     * @throws CytomineClientException
     *         If the geometry cannot be retrieved from host server.
     */
    public Geometry getGeometryAtZeroResolution(boolean recompute) throws CytomineClientException
    {
        if (this.geometry == null || recompute)
        {
            retrieveGeometry();
        }
        return geometry;
    }

    /**
     * Uses the location string on the internal annotation to construct the geometry object describing this annotation.
     * 
     * @throws CytomineClientException
     *         If the location cannot be retrieved from host server. Also, if
     *         the geometry cannot be constructed from server response.
     */
    private void retrieveGeometry() throws CytomineClientException
    {
        this.geometry = null;
        Optional<String> location = getLocation();
        if (location.isPresent())
        {
            WKTReader reader = new WKTReader();
            try
            {
                this.geometry = reader.read(location.get());
            }
            catch (ParseException e)
            {
                throw new CytomineClientException(String.format("Couldn't create geometry for annotation %d", getId()),
                        e);
            }
        }
    }

    private String locationString;

    /**
     * Retrieves the WKT formatted string describing the geometry of this annotation. If the location string is not present on the cached annotation it is
     * requested to the server.
     * Warning: Doing this procedure (requesting location to server) repeatedly on a large list of annotations can be very slow. It is preferable to request
     * multiple annotations in a single request on the client.
     * 
     * @return The WKT formated string with this annotation geometry.
     * @throws CytomineClientException
     *         If the geometry cannot be retrieved from the server.
     */
    public Optional<String> getLocation() throws CytomineClientException
    {
        if (this.locationString == null)
        {
            Optional<String> locationString = getStr("location");
            if (locationString.isPresent())
            {
                this.locationString = locationString.get();
            }
            else
            {
                // WARNING: This can be really slow!!!
                this.locationString = getClient().getAnnotationLocation(getId()).orElse(null);
            }
        }
        return Optional.ofNullable(locationString);
    }

    /**
     * @return The user this annotation is associated with.
     * @throws CytomineClientException
     *         If the user cannot be retrieved from the server.
     */
    public User getUser() throws CytomineClientException
    {
        return getClient().getUser(getUserId().get());
    }

    /**
     * Retrieves the image this annotation is associated with.
     * 
     * @return The image this annotation is associated with.
     * @throws CytomineClientException
     *         If the image instance cannot be retrieved from host server.
     */
    public Image getImage() throws CytomineClientException
    {
        return getClient().getImageInstance(getImageInstanceId().get());
    }

    private List<Property> annotationProperties;

    /**
     * Retrieves the properties associated to this annotation. If {@code recompute} is set to true, the properties are requested to the server even if they are
     * already cached.
     * 
     * @param recompute
     *        If true, the properties are requested to the server even if they are already cached.
     * @return The list of properties.
     * @throws CytomineClientException
     *         If the properties list cannot be retrieved from the server.
     */
    public List<Property> getProperties(boolean recompute) throws CytomineClientException
    {
        if (annotationProperties == null || recompute)
        {
            annotationProperties = getClient().getAbstractAnnotationProperties(this);
        }
        return annotationProperties;
    }

    /**
     * Retrieves the color of this annotation from the term associated to it. The color will be prioritized to the first term found either by association to
     * current user or the any user.
     * 
     * @return The color of the annotation.
     */
    public Color getColor()
    {
        // Use current user terms first.
        Set<Term> terms = getAssociatedTermsByCurrentUser();
        if (terms.isEmpty())
        {
            terms = getAssociatedTerms();
        }

        Color color;
        if (!terms.isEmpty())
        {
            color = terms.iterator().next().getColor();
        }
        else
        {
            color = Term.DEFAULT_TERM_COLOR;
        }
        return color;
    }

    /**
     * @return Set of terms assigned to this annotation by the current user.
     * @throws CytomineClientException
     *         If the terms cannot be retrieved.
     */
    public Set<Term> getAssociatedTermsByCurrentUser() throws CytomineClientException
    {
        Long userId = getClient().getCurrentUser().getId();
        return getAssociatedTermsByUser(userId);
    }

    /**
     * Retrieves the terms associated to this annotation by a given user.
     * 
     * @param userId
     *        Target user identifier.
     * @return The set of terms associated to this annotation by the user.
     * @throws CytomineClientException
     *         If the terms cannot be retrieved.
     */
    public Set<Term> getAssociatedTermsByUser(long userId) throws CytomineClientException
    {
        return getTermUsers().entrySet().stream().filter(entry -> entry.getValue().contains(userId))
                .map(e -> e.getKey())
                .distinct().map(id -> getClient().getTerm(id)).collect(Collectors.toSet());
    }

    /**
     * Retrieves the terms associated to this annotation.
     * 
     * @return The set of terms.
     * @throws CytomineClientException
     *         If the terms cannot be retrieved.
     */
    public Set<Term> getAssociatedTerms() throws CytomineClientException
    {
        return getTermUsers().keySet().stream().map(id -> getClient().getTerm(id)).collect(Collectors.toSet());
    }

    private Map<Long, Set<Long>> termUsers;

    /**
     * Retrieves the terms map with associated users per term.
     * 
     * @return A map of term IDs with their associated user IDs.
     * @throws CytomineClientException
     *         If the map cannot be retrieved.
     */
    public Map<Long, Set<Long>> getTermUsers() throws CytomineClientException
    {
        if (termUsers == null)
        {
            try
            {
                termUsers = getClient().getAnnotationUsersByTerm(this);
            }
            catch (Exception e)
            {
                throw new CytomineClientException(
                        String.format("Could not create term users map for annotation %d", getId()),
                        e);
            }
        }
        return termUsers;
    }

    public void associateTerms(Map<Term, Boolean> termSelection) throws CytomineClientException
    {
        getClient().associateTermsToAnnotation(this, termSelection);
        termUsers = null;
        getInternalAnnotation().getAttr().remove("userByTerm");
        getTermUsers();
    }

//    private void updateModel()
//    {
//        AbstractAnnotation newModel = getClient().downloadAbstractAnnotation(getId());
//        getInternalAnnotation().setAttr(newModel.getInternalAnnotation().getAttr());
//        this.termUsers = null;
//    }

    @Override
    public String toString()
    {
        return String.format("Annotation (%s). Id: %s", getClass().getTypeName(), String.valueOf(getId()));
    }

}
