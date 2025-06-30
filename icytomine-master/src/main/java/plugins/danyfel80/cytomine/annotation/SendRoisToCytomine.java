package plugins.danyfel80.cytomine.annotation;

import java.util.List;

import org.bioimageanalysis.icy.icytomine.core.image.annotation.RoiAnnotationSender;
import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.UserAnnotation;

import icy.plugin.abstract_.Plugin;
import icy.sequence.Sequence;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarArray;
import plugins.adufour.vars.lang.VarSequence;
import vars.cytomine.VarCytomineImage;

public class SendRoisToCytomine extends Plugin implements Block
{

    private VarSequence sequenceVar;
    private VarCytomineImage imageVar;

    private Sequence sequence;
    private Image image;

    private VarArray<AbstractAnnotation> addedAnnotationsVar;

    @Override
    public void declareInput(VarList inputMap)
    {
        sequenceVar = new VarSequence("Sequence", null);
        imageVar = VarCytomineImage.ofNullable(null);
        inputMap.add(sequenceVar.getName(), sequenceVar);
        inputMap.add(imageVar.getName(), imageVar);
    }

    @Override
    public void declareOutput(VarList outputMap)
    {
        addedAnnotationsVar =
                new VarArray<>("Added annotations", AbstractAnnotation[].class, new AbstractAnnotation[0]);
        outputMap.add(addedAnnotationsVar.getName(), addedAnnotationsVar);
    }

    @Override
    public void run()
    {
        retrieveParameters();
        sendAnnotations();
    }

    private void retrieveParameters()
    {
        sequence = sequenceVar.getValue(true);
        image = imageVar.getValue(true);
    }

    private void sendAnnotations()
    {
        RoiAnnotationSender sender = new RoiAnnotationSender(image, sequence, false);
        List<UserAnnotation> annotations;
        try
        {
            annotations = sender.send();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        addedAnnotationsVar.setValue(annotations.toArray(new UserAnnotation[annotations.size()]));
    }

}
