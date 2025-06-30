package org.bioimageanalysis.icy.icytomine.core.image.importer;

import java.awt.image.BufferedImage;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Image;

public class TileImporter
{

    private Image imageInformation;
    private int resolution;
    private int tileIndex;
    private int tileX;
    private int tileY;

    public TileImporter(Image imageInformation, int resolution, int tileIndex, int tileX, int tileY)
    {
        this.imageInformation = imageInformation;
        this.resolution = resolution;
        this.tileIndex = tileIndex;
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public BufferedImage getTile() throws CytomineClientException
    {
        String url = imageInformation.getTileUrl(resolution, tileIndex, tileX, tileY).get();
        try
        {
            return imageInformation.getClient().downloadPictureAsBufferedImage(url, "ndpi");
        }
        catch (CytomineClientException e)
        {
            throw new CytomineClientException("Error downloading tile (" + tileIndex + "): " + url);
        }
    }
}
