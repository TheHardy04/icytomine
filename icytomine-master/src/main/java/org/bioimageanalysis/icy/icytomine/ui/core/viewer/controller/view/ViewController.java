package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view;

import java.awt.geom.Rectangle2D;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.AbstractAnnotationTable.AnnotationSelectionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.CachedViewController.PositionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.CachedViewController.ResolutionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.ViewProvider;

public interface ViewController
{
    void addResolutionListener(ResolutionListener listener);

    void addCursorPositionListener(PositionListener listener);

    void addAnnotationSelectionListener(AnnotationSelectionListener listener);

    void zoomIn();

    void zoomOut();

    void setResolution(double resolutionLevel);

    Image getImageInformation();

    void adjustImageZoomToView();

    void refreshView();

    void stopView();

    void updateAnnotations();

    void setTargetAnnotations(Set<? extends AbstractAnnotation> annotations);

    void addTargetAbstractAnnotations(Set<? extends AbstractAnnotation> annotationsToAdd);

    void removeTargetAbstractAnnotations(Set<? extends AbstractAnnotation> annotationsToRemove);

    Set<AbstractAnnotation> getTargetAnnotations();

    Set<AbstractAnnotation> getActiveAnnotations();

    void setVisibileAnnotations(Set<? extends AbstractAnnotation> newVisibleAnnotations);

    void addVisibileAbstractAnnotations(Set<AbstractAnnotation> visibleAnnotationsToAdd);

    Set<AbstractAnnotation> getVisibleAnnotations();

    void setSelectedAnnotations(Set<? extends AbstractAnnotation> selectedAnnotations);

    Set<AbstractAnnotation> getSelectedAnnotations();

    Rectangle2D getCurrentViewBoundsAtZeroResolution();

    double getCurrentResolution();

    ViewProvider getViewProvider();

    void focusOnAnnotation(AbstractAnnotation a);

}
