/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.renderers;

import info.monitorenter.gui.chart.Chart2D;
import java.awt.Graphics;
import java.awt.Image;
import opti2dapplet.MultiLayerCanvas;
import opti2dapplet.engines.EngineBase;

/**
 *
 * @author Dalius
 */
public class NelderMeadRenderer  extends DefaultRenderer
    {
    public  NelderMeadRenderer (MultiLayerCanvas canvas, Chart2D chart, EngineBase engine)
        {
        super (canvas, chart, engine);
        }
    
    @Override
    protected void plot (boolean track, Graphics midScreen, Graphics offScreen, Image bacground, Image midground, int height)
        {
        synchronized (m_canvas)
            {
            offScreen.drawImage(bacground, 0, 0, null);
            offScreen.setColor(m_pathColor);
            
            for (int i = 0; i < m_engine.getAgentCount(); i += 3)
                {
                int x0 = (int)(m_engine.m_x[i]+0.5f);
                int y0 = height - (int)(m_engine.m_y[i]+0.5f);
                int x1 = (int)(m_engine.m_x[i+1]+0.5f);
                int y1 = height - (int)(m_engine.m_y[i+1]+0.5f);  
                int x2 = (int)(m_engine.m_x[i+2]+0.5f);
                int y2 = height - (int)(m_engine.m_y[i+2]+0.5f);    
                
                offScreen.drawLine(x0, y0,  x1,  y1);
                offScreen.drawLine(x1, y1,  x2,  y2);
                offScreen.drawLine(x2, y2,  x0,  y0);
                }
                
            for (int i = 0; i < m_engine.getAgentCount(); i++)
                {
                int x = (int)(m_engine.m_x[i]+0.5f);
                int y = height - (int)(m_engine.m_y[i]+0.5f);

                offScreen.drawImage(m_canvas.m_imageList[m_engine.m_imageId[i]], x-6, y-6, null);
                }
            }
        }       
    }
