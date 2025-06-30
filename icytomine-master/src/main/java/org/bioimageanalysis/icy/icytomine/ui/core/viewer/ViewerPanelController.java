package org.bioimageanalysis.icy.icytomine.ui.core.viewer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.AlgorithmAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.UserAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.UserJob;
// import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.AbstractAnnotationManagerPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.AbstractAnnotationTable.AnnotationSelectionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.layers.AnnotationLayer;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.layers.AnnotationLayer.LayerType;
// import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationManagerPanel;
// import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationManagerPanelController.AnnotationDeletionListener;
// import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationManagerPanelController.AnnotationTermCommitListener;
// import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationTable.AnnotationSelectionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.cytomine2Icy.CytomineToIcyPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.icy2Cytomine.file.IcyFileToCytominePanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.icy2Cytomine.folder.IcyFolderToCytominePanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.icy2Cytomine.sequence.IcySequenceToCytominePanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewController;

import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameAdapter;
import icy.gui.frame.IcyFrameEvent;

public class ViewerPanelController
{

    private ViewerPanel viewerContainer;
    private ViewController viewController;
    private IcyFrame annotationsFrame;
    private IcyFrame cytomineToIcyFrame;
    private IcyFrame icySequenceToCytomineFrame;
    private IcyFrame icyFileToCytomineFrame;
    private IcyFrame icyFolderToCytomineFrame;

    public ViewerPanelController(ViewerPanel viewerContainer)
    {
        this.viewerContainer = viewerContainer;
    }

    public void startViewer()
    {
        viewController = viewerContainer.getViewCanvasPanel().getViewController();
        setViewControllerListeners();
        setViewerContainerListeners();
        viewerContainer.setZoomLimit(getZoomLevel(0));

        Image targetImage = viewController.getImageInformation();
        viewController.adjustImageZoomToView();

        // At start show only current user annotations
        Set<UserAnnotation> initialTargetAnnotations = new HashSet<>(
                targetImage.getUserAnnotationsWithGeometry(targetImage.getClient().getCurrentUser(), true).values());
        viewController.setTargetAnnotations(initialTargetAnnotations);
        viewController.setVisibileAnnotations(initialTargetAnnotations);
    }

    private void setViewControllerListeners()
    {
        viewController.addCursorPositionListener(
                (Point2D newPosition) -> viewerContainer.setCursorPosition(newPosition,
                        getPositionInMicrons(newPosition)));
        viewController
                .addResolutionListener(
                        (double newResolution) -> viewerContainer.setZoomLevel(getZoomLevel(newResolution)));
        viewController.addAnnotationSelectionListener(getAnnotationSelectionHandler());
    }

    private Point2D getPositionInMicrons(Point2D position)
    {
        double pixelSize = viewController.getImageInformation().getResolution().orElse(1d);
        return new Point2D.Double(position.getX() * pixelSize, position.getY() * pixelSize);
    }

    private double getZoomLevel(double resolutionLevel)
    {
        int intMagnification = viewController.getImageInformation().getMagnification().orElse(1);
        double magnification = intMagnification;
        magnification /= Math.pow(2d, resolutionLevel);
        return magnification;
    }

    private AnnotationSelectionListener getAnnotationSelectionHandler()
    {
        return (Set<AbstractAnnotation> selectedAnnotations) -> {
            if (annotationsFrame != null)
            {
                ((AbstractAnnotationManagerPanel) annotationsFrame.getContentPane())
                        .setSelectedAnnotations(selectedAnnotations);
            }
        };
    }

    private void setViewerContainerListeners()
    {
        viewerContainer.addZoomInListener((ActionEvent e) -> {
            viewController.zoomIn();
        });
        viewerContainer.addZoomOutListener((ActionEvent e) -> {
            viewController.zoomOut();
        });
        viewerContainer
                .addZoomLevelSelectedListener(zoomLevel -> viewController.setResolution(getResolutionLevel(zoomLevel)));
        viewerContainer.addAnnotationFilterMenuListener(getAnnotationMenuHandler());
        viewerContainer.addAnnotationRefreshMenuListener(getAnnotationRefreshMenuHandler());
        viewerContainer.addCytomineToIcyMenuListener(getCytomineToIcyMenuHandler());
        viewerContainer.addIcySequenceToCytomineMenuListener(getIcySequenceToCytomineMenuHandler());
        viewerContainer.addIcyFileToCytomineMenuListener(getIcyFileToCytomineMenuHandler());
        viewerContainer.addIcyFolderToCytomineMenuListener(getIcyFolderToCytomineMenuHandler());
    }

    private double getResolutionLevel(double zoomLevel)
    {
        int intMagnification = viewController.getImageInformation().getMagnification().orElse(1);
        double magnification = intMagnification;
        return Math.log(magnification / zoomLevel) / Math.log(2);
    }

    private ActionListener getAnnotationMenuHandler()
    {
        return e -> {
            System.out.println("Opening annotations menu...");
            if (annotationsFrame != null)
            {
                AbstractAnnotationManagerPanel annotationsPanel =
                        (AbstractAnnotationManagerPanel) annotationsFrame.getContentPane();
                annotationsFrame.close();

                annotationsPanel.removeAnnotationLayersVisibilityListener(this::onLayersVisibilityChanged);
                annotationsPanel.removeAnnotationsVisibilityListener(this::onAnnotationVisibilityChanged);
                annotationsPanel.removeAnnotationSelectionListener(this::onAnnotationSelectionChanged);
                annotationsPanel.removeAnnotationDoubleClickListener(this::onAnnotationFocusRequested);
                annotationsPanel.removeAnnotationTermSelectionCommitListener(this::onAnnotationTermCommited);
                annotationsPanel.removeAnnotationDeletionListener(this::onAnnotationDeleted);
            }

            AbstractAnnotationManagerPanel annotationsPanel = new AbstractAnnotationManagerPanel();
            annotationsPanel.addAnnotationLayersVisibilityListener(this::onLayersVisibilityChanged);
            annotationsPanel.addAnnotationsVisibilityListener(this::onAnnotationVisibilityChanged);
            annotationsPanel.addAnnotationSelectionListener(this::onAnnotationSelectionChanged);
            annotationsPanel.addAnnotationDoubleClickListener(this::onAnnotationFocusRequested);
            annotationsPanel.addAnnotationTermSelectionCommitListener(this::onAnnotationTermCommited);
            annotationsPanel.addAnnotationDeletionListener(this::onAnnotationDeleted);

            Image targetImage = viewController.getImageInformation();
            annotationsPanel.setTargetImage(targetImage);

            annotationsPanel.setLayers(createAnnotationLayers(targetImage));

            Set<AbstractAnnotation> visibleAnnotations = viewController.getVisibleAnnotations();
            Set<AbstractAnnotation> selectedAnnotations = viewController.getSelectedAnnotations();
            Map<AbstractAnnotation, Boolean> targetAnnotationVisibilities =
                    viewController.getTargetAnnotations().stream()
                            .collect(Collectors.toMap(Function.identity(), a -> visibleAnnotations.contains(a)));
            annotationsPanel.setTargetAnnotations(targetAnnotationVisibilities);
            annotationsFrame = createIcyDialog("Annotations - Icytomine", annotationsPanel, true);
            annotationsFrame.setSize(new Dimension(400, 400));
            // set elements already selected
            annotationsPanel.setSelectedAnnotations(selectedAnnotations);
            annotationsFrame.setVisible(true);
        };
    }

    private List<AnnotationLayer> createAnnotationLayers(Image targetImage)
    {
        // User layers;
        Map<User, List<UserAnnotation>> userLayers = targetImage.getClient().getImageUserAnnotations(targetImage)
                .values().stream().collect(Collectors.groupingBy(a -> a.getUser()));
        // Algorithm layers;
        Map<UserJob, List<AlgorithmAnnotation>> algoLayers =
                targetImage.getClient().getImageAlgorithmAnnotations(targetImage).values().stream()
                        .collect(Collectors.groupingBy(a -> a.getUserJob()));
        // TODO add reviewed layer

        List<AnnotationLayer> layers = new ArrayList<>(userLayers.size() + algoLayers.size());
        userLayers.entrySet().stream().forEach(e -> layers.add(
                new AnnotationLayer(e.getKey().getName().orElse("Unknown"), LayerType.USER, e.getKey(),
                        new HashSet<>(e.getValue()))));
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy h:mm:ss a");
        algoLayers.entrySet().stream().forEach(e -> layers.add(
                new AnnotationLayer(
                        e.getKey().getName().orElse("Unknown") + " - "
                                + formatter.format(e.getKey().getDate().get()),
                        LayerType.ALGORITHM, e.getKey(),
                        new HashSet<>(e.getValue()))));

        return layers;
    }

    private void onLayersVisibilityChanged(Map<AnnotationLayer, Boolean> layersState)
    {
        Set<AnnotationLayer> visibleLayers =
                layersState.entrySet().stream().filter(Entry::getValue).map(Entry::getKey).collect(Collectors.toSet());

        // Make sure geometries are set for visible layers
        Set<User> users = visibleLayers.stream().filter(l -> l.getLayerType() == LayerType.USER)
                .map(l -> (User) l.getLayerTypeEntity()).collect(Collectors.toSet());
        Set<UserJob> userJobs = visibleLayers.stream().filter(l -> l.getLayerType() == LayerType.ALGORITHM)
                .map(l -> (UserJob) l.getLayerTypeEntity()).collect(Collectors.toSet());
        Map<Long, UserAnnotation> visibleUserAnnotations =
                viewController.getImageInformation().getUserAnnotationsWithGeometry(users);
        Map<Long, AlgorithmAnnotation> visibleAlgoAnnotations =
                viewController.getImageInformation().getAlgorithmAnnotationsWithGeometry(userJobs);

        Set<AbstractAnnotation> visibleAnnotations =
                new HashSet<>(visibleUserAnnotations.size() + visibleAlgoAnnotations.size());
        visibleAnnotations.addAll(visibleUserAnnotations.values());
        visibleAnnotations.addAll(visibleAlgoAnnotations.values());

        Set<AbstractAnnotation> nonVisibleAnnotations = layersState.entrySet().stream()
                .filter(e -> !e.getValue())
                .map(Entry::getKey)
                .reduce(new HashSet<AbstractAnnotation>(),
                        (annotations, layer) -> {
                            annotations.addAll(layer.getIncludedAnnotations());
                            return annotations;
                        }, (a1, a2) -> {
                            a1.addAll(a2);
                            return a1;
                        });

        viewController.addTargetAbstractAnnotations(visibleAnnotations);
        viewController.removeTargetAbstractAnnotations(nonVisibleAnnotations);
        viewController.addVisibileAbstractAnnotations(visibleAnnotations);

        Map<AbstractAnnotation, Boolean> annotationVisibilities =
                visibleAnnotations.stream().collect(Collectors.toMap(Function.identity(), a -> true));

        ((AbstractAnnotationManagerPanel) annotationsFrame.getContentPane())
                .addTargetAnnotations(annotationVisibilities);

        ((AbstractAnnotationManagerPanel) annotationsFrame.getContentPane())
                .removeTargetAnnotations(nonVisibleAnnotations);
        // viewController.setTargetAnnotations(visibleAnnotations);
        // ((AbstractAnnotationManagerPanel) annotationsFrame.getContentPane()).setVisibleAnnotations(visibleAnnotations);
    }

    private void onAnnotationVisibilityChanged(Set<AbstractAnnotation> newVisibleAnnotations)
    {
        viewController.setVisibileAnnotations(newVisibleAnnotations);
    }

    private void onAnnotationSelectionChanged(Set<AbstractAnnotation> selectedAnnotations)
    {
        viewController.setSelectedAnnotations(selectedAnnotations);
    }

    private void onAnnotationFocusRequested(AbstractAnnotation a)
    {
        viewController.focusOnAnnotation(a);
    }

    private void onAnnotationTermCommited(Set<AbstractAnnotation> annotations)
    {
        Set<AbstractAnnotation> annotationsToSelect = new HashSet<>(viewController.getSelectedAnnotations());
        annotationsToSelect.addAll(annotations);

        viewController.getViewProvider().addTargetAbstractAnnotations(annotations);
        Map<AbstractAnnotation, Boolean> visibilities =
                annotations.stream().collect(Collectors.toMap(Function.identity(), a -> true));

        ((AbstractAnnotationManagerPanel) annotationsFrame.getContentPane()).addTargetAnnotations(visibilities);
        ((AbstractAnnotationManagerPanel) annotationsFrame.getContentPane()).setSelectedAnnotations(annotationsToSelect);
    }

    private void onAnnotationDeleted(Set<AbstractAnnotation> annotations)
    {
        viewController.removeTargetAbstractAnnotations(annotations);
        ((AbstractAnnotationManagerPanel) annotationsFrame.getContentPane()).removeTargetAnnotations(annotations);
    }

    private ActionListener getAnnotationRefreshMenuHandler()
    {
        return event -> {
            // TODO fix annotation refresh implementation
            // viewController.updateAnnotations(true);
            viewController.refreshView();
        };
    }

    private static IcyFrame createIcyDialog(String title, JPanel contentPane, boolean resizable)
    {
        IcyFrame frame = new IcyFrame(title, resizable, true, false, false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(10, 10));
        frame.setContentPane(contentPane);
        frame.setSize(contentPane.getPreferredSize());
        frame.addToDesktopPane();
        frame.center();
        frame.setAlwaysOnTop(true);
        return frame;
    }

    private ActionListener getCytomineToIcyMenuHandler()
    {
        return e -> {
            System.out.println("Opening cytomine -> icy dialog...");
            if (cytomineToIcyFrame != null)
            {
                cytomineToIcyFrame.close();
            }

            CytomineToIcyPanel contentPane = new CytomineToIcyPanel(viewController);
            cytomineToIcyFrame = createIcyDialog("Download view from Cytomine - Icytomine", contentPane, false);
            contentPane.addCloseListener(a -> cytomineToIcyFrame.close());
            cytomineToIcyFrame.addFrameListener(new IcyFrameAdapter()
            {
                @Override
                public void icyFrameClosed(IcyFrameEvent e)
                {
                    contentPane.getController().close();
                }
            });
            cytomineToIcyFrame.setVisible(true);
        };
    }

    private ActionListener getIcySequenceToCytomineMenuHandler()
    {
        return e -> {
            System.out.println("Opening icy sequence -> cytomine dialog...");
            if (icySequenceToCytomineFrame != null)
            {
                icySequenceToCytomineFrame.close();
            }

            IcySequenceToCytominePanel contentPane = new IcySequenceToCytominePanel(viewController);
            icySequenceToCytomineFrame =
                    createIcyDialog("Send Sequence ROIs to Cytomine - Icytomine", contentPane, false);
            contentPane.addCloseListener(a -> icySequenceToCytomineFrame.close());
            icySequenceToCytomineFrame.addFrameListener(new IcyFrameAdapter()
            {
                @Override
                public void icyFrameClosed(IcyFrameEvent e)
                {
                    contentPane.getController().close();
                }
            });
            icySequenceToCytomineFrame.setVisible(true);
        };
    }

    private ActionListener getIcyFileToCytomineMenuHandler()
    {
        return e -> {
            System.out.println("Opening icy file -> cytomine dialog...");
            if (icyFileToCytomineFrame != null)
            {
                icyFileToCytomineFrame.close();
            }

            IcyFileToCytominePanel contentPane = new IcyFileToCytominePanel(viewController);
            icyFileToCytomineFrame = createIcyDialog("Send File ROIs to Cytomine - Icytomine", contentPane, false);
            contentPane.getController().addCloseListener(a -> icyFileToCytomineFrame.close());
            icyFileToCytomineFrame.addFrameListener(new IcyFrameAdapter()
            {
                @Override
                public void icyFrameClosed(IcyFrameEvent e)
                {
                    contentPane.getController().close();
                }
            });
            icyFileToCytomineFrame.setVisible(true);
        };
    }

    private ActionListener getIcyFolderToCytomineMenuHandler()
    {
        return e -> {
            System.out.println("Opening icy folder -> cytomine dialog...");
            if (icyFolderToCytomineFrame != null)
            {
                icyFolderToCytomineFrame.close();
            }

            IcyFolderToCytominePanel contentPane = new IcyFolderToCytominePanel(viewController);
            icyFolderToCytomineFrame = createIcyDialog("Send Folder ROIs to Cytomine - Icytomine", contentPane, false);
            contentPane.getController().addCloseListener(a -> icyFolderToCytomineFrame.close());
            icyFolderToCytomineFrame.addFrameListener(new IcyFrameAdapter()
            {
                @Override
                public void icyFrameClosed(IcyFrameEvent e)
                {
                    contentPane.getController().close();
                }
            });
            icyFolderToCytomineFrame.setVisible(true);
        };
    }

    public void stopViewer()
    {
        closeFrame(annotationsFrame);
        closeFrame(cytomineToIcyFrame);
        closeFrame(icySequenceToCytomineFrame);
        closeFrame(icyFileToCytomineFrame);
        closeFrame(icyFolderToCytomineFrame);

        viewController.stopView();
    }

    private void closeFrame(IcyFrame frame)
    {
        if (frame != null)
            frame.close();
    }
}
