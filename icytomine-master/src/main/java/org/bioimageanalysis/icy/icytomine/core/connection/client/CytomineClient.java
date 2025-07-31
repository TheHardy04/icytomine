/*
 * Copyright 2010-2018 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bioimageanalysis.icy.icytomine.core.connection.client;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import be.cytomine.client.models.*;
import org.bioimageanalysis.icy.icytomine.core.connection.client.collection.ImageServers;
import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.AlgorithmAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Description;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Ontology;
import org.bioimageanalysis.icy.icytomine.core.model.Project;
import org.bioimageanalysis.icy.icytomine.core.model.Property;
import org.bioimageanalysis.icy.icytomine.core.model.ReviewedAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.UserAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.UserJob;
import org.bioimageanalysis.icy.icytomine.core.model.cache.AbstractAnnotationCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.DescriptionCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.EntityCacheException;
import org.bioimageanalysis.icy.icytomine.core.model.cache.ImageInstanceCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.OntologyCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.ProjectCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.TermCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.UserCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.UserJobCache;
import org.bioimageanalysis.icy.icytomine.core.model.key.DescriptionId;
import org.bioimageanalysis.icy.icytomine.geom.WKTUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.collections.AnnotationCollection;
import be.cytomine.client.collections.Collection;
import be.cytomine.client.collections.PropertyCollection;
import danyfel80.common.stream.StreamUtils;

/**
 * This class represents the connection point between local and remote cytomine
 * model.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class CytomineClient implements AutoCloseable
{

    /**
     * @param host
     *        The URL of the host server.
     * @param publicKey
     *        The public key of the user.
     * @param privateKey
     *        The private key of the user.
     * @return The Client ready to use.
     * @throws CytomineClientException
     *         If the host server is not well specified. Also if the user
     *         credentials are not correct.
     */
    public static CytomineClient create(URL host, String publicKey, String privateKey) throws CytomineClientException
    {

        Cytomine client;
        try
        {
            Cytomine.connection(host.toString(), publicKey, privateKey);
            client = Cytomine.getInstance();
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(e);
        }

        CytomineClient cytomineClient = new CytomineClient(client);
        return cytomineClient;
    }

    private Cytomine internalClient;
    private User currentUser;

    private UserCache userCache;
    private ProjectCache projectCache;
    private DescriptionCache descriptionCache;
    private OntologyCache ontologyCache;
    private TermCache termCache;
    private ImageInstanceCache imageInstanceCache;
    private AbstractAnnotationCache abstractAnnotationCache;
    private UserJobCache userJobCache;

    /**
     * @throws CytomineClientException
     *         see {@link #create(URL, String, String)}
     */
    private CytomineClient(Cytomine client) throws CytomineClientException
    {
        this.internalClient = client;
        checkCurrentUser();
    }

    /**
     * @throws CytomineClientException
     *         If credentials are not recognized by host server. Also, if a
     *         connection to the host server cannot be established.
     */
    private void checkCurrentUser() throws CytomineClientException
    {
        try
        {
            be.cytomine.client.models.User internalUser = getInternalClient().getCurrentUser();
            if (internalUser.getAttr() == null)
                throw new CytomineClientException(String.format("User credentials not recognized for public key: %s",
                        getInternalClient().getPublicKey()));
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException("Could not connect to server: " + e.getMessage(), e);
        }
    }

    protected Cytomine getInternalClient()
    {
        return internalClient;
    }

    public String getHost()
    {
        return getInternalClient().getHost();
    }

    public String getPublicKey()
    {
        return getInternalClient().getPublicKey();
    }

    /**
     * @return The current user (The one who is logged in).
     * @throws CytomineClientException
     *         If the user data cannot be retrieved from the host server.
     */
    public User getCurrentUser() throws CytomineClientException
    {
        if (currentUser == null)
        {
            currentUser = downloadCurrentUser();
            getUserCache().store(currentUser.getId(), currentUser);
        }
        return currentUser;
    }

    private User downloadCurrentUser() throws CytomineClientException
    {
        try
        {
            be.cytomine.client.models.User user = getInternalClient().getCurrentUser();
            if (user.getAttr() == null)
            {
                throw new CytomineClientException("No user data downloaded");
            }
            return new User(this, user);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException("Could not download user data", e);
        }
    }

    private UserCache getUserCache()
    {
        if (userCache == null)
        {
            userCache = UserCache.create(this);
        }
        return userCache;
    }

    /**
     * @param userId
     *        The user id.
     * @return The user model element.
     * @throws CytomineClientException
     *         If the user data cannot be retrieved from the host server.
     */
    public User getUser(long userId) throws CytomineClientException
    {
        User user;
        try
        {
            user = getUserCache().retrieve(userId);
        }
        catch (EntityCacheException e)
        {
            user = downloadUser(userId);
            getUserCache().store(userId, user);
        }
        return user;
    }

    private User downloadUser(long userId) throws CytomineClientException
    {
        try
        {
            be.cytomine.client.models.User user = getInternalClient().getCurrentUser();
            if (user.getAttr() == null)
            {
                throw new CytomineClientException("No user data downloaded");
            }
            return new User(this, user);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(String.format("Could not download user data (user id=%d)", userId), e);
        }
    }

    /**
     * @param userId
     *        The user id
     * @return The list of projects the user has access to.
     * @throws CytomineClientException
     *         If the user projects cannot be retrieved from the host server.
     */
    public List<Project> getUserProjects(long userId) throws CytomineClientException
    {
        Collection<be.cytomine.client.models.Project> projectCollection;
        try
        {
            projectCollection = Collection.fetchWithFilter(
                    be.cytomine.client.models.Project.class, be.cytomine.client.models.User.class, userId, 0, 0);

            // projectCollection = getInternalClient().getProjectsByUser(userId);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(String.format("Could not download user projects (user id=%d)", userId),
                    e);
        }

        List<Project> projects = new ArrayList<>(projectCollection.size());
        for (int i = 0; i < projectCollection.size(); i++)
        {
            be.cytomine.client.models.Project p = projectCollection.get(i);
            be.cytomine.client.models.Project newP = new be.cytomine.client.models.Project();
            newP.setAttr(p.getAttr());
            Project project = new Project(this, newP);
            getProjectCache().store(project.getId(), project);
            projects.add(project);
        }

        return projects;
    }

    /**
     * @param projectId
     *        The project id.
     * @return The project model element.
     * @throws CytomineClientException
     *         If the project data cannot be retrieved from the host server.
     */
    public Project getProject(long projectId) throws CytomineClientException
    {
        Project project;
        try
        {
            project = getProjectCache().retrieve(projectId);
        }
        catch (EntityCacheException e)
        {
            project = downloadProject(projectId);
            getProjectCache().store(projectId, project);
        }
        return project;
    }

    private ProjectCache getProjectCache()
    {
        if (projectCache == null)
        {
            projectCache = ProjectCache.create(this);
        }
        return projectCache;
    }

    private Project downloadProject(long projectId) throws CytomineClientException
    {
        try
        {
            be.cytomine.client.models.Project project = new be.cytomine.client.models.Project().fetch(projectId);
            if (project.getAttr() == null)
            {
                throw new CytomineClientException("No project data downloaded");
            }
            return new Project(this, project);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(
                    String.format("Could not download project data (project id=%d)", projectId), e);
        }
    }

    /**
     * @param describedEntityId
     *        The entity id
     * @param describedDomainName
     *        The domain name of the requested element.
     * @return The Description associated to the entity.
     * @throws CytomineClientException
     *         If the description data cannot be retrieved from the host server.
     */
    public Description getDescription(long describedEntityId, String describedDomainName) throws CytomineClientException
    {
        Description description;
        DescriptionId descriptionId = new DescriptionId(describedDomainName, describedEntityId);
        try
        {
            description = getDescriptionCache().retrieve(descriptionId);
        }
        catch (EntityCacheException e)
        {
            description = downloadDescription(descriptionId);
            getDescriptionCache().store(descriptionId, description);
        }
        return description;
    }

    private DescriptionCache getDescriptionCache()
    {
        if (descriptionCache == null)
        {
            descriptionCache = DescriptionCache.create(this);
        }
        return descriptionCache;
    }

    private Description downloadDescription(DescriptionId descriptionId) throws CytomineClientException
    {
        try
        {
            be.cytomine.client.models.Description description = getInternalClient()
                    .getDescription(descriptionId.getDescribedEntityId(), descriptionId.getDescribedDomainName());
            if (description.getAttr() == null)
            {
                throw new CytomineClientException("No description data downloaded");
            }
            return new Description(this, description);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(
                    String.format("Could not download description data (description key=%s)", descriptionId), e);
        }
    }

    /**
     * @param projectId
     *        The id of the project.
     * @return The List of users the project has associated.
     * @throws CytomineClientException
     *         If the project users cannot be retrieved from the host server.
     */
    public List<User> getProjectUsers(long projectId) throws CytomineClientException
    {
        Collection<be.cytomine.client.models.User> userCollection;
        try
        {
            userCollection = Collection.fetchWithFilter(
                    be.cytomine.client.models.User.class, be.cytomine.client.models.Project.class, projectId, 0, 0);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(
                    String.format("Could not download project users (project id=%d)", projectId), e);
        }

        List<User> users = new ArrayList<>(userCollection.size());
        for (int i = 0; i < userCollection.size(); i++)
        {
            be.cytomine.client.models.User userIn = userCollection.get(i);
            be.cytomine.client.models.User newUserIn = new be.cytomine.client.models.User();
            newUserIn.setAttr(userIn.getAttr());

            User user = new User(this, newUserIn);
            getUserCache().store(user.getId(), user);
            users.add(user);
        }

        return users;
    }

    /**
     * @param ontologyId
     *        The id of the ontology.
     * @return The ontology model element.
     * @throws CytomineClientException
     *         If the ontology data cannot be retrieved from the host server.
     */
    public Ontology getOntology(long ontologyId) throws CytomineClientException
    {
        Ontology ontology;
        try
        {
            ontology = getOntologyCache().retrieve(ontologyId);
        }
        catch (EntityCacheException e)
        {
            ontology = downloadOntology(ontologyId);
            getOntologyCache().store(ontologyId, ontology);
        }
        return ontology;
    }

    private OntologyCache getOntologyCache()
    {
        if (ontologyCache == null)
        {
            ontologyCache = OntologyCache.create(this);
        }
        return ontologyCache;
    }

    private Ontology downloadOntology(long ontologyId) throws CytomineClientException
    {
        be.cytomine.client.models.Ontology ontology;
        try
        {
            ontology = new be.cytomine.client.models.Ontology().fetch(ontologyId);
            if (ontology.getAttr() == null)
            {
                throw new CytomineClientException("No ontology data downloaded");
            }
            return new Ontology(this, ontology);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(
                    String.format("Could not download ontology data (ontology id=%d)", ontologyId), e);
        }
    }

    /**
     * @param ontologyId
     *        The ontology id.
     * @return The set of terms associated to the ontology.
     * @throws CytomineClientException
     *         If the ontology terms cannot be retrieved from the host server.
     */
    public Set<Term> getOntologyTerms(long ontologyId) throws CytomineClientException
    {
        Collection<be.cytomine.client.models.Term> termCollection;
        try
        {
            termCollection = Collection.fetchWithFilter(
                    be.cytomine.client.models.Term.class, be.cytomine.client.models.Ontology.class, ontologyId, 0, 0);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(
                    String.format("Could not download ontology terms (ontology id=%d)", ontologyId), e);
        }

        Set<Term> terms = new HashSet<>(termCollection.size());
        for (int i = 0; i < termCollection.size(); i++)
        {
            be.cytomine.client.models.Term inTerm = termCollection.get(i);
            be.cytomine.client.models.Term newInTerm = new be.cytomine.client.models.Term();
            newInTerm.setAttr(inTerm.getAttr());

            Term term = new Term(this, newInTerm);
            getTermCache().store(term.getId(), term);
            terms.add(term);
        }

        return terms;
    }

    private TermCache getTermCache()
    {
        if (termCache == null)
        {
            termCache = TermCache.create(this);
        }
        return termCache;
    }

    /**
     * @param termId
     *        The term id
     * @return The term model element.
     * @throws CytomineClientException
     *         If the term cannot be retrieved from the host server.
     */
    public Term getTerm(long termId) throws CytomineClientException
    {
        Term term;
        try
        {
            term = getTermCache().retrieve(termId);
        }
        catch (EntityCacheException e)
        {
            term = downloadTerm(termId);
            getTermCache().store(termId, term);
        }
        return term;
    }

    private Term downloadTerm(long termId) throws CytomineClientException
    {
        be.cytomine.client.models.Term term;
        try
        {
            term = new be.cytomine.client.models.Term().fetch(termId);
            if (term.getAttr() == null)
            {
                throw new CytomineClientException("No term data downloaded");
            }
            return new Term(this, term);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(String.format("Could not download term data (term id=%d)", termId), e);
        }
    }

    /**
     * @param projectId
     *        The project id.
     * @return The list of images on the project.
     * @throws CytomineClientException
     *         If the project images cannot be retrieved from the host server.
     */
    public List<Image> getProjectImages(long projectId) throws CytomineClientException
    {
        Collection<ImageInstance> imageInstanceCollection;
        try
        {
            imageInstanceCollection = Collection.fetchWithFilter(
                    ImageInstance.class, be.cytomine.client.models.Project.class, projectId,
                    0, 0);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(
                    String.format("Could not download project images (project id=%d)", projectId), e);
        }

        List<Image> images = new ArrayList<>(imageInstanceCollection.size());
        for (int i = 0; i < imageInstanceCollection.size(); i++)
        {
            ImageInstance inImage = imageInstanceCollection.get(i);
            ImageInstance newInImage = new ImageInstance();
            newInImage.setAttr(inImage.getAttr());

            Image image = new Image(this, newInImage);
            getImageCache().store(image.getId(), image);
            images.add(image);
        }

        return images;
    }

    private ImageInstanceCache getImageCache()
    {
        if (imageInstanceCache == null)
        {
            imageInstanceCache = ImageInstanceCache.create(this);
        }
        return imageInstanceCache;
    }

    /**
     * @param imageInstanceId
     *        The image instance id.
     * @return The image model element.
     * @throws CytomineClientException
     *         If the image instance cannot be retrieved from the host server.
     */
    public Image getImageInstance(long imageInstanceId) throws CytomineClientException
    {
        Image image;
        try
        {
            image = getImageCache().retrieve(imageInstanceId);
        }
        catch (EntityCacheException e)
        {
            image = downloadImage(imageInstanceId);
            getImageCache().store(imageInstanceId, image);
        }
        return image;
    }

    private Image downloadImage(long imageInstanceId) throws CytomineClientException
    {
        ImageInstance imageInstance;
        try
        {
            imageInstance = new ImageInstance().fetch(imageInstanceId);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(
                    String.format("Could not download image instance data (image instance id=%d)", imageInstanceId), e);
        }

        if (imageInstance.getAttr() == null)
        {
            throw new CytomineClientException("No image instance data downloaded");
        }
        return new Image(this, imageInstance);
    }

    /**
     * @param image
     *        The target image.
     * @return The list of server URLs for the given image.
     * @throws CytomineClientException
     *         If the image servers cannot be retrieved from the host server.
     */
    // public List<SliceInstance> getImageServers(Image image) throws CytomineClientException
    @SuppressWarnings("unchecked")
    public List<String> getImageServers(Image image) throws CytomineClientException
    {

        // List<SliceInstance> serverList = new ArrayList<>();
        //
        // be.cytomine.client.collections.Collection<SliceInstance> c = new be.cytomine.client.collections.Collection<>(
        // SliceInstance.class, 0, 0);
        // c.addFilter("imageinstance", String.valueOf(image.getId().longValue()));
        // try
        // {
        // c.fetch();
        // for (int i = 0; i < c.size(); i++)
        // {
        // SliceInstance slice = new SliceInstance();
        // slice.setAttr(c.get(i).getAttr());
        // serverList.add(slice);
        // // c.get(i).getStr("imageServerUrl") + "/slice/tile?fif=" + c.get(i).getStr("path")
        // }
        // }
        // catch (CytomineException e)
        // {
        // throw new CytomineClientException(
        // String.format("Could not download image servers (image=%s)", image.toString()), e);
        // }

//        JSONArray serverJSONArray = null;
//
//        try
//        {
//            AbstractImage abstractImage = new AbstractImage();
//            abstractImage.set("id", image.getInternalImage().get("baseImage"));
//            Collection<ImageServers> servers = new Collection<>(ImageServers.class, 0, 0);
//            servers.addFilter("abstractimage", "" + abstractImage.getId());
//            JSONObject answer = Cytomine.getInstance().getDefaultCytomineConnection().doGet(servers.toURL());
//            serverJSONArray = (JSONArray) answer.getOrDefault("imageServersURLs", new JSONArray());
//            // servers = getInternalClient().getImageInstanceServers(image.getInternalImage());
//        }
//        catch (CytomineException e)
//        {
//            throw new CytomineClientException(
//                  String.format("Could not download image servers (image=%s)", image.toString()), e);
//            //  System.out.println("Could not download image servers (image=" + image.toString() + "): " + e.getMessage());
//        }
//
//        List<String> serverList = ((ArrayList<Object>) serverJSONArray).stream().map(s -> {
//            return s.toString().replace(" ", "%20");
//        }).collect(Collectors.toList());
//
//        return serverList;

        JSONArray serverJSONArray;

        try
        {
            // Fetch the abstract image directly
            AbstractImage abstractImage = new AbstractImage().fetch(image.getAbstractImageId().get());
            // Get the image server URLs from the abstract image's attributes
            serverJSONArray = (JSONArray) abstractImage.getAttr().getOrDefault("imageServersURLs", new JSONArray());
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(
                    String.format("Could not download image servers (image=%s)", image.toString()), e);
        }

        List<String> serverList = ((ArrayList<Object>) serverJSONArray).stream().map(s -> {
            return s.toString().replace(" ", "%20");
        }).collect(Collectors.toList());

        return serverList;
    }

    /**
     * Retrieves a specific slice instance by its ID.
     *
     * @param sliceInstanceId The ID of the slice instance.
     * @return The SliceInstance object.
     * @throws CytomineClientException if the request fails.
     */
    public SliceInstance getSliceInstance(long sliceInstanceId) throws CytomineClientException {
        try {
            return new SliceInstance().fetch(sliceInstanceId);
        } catch (CytomineException e) {
            throw new CytomineClientException("Failed to retrieve slice instance " + sliceInstanceId, e);
        }
    }

    /**
     * @param imageInstanceId
     *        The image instance id.
     * @return The {@link BufferedImage} preview downloaded.
     * @throws CytomineClientException
     *         If the image preview cannot be retrieved from the host server.
     */
    public BufferedImage downloadImageAsBufferedImage(long imageInstanceId) throws CytomineClientException
    {
        try
        {
            // String url = getHost() + "/api/imageinstance/" + imageInstanceId + "/thumb.png";
            String url = getHost() + "/api/abstractimage/" + imageInstanceId + "/thumb.png?maxSize=256";
            return getInternalClient().getDefaultCytomineConnection().getPictureAsBufferedImage(url, "png");

            // return getInternalClient().getAbstractImageThumb(abstractImageId, maxSize);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(
                    String.format("Could not download image as buffered image (abstract image id=%d)", imageInstanceId),
                    e);
        }
    }

    public UserAnnotation addUserAnnotationWithTerms(Long imageId, String geometryDescription, List<Long> termIds)
    {
        Annotation internalAnnotation;
        try
        {
            internalAnnotation = new Annotation().save();
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException("Could not create annotation.", e);
        }
        UserAnnotation annotation = new UserAnnotation(this, internalAnnotation);
        getAbstractAnnotationCache().store(annotation.getId(), annotation);
        return annotation;
    }

    public Map<Long, AbstractAnnotation> getImageAbstractAnnotationsWithGeometry(Image image,
            boolean recompute)
    {
        Map<Long, AbstractAnnotation> requestedImageAnnotations = getImageAbstractAnnotations(image);

        long annotationsWithoutGeometryCount =
                requestedImageAnnotations.values().stream().filter(a -> !a.getLocation().isPresent()).count();
        if (annotationsWithoutGeometryCount > 0 || recompute)
        {
            Map<Long, AbstractAnnotation> geometryAnnotations = getImageAbstractAnnotationGeometries(image);
            requestedImageAnnotations.values().stream().forEach(StreamUtils.wrapConsumer(a -> {
                AbstractAnnotation geomAnnotation = geometryAnnotations.get(a.getId());
                a.getInternalAnnotation().set("location", geomAnnotation.getInternalAnnotation().get("location"));
            }));
        }

        return requestedImageAnnotations;
    }

    public Map<Long, AbstractAnnotation> getImageAbstractAnnotations(Image image) throws CytomineClientException
    {
        Map<Long, AbstractAnnotation> annotations = new HashMap<>();
        annotations.putAll(getImageUserAnnotations(image));
        annotations.putAll(getImageAlgorithmAnnotations(image));
        annotations.putAll(getImageReviewedAnnotations(image));
        return annotations;
    }

    public Map<Long, UserAnnotation> getImageUserAnnotations(Image image) throws CytomineClientException
    {
        List<User> users = image.getProject().getUsers(false);

        if (users == null || users.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            Set<Long> userIds = users.stream().filter(Objects::nonNull).map(u -> u.getId()).collect(Collectors.toSet());
            return getImageUserAnnotations(image, userIds);
        }
    }

    public Map<Long, UserAnnotation> getImageUserAnnotations(Image image, Set<Long> userIds)
            throws CytomineClientException
    {
        if (userIds == null || userIds.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            AnnotationCollection annotationCollection;
            try
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("images", image.getId());
                parameters.put("users", userIds.stream().filter(Objects::nonNull).map(u -> u.toString())
                        .collect(Collectors.joining(",")));
                annotationCollection = AnnotationCollection.fetchWithParameters(parameters);

            }
            catch (CytomineException e)
            {
                throw new CytomineClientException(
                        String.format("Could not download image annotations (image instance id=%d)", image.getId()), e);
            }

            Map<Long, UserAnnotation> foundAnnotations = new HashMap<>();
            for (int i = 0; i < annotationCollection.size(); i++)
            {
                UserAnnotation a = new UserAnnotation(this, annotationCollection.get(i));
                getAbstractAnnotationCache().store(a.getId(), a);
                foundAnnotations.put(a.getId(), a);
            }
            return foundAnnotations;
        }
    }

    public Map<Long, AlgorithmAnnotation> getImageAlgorithmAnnotations(Image image) throws CytomineClientException
    {

        List<UserJob> jobs = image.getProject().getUserJobs(false);

        if (jobs.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            Set<Long> jobIds = jobs.stream().filter(Objects::nonNull).map(u -> u.getId()).collect(Collectors.toSet());
            return getImageAlgorithmAnnotations(image, jobIds);
        }

    }

    public Map<Long, AlgorithmAnnotation> getImageAlgorithmAnnotations(Image image, Set<Long> userJobIds)
    {
        if (userJobIds == null || userJobIds.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            AnnotationCollection annotationCollection;
            try
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("images", image.getId());
                String jobsString = userJobIds.stream().filter(Objects::nonNull).map(u -> u.toString())
                        .collect(Collectors.joining(","));
                parameters.put("users", jobsString);

                annotationCollection = AnnotationCollection.fetchWithParameters(parameters);
            }
            catch (CytomineException e)
            {
                throw new CytomineClientException(
                        String.format("Could not download image annotations (image instance id=%d)", image.getId()), e);
            }

            Map<Long, AlgorithmAnnotation> foundAnnotations = new HashMap<>();
            for (int i = 0; i < annotationCollection.size(); i++)
            {
                AlgorithmAnnotation a = new AlgorithmAnnotation(this, annotationCollection.get(i));
                getAbstractAnnotationCache().store(a.getId(), a);
                foundAnnotations.put(a.getId(), a);
            }
            return foundAnnotations;
        }
    }

    public Map<Long, ReviewedAnnotation> getImageReviewedAnnotations(Image image) throws CytomineClientException
    {
        List<User> users = image.getProject().getUsers(false);
        if (users == null || users.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            Set<Long> reviewUserIds =
                    users.stream().filter(Objects::nonNull).map(u -> u.getId()).collect(Collectors.toSet());
            return getImageReviewedAnnotations(image, reviewUserIds);
        }
    }

    public Map<Long, ReviewedAnnotation> getImageReviewedAnnotations(Image image, Set<Long> reviewUserIds)
            throws CytomineClientException
    {
        if (reviewUserIds == null || reviewUserIds.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            AnnotationCollection annotationCollection;
            try
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("images", image.getId());
                parameters.put("reviewed", true);
                String usersString = reviewUserIds.stream().filter(Objects::nonNull).map(u -> u.toString())
                        .collect(Collectors.joining(","));
                parameters.put("reviewUsers", usersString);

                annotationCollection = AnnotationCollection.fetchWithParameters(parameters);
            }
            catch (CytomineException e)
            {
                throw new CytomineClientException(
                        String.format("Could not download image annotations (image instance id=%d)", image.getId()), e);
            }

            Map<Long, ReviewedAnnotation> foundAnnotations = new HashMap<>();
            for (int i = 0; i < annotationCollection.size(); i++)
            {
                ReviewedAnnotation a = new ReviewedAnnotation(this, annotationCollection.get(i));
                getAbstractAnnotationCache().store(a.getId(), a);
                foundAnnotations.put(a.getId(), a);
            }
            return foundAnnotations;
        }
    }

    private AbstractAnnotationCache getAbstractAnnotationCache()
    {
        if (abstractAnnotationCache == null)
        {
            abstractAnnotationCache = AbstractAnnotationCache.create(this);
        }
        return abstractAnnotationCache;
    }

    public AbstractAnnotation getAbstractAnnotation(long annotationId)
    {
        AbstractAnnotation annotation;
        try
        {
            annotation = getAbstractAnnotationCache().retrieve(annotationId);
        }
        catch (EntityCacheException e)
        {
            annotation = downloadAbstractAnnotation(annotationId);
            getAbstractAnnotationCache().store(annotationId, annotation);
        }
        return annotation;
    }

    public AbstractAnnotation downloadAbstractAnnotation(long annotationId)
    {
        Annotation annotation;
        try
        {
            annotation = new Annotation().fetch(annotationId);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(
                    String.format("Could not download annotation data (annotation id=%d)", annotationId), e);
        }

        if (annotation.getAttr() == null)
        {
            throw new CytomineClientException("No annotation data downloaded");
        }

        AbstractAnnotation abstractAnnotation;
        switch (annotation.getStr("class"))
        {
            case "be.cytomine.ontology.UserAnnotation":
                abstractAnnotation = new UserAnnotation(this, annotation);
                break;
            case "be.cytomine.ontology.AlgoAnnotation":
                abstractAnnotation = new AlgorithmAnnotation(this, annotation);
                break;
            case "be.cytomine.ontology.ReviewedAnnotation":
                abstractAnnotation = new ReviewedAnnotation(this, annotation);
                break;
            default:
                throw new CytomineClientException("Annotation with Id " + annotationId + " is of incompatible type "
                        + annotation.getStr("class"));
        }
        return abstractAnnotation;
    }

    public Map<Long, AbstractAnnotation> getImageAbstractAnnotationGeometries(Image image)
    {
        Map<Long, AbstractAnnotation> annotations = new HashMap<>();
        annotations.putAll(getImageUserAnnotationGeometries(image));
        annotations.putAll(getImageAlgorithmAnnotationGeometries(image));
        annotations.putAll(getImageReviewedAnnotationGeometries(image));
        return annotations;
    }

    public Map<Long, UserAnnotation> getImageUserAnnotationGeometries(Image image)
    {
        List<User> users = image.getProject().getUsers(false);
        if (users == null || users.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            return getImageUserAnnotationGeometries(image,
                    users.stream().map(u -> u.getId()).collect(Collectors.toSet()));
        }
    }
    
    public Map<Long, UserAnnotation> getImageUserAnnotationGeometries(Image image, Rectangle2D imageArea)
    {
        List<User> users = image.getProject().getUsers(false);
        if (users == null || users.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            return getImageUserAnnotationGeometries(image, users.stream().map(u -> u.getId()).collect(Collectors.toSet()),
                    imageArea);
        }
    }

    public Map<Long, UserAnnotation> getImageUserAnnotationGeometries(Image image, Set<Long> userIds)
    {
        if (userIds == null || userIds.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            AnnotationCollection annotationCollection;
            try
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("images", image.getId());
                parameters.put("users", userIds.stream().filter(Objects::nonNull).map(u -> u.toString())
                        .collect(Collectors.joining(",")));
                parameters.put("showWKT", "true");
                annotationCollection = AnnotationCollection.fetchWithParameters(parameters);

            }
            catch (CytomineException e)
            {
                throw new CytomineClientException(
                        String.format("Could not download image annotation geometries (image instance id=%d)",
                                image.getId()),
                        e);
            }

            Map<Long, UserAnnotation> foundAnnotations = new HashMap<>();
            for (int i = 0; i < annotationCollection.size(); i++)
            {
                UserAnnotation a = new UserAnnotation(this, annotationCollection.get(i));
                foundAnnotations.put(a.getId(), a);
            }
            return foundAnnotations;
        }
    }

    public Map<Long, UserAnnotation> getImageUserAnnotationGeometries(Image image, Set<Long> userIds, Rectangle2D imageArea)
    {
        if (userIds == null || userIds.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            AnnotationCollection annotationCollection;
            try
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("images", image.getId());
                parameters.put("users", userIds.stream().filter(Objects::nonNull).map(u -> u.toString())
                        .collect(Collectors.joining(",")));
                parameters.put("bbox", WKTUtils.createFromRectangle2D(imageArea).replace(" ", "%20"));
                parameters.put("showWKT", "true");
                annotationCollection = AnnotationCollection.fetchWithParameters(parameters);

            }
            catch (CytomineException e)
            {
                throw new CytomineClientException(
                        String.format("Could not download image annotation geometries (image instance id=%d)",
                                image.getId()),
                        e);
            }

            Map<Long, UserAnnotation> foundAnnotations = new HashMap<>();
            for (int i = 0; i < annotationCollection.size(); i++)
            {
                UserAnnotation a = new UserAnnotation(this, annotationCollection.get(i));
                foundAnnotations.put(a.getId(), a);
            }
            return foundAnnotations;
        }
    }

    public Map<Long, AlgorithmAnnotation> getImageAlgorithmAnnotationGeometries(Image image)
    {
        List<UserJob> userJobs;
        userJobs = image.getProject().getUserJobs(false);

        if (userJobs.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            return getImageAlgorithmAnnotationGeometries(image,
                    userJobs.stream().map(uj -> uj.getId()).collect(Collectors.toSet()));
        }
    }

    public Map<Long, AlgorithmAnnotation> getImageAlgorithmAnnotationGeometries(Image image, Set<Long> userJobIds)
    {
        if (userJobIds == null || userJobIds.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            AnnotationCollection annotationCollection;
            try
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("images", image.getId());
                String jobsString = userJobIds.stream().filter(Objects::nonNull).map(u -> u.toString())
                        .collect(Collectors.joining(","));
                parameters.put("users", jobsString);
                parameters.put("showWKT", "true");

                annotationCollection = AnnotationCollection.fetchWithParameters(parameters);
            }
            catch (CytomineException e)
            {
                throw new CytomineClientException(
                        String.format("Could not download image annotations (image instance id=%d)", image.getId()),
                        e);
            }

            Map<Long, AlgorithmAnnotation> foundAnnotations = new HashMap<>();
            for (int i = 0; i < annotationCollection.size(); i++)
            {
                AlgorithmAnnotation a = new AlgorithmAnnotation(this, annotationCollection.get(i));
                foundAnnotations.put(a.getId(), a);
            }
            return foundAnnotations;
        }
    }

    public Map<Long, ReviewedAnnotation> getImageReviewedAnnotationGeometries(Image image)
    {
        List<User> users = image.getProject().getUsers(false);
        if (users == null || users.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            return getImageReviewedAnnotationGeometries(image,
                    users.stream().map(u -> u.getId()).collect(Collectors.toSet()));
        }
    }

    public Map<Long, ReviewedAnnotation> getImageReviewedAnnotationGeometries(Image image, Set<Long> reviewUserIds)
    {
        if (reviewUserIds == null || reviewUserIds.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            AnnotationCollection annotationCollection;
            try
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("images", image.getId());
                parameters.put("reviewed", true);
                String usersString = reviewUserIds.stream().filter(Objects::nonNull).map(u -> u.toString())
                        .collect(Collectors.joining(","));
                parameters.put("reviewUsers", usersString);
                parameters.put("showWKT", "true");

                annotationCollection = AnnotationCollection.fetchWithParameters(parameters);
            }
            catch (CytomineException e)
            {
                throw new CytomineClientException(
                        String.format("Could not download image annotations (image instance id=%d)", image.getId()), e);
            }

            Map<Long, ReviewedAnnotation> foundAnnotations = new HashMap<>();
            for (int i = 0; i < annotationCollection.size(); i++)
            {
                ReviewedAnnotation a = new ReviewedAnnotation(this, annotationCollection.get(i));
                foundAnnotations.put(a.getId(), a);
            }
            return foundAnnotations;
        }
    }

    public Map<Long, AbstractAnnotation> getImageAbstractAnnotationsAt(Long imageId, Rectangle2D imageArea)
            throws CytomineClientException
    {
        Map<Long, AbstractAnnotation> annotations = new HashMap<>();
        annotations.putAll(getImageUserAnnotationsAt(imageId, imageArea));
        annotations.putAll(getImageAlgorithmAnnotationsAt(imageId, imageArea));
        annotations.putAll(getImageReviewedAnnotationsAt(imageId, imageArea));
        return annotations;
    }

    public Map<Long, UserAnnotation> getImageUserAnnotationsAt(Long imageId,
            Rectangle2D imageArea)
    {
        Image image = getImageInstance(imageId);
        List<User> users = image.getProject().getUsers(false);
        if (users == null || users.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            return getImageUserAnnotationsAt(imageId, users.stream().map(u -> u.getId()).collect(Collectors.toSet()),
                    imageArea);
        }
    }

    public Map<Long, UserAnnotation> getImageUserAnnotationsAt(Long imageId, Set<Long> userIds, Rectangle2D imageArea)
    {
        if (userIds == null || userIds.isEmpty() || imageArea == null)
        {
            return new HashMap<>(0);
        }
        else
        {
            AnnotationCollection annotationCollection;
            try
            {
                Image image = getImageInstance(imageId);
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("images", image.getId());
                parameters.put("users", userIds.stream().filter(Objects::nonNull).map(u -> u.toString())
                        .collect(Collectors.joining(",")));
                parameters.put("bbox", WKTUtils.createFromRectangle2D(imageArea).replace(" ", "%20"));
                annotationCollection = AnnotationCollection.fetchWithParameters(parameters);
            }
            catch (CytomineException e)
            {
                throw new CytomineClientException(
                        String.format("Could not download image annotation geometries (image instance id=%d)", imageId),
                        e);
            }

            Map<Long, UserAnnotation> foundAnnotations = new HashMap<>();
            for (int i = 0; i < annotationCollection.size(); i++)
            {
                UserAnnotation a = new UserAnnotation(this, annotationCollection.get(i));
                foundAnnotations.put(a.getId(), a);
            }
            return foundAnnotations;
        }
    }

    public Map<Long, AlgorithmAnnotation> getImageAlgorithmAnnotationsAt(Long imageId,
            Rectangle2D imageArea)
    {
        Image image = getImageInstance(imageId);
        List<UserJob> userJobs;
        userJobs = image.getProject().getUserJobs(false);

        if (userJobs.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            return getImageAlgorithmAnnotationsAt(imageId,
                    userJobs.stream().map(uj -> uj.getId()).collect(Collectors.toSet()), imageArea);
        }
    }

    public Map<Long, AlgorithmAnnotation> getImageAlgorithmAnnotationsAt(Long imageId, Set<Long> userJobIds,
            Rectangle2D imageArea)
    {
        if (userJobIds == null || userJobIds.isEmpty() || imageArea == null)
        {
            return new HashMap<>(0);
        }
        else
        {
            Image image = getImageInstance(imageId);

            AnnotationCollection annotationCollection;
            try
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("images", image.getId());
                String jobsString = userJobIds.stream().filter(Objects::nonNull).map(u -> u.toString())
                        .collect(Collectors.joining(","));
                parameters.put("users", jobsString);
                parameters.put("bbox", WKTUtils.createFromRectangle2D(imageArea).replace(" ", "%20"));
                annotationCollection = AnnotationCollection.fetchWithParameters(parameters);
            }
            catch (CytomineException e)
            {
                throw new CytomineClientException(
                        String.format("Could not download image annotations (image instance id=%d)", image.getId()),
                        e);
            }

            Map<Long, AlgorithmAnnotation> foundAnnotations = new HashMap<>();
            for (int i = 0; i < annotationCollection.size(); i++)
            {
                AlgorithmAnnotation a = new AlgorithmAnnotation(this, annotationCollection.get(i));
                foundAnnotations.put(a.getId(), a);
            }
            return foundAnnotations;

        }
    }

    public Map<Long, ReviewedAnnotation> getImageReviewedAnnotationsAt(Long imageId, Rectangle2D imageArea)
    {
        Image image = getImageInstance(imageId);
        List<User> users = image.getProject().getUsers(false);
        if (users == null || users.isEmpty())
        {
            return new HashMap<>(0);
        }
        else
        {
            return getImageReviewedAnnotationsAt(imageId,
                    users.stream().map(u -> u.getId()).collect(Collectors.toSet()),
                    imageArea);
        }
    }

    public Map<Long, ReviewedAnnotation> getImageReviewedAnnotationsAt(Long imageId, Set<Long> reviewUserIds,
            Rectangle2D imageArea)
    {
        if (reviewUserIds == null || reviewUserIds.isEmpty() || imageArea == null)
        {
            return new HashMap<>(0);
        }
        else
        {
            AnnotationCollection annotationCollection;
            try
            {
                Image image = getImageInstance(imageId);
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("images", image.getId());
                parameters.put("reviewed", true);
                parameters.put("reviewUsers", reviewUserIds.stream().filter(Objects::nonNull).map(u -> u.toString())
                        .collect(Collectors.joining(",")));
                parameters.put("bbox", WKTUtils.createFromRectangle2D(imageArea).replace(" ", "%20"));
                annotationCollection = AnnotationCollection.fetchWithParameters(parameters);
            }
            catch (CytomineException e)
            {
                throw new CytomineClientException(
                        String.format("Could not download image annotation geometries (image instance id=%d)", imageId),
                        e);
            }

            Map<Long, ReviewedAnnotation> foundAnnotations = new HashMap<>();
            for (int i = 0; i < annotationCollection.size(); i++)
            {
                ReviewedAnnotation a = new ReviewedAnnotation(this, annotationCollection.get(i));
                foundAnnotations.put(a.getId(), a);
            }
            return foundAnnotations;
        }
    }

    public List<Property> getAbstractAnnotationProperties(AbstractAnnotation annotation) throws CytomineClientException
    {
        PropertyCollection propertyList;
        try
        {
            propertyList = getInternalClient().getDomainProperties("annotation", annotation.getId());
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(
                    String.format("Could not retrieve annotation %d properties", annotation.getId()), e);
        }

        List<Property> properties = new ArrayList<>(propertyList.size());
        for (int i = 0; i < propertyList.size(); i++)
        {
            properties.add(new Property(this, (be.cytomine.client.models.Property) propertyList.get(i)));
        }
        return properties;
    }

    public void associateTermsToAnnotation(AbstractAnnotation annotation, Map<Term, Boolean> termSelection)
            throws CytomineClientException
    {

        for (Entry<Term, Boolean> termEntry : termSelection.entrySet())
        {
            try
            {
                if (termEntry.getValue())
                {
                    new AnnotationTerm(annotation.getId(), termEntry.getKey().getId()).save();
                    // new be.cytomine.client.models.AnnotationTerm(annotation.getId(), termEntry.getKey().getId()).save();
                }
                else
                {
                    new AnnotationTerm(annotation.getId(), termEntry.getKey().getId()).delete();
                }
            }
            catch (CytomineException e)
            {
                System.err.println(
                        String.format("Could not associate terms to annotation %d", annotation.getId().longValue()));
                e.printStackTrace();
            }
        }

    }

    // /**
    // * @deprecated
    // * @param imageInstanceId
    // * The image instance id.
    // * @return Annotations of the specified image in full format (i.e. With
    // * geometry data). To retrieve simple data use
    // * {@link #getImageAnnotations(long)}.
    // * @throws CytomineClientException
    // * If the image annotations cannot be retrieved from the host
    // * server.
    // */
    // public List<Annotation> getFullImageAnnotations(long imageInstanceId) throws CytomineClientException
    // {
    // AnnotationCollection annotationCollection;
    // try
    // {
    // Map<String, Object> filters = getFullImageAnnotationsFilters(imageInstanceId);
    // annotationCollection = AnnotationCollection.fetchWithParameters(filters);
    // System.out.println(annotationCollection.toURL());
    // }
    // catch (CytomineException e)
    // {
    // throw new CytomineClientException(
    // String.format("Could not download full image annotations (image instance id=%d)", imageInstanceId),
    // e);
    // }
    //
    // return convertAnnotationCollectionToAnnotationList(annotationCollection);
    // }
    //
    // @Deprecated
    // private List<Annotation> convertAnnotationCollectionToAnnotationList(AnnotationCollection annotationCollection)
    // {
    // @SuppressWarnings("unchecked")
    // List<Annotation> annotations = ((Stream<Object>) annotationCollection.getList().parallelStream())
    // .map(element -> {
    // be.cytomine.client.models.Annotation nativeAnnotation = new be.cytomine.client.models.Annotation();
    // nativeAnnotation.setAttr((JSONObject) element);
    // Annotation annotation = new Annotation(this, nativeAnnotation);
    // return annotation;
    // }).collect(Collectors.toList());
    //
    // annotations.stream().forEach(a -> getAnnotationCache().store(a.getId(), a));
    //
    // return annotations;
    // }
    //
    // private Map<String, Object> getFullImageAnnotationsFilters(long imageInstanceId)
    // {
    // Map<String, Object> filters = new HashMap<>();
    // filters.put("image", String.valueOf(imageInstanceId));
    // filters.put("showMeta", "true");
    // filters.put("showWKT", "true");
    // filters.put("showGIS", "false");
    // filters.put("showTerm", "true");
    // return filters;
    // }
    //
    // @Deprecated
    // public List<Annotation> getFullImageAnnotations(Long imageInstanceId, Rectangle2D currentTileArea)
    // throws CytomineClientException
    // {
    // AnnotationCollection annotationCollection;
    // try
    // {
    // Map<String, Object> filters = getFullImageAnnotationsFilters(imageInstanceId);
    // filters.put("bbox", WKTUtils.createFromRectangle2D(currentTileArea).replace(" ", "%20"));
    // annotationCollection = AnnotationCollection.fetchWithParameters(filters);
    // }
    // catch (CytomineException e)
    // {
    // throw new CytomineClientException(
    // String.format("Could not download full image annotations (image instance id=%d) for area (%s)",
    // imageInstanceId, String.valueOf(currentTileArea)),
    // e);
    // }
    //
    // List<Annotation> annotations = new ArrayList<>(annotationCollection.size());
    // for (int i = 0; i < annotationCollection.size(); i++)
    // {
    // be.cytomine.client.models.Annotation newAnnotationIn = new be.cytomine.client.models.Annotation();
    // newAnnotationIn.setAttr(annotationCollection.get(i).getAttr());
    // Annotation annotation = new Annotation(this, newAnnotationIn);
    // getAnnotationCache().store(annotation.getId(), annotation);
    // annotations.add(annotation);
    // }
    //
    // return annotations;
    // }
    //
    // /**
    // * @deprecated
    // * @param annotationId
    // * The annotation id.
    // * @return The annotation model element.
    // * @throws CytomineClientException
    // * If the annotation cannot be retrieved from the host server.
    // */
    // public Annotation getAnnotation(long annotationId) throws CytomineClientException
    // {
    // Annotation annotation;
    // try
    // {
    // annotation = getAnnotationCache().retrieve(annotationId);
    // }
    // catch (EntityCacheException e)
    // {
    // annotation = downloadAnnotation(annotationId);
    // getAnnotationCache().store(annotationId, annotation);
    // }
    // return annotation;
    // }
    //
    // @Deprecated
    // public Annotation downloadAnnotation(long annotationId) throws CytomineClientException
    // {
    // be.cytomine.client.models.Annotation annotation;
    // try
    // {
    // annotation = new be.cytomine.client.models.Annotation().fetch(annotationId);
    // }
    // catch (CytomineException e)
    // {
    // throw new CytomineClientException(
    // String.format("Could not download annotation data (annotation id=%d)", annotationId), e);
    // }
    //
    // if (annotation.getAttr() == null)
    // {
    // throw new CytomineClientException("No annotation data downloaded");
    // }
    // return new Annotation(this, annotation);
    // }

    /**
     * @param annotationId
     *        The annotation id.
     * @return The WKT geometry of the annotation.
     * @throws CytomineClientException
     *         If the annotation location cannot be retrieved from the host
     *         server.
     */
    public Optional<String> getAnnotationLocation(long annotationId) throws CytomineClientException
    {
        AbstractAnnotation annotation = downloadAbstractAnnotation(annotationId);
        getAbstractAnnotationCache().store(annotationId, annotation);
        return annotation.getLocation();
    }

    // // FIXME Not working -- AnnotationTerm does not work like this.
    // public List<AnnotationTerm> downloadAnnotationTerms(long annotationId) throws CytomineClientException
    // {
    // be.cytomine.client.collections.Collection<be.cytomine.client.models.AnnotationTerm> annotationTermCollection;
    // try
    // {
    // annotationTermCollection = be.cytomine.client.collections.Collection.fetchWithFilter(
    // be.cytomine.client.models.AnnotationTerm.class, be.cytomine.client.models.Annotation.class,
    // annotationId, 0, 0);
    // }
    // catch (CytomineException e)
    // {
    // throw new CytomineClientException(
    // String.format("Could not download annotation terms (annotation id=%d)", annotationId), e);
    // }
    //
    // List<AnnotationTerm> annotationTerms = new ArrayList<>(annotationTermCollection.size());
    // for (int i = 0; i < annotationTermCollection.size(); i++)
    // {
    // be.cytomine.client.models.AnnotationTerm internalAnnotationTerm =
    // new be.cytomine.client.models.AnnotationTerm();
    // internalAnnotationTerm.setAttr(annotationTermCollection.get(i).getAttr());
    // AnnotationTerm annotation = new AnnotationTerm(this, internalAnnotationTerm);
    // annotationTerms.add(annotation);
    // }
    //
    // return annotationTerms;
    // }

    public BufferedImage downloadPictureAsBufferedImage(String url, String format) throws CytomineClientException
    {
        try
        {
            return getInternalClient().getDefaultCytomineConnection().getPictureAsBufferedImage(url, format);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(e.getMsg(), e);
        }
    }

    // @Deprecated
    // public Annotation addAnnotationWithTerms(long imageInstanceId, String geometry, List<Long> termIds)
    // throws CytomineClientException
    // {
    // be.cytomine.client.models.Annotation internalAnnotation;
    // try
    // {
    // internalAnnotation = new be.cytomine.client.models.Annotation(geometry, imageInstanceId, termIds).save();
    // }
    // catch (CytomineException e)
    // {
    // throw new CytomineClientException("Could not create annotation.", e);
    // }
    // Annotation annotation = new Annotation(this, internalAnnotation);
    // getAnnotationCache().store(annotation.getId(), annotation);
    // return annotation;
    // }
    //
    // @Deprecated
    // public void associateTerms(Annotation annotation, Map<Term, Boolean> termSelection) throws CytomineClientException
    // {
    // try
    // {
    // for (Entry<Term, Boolean> termEntry : termSelection.entrySet())
    // {
    // if (termEntry.getValue())
    // {
    // new be.cytomine.client.models.AnnotationTerm(annotation.getId(), termEntry.getKey().getId()).save();
    // }
    // else
    // {
    // getInternalClient().deleteAnnotationTerm(annotation.getId(), termEntry.getKey().getId());
    // }
    // }
    // }
    // catch (CytomineException e)
    // {
    // throw new CytomineClientException(
    // String.format("Could not associate terms to annotation %d", annotation.getId().longValue()), e);
    // }
    //
    // }

    public void removeAnnotation(long annotationId) throws CytomineClientException
    {
        try
        {
            new Annotation().delete(annotationId);
            // getAnnotationCache().remove(annotationId);
            getAbstractAnnotationCache().remove(annotationId);
        }
        catch (CytomineException e)
        {
            throw new CytomineClientException(String.format("Could not remove annotation %d", annotationId), e);
        }
    }

    // @Deprecated
    // public JSONArray getAnnotationUsersByTerm(Annotation annotation) throws CytomineClientException
    // {
    // try
    // {
    // return be.cytomine.client.collections.Collection.fetchWithFilter(be.cytomine.client.models.Term.class,
    // be.cytomine.client.models.Annotation.class, annotation.getId(), 0, 0).getList();
    // }
    // catch (CytomineException e)
    // {
    // throw new CytomineClientException(
    // String.format("Could not retrieve annotation %d termUsers", annotation.getId()), e);
    // }
    // }

    @SuppressWarnings("unchecked")
    public Map<Long, Set<Long>> getAnnotationUsersByTerm(AbstractAnnotation annotation) throws CytomineClientException
    {
        JSONArray termUsersArray = (JSONArray) annotation.getInternalAnnotation().get("userByTerm");
        if (termUsersArray == null)
        {
            try
            {
                termUsersArray =
                        Collection.fetchWithFilter(be.cytomine.client.models.Term.class,
                                Annotation.class, annotation.getId(), 0, 0).getList();
            }
            catch (CytomineException e)
            {
                throw new CytomineClientException(
                        String.format("Could not retrieve annotation %d termUsers", annotation.getId()), e);
            }

        }

        Map<Long, Set<Long>> termUsers = new HashMap<>();
        for (Object tObject : termUsersArray)
        {
            JSONObject termUser = (JSONObject) tObject;
            long termId = (Long) termUser.get("term");
            termUsers.putIfAbsent(termId, new HashSet<>());

            JSONArray userIds;
            if (termUser.get("user") instanceof JSONArray)
            {
                userIds = (JSONArray) termUser.get("user");
            }
            else if (termUser.get("user") != null)
            {
                userIds = new JSONArray();
                userIds.add((Long) termUser.get("user"));
            }
            else
            {
                throw new CytomineClientException("Term " + termId + " has not user associated");
            }

            for (Object uObject : userIds)
            {
                long userId = (Long) uObject;
                termUsers.get(termId).add(userId);
            }
        }

        return termUsers;
    }

    // @Deprecated
    // public List<Property> getAnnotationProperties(Annotation annotation)
    // {
    // PropertyCollection propertyList;
    // try
    // {
    // propertyList = getInternalClient().getDomainProperties("annotation", annotation.getId());
    // }
    // catch (CytomineException e)
    // {
    // throw new CytomineClientException(
    // String.format("Could not retrieve annotation %d properties", annotation.getId()), e);
    // }
    //
    // List<Property> properties = new ArrayList<>(propertyList.size());
    // for (int i = 0; i < propertyList.size(); i++)
    // {
    // properties.add(new Property(this, (be.cytomine.client.models.Property) propertyList.get(i)));
    // }
    // return properties;
    // }

    public List<UserJob> getUserJobsInProject(Project project) throws CytomineClientException
    {
        Collection<be.cytomine.client.models.UserJob> userJobCollection =
                new Collection<>(be.cytomine.client.models.UserJob.class, 0, 0);
        userJobCollection.addFilter("project", project.getId().toString());

        try
        {
            JSONObject json =
                    internalClient.getDefaultCytomineConnection().doGet(userJobCollection.toURL().toLowerCase());
            userJobCollection.setList((JSONArray) json.get("collection"));
        }
        catch (CytomineException e)
        {
            // TEST
//            throw new CytomineClientException(
//                    String.format("Could not retrieve user jobs in project %d", project.getId()), e);
            System.out.printf("Could not retrieve user jobs in project %d: %s%n", project.getId(), e.getMessage());
            ;
        }

        List<UserJob> foundUserJobs = new ArrayList<>(userJobCollection.size());
        for (int i = 0; i < userJobCollection.size(); i++)
        {
            UserJob userJob = new UserJob(this, userJobCollection.get(i));
            foundUserJobs.add(userJob);
            getUserJobCache().store(userJob.getId(), userJob);
        }
        return foundUserJobs;
    }

    public UserJob getUserJob(Long userJobId) throws CytomineClientException
    {
        UserJob userJob;
        try
        {
            userJob = getUserJobCache().retrieve(userJobId);
        }
        catch (EntityCacheException e)
        {
            be.cytomine.client.models.UserJob internalUserJob;
            try
            {
                internalUserJob = new be.cytomine.client.models.UserJob()
                        .fetch(internalClient.getDefaultCytomineConnection(), userJobId);
                if (internalUserJob.getAttr() == null)
                {
                    throw new CytomineClientException("No user job data downloaded");
                }
            }
            catch (CytomineException e1)
            {
                throw new CytomineClientException("Could not retrieve user job " + userJobId, e1);
            }

            userJob = new UserJob(this, internalUserJob);
            getUserJobCache().store(userJobId, userJob);
        }
        return userJob;
    }

    private UserJobCache getUserJobCache()
    {
        if (userJobCache == null)
        {
            userJobCache = UserJobCache.create(this);
        }
        return userJobCache;
    }

    public void updateImageMagnfication(Image image, Integer newMagnification) throws CytomineClientException
    {
        try
        {
            AbstractImage abstractImage = new AbstractImage().fetch(image.getAbstractImageId().get());
            abstractImage.set("magnification", newMagnification);
            abstractImage.update();
            // AbstractImage newAbstractImage = getInternalClient().updateModel(abstractImage);
            if (Objects.equals(abstractImage.getInt("magnification"), newMagnification))
            {
                image.getInternalImage().set("magnification", abstractImage.get("magnification"));
            }
            else
            {
                throw new CytomineClientException(String.format("You must be the original uploader of the image"));
            }
        }
        catch (Exception e)
        {
            throw new CytomineClientException(
                    String.format("Could not set the magnification of the image %s to %sX. " + e.getMessage(),
                            image.getId(), newMagnification),
                    e);
        }
    }

    public void updateImageResolution(Image image, Double newResolution)
    {
        try
        {
            AbstractImage abstractImage = new AbstractImage().fetch(image.getAbstractImageId().get());
            abstractImage.set("resolution", newResolution);
            abstractImage.update();
            // AbstractImage newAbstractImage = getInternalClient().updateModel(abstractImage);
            if (Objects.equals(abstractImage.getDbl("resolution"), newResolution))
            {
                image.getInternalImage().set("resolution", abstractImage.get("resolution"));
            }
            else
            {
                throw new CytomineClientException(String.format("You must be the original uploader of the image"));
            }
        }
        catch (Exception e)
        {
            throw new CytomineClientException(String.format(
                    "Could not set the resolution of the image %s to %s mics/px", image.getId(), newResolution), e);
        }
    }

    @Override
    public String toString()
    {
        return String.format("Cytomine client: host=%s, public key=%s", String.valueOf(getHost()),
                String.valueOf(getPublicKey()));
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result;
        if (internalClient != null)
        {
            result += getHost().hashCode();
            result = prime * result + getPublicKey().hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof CytomineClient))
        {
            return false;
        }

        CytomineClient other = (CytomineClient) obj;
        return hashCode() == other.hashCode();
    }

    @Override
    public void close() throws Exception
    {
        closeCaches();
    }

    private void closeCaches()
    {
        UserCache.getCacheManager().removeCache(userCache.getCacheAlias());
        ProjectCache.getCacheManager().removeCache(projectCache.getCacheAlias());
        DescriptionCache.getCacheManager().removeCache(descriptionCache.getCacheAlias());
        OntologyCache.getCacheManager().removeCache(ontologyCache.getCacheAlias());
        TermCache.getCacheManager().removeCache(termCache.getCacheAlias());
        ImageInstanceCache.getCacheManager().removeCache(imageInstanceCache.getCacheAlias());
        AbstractAnnotationCache.getCacheManager().removeCache(abstractAnnotationCache.getCacheAlias());
        UserJobCache.getCacheManager().removeCache(userJobCache.getCacheAlias());
    }
}
