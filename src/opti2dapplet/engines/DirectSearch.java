/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.engines;
import opti2dapplet.ParametricBase;
import opti2dapplet.surfaces.LossFunctionBase;

/**
 *
 * @author Dalius
 */
public class DirectSearch extends EngineBase
{

private ParametricBase.BasicNumericProperty m_prAgentCnt = new ParametricBase.BasicNumericProperty (this, int.class,     "Agents",   "Settings", 40,     false, 1,    5000);
private ParametricBase.BasicNumericProperty m_prShowSearch = new ParametricBase.BasicNumericProperty (this, boolean.class, "Show search lines",  "Display", true,  false);

private int m_dataHeight;
private int m_dataWidth;

protected float[] m_errs;
public double[] m_rads;
public boolean m_showSearchLn;
    
public DirectSearch (LossFunctionBase lossFunction) 
    {
    super (lossFunction);
    ReadAndValidateProps ();
    }

@Override    
public void initialize ()
    {
    m_tickCount = 0;
    m_dataHeight = m_lossFunction.getHeight();
    m_dataWidth  = m_lossFunction.getWidth();

    m_errs   = new float[m_agentCnt]; 
    m_rads   = new double[m_agentCnt];  
    
    m_x      = new float[m_agentCnt];
    m_y      = new float[m_agentCnt];
    
    m_xPrev  = new float[m_agentCnt];
    m_yPrev  = new float[m_agentCnt];

    m_imageId = new int[m_agentCnt];

    float sum_error = 0.0f;
    float min_err = 0.0f;
    
    for (int i = 0; i < m_agentCnt; i++)
        {
        m_xPrev[i] = m_x[i] = m_dataWidth * rnd.nextFloat();
        m_yPrev[i] = m_y[i] = m_dataHeight * rnd.nextFloat();
        m_rads[i] = m_dataWidth/4.0f;
        
        m_errs[i]  = m_lossFunction.getErrorValue(m_x[i], m_y[i]);
        sum_error += m_errs[i];
        
        min_err = ( 0==i || min_err > m_errs[i]) ? m_errs[i] : min_err;
        }
    
    
    m_currAvverageError = sum_error/m_agentCnt;
    m_currGlobalError = min_err;
    }

@Override
public void tick ()
    {
    float sum_error = 0.0f;
    float min_err = 0.0f;
    
    for (int n = 0; n < m_agentCnt; n++)
        {
        if (m_rads[n] < 0.00000001) //makes no sence to have too small radius in a discrete space
            {
            m_rads[n] = 0;
            sum_error += m_errs[n];
            min_err = ( 0==n || min_err > m_errs[n]) ? m_errs[n] : min_err;            
            continue;
            }
        
        m_xPrev[n] = m_x[n];
        m_yPrev[n] = m_y[n];              
            
        double step = m_rads[n] / 20.0;
        double newRad = m_rads[n];
        float newX = m_x[n];
        float newY = m_y[n];
        
        for (double x = m_x[n] - m_rads[n]; x < m_x[n] + m_rads[n]; x+= step)
            {
            float err = m_lossFunction.getErrorValue((float)x, m_y[n]);
            if (err < m_errs[n])
                {
                newX = (float)x;
                m_errs[n] = err;
                newRad = m_x[n] -(float) x;
                newRad = newRad * Math.signum(newRad);
                }
            }
        
        for (double y = m_y[n] - m_rads[n]; y < m_y[n] + m_rads[n]; y+= step)
            {
            float err = m_lossFunction.getErrorValue(m_x[n], (float)y);
            if (err < m_errs[n])
                {
                newX = m_x[n];
                newY = (float)y;
                m_errs[n] = err;
                newRad = m_y[n] - (float)y;
                newRad = newRad * Math.signum(newRad);
                }
            }
        
        if (m_rads[n] == newRad && m_x[n] == newX && m_y[n] == newY)
            {
            m_rads[n] = 0; //cannot find better..
            }
        else
            {
            m_rads[n] = newRad;
            m_x[n] = newX;
            m_y[n] = newY;                
            }
        
        sum_error += m_errs[n];
        min_err = ( 0==n || min_err > m_errs[n]) ? m_errs[n] : min_err;
        }
   

    m_currAvverageError = sum_error/m_agentCnt;
    m_currGlobalError = min_err;
    m_tickCount ++;
    }

    /**
     *
     * @return
     */
    @Override
    protected final boolean ReadAndValidateProps ()
        {
        m_agentCnt  =  m_prAgentCnt.getIntValue();
        m_showSearchLn = m_prShowSearch.getBoolValue();
        return true;
        }

    @Override
    public void enableProperties (boolean enable)
        {
        m_prAgentCnt.setEditable(enable);
        }

} //eof class
