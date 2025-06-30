/**
 * 
 */
package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;

import danyfel80.common.stream.StreamUtils;

/**
 * Annotation table model for annotation tables.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class AbstractAnnotationTableModel extends AbstractTableModel
{

    private static final long serialVersionUID = -6011058852592614856L;

    private static final String[] columnNames = new String[] {"Visible", "Name", "Terms", "Author", "Type"};

    /**
     * Listener that handles annotation visibility events.
     * 
     * @author Daniel Felipe Gonzalez Obando
     */
    public interface AnnotationVisibilityListener
    {
        void onAnnotationVisibilityChanged(AbstractAnnotation annotation, boolean visible);
    }

    private List<AbstractAnnotation> annotations;
    private Map<AbstractAnnotation, Boolean> visibilityForAnnotation;
    private List<AnnotationVisibilityListener> annotationVisibilityListeners;

    public AbstractAnnotationTableModel(Map<? extends AbstractAnnotation, Boolean> visibilityForAnnotation)
    {
        this.annotations = new ArrayList<>(visibilityForAnnotation.keySet());
        this.visibilityForAnnotation = new ConcurrentHashMap<>(visibilityForAnnotation);
        this.annotationVisibilityListeners = new ArrayList<>();
        this.addTableModelListener(this::onTableValuesChanged);
    }

    @Override
    public String getColumnName(int column)
    {
        return (0 <= column && column < columnNames.length) ? columnNames[column] : null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        switch (columnIndex)
        {
            case 0:
                return Boolean.class;
            case 1:
                return String.class;
            case 2:
                return List.class;
            case 3:
                return String.class;
            case 4:
                return String.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return columnIndex == 0 && 0 <= rowIndex && rowIndex < annotations.size();
    }

    @Override
    public int getRowCount()
    {
        return annotations.size();
    }

    @Override
    public int getColumnCount()
    {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (0 <= rowIndex && rowIndex < annotations.size())
        {
            switch (columnIndex)
            {
                case 0:
                    return isAnnotationVisible(rowIndex);
                case 1:
                    return getAnnotationId(rowIndex);
                case 2:
                    return getAnnotationTerms(rowIndex);
                case 3:
                    return getAnnotationAuthor(rowIndex);
                case 4:
                    return getAnnotationType(rowIndex);
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * Checks whether an annotation is visible in the table given the row index.
     * 
     * @param rowIndex
     *        Index of the row in the table.
     * @return {@code true} if the annotation is visible. {@code false} otherwise.
     */
    public Boolean isAnnotationVisible(int rowIndex)
    {
        return visibilityForAnnotation.get(getAnnotationAt(rowIndex));
    }

    /**
     * Retrieves the annotation identifier of the annotation located at the given row index.
     * 
     * @param rowIndex
     *        Index of the row in the table.
     * @return The annotation identifier.
     */
    public Long getAnnotationId(int rowIndex)
    {
        return getAnnotationAt(rowIndex).getId();
    }

    /**
     * Retrieves the list of terms associated to the annotation at the given row index.
     * 
     * @param rowIndex
     *        Index of the row in the table.
     * @return The list of terms associated to the annotation.
     */
    public List<String> getAnnotationTerms(int rowIndex)
    {
        try
        {
            return getAnnotationAt(rowIndex).getAssociatedTerms().stream()
                    .map(StreamUtils.wrapFunction(t -> t.getName().orElse("Not specified")))
                    .collect(Collectors.toList());
        }
        catch (CytomineClientException e)
        {
            return new ArrayList<>(0);
        }
    }

    /**
     * Retrieves the author user name of the annotation at the given row index.
     * 
     * @param rowIndex
     *        Index of the row in the table.
     * @return The user name of the annotation author.
     */
    public String getAnnotationAuthor(int rowIndex)
    {
        try
        {
            return getAnnotationAt(rowIndex).getUser().getName().orElse("Not specified");
        }
        catch (CytomineClientException e)
        {
            return "Unknown";
        }
    }

    /**
     * Retrieves the type of the annotation at the given row index.
     * 
     * @param rowIndex
     *        Index of the row in the table.
     * @return The annotation type. null if the row is not valid.
     */
    public String getAnnotationType(int rowIndex)
    {
        if (0 <= rowIndex && rowIndex < annotations.size())
            return getAnnotationAt(rowIndex).getClass().getSimpleName();
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if (columnIndex == 0)
        {
            visibilityForAnnotation.put(getAnnotationAt(rowIndex), (Boolean) aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    /**
     * Adds a listener to handle annotation visibility change events.
     * 
     * @param listener
     *        The event handler
     */
    public void addAnnotationVisibilityListener(AnnotationVisibilityListener listener)
    {
        synchronized (annotationVisibilityListeners)
        {
            this.annotationVisibilityListeners.add(listener);
        }
    }

    /**
     * removes a listener to handle annotation visibility change events.
     * 
     * @param listener
     *        The event handler
     */
    public void removeAnnotationVisibilityListener(AnnotationVisibilityListener listener)
    {
        synchronized (annotationVisibilityListeners)
        {
            this.annotationVisibilityListeners.remove(listener);
        }
    }

    private void onTableValuesChanged(TableModelEvent event)
    {
        if (event.getType() == TableModelEvent.UPDATE)
        {
            notifyAnnotationVisibilityChanged(event.getFirstRow());
        }
    }

    private void notifyAnnotationVisibilityChanged(int annotationIndex)
    {
        if (annotations.size() > 0)
        {
            AbstractAnnotation annotation = getAnnotationAt(annotationIndex);
            this.annotationVisibilityListeners
                    .forEach(listener -> listener.onAnnotationVisibilityChanged(annotation,
                            visibilityForAnnotation.get(annotation)));
        }
    }

    /**
     * Checks whether an annotation is visible in the annotation table.
     * 
     * @param annotation
     *        The annotation to check.
     * @return true if the annotation is present on the table and if it is checked as visible. false otherwise.
     */
    public boolean isAnnotationVisible(AbstractAnnotation annotation)
    {
        return visibilityForAnnotation.getOrDefault(annotation, false);
    }

    /**
     * @return The set of annotations present on the table.
     */
    public Set<AbstractAnnotation> getAnnotations()
    {
        return new HashSet<>(annotations);
    }

    /**
     * @return The visibility map for annotations on this table.
     */
    public Map<AbstractAnnotation, Boolean> getAnnotationVisibility()
    {
        return Collections.unmodifiableMap(visibilityForAnnotation);
    }

    /**
     * @return The set of visible annotations in the table.
     */
    public Set<AbstractAnnotation> getVisibleAnnotations()
    {
        return this.visibilityForAnnotation.entrySet()
                .stream()
                .filter(e -> e.getValue())
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
    }

    /**
     * @param rowIndex
     *        The row index of the annotation.
     * @return The annotation at the given index.
     */
    public AbstractAnnotation getAnnotationAt(int rowIndex) throws IndexOutOfBoundsException
    {
        return annotations.get(rowIndex);
    }

}
