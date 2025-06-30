package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.ViewListener;

/**
 * Instances of this class should provide a view of a given cytomine image on
 * demand. View change listeners can be added to a provider to refresh the view.
 * The view status events can be handled as well with listeners. Annotation
 * visibility can be handled from this class by setting the visible annotations.
 * Active annotations (those that are visible and presented on the current view)
 * can be queried.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public abstract class ViewProvider
{

    public interface ViewProcessListener
    {
        void onViewProcessEvent(boolean isProcessing);
    }

    /**
     * Position of the view at zero resolution.
     */
    private Point2D position;
    /**
     * Resolution used to provide views.
     */
    private double resolutionLevel;

    public ViewProvider()
    {
        position = new Point2D.Double();
        resolutionLevel = 0d;
    }

    public Point2D getPosition()
    {
        return position;
    }

    public void setPosition(Point2D position)
    {
        this.position.setLocation(position);
    }

    public double getResolution()
    {
        return resolutionLevel;
    }

    public void setResolutionLevel(double resolutionLevel)
    {
        this.resolutionLevel = resolutionLevel;
    }

    public abstract void addViewListener(ViewListener listener);

    public abstract BufferedImage getView(Dimension viewSize);

    public abstract void stop();

    public abstract void addViewProcessListener(ViewProcessListener listener);

    public abstract Image getImageInformation();

    public abstract void setTargetAbstractAnnotaions(Set<? extends AbstractAnnotation> newTargetAnnotations);

    public abstract void addTargetAbstractAnnotations(Set<? extends AbstractAnnotation> annotationsToAdd);

    public abstract void removeTargetAbstractAnnotations(Set<? extends AbstractAnnotation> annotationsToRemove);

    public abstract Set<AbstractAnnotation> getTargetAbstractAnnotations();

    public abstract void setVisibleAbstractAnnotations(Set<? extends AbstractAnnotation> annotations);

    public abstract void addVisibleAbstractAnnotations(Set<? extends AbstractAnnotation> annotationSet);

    public abstract void removeVisibleAbstractAnnotations(Set<? extends AbstractAnnotation> annotationSet);

    public abstract Set<AbstractAnnotation> getVisibleAbstractAnnotations();

    public abstract Set<AbstractAnnotation> getActiveAbstractAnnotations();

    public abstract void setSelectedAbstractAnnotations(Set<? extends AbstractAnnotation> annotations);

    public abstract Set<AbstractAnnotation> getSelectedAbstractAnnotations();

}
