package org.bioimageanalysis.icy.icytomine.core.model;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;

import be.cytomine.client.models.ImageInstance;
import danyfel80.common.stream.StreamUtils;

public class Image extends Entity
{

    private static final int DEFAULT_TILE_SIZE = 256;

    public static Image retrieve(CytomineClient client, long imageInstanceId) throws CytomineClientException
    {
        return client.getImageInstance(imageInstanceId);
    }

    // private List<SliceInstance> imageServers;
    private List<String> imageServers;
    // private List<Annotation> annotations;
    private Map<Long, AbstractAnnotation> abstractAnnotations;

    public Image(CytomineClient client, ImageInstance internalImage)
    {
        super(client, internalImage);
    }

    public ImageInstance getInternalImage()
    {
        return (ImageInstance) getModel();
    }

    /**
     * @return Date of creation for this image.
     */
    public Optional<Calendar> getCreationDate()
    {
        Optional<Long> numericDate = getLong("created");
        if (numericDate.isPresent())
        {
            Calendar c = GregorianCalendar.getInstance();
            c.setTimeInMillis(numericDate.get());
            return Optional.of(c);
        }
        else
        {
            return Optional.ofNullable(null);
        }
    }

    public Optional<Long> getAbstractImageId()
    {
        return getLong("baseImage");
    }

    public Optional<Long> getProjectId()
    {
        return getLong("project");
    }

    public Optional<String> getName()
    {
        return getStr("originalFilename");
    }

    /**
     * @return Format used to store this image.
     */
    public Optional<String> getMimeType()
    {
        return Optional.ofNullable(getInternalImage().getStr("mime"));
        // if (imageServers == null)
        // {
        // imageServers = null;
        // imageServers = getClient().getImageServers(this);
        // }
        // if (imageServers.isEmpty())
        // {
        // return getStr("mime");
        // }
        // else
        // {
        // return Optional.of("tif");// Optional.ofNullable(imageServers.get(0).getStr("mime"));
        // }
    }

    /**
     * @return Size of the image expressed in pixels in x direction.
     */
    public Optional<Integer> getSizeX()
    {
        return getInt("width");
    }

    /**
     * @return Size of the image expressed in pixels in y direction.
     */
    public Optional<Integer> getSizeY()
    {
        return getInt("height");
    }

    /**
     * @return Size of the image expressed in pixels in x and y directions.
     */
    public Optional<Dimension> getSize()
    {
        if (getSizeX().isPresent() && getSizeY().isPresent())
        {
            return Optional.of(new Dimension(getSizeX().get(), getSizeY().get()));
        }
        else
        {
            return Optional.ofNullable(null);
        }
    }

    /**
     * @return Resolution of each pixel in x and y directions expressed in
     *         microns.
     */
    public Optional<Double> getResolution()
    {
        return super.getDbl("resolution");
    }

    /**
     * @return Size of the image expressed in microns in x direction.
     */
    public Optional<Double> getDimensionX()
    {
        Optional<Double> resolution = getResolution();
        Optional<Integer> size = getSizeX();
        if (size.isPresent() && resolution.isPresent())
        {
            return Optional.of(resolution.get() * size.get());
        }
        else
        {
            return Optional.ofNullable(null);
        }
    }

    /**
     * @return Size of the image expressed in microns in y direction.
     */
    public Optional<Double> getDimensionY()
    {
        Optional<Double> resolution = getResolution();
        Optional<Integer> size = getSizeY();
        if (size.isPresent() && resolution.isPresent())
        {
            return Optional.of(resolution.get() * size.get());
        }
        else
        {
            return Optional.ofNullable(null);
        }
    }

    /**
     * @return Size of the image expressed in microns in x and y directions.
     */
    public Optional<Dimension2D> getDimension()
    {
        Optional<Dimension> size = getSize();
        Optional<Double> resolution = getResolution();
        if (size.isPresent() && resolution.isPresent())
        {
            return Optional.of(new icy.type.dimension.Dimension2D.Double(size.get().width * resolution.get(),
                    size.get().height * resolution.get()));
        }
        else
        {
            return Optional.ofNullable(null);
        }
    }

    /**
     * @return Magnification used when capturing this image.
     */
    public Optional<Integer> getMagnification()
    {
        return getInt("magnification");
    }

    /**
     * @return The maximum resolution that can be requested.
     */
    public Optional<Long> getDepth()
    {
        // return getLong("zoom");
        return getLong("depth");
    }

    /**
     * @return Size of the tile in x direction.
     */
    public Optional<Integer> getTileWidth()
    {
        return Optional.of(DEFAULT_TILE_SIZE);
    }

    /**
     * @return Size of the tile in y direction.
     */
    public Optional<Integer> getTileHeight()
    {
        return Optional.of(DEFAULT_TILE_SIZE);
    }

    /**
     * @return Size of the tile in x and y directions.
     */
    public Optional<Dimension> getTileSize()
    {
        if (getTileWidth().isPresent() && getTileHeight().isPresent())
        {
            return Optional.of(new Dimension(getTileWidth().get(), getTileHeight().get()));
        }
        else
        {
            return Optional.ofNullable(null);
        }

    }

    /**
     * @return Id of the user who uploaded the image.
     */
    public Long getOriginalUserId()
    {
        return getLong("user").get();
    }

    /**
     * @return Number of annotations users have associated to this image.
     */
    public Optional<Long> getAnnotationsOfUsersNumber()
    {
        return getLong("numberOfAnnotations");
    }

    /**
     * @return Number of annotations associated to this image produced by
     *         algorithms.
     */
    public Optional<Long> getAnnotationsOfAlgorithmNumber()
    {
        return getLong("numberOfJobAnnotations");
    }

    /**
     * Retrieves the URL used to retrieve a given tile at a given resolution.
     * 
     * @param resolution
     *        Resolution of the image to retrieve.
     * @param tileIndex
     *        Tile index. Usually x + y*tiles.width
     * @param x
     *        Tile index in x direction.
     * @param y
     *        Tile index in y direction.
     * @return URL used to retrieve the tile from the server.
     * @throws CytomineClientException
     *         If image servers for this image cannot be retrieved.
     */
    public Optional<String> getTileUrl(long resolution, int tileIndex, int x, int y) throws CytomineClientException
    {
        List<String> servers = getImageServers(false);
        if (servers.isEmpty())
        {
            return Optional.ofNullable(null);
        }
        // return Optional.of(String.format("%s&tileIndex=%d&z=%d&mimeType=%s", servers.get(0), tileIndex,
        // getDepth().orElse(0L) - resolution, getMimeType().orElse("ndpi")));
        return Optional.of(String.format("%s&x=%d&y=%d&z=%d&mimeType=%s", servers.get(0), x, y,
                getDepth().orElse(0L) - resolution, getMimeType().orElse("ndpi")));
    }

    /**
     * @param recompute
     *        If true, the list is requested to the server. Otherwise, the cached list is used if it is not null.
     * @return Collection with image servers available for this image.
     * @throws CytomineClientException
     *         If the image servers cannot be retrieved from the server.
     */
    public List<String> getImageServers(boolean recompute) throws CytomineClientException
    {
        if (imageServers == null || recompute)
        {
            imageServers = null;
            imageServers = getClient().getImageServers(this);
        }
        return imageServers;
        // return imageServers.stream()
        // .map(slice -> slice.getStr("imageServerUrl") + "/slice/tile?fif=" + slice.getStr("path"))
        // .collect(Collectors.toList());
    }

    /**
     * Retrieves a thumbnail of this image.
     * 
     * @param maxSize
     *        Maximum size of the retrieved thumbnail.
     * @return Thumbnail.
     * @throws CytomineClientException
     *         If the thumbnail cannot be retrieved from the server.
     */
    public BufferedImage getThumbnail(int maxSize) throws CytomineClientException
    {
        return getClient().downloadImageAsBufferedImage(getAbstractImageId().get());
        // return getClient().downloadImageAsBufferedImage(getId().longValue());
    }

    public Map<Long, UserAnnotation> getUserAnnotationsWithGeometry(User currentUser, boolean forceDownload)
    {
        Set<Long> userIds = new HashSet<>(1);
        userIds.add(currentUser.getId());
        Map<Long, UserAnnotation> annotations = getClient().getImageUserAnnotations(this, userIds);
        Map<Long, UserAnnotation> geometries = getClient().getImageUserAnnotationGeometries(this, userIds);
        geometries.forEach((aId, g) -> {
            UserAnnotation a = annotations.get(aId);
            if (a != null)
            {
                a.getInternalAnnotation().set("location", g.getInternalAnnotation().getStr("location"));
            }
        });
        return annotations;
    }

    /**
     * Retrieves all the annotations present for this image. Note: This does not guarantees to include all annotation geometries. Use
     * {@link #getAbstractAnnotationsWithGeometry(Set, boolean)} for this.
     * 
     * @param recompute
     *        If true annotations are requested to the server even if they are already in the cache.
     * @return The list of annotations for this image.
     * @throws CytomineClientException
     *         If annotations cannot be retrieved.
     */
    public Map<Long, AbstractAnnotation> getAbstractAnnotations(boolean recompute) throws CytomineClientException
    {
        if (abstractAnnotations == null || recompute)
        {
            abstractAnnotations = null;
            abstractAnnotations = getClient().getImageAbstractAnnotations(this);
        }
        return abstractAnnotations;
    }

    /**
     * Retrieves the target annotations associated to this image containing their geometric information.
     * 
     * @param annotationIds
     *        Annotations being requested.
     * @param recompute
     *        If true, annotations are requested again to the server even if they are already cached.
     * @return Annotation map with requested annotations.
     * @throws CytomineClientException
     *         If full annotations for this image cannot be retrieved from the
     *         server.
     */
    public Map<Long, AbstractAnnotation> getAbstractAnnotationsWithGeometry(Set<Long> annotationIds, boolean recompute)
            throws CytomineClientException
    {
        long annotationsWithoutGeometry = 0;
        if (abstractAnnotations != null)
        {
            annotationsWithoutGeometry = annotationIds.stream()
                    .filter(aId -> {
                        AbstractAnnotation a = abstractAnnotations.get(aId);
                        if (a == null)
                            return true;
                        if (!a.getLocation().isPresent())
                            return true;
                        return false;
                    })
                    .count();
        }

        if (recompute || annotationsWithoutGeometry > 0)
        {
            Map<Long, AbstractAnnotation> annotationsWithGeometry =
                    getClient().getImageAbstractAnnotationsWithGeometry(this, recompute);
            if (abstractAnnotations == null)
                abstractAnnotations = annotationsWithGeometry;
            else
                abstractAnnotations.putAll(annotationsWithGeometry);
        }

        return annotationIds.stream().map(StreamUtils.wrapFunction(aId -> abstractAnnotations.get(aId)))
                .collect(Collectors.toMap(AbstractAnnotation::getId, Function.identity()));
    }

    public Map<Long, UserAnnotation> getUserAnnotationsWithGeometry(Set<User> users)
    {

        Set<Long> userIds = users.stream().map(u -> u.getId()).collect(Collectors.toSet());
        Map<Long, UserAnnotation> annotations = getClient().getImageUserAnnotations(this, userIds);
        Map<Long, UserAnnotation> geometries = getClient().getImageUserAnnotationGeometries(this, userIds);
        geometries.forEach((aId, g) -> {
            UserAnnotation a = annotations.get(aId);
            if (a != null)
            {
                a.getInternalAnnotation().set("location", g.getInternalAnnotation().getStr("location"));
            }
        });
        return annotations;
    }

    public Map<Long, AlgorithmAnnotation> getAlgorithmAnnotationsWithGeometry(Set<UserJob> userJobs)
    {
        Set<Long> userJobIds = userJobs.stream().map(u -> u.getId()).collect(Collectors.toSet());
        Map<Long, AlgorithmAnnotation> annotations = getClient().getImageAlgorithmAnnotations(this, userJobIds);
        Map<Long, AlgorithmAnnotation> geometries = getClient().getImageAlgorithmAnnotationGeometries(this, userJobIds);
        geometries.forEach((aId, g) -> {
            AlgorithmAnnotation a = annotations.get(aId);
            if (a != null)
            {
                a.getInternalAnnotation().set("location", g.getInternalAnnotation().getStr("location"));
            }
        });
        return annotations;
    }

    public Map<Long, AbstractAnnotation> getAbstractAnnotationsAt(Rectangle2D imageArea) throws CytomineClientException
    {
        return getClient().getImageAbstractAnnotationsAt(getId(), imageArea);
    }

    /**
     * Removes the given annotations from the image.
     * 
     * @param selectedAnnotations
     *        Target annotations to be deleted.
     */
    public void removeAbstractAnnotations(Set<AbstractAnnotation> selectedAnnotations)
    {
        for (AbstractAnnotation annotation : selectedAnnotations)
        {
            getClient().removeAnnotation(annotation.getId());
        }

        if (abstractAnnotations != null)
        {
            Set<Long> selectedAnnotationIds =
                    selectedAnnotations.stream().map(a -> a.getId()).collect(Collectors.toSet());
            selectedAnnotationIds.forEach(aId -> abstractAnnotations.remove(aId));
        }
    }

    // @Deprecated
    // public List<Annotation> getAnnotationsWithGeometryOf(Rectangle2D currentTileArea) throws CytomineClientException
    // {
    // return getClient().getFullImageAnnotations(getId(), currentTileArea);
    // }

    public Project getProject() throws CytomineClientException
    {
        return getClient().getProject(getProjectId().get());
    }

    public void setMagnification(Integer newMagnification) throws CytomineClientException
    {
        getClient().updateImageMagnfication(this, newMagnification);
    }

    public void setResolution(Double newResolution) throws CytomineClientException
    {
        getClient().updateImageResolution(this, newResolution);
    }

    @Override
    public String toString()
    {
        return String.format("Image instance: id=%s, name=%s", String.valueOf(getId()),
                getName().orElse("Not specified"));
    }
}
