/**
 * 
 */
package org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.User;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class AbstractAnnotationFilterByUser extends AbstractAnnotationFilter
{
    public static class UserItem
    {
        public static final UserItem NO_USER = new UserItem(null);
        public static final UserItem ALL = new UserItem(null);
        public static final UserItem NONE = new UserItem(null);

        private User user;

        public UserItem(User user)
        {
            this.user = user;
        }

        public User getUser()
        {
            return user;
        }

        @Override
        public String toString()
        {
            if (user != null)
            {
                return user.getName().orElse("Not specified");
            }
            else if (this == ALL)
            {
                return "Select All";
            }
            else if (this == NONE)
            {
                return "Select None";
            }
            else
            {
                return "No user";
            }
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((user == null) ? 0 : user.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (!(obj instanceof UserItem))
            {
                return false;
            }
            UserItem other = (UserItem) obj;
            if (user == null)
            {
                if (other.user != null)
                {
                    return false;
                }
                else
                {
                    if (this == ALL)
                        return other == ALL;
                    else if (this == NONE)
                        return other == NONE;
                    else if (this == NO_USER)
                        return other == NO_USER;
                    else
                        return false;
                }
            }
            else if (!user.equals(other.user))
            {
                return false;
            }
            return true;
        }
    }

    private Set<UserItem> activeUsers;

    /**
     * 
     */
    public AbstractAnnotationFilterByUser()
    {
        activeUsers = new HashSet<>(5);
    }

    public void setActiveUsers(Set<UserItem> activeUsers)
    {
        Set<UserItem> previousUsers = this.activeUsers;
        this.activeUsers = activeUsers;
        if (!Objects.equals(previousUsers, this.activeUsers))
        {
            computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
        }
    }

    public Set<UserItem> getActiveUsers()
    {
        return activeUsers;
    }

    @Override
    protected Set<AbstractAnnotation> applyFilter(Set<AbstractAnnotation> inputAnnotations)
    {
        return inputAnnotations.parallelStream().filter(a -> isActive(a)).collect(Collectors.toSet());
    }

    private boolean isActive(AbstractAnnotation a)
    {
        User u;
        try
        {
            u = a.getUser();
        }
        catch (CytomineClientException e)
        {
            return false;
        }

        if (u == null)
        {
            return false;
        }

        UserItem uItem = new UserItem(u);
        return activeUsers.contains(uItem);
    }

}
