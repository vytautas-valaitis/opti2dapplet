/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.renderers;

import info.monitorenter.gui.chart.Chart2D;
import java.awt.Color;
import opti2dapplet.MultiLayerCanvas;
import opti2dapplet.engines.EngineBase;

/**
 *
 * @author Dalius
 */
public abstract class RendererBase extends Thread 
{
protected boolean m_exit = false;
protected MultiLayerCanvas m_canvas;
protected EngineBase m_engine;
protected Chart2D m_chart2D;

protected long m_sleep = 10;
protected boolean m_trackPaths = false;
protected boolean m_paused = false;
protected boolean m_redrawFlag = false;

protected Color m_pathColor;
protected boolean m_singleTick = false;


public  RendererBase (MultiLayerCanvas canvas, Chart2D chart, EngineBase engine)
    {
    super ();
    m_canvas = canvas;
    m_engine = engine;
    m_chart2D = chart;
    m_pathColor = Color.ORANGE;
    }

public void setSpeed (long sleepTime)
    {
    m_sleep = sleepTime;
    }

public void setRunSingleTick ()
    {
    m_singleTick = true;
    }

public void setTrackPaths (boolean trackPaths)
    {
    m_trackPaths = trackPaths;
    }

public boolean getTrackPaths ()
    {
    return m_trackPaths;
    }

public void setPathsColor (Color c)
    {
    m_pathColor = c;
    }

public void setPaused (boolean paused)
    {
    m_paused = paused;
    }

public void redraw ()
    {
    m_redrawFlag = true;
    }

    /**
     *
     */
public void exit ()
    {
    Thread.State state = getState();
    if (Thread.State.NEW == state || Thread.State.TERMINATED == state)
        {
        return;
        }
    
    m_exit = true;
    try
        {
        join ();
        }
    catch (InterruptedException ex) {}
    }    
}
