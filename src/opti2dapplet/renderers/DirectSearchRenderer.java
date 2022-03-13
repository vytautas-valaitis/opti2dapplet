/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.renderers;

import info.monitorenter.gui.chart.Chart2D;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import opti2dapplet.MultiLayerCanvas;
import opti2dapplet.engines.DirectSearch;
import opti2dapplet.engines.EngineBase;

/**
 *
 * @author Dalius
 */
public class DirectSearchRenderer  extends DefaultRenderer
    {
    private DirectSearch m_dsEngine;
    private Color m_srchLnClr;
    public  DirectSearchRenderer (MultiLayerCanvas canvas, Chart2D chart, EngineBase engine)
        {
        super (canvas, chart, engine);
        m_dsEngine = (DirectSearch) engine;
        m_srchLnClr = m_pathColor.brighter();
        }
    
    @Override
    protected void plot (boolean track, Graphics midScreen, Graphics offScreen, Image bacground, Image midground, int height)
        {
        if (track && (!m_paused || m_singleTick))
            {
            midScreen.setColor(m_pathColor);
            for (int i = 0; i < m_dsEngine.getAgentCount(); i++)
                {
                int x0 = (int)(m_dsEngine.m_xPrev[i]+0.5f);
                int y0 = height - (int)(m_engine.m_yPrev[i]+0.5f);
                int x1 = (int)(m_dsEngine.m_x[i]+0.5f);
                int y1 = height - (int)(m_dsEngine.m_y[i]+0.5f);

                midScreen.drawLine(x0, y0,  x1,  y1);
                }
            }

         synchronized (m_canvas)
            {
            offScreen.drawImage(track ? midground : bacground, 0, 0, null);

            offScreen.setColor(m_srchLnClr);
            
            for (int i = 0; i < m_dsEngine.getAgentCount(); i++)
                {
                int x = (int)(m_dsEngine.m_x[i]+0.5f);
                int y = height - (int)(m_dsEngine.m_y[i]+0.5f);

                if (m_dsEngine.m_showSearchLn)
                    {
                    int rad = (int)(m_dsEngine.m_rads[i]+0.5f);
                    if (0 < rad)
                        {
                        offScreen.drawLine(x, y-rad, x, y+rad);
                        offScreen.drawLine(x-1, y-rad, x-1, y+rad);                        
                        offScreen.drawLine(x-rad, y, x+rad, y);
                        offScreen.drawLine(x-rad, y-1, x+rad, y-1);                        

                        offScreen.drawImage(m_canvas.m_imageList[2], x-6, y-6-rad, null);
                        offScreen.drawImage(m_canvas.m_imageList[2], x-6, y-6+rad, null);
                        offScreen.drawImage(m_canvas.m_imageList[2], x-6-rad, y-6, null);
                        offScreen.drawImage(m_canvas.m_imageList[2], x-6+rad, y-6, null); 
                        }
                    }

                offScreen.drawImage(m_canvas.m_imageList[0], x-6, y-6, null);
                }
            }
        }
    
    @Override
    public void setPathsColor (Color c)
        {
        super.setPathsColor(c);
        m_srchLnClr = m_pathColor.brighter();
        }    
    }
