/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.engines;
import com.l2fprod.common.propertysheet.Property;
import opti2dapplet.ParametricBase;
import opti2dapplet.surfaces.LossFunctionBase;

/**
 *
 * @author Dalius
 */
public class DifferentialEvolution extends EngineBase
{

private ParametricBase.BasicNumericProperty m_prAgentCnt = new ParametricBase.BasicNumericProperty (this, int.class,     "Agents",                       "Settings", 40,     false, 5,    5000);
private ParametricBase.BasicNumericProperty m_prCR       = new ParametricBase.BasicNumericProperty (this, float.class,   "Crossover probability (CR)",   "Settings", 0.5f,   false, 0.0f, 1.0f);
private ParametricBase.BasicNumericProperty m_prF        = new ParametricBase.BasicNumericProperty (this, float.class,   "Differential weight (F)",      "Settings", 0.5f,   false, 0.0f, 2.0f);

private int m_dataHeight;
private int m_dataWidth;

private float   m_cr;
private float   m_f;

protected float[] m_errs;
    
public DifferentialEvolution (LossFunctionBase lossFunction) 
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
        int a, b, c;
        a = b = c = n;
        
        //pick three potential parents a, b, c
        //TODO optimize this with permutations
        while (a == n)
            {
            a = rnd.nextInt(m_agentCnt);
            }
        
        while (b == n || b == a )
            {
            b = rnd.nextInt(m_agentCnt);
            }   
        
        while (c == n || c== b || c == a)
            {
            c = rnd.nextInt(m_agentCnt);
            }          
        
        float x = m_x[n];
        float y = m_y[n];
        
        int r = rnd.nextInt(2);
        float rx = rnd.nextFloat ();
        float ry = rnd.nextFloat ();
        
        if (0 == r || rx < m_cr)
            {
            x = m_x[a] + (m_f * (m_x[b]-m_x[c]));
            }

        if (1 == r || ry < m_cr)
            {
            y = m_y[a] + (m_f * (m_y[b]-m_y[c]));
            }
   
        float testErr = m_lossFunction.getErrorValue(x, y);
        
        if (testErr < m_errs[n])
            {
            m_xPrev[n] = m_x[n];
            m_yPrev[n] = m_y[n];
            m_x[n] = x;
            m_y[n] = y;
            m_errs[n] = testErr;
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
        m_cr        =  m_prCR.getFloatValue();
        m_f         =  m_prF.getFloatValue();
        
        return true;
        }

    @Override
    public void enableProperties (boolean enable)
        {
        m_prAgentCnt.setEditable(enable);
        }

} //eof class
