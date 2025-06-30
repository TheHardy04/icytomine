/**
 * 
 */
package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.filters;

import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.ComboBoxModel;

import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilter;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilter.ComputationMode;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilterByUser;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilterByUser.UserItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class AbstractAnnotationFilterByUserPanel extends AbstractAnnotationFilterPanel<UserItem>
{
    private static final long serialVersionUID = 8625556747093295599L;
    private AbstractAnnotationFilterByUser userFilter;

    /**
     * Creates the panel.
     */
    public AbstractAnnotationFilterByUserPanel()
    {
        setLabelText("Users:");
        this.userFilter = new AbstractAnnotationFilterByUser();
        setAnnotationFilter(userFilter);
    }

    public void setPreviousFilter(AbstractAnnotationFilter previousFilter)
    {
        userFilter.setPreviousFilter(previousFilter);
    }

    private Set<UserItem> userItems;

    public void setAvailableUsers(Set<User> users)
    {
        userItems = users.stream().map(u -> new UserItem(u)).collect(Collectors.toSet());
        List<UserItem> sortedItems = userItems.stream()
                .sorted(Comparator.<UserItem, String> comparing(ui -> ui.getUser().getName().orElse("Unknown")))
                .collect(Collectors.toList());
        sortedItems.add(UserItem.NO_USER);
        sortedItems.add(UserItem.ALL);
        sortedItems.add(UserItem.NONE);

        userItems.add(UserItem.NO_USER);
        userItems.add(UserItem.ALL);
        userItems.add(UserItem.NONE);
        userFilter.setActiveUsers(new HashSet<>(userItems));
        setModel(sortedItems.stream().toArray(UserItem[]::new),
                item -> item.toString(),
                item -> item != UserItem.NONE);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void choiceChanged(ActionEvent e)
    {
        JCheckableItem<UserItem> checkableItem = ((JCheckableItem<UserItem>) (((JCheckedComboBox<UserItem>) (e
                .getSource())).getSelectedItem()));
        UserItem userItem = checkableItem.object;
        if (userItem == UserItem.ALL)
        {
            Set<UserItem> activeUsers = userFilter.getActiveUsers();
            userItems.stream().filter(u -> u != UserItem.ALL && u != UserItem.NONE).forEach(u -> activeUsers.add(u));
            ComboBoxModel<JCheckableItem<UserItem>> model = ((JCheckedComboBox<UserItem>) (e.getSource())).getModel();
            for (int i = 0; i < model.getSize(); i++)
            {
                JCheckableItem<UserItem> elem = model.getElementAt(i);
                if (elem.getObject() == UserItem.NONE)
                    elem.setSelected(false);
                else
                    elem.setSelected(true);
            }
            ((JCheckedComboBox<UserItem>) (e.getSource())).invalidate();
        }
        else if (userItem == UserItem.NONE)
        {
            userFilter.getActiveUsers().removeAll(userItems);
            ComboBoxModel<JCheckableItem<UserItem>> model = ((JCheckedComboBox<UserItem>) (e.getSource())).getModel();
            for (int i = 0; i < model.getSize(); i++)
            {
                JCheckableItem<UserItem> elem = model.getElementAt(i);
                elem.setSelected(false);
            }
            ((JCheckedComboBox<UserItem>) (e.getSource())).invalidate();
        }
        else if (!checkableItem.isSelected())
        {
            userFilter.getActiveUsers().add(userItem);
        }
        else
        {
            userFilter.getActiveUsers().remove(userItem);
        }
        userFilter.computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
    }

}
