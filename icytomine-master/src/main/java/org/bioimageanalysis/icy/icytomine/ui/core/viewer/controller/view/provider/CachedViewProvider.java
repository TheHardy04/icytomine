package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.CachedAbstractAnnotationView;
import org.bioimageanalysis.icy.icytomine.core.view.CachedImageView;
import org.bioimageanalysis.icy.icytomine.core.view.ViewListener;

public class CachedViewProvider extends ViewProvider
{
    private CachedImageView cachedView;
    private CachedAbstractAnnotationView annotationView;
    private Set<ViewListener> viewListeners;

    private BufferedImage currentImageView;
    private BufferedImage currentAnnotationView;

    public CachedViewProvider(CachedImageView cachedView, CachedAbstractAnnotationView annotationView)
    {
        super();
        this.cachedView = cachedView;
        this.annotationView = annotationView;
        this.viewListeners = new HashSet<>();
        this.cachedView.addViewListener((BufferedImage... newViews) -> imageLayerUpdated(newViews));
        this.annotationView.addViewListener((BufferedImage... newViews) -> annotationLayerUpdated(newViews));
    }

    @Override
    public void addViewListener(ViewListener listener)
    {
        viewListeners.add(listener);
    }

    @Override
    public void addViewProcessListener(ViewProcessListener listener)
    {
        this.cachedView.addViewProcessListener(listener);
    }

    @Override
    public synchronized BufferedImage getView(Dimension canvasSize)
    {
        if (canvasSize.width == 0 || canvasSize.height == 0)
        {
            return new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        }
        return buildView(canvasSize);
    }

    private BufferedImage buildView(Dimension canvasSize)
    {
        currentImageView = cachedView.getView(getPosition(), canvasSize, getResolution());
        currentAnnotationView = annotationView.getView(getPosition(), canvasSize, getResolution());
        return currentImageView;
    }

    @Override
    public Image getImageInformation()
    {
        return cachedView.getImageInformation();
    }

    @Override
    public void stop()
    {
        this.cachedView.stop();
        this.annotationView.stop();
    }

    private void imageLayerUpdated(BufferedImage[] newViews)
    {
        currentImageView = newViews[0];
        notifyViewListeners();
    }

    private void annotationLayerUpdated(BufferedImage[] newViews)
    {
        currentAnnotationView = newViews[0];
        notifyViewListeners();
    }

    private void notifyViewListeners()
    {
        viewListeners.forEach(l -> l.onViewChanged(currentImageView, currentAnnotationView));
    }

    @Override
    public void setTargetAbstractAnnotaions(Set<? extends AbstractAnnotation> newTargetAnnotations)
    {
        annotationView.setTargetAnnotations(newTargetAnnotations);
        annotationView.forceViewRefresh();
    }

    @Override
    public void addTargetAbstractAnnotations(Set<? extends AbstractAnnotation> annotationsToAdd)
    {
        annotationView.addTargetAnnotations(annotationsToAdd);
    }

    @Override
    public void removeTargetAbstractAnnotations(Set<? extends AbstractAnnotation> annotationsToRemove)
    {
        annotationView.removeTargetAnnotations(annotationsToRemove);
    }

    @Override
    public Set<AbstractAnnotation> getTargetAbstractAnnotations()
    {
        return annotationView.getTargetAnnotations();
    }

    @Override
    public void setVisibleAbstractAnnotations(Set<? extends AbstractAnnotation> newVisibleAnnotations)
    {
        annotationView.setVisibleAnnotations(newVisibleAnnotations);
        annotationView.forceViewRefresh();
    }

    @Override
    public void addVisibleAbstractAnnotations(Set<? extends AbstractAnnotation> annotationsToAdd)
    {
        annotationView.addVisibleAnnotations(annotationsToAdd);
    }

    @Override
    public void removeVisibleAbstractAnnotations(Set<? extends AbstractAnnotation> annotationsToRemove)
    {
        annotationView.removeVisibleAnnotations(annotationsToRemove);
    }

    @Override
    public Set<AbstractAnnotation> getVisibleAbstractAnnotations()
    {
        return annotationView.getVisibleAnnotations();
    }

    @Override
    public Set<AbstractAnnotation> getActiveAbstractAnnotations()
    {
        return annotationView.getActiveAnnotations();
    }

    @Override
    public void setSelectedAbstractAnnotations(Set<? extends AbstractAnnotation> annotations)
    {
        annotationView.setSelectedAnnotations(annotations);
    }

    @Override
    public Set<AbstractAnnotation> getSelectedAbstractAnnotations()
    {
        return annotationView.getSelectedAnnotations();
    }

}
