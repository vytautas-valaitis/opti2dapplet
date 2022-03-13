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
 * @author vytautas
 */
public class StochasticHillClimbing extends EngineBase 
{
    private ParametricBase.BasicNumericProperty m_prAgentCnt  = new ParametricBase.BasicNumericProperty (this, int.class,     "Agents",  "Settings", 40,    false, 2,    5000);
    private ParametricBase.BasicNumericProperty m_prStep  = new ParametricBase.BasicNumericProperty (this, float.class,     "Step",  "Settings", 10.0f,    false, 1.0f,    100.0f);
    private ParametricBase.BasicNumericProperty m_prSearch  = new ParametricBase.BasicNumericProperty (this, boolean.class,     "Show Search",  "Settings", false,    false, false,    true);
    
    private int m_dataHeight;
    private int m_dataWidth;
    private float m_step;
    private boolean m_search;

    protected float[] m_errs;
    
public StochasticHillClimbing (LossFunctionBase lossFunction) 
    {
    super(lossFunction);
    ReadAndValidateProps ();
    }
    
@Override    
public void initialize ()
    {
    m_tickCount = 0;

    m_dataHeight = m_lossFunction.getHeight();
    m_dataWidth  = m_lossFunction.getWidth();

    m_x      = new float[m_agentCnt];
    m_y      = new float[m_agentCnt];    

    m_xPrev  = new float[m_agentCnt];
    m_yPrev  = new float[m_agentCnt];

    m_imageId = new int[m_agentCnt]; //butina initializuoti, kitaip nerodys agentu
    m_errs   = new float[m_agentCnt];

    float sum_error = 0.0f;

    for (int i = 0; i < m_agentCnt; i++)
        {
        m_xPrev[i] = m_x[i] = m_dataWidth * rnd.nextFloat();
        m_yPrev[i] = m_y[i] = m_dataHeight * rnd.nextFloat();
        
        m_errs[i] = m_lossFunction.getErrorValue(m_x[i], m_y[i]);
        sum_error += m_errs[i];
        }
    
    m_currAvverageError = sum_error/m_agentCnt;
    m_currGlobalError = sum_error;
    //JOptionPane.showMessageDialog(null, sum_error);
    }

@Override
public void tick ()
    {
    float error;
    float sum_error = 0.0f;
    float testX = 0.0f, testY = 0.0f;
    
    for (int i = 0; i < m_agentCnt; i++)
    {
        if (m_search) {
            m_xPrev[i] = m_x[i] + ((float)rnd.nextGaussian()) * m_step;
            m_yPrev[i] = m_y[i] + ((float)rnd.nextGaussian()) * m_step;
            error =  m_lossFunction.getErrorValue(m_xPrev[i], m_yPrev[i]);
        } else {
            testX = m_x[i] + ((float)rnd.nextGaussian()) * m_step;
            testY = m_y[i] + ((float)rnd.nextGaussian()) * m_step;
            error =  m_lossFunction.getErrorValue(testX, testY);
        }
        
        //test against loss function
        

        if (error < m_errs[i]) //update local best position
        {
            if (m_search) {
                float x = m_xPrev[i];
                float y = m_yPrev[i];
                m_x[i] = m_xPrev[i];
                m_y[i] = m_yPrev[i];
                m_xPrev[i] = x;
                m_yPrev[i] = y;
            } else {
                m_xPrev[i] = m_x[i];
                m_yPrev[i] = m_y[i];
                m_x[i] = testX;
                m_y[i] = testY;
                
                
            }         
            m_errs[i] = error;
        }
        
        sum_error += m_errs[i];
        }
    
        m_currAvverageError = sum_error/m_agentCnt;
        m_currGlobalError = sum_error;
        m_tickCount ++;
    
    }

    @Override
    protected final boolean ReadAndValidateProps ()
    {
        m_agentCnt = m_prAgentCnt.getIntValue();
        m_step = m_prStep.getFloatValue();
        m_search = m_prSearch.getBoolValue();
        return true;
    }

    @Override
    public void enableProperties(boolean enable)
    {
        m_prAgentCnt.setEditable(enable);
        m_prStep.setEditable(enable);
        m_prSearch.setEditable(enable);
    }


    
} //eof class GradientDescentEngine
