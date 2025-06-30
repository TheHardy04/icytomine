package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.AbstractAnnotationTable.AnnotationDoubleClickListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.AbstractAnnotationTable.AnnotationSelectionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.actions.AbstractAnnotationActionPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.filters.AbstractAnnotationFilteringPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.layers.AnnotationLayer;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.layers.AnnotationLayerSelector;

import icy.gui.dialog.ConfirmDialog;
import icy.gui.dialog.MessageDialog;

public class AbstractAnnotationManagerPanel extends JPanel
{
    private static final long serialVersionUID = -1103632676301490194L;

    public interface AnnotationLayerVisibilityListener
    {
        void annotationLayerVisibilityChanged(Map<AnnotationLayer, Boolean> newVisibleLayers);
    }

    public interface AnnotationTermCommitListener
    {
        void annotationTermCommited(Set<AbstractAnnotation> annotations);
    }

    public interface AnnotationDeletionListener
    {
        void annotationsDeleted(Set<AbstractAnnotation> selectedAnnotations);
    }

    public interface AnnotationsVisibilityListener
    {
        void annotationsVisibiliyChanged(Set<AbstractAnnotation> newVisibleAnnotations);
    }

    /**
     * Creates the panel.
     */
    public AbstractAnnotationManagerPanel()
    {
        setupUI();
        setupListeners();
    }

    private void setupUI()
    {
        setGridBagLayout();
        addLayerSelector();
        addFilterSelector();
        addAnnotationTable();
        addActionsPanel();
    }

    private void setGridBagLayout()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
    }

    private AnnotationLayerSelector annotationLayerSelector;

    private void addLayerSelector()
    {
        annotationLayerSelector = new AnnotationLayerSelector();
        GridBagConstraints gbc_annotationLayerSelector = new GridBagConstraints();
        gbc_annotationLayerSelector.insets = new Insets(0, 0, 5, 0);
        gbc_annotationLayerSelector.fill = GridBagConstraints.BOTH;
        gbc_annotationLayerSelector.gridx = 0;
        gbc_annotationLayerSelector.gridy = 0;
        add(annotationLayerSelector, gbc_annotationLayerSelector);
    }

    private AbstractAnnotationFilteringPanel annotationFilteringPanel;

    private void addFilterSelector()
    {
        annotationFilteringPanel = new AbstractAnnotationFilteringPanel();
        GridBagConstraints gbc_annotationFilteringPanel = new GridBagConstraints();
        gbc_annotationFilteringPanel.insets = new Insets(0, 0, 5, 0);
        gbc_annotationFilteringPanel.fill = GridBagConstraints.BOTH;
        gbc_annotationFilteringPanel.gridx = 0;
        gbc_annotationFilteringPanel.gridy = 1;
        add(annotationFilteringPanel, gbc_annotationFilteringPanel);
    }

    private AbstractAnnotationTable abstractAnnotationTable;

    private void addAnnotationTable()
    {
        abstractAnnotationTable = new AbstractAnnotationTable();
        GridBagConstraints gbc_abstractAnnotationTable = new GridBagConstraints();
        gbc_abstractAnnotationTable.insets = new Insets(0, 0, 5, 0);
        gbc_abstractAnnotationTable.fill = GridBagConstraints.BOTH;
        gbc_abstractAnnotationTable.gridx = 0;
        gbc_abstractAnnotationTable.gridy = 2;
        add(abstractAnnotationTable, gbc_abstractAnnotationTable);
    }

    private AbstractAnnotationActionPanel abstractAnnotationActionPanel;

    private void addActionsPanel()
    {
        abstractAnnotationActionPanel = new AbstractAnnotationActionPanel();
        GridBagConstraints gbc_abstractAnnotationActionPanel = new GridBagConstraints();
        gbc_abstractAnnotationActionPanel.fill = GridBagConstraints.BOTH;
        gbc_abstractAnnotationActionPanel.gridx = 0;
        gbc_abstractAnnotationActionPanel.gridy = 3;
        add(abstractAnnotationActionPanel, gbc_abstractAnnotationActionPanel);
    }

    private List<AnnotationLayerVisibilityListener> annotationLayersVisibilityListeners;
    private List<AnnotationsVisibilityListener> annotationsVisibilitylisteners;
    private List<AnnotationTermCommitListener> annotationTermSelectionCommitListeners;
    private List<AnnotationDeletionListener> annotationDeletionListeners;

    private void setupListeners()
    {
        this.annotationLayersVisibilityListeners = new ArrayList<>();
        this.annotationsVisibilitylisteners = new ArrayList<>();
        this.annotationTermSelectionCommitListeners = new ArrayList<>();
        this.annotationDeletionListeners = new ArrayList<>();

        annotationLayerSelector.addItemChangeListner(this::targetAnnotationLayersUpdated);

        annotationFilteringPanel
                .addAnnotationFilterUpdateListener(this::targetAnnotationsFilteringUpdated);

        abstractAnnotationTable.addAnnotationVisibilityListener(
                this::annotationVisibilityChanged);

        abstractAnnotationActionPanel.addAnnotationTermAssociationListener(this::onAnnotationTermChangeRequested);
        abstractAnnotationActionPanel.addAnnotationDeleteListener(this::onAnnotationDeletionRequested);
    }

    private void targetAnnotationLayersUpdated(ItemEvent event)
    {
        annotationLayersVisibilityListeners
                .forEach(l -> l.annotationLayerVisibilityChanged(annotationLayerSelector.getAnnotationLayerStates()));
    }

    private void targetAnnotationsFilteringUpdated(Set<AbstractAnnotation> filteredAnnotations)
    {
        Map<AbstractAnnotation, Boolean> currentVisibility = abstractAnnotationTable.getAnnotationVisibilityMap();
        Map<AbstractAnnotation, Boolean> newAnnotationVisibility =
                createAnnotationVisibility(filteredAnnotations, currentVisibility);
        abstractAnnotationTable.setAnnotationVisibility(newAnnotationVisibility);
        Set<AbstractAnnotation> visibleAnnotations = newAnnotationVisibility.entrySet().stream()
                .filter(Entry::getValue)
                .map(Entry::getKey)
                .collect(Collectors.toSet());
        notifyAnnotationsVisibilityListeners(visibleAnnotations);
    }

    private Map<AbstractAnnotation, Boolean> createAnnotationVisibility(Set<AbstractAnnotation> filteredAnnotations,
            Map<AbstractAnnotation, Boolean> currentVisibility)
    {
        Map<AbstractAnnotation, Boolean> newAnnotationVisibility = filteredAnnotations.stream()
                .collect(Collectors.toMap(Function.identity(), a -> true));
        return newAnnotationVisibility;
    }

    private void annotationVisibilityChanged(AbstractAnnotation annotation, boolean visible)
    {
        notifyAnnotationsVisibilityListeners(this.abstractAnnotationTable.getAnnotationVisibilityMap().entrySet()
                .stream().filter(Entry::getValue).map(Entry::getKey).collect(Collectors.toSet()));
    }

    private void notifyAnnotationsVisibilityListeners(Set<AbstractAnnotation> newVisibleAnnotations)
    {
        annotationsVisibilitylisteners.forEach(l -> l.annotationsVisibiliyChanged(newVisibleAnnotations));
    }

    public void setTargetImage(Image image)
    {
        annotationFilteringPanel.setTargetImage(image);
        abstractAnnotationActionPanel.setAvailableTerms(getImageAvailableTerms(image));
    }

    private Collection<Term> getImageAvailableTerms(Image image)
    {
        try
        {
            return image.getProject().getOntology().getTerms(false);
        }
        catch (CytomineClientException e)
        {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void setTargetAnnotations(Map<AbstractAnnotation, Boolean> targetAnnotationVisibilities)
    {
        abstractAnnotationTable.setAnnotationVisibility(targetAnnotationVisibilities);
        annotationFilteringPanel.setTargetAnnotations(new HashSet<>(targetAnnotationVisibilities.keySet()));
    }

    public void addTargetAnnotations(Map<AbstractAnnotation, Boolean> annotationVisibilitiesToAdd)
    {
        Map<AbstractAnnotation, Boolean> visibilities =
                new HashMap<>(abstractAnnotationTable.getAnnotationVisibilityMap());
        annotationVisibilitiesToAdd.entrySet().stream().forEach(e -> {
            visibilities.put(e.getKey(), e.getValue());
        });
        abstractAnnotationTable.setAnnotationVisibility(visibilities);
        annotationFilteringPanel.setTargetAnnotations(new HashSet<>(visibilities.keySet()));
    }

    public void removeTargetAnnotations(Set<AbstractAnnotation> annotationToRemove)
    {
        Map<AbstractAnnotation, Boolean> visibilities =
                new HashMap<>(abstractAnnotationTable.getAnnotationVisibilityMap());
        annotationToRemove.stream().forEach(a -> {
            visibilities.remove(a);
        });
        abstractAnnotationTable.setAnnotationVisibility(visibilities);
        annotationFilteringPanel.setTargetAnnotations(new HashSet<>(visibilities.keySet()));
    }

    public void setLayers(List<AnnotationLayer> layers)
    {
        annotationLayerSelector.setLayers(layers.stream().toArray(AnnotationLayer[]::new), l -> l.getName());
    }

    public void setVisibleAnnotations(Set<AbstractAnnotation> visibleAnnotations)
    {
        Map<AbstractAnnotation, Boolean> visibilityMap =
                new HashMap<>(abstractAnnotationTable.getAnnotationVisibilityMap());
        visibilityMap.entrySet().forEach(e -> {
            if (visibleAnnotations.contains(e.getKey()))
            {
                e.setValue(true);
            }
            else
            {
                e.setValue(false);
            }
        });

        abstractAnnotationTable.setAnnotationVisibility(visibilityMap);
    }

    public void setSelectedAnnotations(Set<AbstractAnnotation> selectedAnnotations)
    {
        abstractAnnotationTable.setSelectedAnnotations(selectedAnnotations);
    }

    public void addAnnotationLayersVisibilityListener(AnnotationLayerVisibilityListener listener)
    {
        this.annotationLayersVisibilityListeners.add(listener);
    }

    public void removeAnnotationLayersVisibilityListener(AnnotationLayerVisibilityListener listener)
    {
        this.annotationLayersVisibilityListeners.remove(listener);
    }

    public void addAnnotationsVisibilityListener(AnnotationsVisibilityListener listener)
    {
        this.annotationsVisibilitylisteners.add(listener);
    }

    public void removeAnnotationsVisibilityListener(AnnotationsVisibilityListener listener)
    {
        this.annotationsVisibilitylisteners.remove(listener);
    }

    public void addAnnotationTermSelectionCommitListener(AnnotationTermCommitListener listener)
    {
        this.annotationTermSelectionCommitListeners.add(listener);
    }

    public void removeAnnotationTermSelectionCommitListener(AnnotationTermCommitListener listener)
    {
        this.annotationTermSelectionCommitListeners.remove(listener);
    }

    public void addAnnotationDeletionListener(AnnotationDeletionListener listener)
    {
        this.annotationDeletionListeners.add(listener);
    }

    public void removeAnnotationDeletionListener(AnnotationDeletionListener listener)
    {
        this.annotationDeletionListeners.remove(listener);
    }

    public void addAnnotationSelectionListener(AnnotationSelectionListener selectionListener)
    {
        abstractAnnotationTable.addAnnotationSelectionListener(selectionListener);
    }

    public void removeAnnotationSelectionListener(AnnotationSelectionListener selectionListener)
    {
        abstractAnnotationTable.removeAnnotationSelectionListener(selectionListener);
    }

    public void addAnnotationDoubleClickListener(AnnotationDoubleClickListener listener)
    {
        abstractAnnotationTable.addAnnotationDoubleClickListener(listener);
    }

    public void removeAnnotationDoubleClickListener(AnnotationDoubleClickListener listener)
    {
        abstractAnnotationTable.removeAnnotationDoubleClickListener(listener);
    }

    private void onAnnotationTermChangeRequested(Set<Term> selectedTerms)
    {
        Set<AbstractAnnotation> selectedAnnotations = abstractAnnotationTable.getSelectedAnnotations();
        Set<Term> terms = abstractAnnotationActionPanel.getAvailableTerms();
        Map<Term, Boolean> termAssociation =
                terms.stream().collect(Collectors.toMap(Function.identity(), a -> selectedTerms.contains(a)));
        selectedAnnotations.forEach(a -> {
            a.associateTerms(termAssociation);
        });
        annotationTermSelectionCommitListeners.forEach(l -> l.annotationTermCommited(selectedAnnotations));
    }

    private void onAnnotationDeletionRequested(ActionEvent event)
    {
        Set<AbstractAnnotation> selectedAnnotations = abstractAnnotationTable.getSelectedAnnotations();

        if (selectedAnnotations.isEmpty())
        {
            MessageDialog.showDialog("Deleting annotations - Icytomine", "No annotations selected",
                    MessageDialog.WARNING_MESSAGE);
            return;
        }

        boolean confirmation = ConfirmDialog.confirm("Deleting annotations - Icytomine",
                "Are you sure to delete the annotations?", ConfirmDialog.YES_NO_OPTION);

        if (confirmation)
        {
            AbstractAnnotation anAnnotation = selectedAnnotations.iterator().next();
            Image image = anAnnotation.getImage();
            try
            {
                image.removeAbstractAnnotations(selectedAnnotations);
                System.out.format("Erased %s annotations\n", selectedAnnotations.size());
            }
            catch (CytomineClientException e)
            {
                e.printStackTrace();
                MessageDialog.showDialog("Deleting annotations - Icytomine", "Could not remove annotations.",
                        MessageDialog.ERROR_MESSAGE);
                return;
            }
            finally
            {
                notifyAnnotationDeletion(selectedAnnotations);
            }
        }
    }

    private void notifyAnnotationDeletion(Set<AbstractAnnotation> selectedAnnotations)
    {
        annotationDeletionListeners.forEach(listener -> listener.annotationsDeleted(selectedAnnotations));
    }
}
