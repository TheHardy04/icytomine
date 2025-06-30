package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.layers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

public class AnnotationLayerSelector extends JPanel
{
    private static final long serialVersionUID = -2542566819940944378L;

    public interface ModelChangeListener
    {
        void modelChanged(List<JCheckableItem<AnnotationLayer>> newItems);
    }

    /**
     * Creates the panel.
     */
    public AnnotationLayerSelector()
    {
        setupUI();
        setupListeners();
    }

    private void setupUI()
    {
        setGridBagLayout();
        addTitleLabel();
        addSelectionComboBox();
    }

    private void setGridBagLayout()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
    }

    private JLabel lblNewLabel;

    private void addTitleLabel()
    {
        lblNewLabel = new JLabel("Active layers:");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
        gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        add(lblNewLabel, gbc_lblNewLabel);
    }

    private JCheckedComboBox<AnnotationLayer> comboBox;

    private void addSelectionComboBox()
    {
        comboBox = new JCheckedComboBox<>();
        comboBox.setModel(new DefaultComboBoxModel<>());

        GridBagConstraints gbc_comboBox = new GridBagConstraints();
        gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBox.gridx = 1;
        gbc_comboBox.gridy = 0;
        add(comboBox, gbc_comboBox);
    }

    public void setLayers(AnnotationLayer[] layerList, Function<AnnotationLayer, String> labelFunction)
    {
        @SuppressWarnings("unchecked")
        JCheckableItem<AnnotationLayer>[] checkableItems = Arrays.stream(layerList)
                .map((AnnotationLayer it) -> new JCheckableItem<AnnotationLayer>(it, labelFunction.apply(it), false))
                .toArray(JCheckableItem[]::new);
        comboBox.setModel(new DefaultComboBoxModel<JCheckableItem<AnnotationLayer>>(checkableItems));
    }

    public Map<AnnotationLayer, Boolean> getAnnotationLayerStates()
    {
        Map<AnnotationLayer, Boolean> layerStates = new HashMap<>();
        for (JCheckableItem<AnnotationLayer> item : comboBox.getCheckableItems())
        {
            layerStates.put(item.getObject(), item.isSelected());
        }
        return layerStates;
    }

    private List<ModelChangeListener> modelChangeListeners;

    private void setupListeners()
    {
        this.modelChangeListeners = new ArrayList<ModelChangeListener>();
        comboBox.addPropertyChangeListener("model", this::onModelChangeListener);
    }

    private void onModelChangeListener(PropertyChangeEvent event)
    {
        modelChangeListeners.forEach(l -> l.modelChanged(comboBox.getCheckableItems()));
    }

    public void addModelChangeListener(ModelChangeListener listener)
    {
        this.modelChangeListeners.add(listener);
    }

    public void removeModelChangeListener(ModelChangeListener listener)
    {
        this.modelChangeListeners.remove(listener);
    }

    public void addItemChangeListner(ItemListener listener)
    {
        comboBox.addItemListener(listener);
    }

    public void removeItemChangeListner(ItemListener listener)
    {
        comboBox.removeItemListener(listener);
    }

}
