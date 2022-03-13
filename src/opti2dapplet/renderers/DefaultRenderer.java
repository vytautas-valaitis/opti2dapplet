/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.renderers;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import java.awt.*;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import opti2dapplet.MultiLayerCanvas;
import opti2dapplet.engines.EngineBase;

/**
 *
 * @author Dalius
 */
public class DefaultRenderer extends RendererBase 
{
public  DefaultRenderer (MultiLayerCanvas canvas, Chart2D chart, EngineBase engine)
    {
    super (canvas, chart, engine);
    }

@Override 
public  void run() 
    {
    m_exit = false;
    boolean trackPaths = m_trackPaths;
    
    int h = m_engine.getLossFunction().getHeight() - 1;

    Image backScr = m_canvas.m_background;
    Image midScr = m_canvas.m_midcreen;

    Graphics grphOfs = m_canvas.m_ofscreen.getGraphics();
    Graphics grphMid = m_canvas.m_midcreen.getGraphics();
    
    grphOfs.setColor(Color.red);
    
    grphMid.drawImage(backScr, 0, 0, null);
    
    SortedSet<ITrace2D> traces = m_chart2D.getTraces();
    Iterator it  = traces.iterator();

    ITrace2D traceZero = (ITrace2D)it.next();
    ITrace2D traceGlobal = (ITrace2D)it.next();
    ITrace2D traceAvg = (ITrace2D)it.next();
    
    traceGlobal.removeAllPoints();
    traceAvg.removeAllPoints();
    traceZero.removeAllPoints();
    
    for (int i = 0; i < 1000; i ++)
       {
       traceZero.addPoint(i, 0);
       }
            
    while (!m_exit)
        {
        try 
            {
            boolean cont = (!m_paused || m_redrawFlag || m_singleTick);
            if (trackPaths != m_trackPaths)
                {
                trackPaths = m_trackPaths;
                if (!m_paused || m_singleTick)
                    {
                    grphMid.drawImage(backScr, 0, 0, null);
                    }
                else 
                    {
                    cont = true;
                    }
                }

                if (!cont)
                    {
                    Thread.sleep(200);
                    continue;
                    }

                m_redrawFlag = false;

                plot (trackPaths, grphMid, grphOfs, backScr, midScr, h);
                m_canvas.repaint();

                if (m_paused && !m_singleTick)
                    {
                    Thread.sleep(200);
                    continue;                    
                    }

                m_singleTick = false;
                
                int t = m_engine.m_tickCount;
                float avg = m_engine.m_currAvverageError;
                float glob = m_engine.m_currGlobalError;

                traceGlobal.addPoint(t, glob);
                traceAvg.addPoint(t, avg);

                if (m_engine.m_tickCount >= 1000)
                    {
                    traceZero.addPoint(t, 0);   
                    }

                //DO THE simulation tick. For sake of performance, it was intendet to be in a separate thread, but it performs good there as well...  
                m_engine.tick(); 
                Thread.sleep(m_sleep);
                }
            catch (InterruptedException ex) 
                {
                Logger.getLogger(DefaultRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } //end while
        }//end run

    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>
    protected void plot (boolean track, Graphics midScreen, Graphics offScreen, Image bacground, Image midground, int height)
        {
        if (track && (!m_paused || m_singleTick))
            {
            midScreen.setColor(m_pathColor);
            for (int i = 0; i < m_engine.getAgentCount(); i++)
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

            for (int i = 0; i < m_engine.getAgentCount(); i++)
                {
                int x = (int)(m_engine.m_x[i]+0.5f);
                int y = height - (int)(m_engine.m_y[i]+0.5f);

                offScreen.drawImage(m_canvas.m_imageList[m_engine.m_imageId[i]], x-6, y-6, null);
                }
            }
        }    

    }//end class
