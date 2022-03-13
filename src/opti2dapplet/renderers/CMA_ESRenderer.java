/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.renderers;

import info.monitorenter.gui.chart.Chart2D;
import java.awt.Graphics;
import java.awt.Image;
import opti2dapplet.MultiLayerCanvas;
import opti2dapplet.engines.CMA_ES;
import opti2dapplet.engines.EngineBase;

/**
 *
 * @author Dalius
 */
public class CMA_ESRenderer  extends DefaultRenderer
    {
    private CMA_ES m_cmaEsEngine;
    public  CMA_ESRenderer (MultiLayerCanvas canvas, Chart2D chart, EngineBase engine)
        {
        super (canvas, chart, engine);
        m_cmaEsEngine = (CMA_ES) engine;
        }
    
    @Override
    protected void plot (boolean track, Graphics midScreen, Graphics offScreen, Image bacground, Image midground, int height)
        {
        if (track && (!m_paused || m_singleTick))
            {
            midScreen.setColor(m_pathColor);
            for (int i = 0; i < m_cmaEsEngine.getGroupCount(); i++)
                {
                int x0 = (int)(m_engine.m_xPrev[i]+0.5f);
                int y0 = height - (int)(m_engine.m_yPrev[i]+0.5f);
                int x1 = (int)(m_engine.m_x[i]+0.5f);
                int y1 = height - (int)(m_engine.m_y[i]+0.5f);

                midScreen.drawLine(x0, y0,  x1,  y1);
                }
            }

         synchronized (m_canvas)
            {
            offScreen.drawImage(track ? midground : bacground, 0, 0, null);

            for (int i = m_engine.getAgentCount()-1; i >= 0 ; i--)
                {
                int x = (int)(m_engine.m_x[i]+0.5f);
                int y = height - (int)(m_engine.m_y[i]+0.5f);

                offScreen.drawImage(m_canvas.m_imageList[m_engine.m_imageId[i]], x-5, y-5, null);
                }
            
            if (!m_cmaEsEngine.getShowContour())
                {
                return;
                }
            
            for (int n = 0; n < m_cmaEsEngine.m_grpCnt; n++)
                {
                if (m_cmaEsEngine.m_skipContour[n])
                    {
                    continue;
                    }
                
                for (int i = 0; i < CMA_ES.CONTOUR_SZ; i++)
                    {
                    int k = i < CMA_ES.CONTOUR_SZ - 1 ? i+1 : 0;
                    offScreen.drawLine((int)(m_cmaEsEngine.m_contours[(n*CMA_ES.CONTOUR_SZ) + i].x + 0.5f), 
                                       height -(int)(m_cmaEsEngine.m_contours[(n*CMA_ES.CONTOUR_SZ) + i].y + 0.5f), 
                                       (int)(m_cmaEsEngine.m_contours[(n*CMA_ES.CONTOUR_SZ) + k].x + 0.5f),
                                       height -(int)(m_cmaEsEngine.m_contours[(n*CMA_ES.CONTOUR_SZ) + k].y + 0.5f));
                    }
                }
            
            }
        }    
    }
