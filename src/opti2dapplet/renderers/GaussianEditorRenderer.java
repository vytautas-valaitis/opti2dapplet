/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.renderers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import opti2dapplet.MultiLayerCanvas;

/**
 *
 * @author Dalius
 */
public class GaussianEditorRenderer extends RendererBase
{
    int m_x = -1;
    int m_y = -1;
    float m_sigma = 50.0f;
    
    public  GaussianEditorRenderer (MultiLayerCanvas canvas)
    {
    super (canvas, null, null);
    }

public void SetPos (int x, int y)
    {
    m_x = x;
    m_y = y;
    }

public void SetSigma (float sigma)
    {
    m_sigma = sigma;
    }

public float GetSigma ()
    {
    return m_sigma;
    }
    
@Override 
public  void run() 
    {
    m_paused = false;
    m_exit = false;
   
    int x = m_x;
    int y = m_y;
    float sigma = m_sigma;
    
    Image backScr = m_canvas.m_background;
   

    Graphics grphOfs = m_canvas.m_ofscreen.getGraphics();
    grphOfs.setColor(Color.MAGENTA);
   
    while (!m_exit)
        {
            try {
                if (! m_redrawFlag &  m_x == x && m_y == y && m_sigma == sigma)
                    {
                    Thread.sleep(20);
                    continue;
                    }
                
                m_redrawFlag = false;
                
                x = m_x;
                y = m_y;
                sigma = m_sigma;
                
                int w = (int)sigma/2; 
                
                synchronized (m_canvas)
                    {
                    grphOfs.drawImage(backScr, 0, 0, null);
                    
                    if (x >= 0)
                        {
                        grphOfs.drawOval(x-w, y-w, w*2, w*2);
                        grphOfs.drawOval(x-w+1, y-w+1, w*2-2, w*2-2);
                        grphOfs.drawOval(x-2, y-2, 4, 4);
                        }
                    }
                
                m_canvas.repaint();

                Thread.sleep(20);

            } catch (InterruptedException ex) {
                Logger.getLogger(DefaultRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
     }    
}
