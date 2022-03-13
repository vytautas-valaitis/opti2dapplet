/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.axis.AxisLinear;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;
import java.awt.Color;
import opti2dapplet.engines.*;
import opti2dapplet.renderers.*;
import opti2dapplet.surfaces.*;

/**
 *
 * @author Dalius
 */
public class EngineFactory 
{
public static final String[] supportedEngines = { "Particle Swarm", 
                                                  "Genetic",
                                                  "CMA-ES",
                                                  "Differential Evolution",
                                                  "Direct Search",
                                                  "Stochastic hill climbing",
                                                  "Gradient descent",
                                                  "Coordinate descent",
                                                  "Newton",
                                                  "Quasi-Newton",
                                                  "Nelder-Mead",
                                                  //"e-Greedy",
                                                  };

public static final String[] supportedFunctions = { "Gaussian",
                                                    "Ackley",
                                                    "Himmelblau",
                                                    "Rosenbrock",
                                                    "Schaffer",
                                                    "Shekel",
                                                    "sin(x)", 
                                                    "sin(x)*sin(y)",
                                                    "Gillis",
                                                    "Mount Everest",
                                                    //"Normal K-kanded bandit",
                                                    };

protected EngineBase m_engine = null;
protected LossFunctionBase m_lossFn = null;
protected RendererBase m_renderer = null;
MultiLayerCanvas m_canvas;
Chart2D m_chart;

protected String m_engineName = "";
protected String m_fnName = "";

protected boolean m_running;
protected boolean m_paused;

protected boolean m_greyscale = true;


//<editor-fold defaultstate="collapsed" desc="comment">
        //</editor-fold>
public EngineFactory (MultiLayerCanvas canvas, Chart2D chart)
    {
    m_canvas = canvas;
    m_chart = chart;
    }

//<editor-fold defaultstate="collapsed" desc="comment">
        //</editor-fold>
public void CreateDefaultEngine ()
    {
    CreateEngine (supportedEngines[0], supportedFunctions[0]);
    }

//<editor-fold defaultstate="collapsed" desc="comment">
        //</editor-fold>
public void CreateEngine (String engine, String lossFn)
    {
    boolean change = false;
    if (null == m_lossFn || !lossFn.equals(m_fnName))
        {
        m_lossFn = lossFnByname (lossFn);
        m_fnName = lossFn;
        change = true;
        }
    
    if (null == m_engine || !engine.equals(m_engineName))
        {
        m_engine = engineByName (engine, m_lossFn);
        m_engineName = engine;
        change = true;
        }
    else if (change)
        {
        m_engine.setLossFunction (m_lossFn);
        change = false;
        }
    
    if (change || null == m_renderer)
        {
        m_renderer = rendererByEngine (m_engine);
        }
    
     if (change)
        {
        InitChart (m_engine);
        }
     }

private LossFunctionBase lossFnByname (String name)
    {
    if ("Gaussian".equals(name))      {return new GaussianSurface ();}
    if ("Mount Everest".equals(name)) {return new MountEverest();}
    if ("Shekel".equals(name))        {return new ShekelSurface();}
    if ("Ackley".equals(name))        {return new MiscSutfaces(MiscSutfaces.SurfType.ACKLEY);}
    if ("Himmelblau".equals(name))    {return new MiscSutfaces(MiscSutfaces.SurfType.HIMMELBLAU);}
    if ("Rosenbrock".equals(name))    {return new MiscSutfaces(MiscSutfaces.SurfType.ROSENBROCK);}
    if ("Schaffer".equals(name))      {return new MiscSutfaces(MiscSutfaces.SurfType.SCHAFFER); }
    if ("sin(x)".equals(name))        {return new MiscSutfaces(MiscSutfaces.SurfType.SINEX);}
    if ("sin(x)*sin(y)".equals(name)) {return new MiscSutfaces(MiscSutfaces.SurfType.SINEXY); }
    if ("Gillis".equals(name))        {return new MiscSutfaces(MiscSutfaces.SurfType.JORISGILLIS);}  
    if ("Normal K-kanded bandit".equals(name))  {return new NormalKHandedBandit();}
    
    return null;
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
private RendererBase rendererByEngine (EngineBase engine)
    {
    if (NelderMead.class == engine.getClass())
        {
        return new NelderMeadRenderer (m_canvas, m_chart, engine);
        }
    
    if (DirectSearch.class == engine.getClass())
        {
        return new DirectSearchRenderer (m_canvas, m_chart, engine);
        }  
    
    if (CMA_ES.class == engine.getClass())
        {
        return new CMA_ESRenderer (m_canvas, m_chart, engine);
        }      
   
    return new DefaultRenderer(m_canvas, m_chart, engine);
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
private EngineBase engineByName (String name, LossFunctionBase fn)
    {
    if ("Particle Swarm".equals(name))           {return new BasicSwarmEngine (fn);}
    if ("Genetic".equals(name))                  {return new GeneticEngine(fn);}
    if ("CMA-ES".equals(name))                   {return new CMA_ES(fn);}    
    if ("Gradient descent".equals(name))         {return new GradientDescent(fn);}
    if ("Coordinate descent".equals(name))       {return new CoordinateDescent(fn);}
    if ("Stochastic hill climbing".equals(name)) {return new StochasticHillClimbing(fn);}
    if ("Newton".equals(name))                   {return new Newton(fn);}
    if ("Quasi-Newton".equals(name))             {return new QuasiNewton(fn);}
    if ("Nelder-Mead".equals(name))              {return new NelderMead(fn);}
    if ("Differential Evolution".equals(name))   {return new DifferentialEvolution(fn);}
    if ("Direct Search".equals(name))            {return new DirectSearch(fn);}
    if ("e-Greedy".equals(name))                 {return new EGreedy((NormalKHandedBandit)fn);}
    
    return null;
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
private void InitChart (EngineBase engine)
    {
    //there we should look what engine specific traces to add...  
    //TODO: just a quic crappy implementation, probably charts should go somewhere elsewere...
    
    if (!m_chart.getTraces().isEmpty())
        {
        return;
        }
    
    ITrace2D traceZero = new Trace2DLtd(1000, "");
    ITrace2D traceGlobal = new Trace2DLtd(1000, "Global");
    ITrace2D traceAverage = new Trace2DLtd(1000, "Average");

    traceGlobal.setColor(Color.red);
    traceAverage.setColor(Color.blue);
    traceZero.setColor(Color.black);

    m_chart.addTrace(traceZero);
    m_chart.addTrace(traceGlobal);
    m_chart.addTrace(traceAverage);

    AxisLinear xAxis = (AxisLinear) m_chart.getAxisX();
    xAxis.setRange(new Range (0, 1000)); //TODO: this dont work...
    xAxis.getAxisTitle().setTitle("t");

    AxisLinear yAxis = (AxisLinear) m_chart.getAxisY();
    yAxis.setRange(new Range (0, 10)); //TODO: this dont work...
    yAxis.getAxisTitle().setTitle("error");

    for (int i = 0; i < 1000; i ++)
        {
        traceZero.addPoint(i, 0);
        }        
    }


//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
public boolean isRunning ()
    {
    return m_running;
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
public boolean isPaused ()
    {
    return (m_running && m_paused);
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
public void start (long speed, boolean trackPath, Color pathColor)
    {
    start (speed, trackPath, pathColor, false);
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
private void start (long speed, boolean trackPath, Color pathColor, boolean oneTick)
    {
    if (null != m_renderer)
        {
        m_renderer.exit();
        }
    
    m_engine.initialize();
    m_renderer = rendererByEngine (m_engine);
    m_renderer.setTrackPaths( trackPath);
    m_renderer.setPathsColor (pathColor);
    m_renderer.setSpeed(speed);
    
    if (oneTick)
        {
        m_renderer.setPaused(true);
        m_renderer.setRunSingleTick();
        m_paused = true;
        }
    
    m_renderer.start();
    
    m_running = true;
    m_engine.enableProperties(!m_running);
    m_lossFn.enableProperties(!m_running);         
    }


//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
public void pause ()
    {
    if (!m_running || m_paused)
        {
        return;
        }
    
    m_renderer.setPaused(true);
    m_paused = true;
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
public void resume ()
    {
    if (!m_running)
        {
        m_paused = false;
        return;
        }

    if (!m_renderer.getTrackPaths())
        {
        synchronized (m_canvas)
            {
            m_canvas.ClearOfscreen ();
            }
        }
    
    m_renderer.setPaused(false);
    m_paused = false;
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
public void stop ()
    {
    if (null != m_renderer)
        {
        m_renderer.exit();
        }
    
    m_running = m_paused = false;
    m_engine.enableProperties(!m_running);
    m_lossFn.enableProperties(!m_running);        

    m_canvas.ClearOfscreen ();
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
public void runOneStep (long speed, boolean trackPath, Color pathColor)
    {
    if (!m_running)
        {
        start (speed, trackPath, pathColor, true);
        return;
        }
    
    if (!m_paused)
        {
        return;
        }
    
    m_renderer.setRunSingleTick ();
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
public void updateView (boolean generate, boolean greyscale, boolean contours, boolean invertColors)
    {
    if (generate && !m_running)
        {
        m_lossFn.Generate();
        }
    
    int [] pixels = m_lossFn.getRGBBits (greyscale, contours, invertColors);
    synchronized (m_canvas)
        {
        if (m_running)
            {
            Color clrTo = (greyscale) ? Color.ORANGE : Color.DARK_GRAY;
            Color clrFrom = (m_greyscale) ? Color.ORANGE : Color.DARK_GRAY;
            m_canvas.CreateRGBBackground(m_lossFn.getWidth(), m_lossFn.getHeight(), pixels, clrFrom, clrTo);
            m_renderer.setPathsColor(clrTo);
            }
        else
            {
            m_canvas.CreateRGBBackground(m_lossFn.getWidth(), m_lossFn.getHeight(), pixels);          
            }
        }

    m_greyscale = greyscale;
    
    if (m_running)
        {
        m_renderer.redraw();
        }
    else
        {
        m_canvas.repaint();
        }
    }

public void setRunSpeed (long speed)
    {
    m_renderer.setSpeed(speed);
    }

}
