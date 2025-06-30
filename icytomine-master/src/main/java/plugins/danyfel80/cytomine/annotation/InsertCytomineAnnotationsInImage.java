package plugins.danyfel80.cytomine.annotation;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.image.annotation.AnnotationInserter;
import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;

import icy.plugin.abstract_.Plugin;
import icy.sequence.Sequence;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarGenericArray;
import plugins.adufour.vars.lang.VarInteger;
import plugins.adufour.vars.lang.VarSequence;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class InsertCytomineAnnotationsInImage extends Plugin implements Block
{
    VarSequence varInSequence;
    VarInteger varInResolution;
    VarGenericArray<AbstractAnnotation[]> varInAnnotations;

    @Override
    public void declareInput(VarList inputMap)
    {
        varInSequence = new VarSequence("Sequence", null);
        varInResolution = new VarInteger("Resolution level", 0);
        varInAnnotations = new VarGenericArray<AbstractAnnotation[]>("Annotations", AbstractAnnotation[].class,
                new AbstractAnnotation[0]);

        inputMap.add(varInSequence.getName(), varInSequence);
        inputMap.add(varInResolution.getName(), varInResolution);
        inputMap.add(varInAnnotations.getName(), varInAnnotations);
    }

    @Override
    public void declareOutput(VarList outputMap)
    {
        outputMap.add(varInSequence.getName(), varInSequence);
    }

    @Override
    public void run()
    {
        readParameters();
        insertAnnotationsInSequence();
    }

    private Sequence targetSequence;
    private int resolutionLevel;
    private AbstractAnnotation[] annotations;

    private void readParameters()
    {
        targetSequence = varInSequence.getValue(true);
        resolutionLevel = varInResolution.getValue(true);
        annotations = varInAnnotations.getValue();
    }

    private void insertAnnotationsInSequence()
    {
        AnnotationInserter inserter = new AnnotationInserter(targetSequence);
        Rectangle2D imageBoundsAtResolutionZero = getImageBoundsAtResolutionZero();
        inserter.insertAnnotations(imageBoundsAtResolutionZero, resolutionLevel,
                Arrays.stream(annotations).collect(Collectors.toSet()),
                false);
    }

    private Rectangle2D getImageBoundsAtResolutionZero()
    {
        double pixelSizeX = targetSequence.getPixelSizeX(), pixelSizeY = targetSequence.getPixelSizeY();
        double positionX = targetSequence.getPositionX() / pixelSizeX;
        double positionY = targetSequence.getPositionY() / pixelSizeY;
        Rectangle2D.Double boundsAtCurrentResolution =
                new Rectangle2D.Double(positionX, positionY, targetSequence.getWidth(), targetSequence.getHeight());

        return MagnitudeResolutionConverter.convertRectangle2D(boundsAtCurrentResolution, resolutionLevel, 0);
    }

}
