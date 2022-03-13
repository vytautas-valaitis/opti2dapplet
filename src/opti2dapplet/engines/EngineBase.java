/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.engines;
import java.util.Random;
import opti2dapplet.ParametricBase;
import opti2dapplet.surfaces.LossFunctionBase;
/**
 *
 * @author Dalius
 */
public abstract class EngineBase extends ParametricBase
{
public int m_agentCnt = 0;
public int m_tickCount = 0;
protected LossFunctionBase m_lossFunction;

protected Random rnd = new Random();

//Main variables for any multiagent system; also used for rendering
public float[] m_x;
public float[] m_y;

//Just for tracking path for renderer, must hold previous position
public float[] m_xPrev;
public float[] m_yPrev;

public int[] m_imageId; 
     
public float m_currGlobalError  = 0.0f;
public float m_currAvverageError = 0.0f;

public EngineBase(LossFunctionBase lossFunction)
    {
    setLossFunction(lossFunction);
    rnd.setSeed (System.currentTimeMillis());
    }

public int getAgentCount ()
    {
    return m_agentCnt;    
    }

public final void setLossFunction (LossFunctionBase lossFunction)
    {
    m_lossFunction = lossFunction;
    }

public final LossFunctionBase getLossFunction ()
    {
    return m_lossFunction;
    }

public abstract  void initialize ();
public abstract  void tick ();
}

