/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Dalius
 */
public class MultiLayerCanvas extends Canvas 
{
    public Image  m_background;
    public Image  m_midcreen;
    public Image  m_ofscreen;
    
    public Image[]  m_imageList;
    
    private int m_w = 0;
    private int m_h = 0;
    
    public MultiLayerCanvas()
        {
        super();
        m_imageList = new Image[4];
        try 
            {
            m_imageList[0] = ImageIO.read(getClass().getResourceAsStream("/res/agent1.png"));
            m_imageList[1] = ImageIO.read(getClass().getResourceAsStream("/res/agent2.png"));
            m_imageList[2] = ImageIO.read(getClass().getResourceAsStream("/res/agent3.png"));
            m_imageList[3] = ImageIO.read(getClass().getResourceAsStream("/res/agent4.png"));  
            } 
         catch (IOException ex) 
            {
            Logger.getLogger(MultiLayerCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
    @Override
    public void update(Graphics g)
        {
        paint(g);
        }

    /**
     *
     * @param g
     */
    @Override
    public void paint(Graphics g)
        {
        if (null != m_ofscreen)
            {
            synchronized (this)
                {
                g.drawImage(m_ofscreen, 0, 0, null);
                }
            }
        }

        /**
     *
     * @param w
     * @param h
     * @param pixels
     */
    public void CreateRGBBackground (int w, int h, int[] pixels)
        {
        CreateRGBBackground   (w, h, pixels, null, null);
        }
    
    /**
     *
     * @param w
     * @param h
     * @param pixels
     */
    public void CreateRGBBackground (int w, int h, int[] pixels, Color fromClr, Color toClr)
        {
        BufferedImage image;
        
        if (null != m_background && (w != m_w || h != m_h))
            {
            m_background.flush();
            m_midcreen.flush();
            m_ofscreen.flush();
            m_background = null;
            m_midcreen = null;
            m_ofscreen = null;
            System.gc();
            }
        
        boolean copyColorFromMid = (null != fromClr && null != toClr);
        
        if (null != m_background)
            {
            image = (BufferedImage)m_background;
            }
        else
            {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            m_midcreen = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            m_ofscreen = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            copyColorFromMid = false;
            }
                         
         WritableRaster back_raster = image.getRaster();
         
         back_raster.setPixels(0,0,w,h,pixels);
         m_background = image;
         
         m_ofscreen.getGraphics().drawImage(m_background, 0, 0, null);         
         
         if (copyColorFromMid)
            {
            int rf = fromClr.getRed();
            int gf = fromClr.getGreen();
            int bf = fromClr.getBlue();

            int rt = toClr.getRed();
            int gt = toClr.getGreen();
            int bt = toClr.getBlue();            
            
            float[] src_pixels = null;
            
            WritableRaster mid_raster = ((BufferedImage)m_midcreen).getRaster();
            
            src_pixels = mid_raster.getPixels(0, 0, m_w, m_h, src_pixels);                
            for (int i = 0; i < w*h*3; i+=3) 
                {
                if (rf == src_pixels[i] && gf == src_pixels[i+1] && bf == src_pixels[i+2])
                    {
                    pixels[i]   = rt;
                    pixels[i+1] = gt;
                    pixels[i+2] = bt;
                    }
                }
            
            mid_raster.setPixels(0,0,w,h,pixels);
            }
         else
             {
             m_midcreen.getGraphics().drawImage(m_background, 0, 0, null);
             }
         
         m_w = w;
         m_h = h;
        }
    
    public void ClearOfscreen ()
        {
        m_midcreen.getGraphics().drawImage(m_background, 0, 0, null);
        m_ofscreen.getGraphics().drawImage(m_background, 0, 0, null);
        repaint();
        }
}
