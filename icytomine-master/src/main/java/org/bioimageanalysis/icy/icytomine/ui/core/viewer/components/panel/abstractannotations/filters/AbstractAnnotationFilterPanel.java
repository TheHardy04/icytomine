/**
 * 
 */
package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.filters;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilter;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

import icy.resource.ResourceUtil;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
@SuppressWarnings("serial")
public abstract class AbstractAnnotationFilterPanel<E> extends JPanel
{

    public interface AnnotationFilterListener
    {
        void filterUpdated(Set<AbstractAnnotation> activeAnnotations);
    }

    /**
     * 
     */
    public AbstractAnnotationFilterPanel()
    {
        setupUI();
    }

    private void setupUI()
    {
        setGridBagLayout();
        addFilterLabel();
        addFilterSelector();
        addFilterDeletionButton();
    }

    private void setGridBagLayout()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
    }

    private JLabel lblFilterLabel;

    private void addFilterLabel()
    {

        lblFilterLabel = new JLabel("Filter");
        GridBagConstraints gbc_lblFilterLabel = new GridBagConstraints();
        gbc_lblFilterLabel.insets = new Insets(0, 0, 0, 5);
        gbc_lblFilterLabel.anchor = GridBagConstraints.EAST;
        gbc_lblFilterLabel.gridx = 0;
        gbc_lblFilterLabel.gridy = 0;
        add(lblFilterLabel, gbc_lblFilterLabel);
    }

    private JCheckedComboBox<E> checkedSelectorFilterOptions;

    private void addFilterSelector()
    {
        checkedSelectorFilterOptions = new JCheckedComboBox<>();
        GridBagConstraints gbc_checkedSelectorFilterOptions = new GridBagConstraints();
        gbc_checkedSelectorFilterOptions.insets = new Insets(0, 0, 0, 5);
        gbc_checkedSelectorFilterOptions.fill = GridBagConstraints.HORIZONTAL;
        gbc_checkedSelectorFilterOptions.gridx = 1;
        gbc_checkedSelectorFilterOptions.gridy = 0;
        add(checkedSelectorFilterOptions, gbc_checkedSelectorFilterOptions);
    }

    private JButton btnDeleteFilter;

    private void addFilterDeletionButton()
    {
        btnDeleteFilter =
                new JButton(new ImageIcon(ResourceUtil.ICON_DELETE.getScaledInstance(15, 15, Image.SCALE_SMOOTH)));
        btnDeleteFilter.setMinimumSize(new Dimension(15, 15));
        btnDeleteFilter.setPreferredSize(new Dimension(21, 21));
        GridBagConstraints gbc_btnDeleteFilter = new GridBagConstraints();
        gbc_btnDeleteFilter.gridx = 2;
        gbc_btnDeleteFilter.gridy = 0;
        add(btnDeleteFilter, gbc_btnDeleteFilter);
    }

    public void setLabelText(String text)
    {
        lblFilterLabel.setText(text);
    }

    private AbstractAnnotationFilter filter;

    public void setAnnotationFilter(AbstractAnnotationFilter filter)
    {
        this.filter = filter;
    }

    public AbstractAnnotationFilter getAnnotationFilter()
    {
        return filter;
    }

    public void setModel(E[] items, Function<E, String> labelFunction, Function<E, Boolean> selectionFunction)
    {
        @SuppressWarnings("unchecked")
        JCheckableItem<E>[] checkableItems = Arrays.stream(items)
                .map((E it) -> new JCheckableItem<E>(it, labelFunction.apply(it), selectionFunction.apply(it)))
                .toArray(JCheckableItem[]::new);
        checkedSelectorFilterOptions.setModel(new DefaultComboBoxModel<JCheckableItem<E>>(checkableItems));
        checkedSelectorFilterOptions.addActionListener(this::choiceChanged);
    }

    protected abstract void choiceChanged(ActionEvent event);

    public void addRemoveButtonActionListener(ActionListener listener)
    {
        btnDeleteFilter.addActionListener(listener);
    }

    public void removeRemoveButtonActionListener(ActionListener listener)
    {
        btnDeleteFilter.removeActionListener(listener);
    }
}
