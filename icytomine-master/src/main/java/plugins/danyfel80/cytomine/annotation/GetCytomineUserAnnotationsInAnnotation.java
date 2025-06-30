/**
 * 
 */
package plugins.danyfel80.cytomine.annotation;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Project;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.UserAnnotation;

import com.vividsolutions.jts.geom.Geometry;

import icy.plugin.abstract_.Plugin;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarGenericArray;
import plugins.adufour.vars.lang.VarString;
import vars.cytomine.VarCytomineAbstractAnnotation;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class GetCytomineUserAnnotationsInAnnotation extends Plugin implements Block
{

    private VarCytomineAbstractAnnotation varInTargetAnnotation;
    private VarString varInUserNameFilter;
    private VarString varInTermNameFilter;

    @Override
    public void declareInput(VarList inputMap)
    {
        varInTargetAnnotation = VarCytomineAbstractAnnotation.ofNullable(null);
        varInUserNameFilter = new VarString("User names", "noUser");
        varInTermNameFilter = new VarString("Term name", "No Term");

        inputMap.add(varInTargetAnnotation.getName(), varInTargetAnnotation);
        inputMap.add(varInUserNameFilter.getName(), varInUserNameFilter);
        inputMap.add(varInTermNameFilter.getName(), varInTermNameFilter);
    }

    private VarGenericArray<AbstractAnnotation[]> varOutAnnotations;

    @Override
    public void declareOutput(VarList outputMap)
    {
        varOutAnnotations = new VarGenericArray<AbstractAnnotation[]>("Annotations", AbstractAnnotation[].class,
                new AbstractAnnotation[0]);
        outputMap.add(varOutAnnotations.getName(), varOutAnnotations);
    }

    @Override
    public void run()
    {
        readParameters();
        computeAnnotations();
        setOutputValues();
    }

    private AbstractAnnotation targetAnnotation;
    private Set<String> usernames;
    private Set<String> termNames;

    private void readParameters()
    {
        this.targetAnnotation = varInTargetAnnotation.getValue(true);
        this.usernames = Arrays.stream(varInUserNameFilter.getValue(true).toLowerCase().split(" *, *"))
                .filter(u -> !u.isEmpty()).distinct()
                .collect(Collectors.toSet());
        this.termNames = Arrays.stream(varInTermNameFilter.getValue(true).split(" *, *"))
                .filter(t -> !t.isEmpty()).distinct()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    Set<UserAnnotation> resultAnnotations;

    private void computeAnnotations()
    {
        final Long taId = targetAnnotation.getId();
        final Image taImage = targetAnnotation.getImage(); 
        final Long taImageId = taImage.getId(); 
        final Project taImageProject = taImage.getProject();
        final CytomineClient taClient = targetAnnotation.getClient();
        final Rectangle2D taBounds = targetAnnotation.getBounds();        
        final List<User> users = taImageProject.getUsers(false);
        final Set<Term> terms = taImageProject.getOntology().getTerms(false);
        final Geometry taGeometry = targetAnnotation.getGeometryAtZeroResolution(false);
//        final Geometry taEnvelope = taGeometry.getEnvelope();

        final Set<Long> targetUserIds = users.stream()
                .filter(u -> usernames.contains(u.getName().orElse("").toLowerCase()))
                .map(u -> u.getId())
                .collect(Collectors.toSet());
        final Set<Term> targetTerms = terms.stream()
                .filter(t -> termNames.contains(t.getName().orElse("").toLowerCase()))
                .collect(Collectors.toSet());
        boolean containsNoTerm = termNames.contains("No Term");
        
        final Map<Long, UserAnnotation> foundAnnotations = targetUserIds.isEmpty()
            ? taClient.getImageUserAnnotationsAt(taImageId, taBounds)
            : taClient.getImageUserAnnotationsAt(taImageId, targetUserIds, taBounds);
        final Map<Long, UserAnnotation> foundGeometries = targetUserIds.isEmpty()
                ? taClient.getImageUserAnnotationGeometries(taImage, taBounds)
                : taClient.getImageUserAnnotationGeometries(taImage, targetUserIds, taBounds);
        
        resultAnnotations = new HashSet<>();
        
        for(UserAnnotation annot: foundAnnotations.values())
        {
            final Long id = annot.getId();
            
            // avoid target annotation
            if (!id.equals(taId))
            {
                final Set<Term> annotTerms = annot.getAssociatedTerms();
                boolean accepted = termNames.isEmpty() || (annotTerms.isEmpty() && containsNoTerm);
                
                if (!accepted)
                {
                    for(Term annotTerm: annotTerms)
                    {
                        if (targetTerms.contains(annotTerm))
                        {
                            accepted = true;
                            break;
                        }
                    }
                }
                
                // accepted ?
                if (accepted)
                {
                    final UserAnnotation geometry = foundGeometries.get(id);

                    // set location (geometry)
                    if (geometry != null)
                        annot.getInternalAnnotation().set("location", geometry.getInternalAnnotation().getStr("location"));
                    // check if geometry intersects
                    if (taGeometry.intersects(annot.getGeometryAtZeroResolution(false)))
                        resultAnnotations.add(annot);
                }
            }
        }
    }

    private void setOutputValues()
    {
        varOutAnnotations.setValue(resultAnnotations.stream().toArray(AbstractAnnotation[]::new));
    }

}
