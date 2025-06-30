/**
 * 
 */
package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.filters;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class AbstractAnnotationFilterAdditionPanel extends JPanel
{
    private static final long serialVersionUID = 4137122890897621036L;

    public interface AbstractAnnotationFilterAdditionListener
    {
        void filterAdditionRequested(String filterName);
    }

    /**
     * Creates the panel UI.
     */
    public AbstractAnnotationFilterAdditionPanel()
    {
        setupUI();
        createListenerLists();
        setControlListeners();
    }

    private void setupUI()
    {
        setGridBagLayout();
        addFilterSelector();
        addAdditionButton();
    }

    private void setGridBagLayout()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0};
        gridBagLayout.columnWeights = new double[] {1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
    }

    private JComboBox<String> filterSelector;

    private void addFilterSelector()
    {
        this.filterSelector = new JComboBox<>();
        filterSelector.setModel(new DefaultComboBoxModel<String>(new String[] {"User", "Term"}));
        GridBagConstraints gbc_filterSelector = new GridBagConstraints();
        gbc_filterSelector.insets = new Insets(0, 0, 0, 5);
        gbc_filterSelector.fill = GridBagConstraints.HORIZONTAL;
        gbc_filterSelector.gridx = 0;
        gbc_filterSelector.gridy = 0;
        add(filterSelector, gbc_filterSelector);
    }

    private JButton btnAddFilter;

    private void addAdditionButton()
    {
        this.btnAddFilter = new JButton("Add Filter");
        GridBagConstraints gbc_btnAddFilter = new GridBagConstraints();
        gbc_btnAddFilter.gridx = 1;
        gbc_btnAddFilter.gridy = 0;
        add(btnAddFilter, gbc_btnAddFilter);
    }

    private List<AbstractAnnotationFilterAdditionListener> filterAdditionListeners;

    private void createListenerLists()
    {
        this.filterAdditionListeners = new ArrayList<AbstractAnnotationFilterAdditionListener>(1);
    }

    private void setControlListeners()
    {
        btnAddFilter.addActionListener(this::fireAnnotationFilterAddition);
    }

    private void fireAnnotationFilterAddition(ActionEvent actionevent1)
    {
        filterAdditionListeners
                .forEach(l -> l.filterAdditionRequested((String) filterSelector.getSelectedItem()));
    }

    public void addAnnotationFilterAdditionListener(AbstractAnnotationFilterAdditionListener listener)
    {
        filterAdditionListeners.add(listener);
    }

    public void removeAnnotationFilterAdditionListener(AbstractAnnotationFilterAdditionListener listener)
    {
        filterAdditionListeners.remove(listener);
    }
}
