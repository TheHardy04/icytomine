package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.AbstractAnnotationTableModel.AnnotationVisibilityListener;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class AbstractAnnotationTable extends JPanel
{
    private static final long serialVersionUID = -7027821039693155920L;

    public interface AnnotationSelectionListener
    {
        void selectionChanged(Set<AbstractAnnotation> selectedAnnotations);
    }

    public interface AnnotationDoubleClickListener
    {
        void annotationDoubleClicked(AbstractAnnotation annotation);
    }

    /**
     * Creates the table UI.
     */
    public AbstractAnnotationTable()
    {
        setupUI();
        setListenerLists();
        setAnnotationsInTable(Collections.emptyMap());
        setListenersInTable();
    }

    private void setupUI()
    {
        setGridBagLayout();
        addTable();
    }

    private void setGridBagLayout()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {224, 0};
        gridBagLayout.rowHeights = new int[] {100, 0};
        gridBagLayout.columnWeights = new double[] {1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
    }

    private JTable table;
    private JScrollPane tableScrollPane;

    private void addTable()
    {
        tableScrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 0;
        add(tableScrollPane, gbc_scrollPane);

        table = new JTable();
        tableScrollPane.setViewportView(table);
    }

    private List<AnnotationVisibilityListener> annotationVisibilityListeners;
    private List<AnnotationSelectionListener> annotationSelectionListeners;
    private List<AnnotationDoubleClickListener> annotationDoubleClickListeners;

    private void setListenerLists()
    {
        annotationVisibilityListeners = new ArrayList<>();
        annotationSelectionListeners = new ArrayList<>();
        annotationDoubleClickListeners = new ArrayList<>();
    }

    private void removeAnnotationVisibilityListenersFromTableModel()
    {
        if (tableModel != null)
        {
            annotationVisibilityListeners
                    .forEach(listener -> tableModel.removeAnnotationVisibilityListener(listener));
        }

    }

    private void addAnnotationVisibilityListenersFromTableModel()
    {
        if (tableModel != null)
        {
            annotationVisibilityListeners
                    .forEach(listener -> tableModel.addAnnotationVisibilityListener(listener));
        }
    }

    public void setAnnotationVisibility(Map<AbstractAnnotation, Boolean> annotationVisibility)
    {
        setAnnotationsInTable(annotationVisibility);
    }

    private AbstractAnnotationTableModel tableModel;

    private void setAnnotationsInTable(Map<AbstractAnnotation, Boolean> visibilityForAnnotation)
    {
        AbstractAnnotationTableModel tableModel = new AbstractAnnotationTableModel(visibilityForAnnotation);
        synchronized (table)
        {
            removeAnnotationVisibilityListenersFromTableModel();
            table.setModel(tableModel);
            this.tableModel = tableModel;
            tableModel.fireTableDataChanged();
            addAnnotationVisibilityListenersFromTableModel();
        }
    }

    public Map<AbstractAnnotation, Boolean> getAnnotationVisibilityMap()
    {
        return tableModel.getAnnotationVisibility();
    }

    private void setListenersInTable()
    {
        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting())
            {
                notifySelectionChange();
            }
        });
        table.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1)
                {
                    Point point = e.getPoint();
                    int row = table.rowAtPoint(point);
                    if (row != -1)
                    {
                        notifyDoubleClickSelection();
                    }
                }
            }
        });
    }

    private void notifySelectionChange()
    {
        Set<AbstractAnnotation> selectedAnnotations = getSelectedAnnotations();
        this.annotationSelectionListeners.forEach(l -> l.selectionChanged(selectedAnnotations));
    }

    public Set<AbstractAnnotation> getSelectedAnnotations()
    {
        ListSelectionModel selectionModel = table.getSelectionModel();
        Set<AbstractAnnotation> selectedAnnotations = new HashSet<>();
        if (!selectionModel.isSelectionEmpty())
        {
            int minIndex = selectionModel.getMinSelectionIndex();
            int maxIndex = selectionModel.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++)
            {
                if (selectionModel.isSelectedIndex(i))
                {
                    selectedAnnotations.add(((AbstractAnnotationTableModel) table.getModel()).getAnnotationAt(i));
                }
            }
        }

        return selectedAnnotations;
    }

    private void notifyDoubleClickSelection()
    {
        this.annotationDoubleClickListeners.forEach(l -> l.annotationDoubleClicked(getSelectedAnnotation()));
    }

    private AbstractAnnotation getSelectedAnnotation()
    {
        ListSelectionModel selectionModel = table.getSelectionModel();
        int rowIndex = selectionModel.getMinSelectionIndex();
        if (rowIndex != -1)
            return ((AbstractAnnotationTableModel) table.getModel()).getAnnotationAt(rowIndex);
        else
            return null;
    }

    public void addAnnotationVisibilityListener(AnnotationVisibilityListener listener)
    {
        synchronized (table)
        {
            annotationVisibilityListeners.add(listener);
            if (tableModel != null)
            {
                tableModel.addAnnotationVisibilityListener(listener);
            }
        }
    }

    public void removeAnnotationVisibilityListener(AnnotationVisibilityListener listener)
    {
        synchronized (table)
        {
            annotationVisibilityListeners.remove(listener);
            if (tableModel != null)
            {
                tableModel.removeAnnotationVisibilityListener(listener);
            }
        }
    }

    public void addAnnotationSelectionListener(AnnotationSelectionListener listener)
    {
        this.annotationSelectionListeners.add(listener);
    }

    public void removeAnnotationSelectionListener(AnnotationSelectionListener listener)
    {
        this.annotationSelectionListeners.remove(listener);
    }

    public void addAnnotationDoubleClickListener(AnnotationDoubleClickListener listener)
    {
        this.annotationDoubleClickListeners.add(listener);
    }

    public void removeAnnotationDoubleClickListener(AnnotationDoubleClickListener listener)
    {
        this.annotationDoubleClickListeners.remove(listener);
    }

    public void setSelectedAnnotations(Set<AbstractAnnotation> selectedAnnotations)
    {
        int numAnnotations = tableModel.getRowCount();
        table.getSelectionModel().clearSelection();
        boolean firstFound = true;
        for (int row = 0; row < numAnnotations; row++)
        {
            AbstractAnnotation annotationAtRow = tableModel.getAnnotationAt(row);
            if (selectedAnnotations.contains(annotationAtRow))
            {
                table.getSelectionModel().addSelectionInterval(row, row);
                if (firstFound)
                {
                    table.scrollRectToVisible(table.getCellRect(row, 0, true));
                    firstFound = false;
                }
            }
        }

    }
}
