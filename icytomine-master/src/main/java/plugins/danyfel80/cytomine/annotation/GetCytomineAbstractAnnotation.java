package plugins.danyfel80.cytomine.annotation;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;

import icy.plugin.abstract_.PluginActionable;
import icy.plugin.interface_.PluginLibrary;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarBoolean;
import plugins.adufour.vars.lang.VarLong;
import vars.cytomine.VarCytomineAbstractAnnotation;
import vars.cytomine.VarCytomineClient;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class GetCytomineAbstractAnnotation extends PluginActionable implements Block, PluginLibrary
{
    protected VarCytomineClient varInClient;
    protected VarLong varInAnnotationId;
    protected VarBoolean varInRetrieveProperties;

    @Override
    public void declareInput(VarList inputMap)
    {
        varInClient = VarCytomineClient.ofNullable(null);
        varInAnnotationId = new VarLong("Annotation id", 0L);
        varInRetrieveProperties = new VarBoolean("Include properties", false);
        inputMap.add(varInClient.getName(), varInClient);
        inputMap.add(varInAnnotationId.getName(), varInAnnotationId);
        inputMap.add(varInRetrieveProperties.getName(), varInRetrieveProperties);
    }

    VarCytomineAbstractAnnotation varOutAnnotation;

    @Override
    public void declareOutput(VarList outputMap)
    {
        varOutAnnotation = VarCytomineAbstractAnnotation.ofNullable(null);
        outputMap.add(varOutAnnotation.getName(), varOutAnnotation);
    }

    @Override
    public void run()
    {
        CytomineClient client = varInClient.getValue(true);
        long annotationId = varInAnnotationId.getValue(true);
        AbstractAnnotation annotationInstance;
        try
        {
            annotationInstance = client.getAbstractAnnotation(annotationId);
            annotationInstance.getProperties(varInRetrieveProperties.getValue());
        }
        catch (CytomineClientException e)
        {
            throw new RuntimeException(e);
        }
        varOutAnnotation.setValue(annotationInstance);
    }

}
