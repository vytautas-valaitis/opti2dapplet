/**
 *
 * @author Vytautas
 */
package opti2dapplet.engines;
import opti2dapplet.ParametricBase;
import opti2dapplet.surfaces.LossFunctionBase;
import opti2dapplet.surfaces.LossFunctionBase.Vector2d;

/**
 *
 * @author Vytautas
 */
public class CoordinateDescent extends EngineBase
{

private ParametricBase.BasicNumericProperty m_prAgentCnt  = new ParametricBase.BasicNumericProperty (this, int.class,      "agents",   "Settings", 40,    false, 1,    5000);
private ParametricBase.BasicNumericProperty m_prGamma     = new ParametricBase.BasicNumericProperty (this, float.class,    "step",     "Settings", 0.001f, false, 0.0f, 10000.0f);

private ParametricBase.BasicNumericProperty m_prLineSearch    = new ParametricBase.BasicNumericProperty (this, boolean.class,  "line search",      "Backtracking", false, false);
private ParametricBase.BasicNumericProperty m_prAlpha         = new ParametricBase.BasicNumericProperty (this, float.class,    "alpha",           "Backtracking", 0.001f, false, 0,    0.5f);
private ParametricBase.BasicNumericProperty m_prBeta         = new ParametricBase.BasicNumericProperty (this, float.class,     "beta",            "Backtracking", 0.9f,   false, 0.0f, 1.0f);

private int m_dataHeight;
private int m_dataWidth;
private float vx = 0;
private float vy = 0;
private float m_scale = 1.0f;

private boolean m_lineSerach = false;
private float   m_alpha;
private float   m_beta;

private float   m_gamma;
private float   m_gammaPrev;

protected float[] m_errs;
    
public CoordinateDescent (LossFunctionBase lossFunction) 
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

    m_errs    = new float[m_agentCnt];    
    m_imageId = new int[m_agentCnt];

    float sum_error = 0.0f;
    
    m_currGlobalError = 1.0f;
    
    for (int i = 0; i < m_agentCnt; i++)
        {
        m_xPrev[i] = m_x[i] = m_dataWidth * rnd.nextFloat();
        m_yPrev[i] = m_y[i] = m_dataHeight * rnd.nextFloat();
        float err = m_lossFunction.getErrorValue(m_x[i], m_y[i]);
        
        m_errs[i] = err;
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
    int eaven_step;
    int odd_step;
    
    for (int i = 0; i < m_agentCnt; i++)
        {

        if (m_tickCount % 2 == 0) {
            
            eaven_step = 1;
            odd_step = 0;
            
        } else {
            
            eaven_step = 0;
            odd_step = 1;
        }
            
        vx = m_x[i]-m_xPrev[i];
        vy = m_y[i]-m_yPrev[i];
        m_xPrev[i] = m_x[i];
        m_yPrev[i] = m_y[i];
        Vector2d g = m_lossFunction.getGradient(m_x[i], m_y[i]);
            
        float delta_x = (m_tickCount % 2 == 0) ? g.x : 0;
        float delta_y = (m_tickCount % 2 != 0) ? g.y : 0;
        
        if (m_lineSerach)
            {
            float val0 = m_lossFunction.getValue(m_x[i], m_y[i]);
            float step = 1.0f/m_scale;

            float grad_delta0 = ((g.x*delta_x) + (g.y*delta_y));
            
            int j = 0;
            //while satisfies Wolfe confitions
            while (m_lossFunction.getValue(m_x[i]+(g.x*step*eaven_step), m_y[i]+(g.y*step*odd_step)) < val0 + (step*m_alpha * grad_delta0)) 
            {
                step = step * m_beta;
                //just in case escape forever loop
                if (j++ > 100)
                    {
                    break;
                    }
                }
                if (m_tickCount % 2 == 0) {
                    delta_x = step*delta_x;
                } else {
                    delta_y = step*delta_y;
                }
            }
        else {       
            if (m_tickCount % 2 == 0) {
                delta_x = m_gamma*g.x/m_scale;
            } else {
                delta_y = m_gamma*g.y/m_scale;
            }
        }
        
          
        //fix the deltas, so that we dont "fly away" into forever increecing zig-zag.
        if (m_dataWidth/2 <  Math.abs(delta_x) || m_dataHeight/2 < Math.abs(delta_y))  
            {
            if (m_tickCount % 2 == 0) {
               delta_x = delta_x/(m_dataWidth/2);
            } else {
               delta_y = delta_y/(m_dataHeight/2);
            }
          
            }
        
            if (m_tickCount % 2 == 0) {
               m_x[i] += delta_x + vx;

            } else {
               m_y[i] += delta_y + vy;
            }
        
        
        
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
 
} //eof class CoordinateDescent

