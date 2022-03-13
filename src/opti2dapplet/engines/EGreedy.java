/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.engines;
import opti2dapplet.ParametricBase;
import opti2dapplet.surfaces.LossFunctionBase;
import opti2dapplet.surfaces.NormalKHandedBandit;

/**
 *
 * @author Dalius
 */
public class EGreedy extends EngineBase
{
private int m_dataWidth;
private int m_dataHeight;
private int m_best;
private float m_max;

private float[] m_q;
private int[] m_k;
private int m_optCnt;

private NormalKHandedBandit m_dist;

    
public EGreedy (NormalKHandedBandit lossFunction) 
    {
    super (lossFunction);
    m_dist = lossFunction;
    ReadAndValidateProps ();
    }

@Override    
public void initialize ()
    {
    m_tickCount = 0;
    m_dataWidth  = m_lossFunction.getWidth();
    m_dataHeight = m_lossFunction.getHeight();
            
    m_agentCnt = m_dataWidth*2;
    
    m_q      = new float[m_dataWidth];
    m_k      = new int[m_dataWidth];
    
    m_x      = new float[m_agentCnt];
    m_y      = new float[m_agentCnt];
    
    m_xPrev  = new float[m_agentCnt];
    m_yPrev  = new float[m_agentCnt];

    m_imageId = new int[m_agentCnt];
    m_optCnt = 0;
    m_max = 0;

    for (int i = 0; i < m_dataWidth; i++)
        {
        m_xPrev[i] = m_x[i] = i;
        m_yPrev[i] = m_y[i] = (float)(m_dist.m_mu[i] + (rnd.nextGaussian()*m_dist.m_std[i]));
        m_imageId[i] = 1;
        }
    
    for (int i = m_dataWidth; i < m_agentCnt; i++)
        {
        float expected = m_dataHeight * rnd.nextFloat();
        m_xPrev[i] = m_x[i] = i-m_dataWidth;
        m_yPrev[i] = m_y[i] = m_q[i-m_dataWidth] = expected;
        m_imageId[i] = 0;
        m_k[i-m_dataWidth] = 0;
        
        if (m_max < expected)
            {
            m_max = expected;
            m_best = i - m_dataWidth;
            }
        }    
    }

@Override
public void tick ()
    {
    for (int i = 1; i < 1000; i ++)
        {
        int pick = m_best;
        if (0.1f > rnd.nextFloat())
            {
            pick = rnd.nextInt(m_dataWidth);
            }

        m_yPrev[pick] = m_y[pick] = (float)(m_dist.m_mu[pick] + (rnd.nextGaussian()*m_dist.m_std[pick]));    

        float reward = m_y[pick];
        m_k[pick] ++;

        m_q[pick] = m_q[pick] + ((1.0f/(float)m_k[pick])*(reward-m_q[pick]));
        m_yPrev[pick+m_dataWidth] = m_y[pick+m_dataWidth] = m_q[pick];

        
        for (int j = 0; j < m_dataWidth; j++)
            {
            if (m_q[j] > m_q[m_best])
                {
                m_best = j;
                }
            }
        
        if (pick > 199 && pick < 201) {m_optCnt++;}
        m_tickCount ++;
        }
    m_currGlobalError = ((float)m_optCnt/(float)m_tickCount);
    }

    /**
     *
     * @return
     */
    @Override
    protected final boolean ReadAndValidateProps ()
        {
        return true;
        }

    @Override
    public void enableProperties (boolean enable)
        {
        }

} //eof class
