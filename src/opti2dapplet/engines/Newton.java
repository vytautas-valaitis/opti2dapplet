/**
 *
 * @author Dalius
 */
package opti2dapplet.engines;
import com.l2fprod.common.propertysheet.Property;
import opti2dapplet.ParametricBase;
import opti2dapplet.surfaces.LossFunctionBase;
import opti2dapplet.surfaces.LossFunctionBase.Vector2d;

/**
 *
 * @author Dalius
 */
public class Newton extends EngineBase
{

private ParametricBase.BasicNumericProperty m_prAgentCnt      = new ParametricBase.BasicNumericProperty (this, int.class,      "agents",           "Settings", 40,    false, 1,    5000);
private ParametricBase.BasicNumericProperty m_prGamma         = new ParametricBase.BasicNumericProperty (this, float.class,    "step",             "Settings", 0.1f,  false, 0.0f, 10000.0f);

private ParametricBase.BasicNumericProperty m_prLineSearch    = new ParametricBase.BasicNumericProperty (this, boolean.class,  "line search",      "Backtracking", false, false);
private ParametricBase.BasicNumericProperty m_prAlpha         = new ParametricBase.BasicNumericProperty (this, float.class,    "alpha",           "Backtracking", 0.001f, false, 0,    0.5f);
private ParametricBase.BasicNumericProperty m_prBeta         = new ParametricBase.BasicNumericProperty (this, float.class,     "beta",            "Backtracking", 0.9f,   false, 0.0f, 1.0f);


private int m_dataHeight;
private int m_dataWidth;

private float   m_scale = 1.0f;
private float   m_gamma;
private float   m_gammaPrev;
private float   m_alpha;
private float   m_beta;
private boolean m_lineSerach = false;

   
public Newton (LossFunctionBase lossFunction) 
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
    m_scale      = m_lossFunction.getScale();
    
    m_x      = new float[m_agentCnt];
    m_y      = new float[m_agentCnt];
   
    m_xPrev  = new float[m_agentCnt];
    m_yPrev  = new float[m_agentCnt];
    
    m_imageId = new int[m_agentCnt];

    float sum_error = 0.0f;
    
    m_currGlobalError = 1.0f;
    
    for (int i = 0; i < m_agentCnt; i++)
        {
        m_xPrev[i] = m_x[i] = m_dataWidth * rnd.nextFloat();
        m_yPrev[i] = m_y[i] = m_dataHeight * rnd.nextFloat();
        float err = m_lossFunction.getErrorValue(m_x[i], m_y[i]);
        
        sum_error += err;
        
        if (err < m_currGlobalError)
            {
            m_currGlobalError = err;
            }
        }
    
    m_currAvverageError = sum_error/m_agentCnt;
    }

@Override
public void tick ()
    {
    float sum_error = 0.0f;
    m_currGlobalError = 1.0f;
    
    for (int i = 0; i < m_agentCnt; i++)
        {
        //remember previous position
        m_xPrev[i] = m_x[i];
        m_yPrev[i] = m_y[i];            
            
        Vector2d g = m_lossFunction.getGradient(m_x[i], m_y[i]);
        LossFunctionBase.Matrix2x2 h = m_lossFunction.getHessian(m_x[i], m_y[i]);

        // <editor-fold defaultstate="collapsed" desc="second derivative test">          
        if (h.det > 0 && h.c11 < 0) //looking for local maximum
            {
            m_imageId[i] = 0;
            }        
        else if (h.det < 0) //saddle point
            {
            m_imageId[i] = 1; 
            }
        else if (h.det > 0 && h.c11 > 0) //looking for local minimum
            {
            m_imageId[i] = 2; 
            }
        else if (h.det == 0) //then the second derivative test is inconclusive.
            {
            m_imageId[i] = 3;
            }          
        // </editor-fold>
        h.invert();
        
        float delta_x = - ((g.x*h.c11) + (g.y*h.c21));
        float delta_y = - ((g.x*h.c12) + (g.y*h.c22));
        
        if (m_lineSerach)
            {
            float val0 = m_lossFunction.getValue(m_x[i], m_y[i]);
            float step = 1.0f/m_scale;

            float grad_delta0 = ((g.x*delta_x) + (g.y*delta_y));
            
            int j = 0;
            //while satisfies Wolfe confitions
            while (m_lossFunction.getValue(m_x[i]+(delta_x*step), m_y[i]+(delta_y*step)) < val0 + (step*m_alpha*grad_delta0))
                {
                step = step * m_beta;
                //just in case escape forever loop
                if (j++ > 100)
                    {
                    break;
                    }
                }
            
            delta_x = step*delta_x;
            delta_y = step*delta_y;            
            }
        else
            {
            delta_x = m_gamma*delta_x/m_scale;
            delta_y = m_gamma*delta_y/m_scale;
            }
        
        m_x[i] += delta_x;
        m_y[i] += delta_y;
        
        float err = m_lossFunction.getErrorValue(m_x[i], m_y[i]);

        sum_error += err;
        if (err < m_currGlobalError)
            {
            m_currGlobalError = err;
            }        
        }
    
    m_currAvverageError = sum_error/m_agentCnt;
    m_tickCount ++;
    }

    /**
     *
     * @return
     */
    @Override
    protected final boolean ReadAndValidateProps ()
        {
        boolean search = m_lineSerach;
        
        m_agentCnt  = m_prAgentCnt.getIntValue();
        m_gamma     = m_prGamma.getFloatValue();
        m_alpha     = m_prAlpha.getFloatValue();
        m_beta      = m_prBeta.getFloatValue();
        m_lineSerach = m_prLineSearch.getBoolValue();
        
        m_prGamma.setEditable(!m_lineSerach);
        m_prBeta.setEditable(m_lineSerach);
        m_prAlpha.setEditable(m_lineSerach); 
        
        if (search != m_lineSerach)
            {
            if (m_lineSerach)
                {
                m_gammaPrev = m_gamma;
                m_prGamma.setValue(1.0f);
                }
            else
                {
                m_prGamma.setValue(m_gammaPrev);
                m_gamma = m_gammaPrev;
                }
            }
        
        return true;
        }

    @Override
    public void enableProperties (boolean enable)
        {
        m_prAgentCnt.setEditable(enable);
        }
 
} //eof class GradientDescent

