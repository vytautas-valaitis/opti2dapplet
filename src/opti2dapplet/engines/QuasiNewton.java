/**
 *
 * @author Dalius
 */
package opti2dapplet.engines;
import opti2dapplet.ParametricBase;
import opti2dapplet.surfaces.LossFunctionBase;
import opti2dapplet.surfaces.LossFunctionBase.Vector2d;
import opti2dapplet.surfaces.LossFunctionBase.Matrix2x2;

/**
 *
 * @author Dalius
 */
public class QuasiNewton extends EngineBase
{
public enum QNMethod {DFP, BFGS, Broyden, SR1}
private String [] m_methods  = {"DFP", "BFGS", "Broyden", "SR1"};

private ParametricBase.BasicNumericProperty m_prAgentCnt      = new ParametricBase.BasicNumericProperty (this, int.class,      "agents",           "Settings", 40,    false, 1,    5000);
private ParametricBase.BasicNumericProperty m_prGamma         = new ParametricBase.BasicNumericProperty (this, float.class,    "step",             "Settings", 0.001f,  false, 0.0f, 10000.0f);
private ParametricBase.PickListProperty     m_prMethod        = new ParametricBase.PickListProperty     (this,                 "method",           "Settings", m_methods, 3, false);

private ParametricBase.BasicNumericProperty m_prLineSearch    = new ParametricBase.BasicNumericProperty (this, boolean.class,  "line search",     "Backtracking", false,  false);
private ParametricBase.BasicNumericProperty m_prAlpha         = new ParametricBase.BasicNumericProperty (this, float.class,    "alpha",           "Backtracking", 0.001f, false, 0,    0.5f);
private ParametricBase.BasicNumericProperty m_prBeta          = new ParametricBase.BasicNumericProperty (this, float.class,    "beta",            "Backtracking", 0.9f,   false, 0.0f, 1.0f);

private QNMethod m_method = QNMethod.DFP;

private int m_dataHeight;
private int m_dataWidth;

private float   m_scale = 1.0f;
private float   m_gamma;
private float   m_gammaPrev;
private float   m_alpha;
private float   m_beta;
private boolean m_lineSerach = false;

private Vector2d[] m_grad;
private Matrix2x2[]  m_Binv;
   
public QuasiNewton (LossFunctionBase lossFunction) 
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
    
    m_method = QNMethod.values()[m_prMethod.getIntValue()];
    
    m_x      = new float[m_agentCnt];
    m_y      = new float[m_agentCnt];
   
    m_xPrev  = new float[m_agentCnt];
    m_yPrev  = new float[m_agentCnt];
    
    m_imageId = new int[m_agentCnt];
    
    m_Binv = new Matrix2x2[m_agentCnt];
    m_grad = new Vector2d[m_agentCnt];
    
    float sum_error = 0.0f;
    
    m_currGlobalError = 1.0f;
    
    for (int i = 0; i < m_agentCnt; i++)
        {
        m_xPrev[i] = m_x[i] = m_dataWidth * rnd.nextFloat();
        m_yPrev[i] = m_y[i] = m_dataHeight * rnd.nextFloat();
        
        m_Binv[i] = new Matrix2x2 (-1.0f,-1.0f,0.0f);        
        m_grad[i] = m_lossFunction.getGradient(m_x[i], m_y[i]);
        
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
    
    float [] dx = new float[1];
    float [] dy = new float[1];
    
    for (int i = 0; i < m_agentCnt; i++)
        {
        m_xPrev[i] = m_x[i];
        m_yPrev[i] = m_y[i];            

        // <editor-fold defaultstate="collapsed" desc="second derivative test">       
        float det = ((m_Binv[i].c11*m_Binv[i].c22)-(m_Binv[i].c12*m_Binv[i].c21));
        if (det > 0 && m_Binv[i].c11 < 0) //looking for local maximum
            {
            m_imageId[i] = 0;
            }        
        else if (det < 0) //saddle point
            {
            m_imageId[i] = 1; 
            }
        else if (det > 0 && m_Binv[i].c11 > 0) //looking for local minimum
            {
            m_imageId[i] = 2; 
            }
        else if (det == 0) //then the second derivative test is inconclusive.
            {
            m_imageId[i] = 3;
            } 
        // </editor-fold>
        
        float delta_x = - ((m_grad[i].x*m_Binv[i].c11) + (m_grad[i].y*m_Binv[i].c21));
        float delta_y = - ((m_grad[i].x*m_Binv[i].c12) + (m_grad[i].y*m_Binv[i].c22));
        
        if (m_lineSerach)
            {
            float val0 = m_lossFunction.getValue(m_x[i], m_y[i]);
            float step = 1.0f/m_scale;

            float grad_delta0 = ((m_grad[i].x*delta_x) + (m_grad[i].y*delta_y));
            
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
            delta_y = step*delta_y;  /**/          
            }
        else
            {
            delta_x = m_gamma*delta_x/m_scale;
            delta_y = m_gamma*delta_y/m_scale;
            }
        
        m_x[i] += delta_x;
        m_y[i] += delta_y;
        
        //calc next B for next iteration
        Vector2d g = m_lossFunction.getGradient(m_x[i], m_y[i]);
        Vector2d dY = new Vector2d(g.x - m_grad[i].x, g.y - m_grad[i].y);
        Vector2d dX = new Vector2d(delta_x, delta_y);
        m_grad[i] = g;
        
        if (QNMethod.DFP == m_method)
            {
            doDFP (m_Binv[i], dX, dY);
            }
        else if (QNMethod.BFGS == m_method)
            {
            doBFGS (m_Binv[i], dX, dY);
            }
        else if (QNMethod.Broyden == m_method)
            {
            doBroyden (m_Binv[i], dX, dY);
            }
        else if (QNMethod.SR1 == m_method)
            {
            doSR1 (m_Binv[i], dX, dY);
            }        
        
        //do metrix..
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

//<editor-fold defaultstate="collapsed" desc="comment">
            //</editor-fold>
    private static void doDFP (Matrix2x2 h, Vector2d dX, Vector2d dY)
        {
        float a = dY.dotProduct(dX);
        float b = dY.multiply(h).dotProduct(dY);
        if (0 == a || 0 == b)
            {
            return;
            }

        Matrix2x2 ht = new Matrix2x2(h);
        ht.transpose();
        
        Matrix2x2 m1 = dX.multiply(dX);
        m1.multiplySelf(1.0f/a);
        
        Matrix2x2 m2 = h.multiply(dY).multiply(dY).multiply(ht);
        m2.multiplySelf(-1.0f/b);
        
        h.addSelf(m1);
        h.addSelf(m2);
        }
    
    //<editor-fold defaultstate="collapsed" desc="comment">
            //</editor-fold>
    private static void doBFGS (Matrix2x2 h, Vector2d dX, Vector2d dY)
        {
        float a = dY.dotProduct(dX);
        if (0 == a)
            {
            return;
            }

        Matrix2x2 m0 = new Matrix2x2(1, 0, 0, 1);
        Matrix2x2 m1 = dY.multiply(dX);
        m1.multiplySelf(-1.0f/a);
        m0.addSelf(m1);
        m1.set(m0);
        m1.transpose();
        
        Matrix2x2 h0 = m1.multiply(h).multiply(m0);
        
        m1 = dX.multiply(dX);
        m1.multiplySelf(1.0f/a);
        
        h0.addSelf(m1);
        h.set (h0);
        }    

    //<editor-fold defaultstate="collapsed" desc="comment">
            //</editor-fold>
    private static void doBroyden (Matrix2x2 h, Vector2d dX, Vector2d dY)
        {
        float a = dX.multiply(h).dotProduct(dY);;
        if (0 == a)
            {
            return;
            }

        Vector2d v0 = h.multiply(dY);
        v0.x = dX.x - v0.x;
        v0.y = dX.y - v0.y;
        Matrix2x2 m0 = v0.multiply(dX).multiply(h);
        m0.multiplySelf(1.0f/a);
        h.addSelf(m0);
        }    
    
    //<editor-fold defaultstate="collapsed" desc="comment">
            //</editor-fold>
    private static void doSR1 (Matrix2x2 h, Vector2d dX, Vector2d dY)
        {
        Vector2d v0 = h.multiply(dY);
        v0.x = dX.x - v0.x;
        v0.y = dX.y - v0.y;
        
        float a = v0.dotProduct(dY);
        if (0 == a)
            {
            return;
            }        
        
        Matrix2x2 m0 = v0.multiply(v0);
        m0.multiplySelf(1.0f/a);
        h.addSelf(m0);
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
        m_prMethod.setEditable(enable);
        }
 
} //eof class GradientDescent

