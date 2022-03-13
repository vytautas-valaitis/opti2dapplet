/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.surfaces;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Dalius
 */
public class MountEverest extends LossFunctionBase
{
    public MountEverest ()
        {
        super ();
        m_isDirty = true;
        }

    @Override
    public void Generate() 
        {
        if (!m_isDirty && null != m_data && m_w*m_h == m_data.length)
            {
            return;
            }      
        
        init ();
        
        BufferedImage img;
        try 
            {
            img = ImageIO.read(getClass().getResourceAsStream("/res/everest.png"));
            }
        catch (IOException ex) 
            {
            Logger.getLogger(MountEverest.class.getName()).log(Level.SEVERE, null, ex);
            return;
            }
          
        WritableRaster raster = img.getRaster();
        float[] pixels = null;
        pixels = raster.getPixels(0, 0, m_w, m_h, pixels);
        raster = null;
        img = null;
        System.gc ();
        
        int sz = (int) pixels.length/(m_w*m_h);
        
        m_min = 255.0f;
        m_max = 0.0f;
        int i = 0;
        for (int y = 0; y < m_w; y ++)
            {
            for (int x = 0; x < m_h; x ++)
                {
                float px = pixels[i*sz];
                m_data[((m_h-y-1)*m_w)+x] = px;
                i++;
                if (m_min > px) {m_min = px;}
                if (m_max < px) {m_max = px;}
                }
            }
        
        m_isDirty = false;
        }

    @Override
    protected boolean ReadAndValidateProps() 
        {
        return true;
        }
  
    @Override
    public boolean canDesign ()
        {
        return true;
        }
    
    @Override
    public boolean canSupportGradient ()
        {
        return false;
        }
    
    @Override
    public boolean canGenerate ()
        {
        return m_isDirty;
        }    
    
}
