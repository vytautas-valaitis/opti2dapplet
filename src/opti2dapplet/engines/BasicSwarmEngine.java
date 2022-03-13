/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.engines;
import com.l2fprod.common.propertysheet.Property;
import opti2dapplet.surfaces.LossFunctionBase;

/**
 *
 * @author Dalius
 */
public class BasicSwarmEngine extends EngineBase
{
private BasicNumericProperty m_prGrpCnt = new BasicNumericProperty (this, int.class, "Groups", "Swarm size", 1, false, 1, 5000);
private BasicNumericProperty m_prAgentsPerGrpCnt = new BasicNumericProperty (this, int.class, "Agents in Group", "Swarm size", 40, false, 1, 5000);
private BasicNumericProperty m_prAgentCnt = new BasicNumericProperty (this, int.class, "Agents", "Swarm size", 40, true);

private BasicNumericProperty m_prW0 = new BasicNumericProperty (this, float.class, "momentum",      "Weights", 0.99f, false, -10.0f, 10.0f);
private BasicNumericProperty m_prW1 = new BasicNumericProperty (this, float.class, "group",         "Weights", 0.04f, false, -10.0f, 10.0f);
private BasicNumericProperty m_prW2 = new BasicNumericProperty (this, float.class, "self",          "Weights", 0.01f, false, -10.0f, 10.0f);
private BasicNumericProperty m_prW3 = new BasicNumericProperty (this, float.class, "global",        "Weights", 0.0f,   false, -10.0f, 10.0f);
private BasicNumericProperty m_prW4 = new BasicNumericProperty (this, float.class, "random",        "Weights", 0.0f,   false, -10.0f, 10.0f);
private BasicNumericProperty m_prW5 = new BasicNumericProperty (this, float.class, "common factor", "Weights", 1.0f,   false, -10.0f, 10.0f);

private int m_groupCnt  = 100;
private int m_agentsPerGroupCnt = 10;

private float[] m_w;
private Property [] m_wProps;

private int m_dataHeight;
private int m_dataWidth;


protected int[] m_grpIds;

protected float[] m_errs;

protected float[] m_bx;
protected float[] m_by;
protected float[] m_vx;
protected float[] m_vy;

protected float[] m_xGrp;
protected float[] m_yGrp;
protected float[] m_errsGrp;

protected int m_topGroup; //top group contains global error
    
public BasicSwarmEngine (LossFunctionBase lossFunction) 
    {
    super (lossFunction);
    m_wProps = new Property[6];
    m_w = new float[6];
    
    m_wProps[0] = m_prW0; 
    m_wProps[1] = m_prW1; 
    m_wProps[2] = m_prW2; 
    m_wProps[3] = m_prW3; 
    m_wProps[4] = m_prW4; 
    m_wProps[5] = m_prW5; 

    m_agentCnt = m_groupCnt * m_agentsPerGroupCnt;
    
    ReadProps(false);
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
    
    m_bx      = new float[m_agentCnt];
    m_by      = new float[m_agentCnt];
    m_vx      = new float[m_agentCnt];
    m_vy      = new float[m_agentCnt];
    m_errs    = new float[m_agentCnt];
    m_imageId = new int[m_agentCnt];
    m_grpIds  = new int[m_agentCnt];    
  
    m_xGrp    = new float[m_groupCnt];
    m_yGrp    = new float[m_groupCnt];
    m_errsGrp = new float[m_groupCnt];
    
    m_topGroup = 0;
    
    float sum_error = 0.0f;
    
    int i = 0;
    for (int n = 0; n < m_groupCnt; n ++)
        {
        m_errsGrp[n] = 1.0f;
        m_xGrp[n] = m_yGrp[n] = 0;

        for (int k = 0; k < m_agentsPerGroupCnt; k ++)
            {
            m_grpIds[i] = n;
            m_xPrev[i] = m_bx[i] = m_x[i] = m_dataWidth * rnd.nextFloat();
            m_yPrev[i] = m_by[i] = m_y[i] = m_dataHeight * rnd.nextFloat();
            m_vx[i] = (2.0f*rnd.nextFloat()) - 1.0f;
            m_vy[i] = (2.0f*rnd.nextFloat()) - 1.0f;

            m_errs[i] = m_lossFunction.getErrorValue(m_x[i], m_y[i]);
            sum_error += m_errs[i];
            
            if (m_errsGrp[n] > m_errs[i])
                {
                m_xGrp[n] =  m_x[i];
                m_yGrp[n] =  m_y[i];
                m_errsGrp[n] = m_errs[i];

                if (n != m_topGroup && m_errsGrp[m_topGroup] > m_errsGrp[n])
                    {
                    m_topGroup = n;
                    }
                }

            i++;
            }
        } 
    
    m_currAvverageError = sum_error/m_agentCnt;
    m_currGlobalError = m_errsGrp[m_topGroup];    
    }

@Override
public void tick ()
    {
    float sum_error = 0.0f;
    for (int i = 0; i < m_agentCnt; i++)
        {
        int grpId = m_grpIds[i];
        
        float wx1 = rnd.nextFloat();
        float wx2 = rnd.nextFloat();
        float wx3 = rnd.nextFloat();
        float wx4 = (2.0f*rnd.nextFloat()) - 1.0f;
        
        float wy1 = rnd.nextFloat();
        float wy2 = rnd.nextFloat();
        float wy3 = rnd.nextFloat();
        float wy4 = (2.0f*rnd.nextFloat()) - 1.0f;     
        
        //remember previous position
        m_xPrev[i] = m_x[i];
        m_yPrev[i] = m_y[i];
        
        //Calc velocity
        m_vx[i] = m_w[0]*m_vx[i] + m_w[1]*wx1*(m_xGrp[grpId]-m_x[i]) + m_w[2]*wx2*(m_bx[i]-m_x[i]) + m_w[3]*wx3*(m_xGrp[m_topGroup]-m_x[i])+m_w[4]*wx4;
        m_vy[i] = m_w[0]*m_vy[i] + m_w[1]*wy1*(m_yGrp[grpId]-m_y[i]) + m_w[2]*wy2*(m_by[i]-m_y[i]) + m_w[3]*wy3*(m_yGrp[m_topGroup]-m_y[i])+m_w[4]*wy4;
        
        //update position
        m_x[i] += m_w[5]*m_vx[i];
        m_y[i] += m_w[5]*m_vy[i];
        
        float error = m_lossFunction.getErrorValue(m_x[i], m_y[i]);

        if (error < m_errs[i]) //update local best position
            {
            m_errs[i] = error;
            m_bx[i] = m_x[i];
            m_by[i] = m_y[i];
            
            if (m_errsGrp[grpId] > error) //update group best position
                {
                m_errsGrp[grpId] = error;
                m_xGrp [grpId] = m_x[i];
                m_yGrp [grpId] = m_y[i];
                
                //update global best position
                if (m_topGroup != grpId && m_errsGrp[m_topGroup]  > error)
                    {
                    m_topGroup = grpId;
                    }
                }
            }
        
        sum_error += m_errs[i];
        }
    
    m_currAvverageError = sum_error/m_agentCnt;
    m_currGlobalError = m_errsGrp[m_topGroup];
    m_tickCount ++;
    }

    /**
     *
     * @return
     */
    @Override
    protected final boolean ReadAndValidateProps ()
        {
        return ReadProps (true);
        }
     
    /**
     *
     * @param validate
     * @return
     */
    private boolean ReadProps (boolean validate)
    {
    for (int i = 0; i < 6; i ++)
        {
        m_w[i] = ((BasicNumericProperty)m_wProps[i]).getFloatValue();  
        }
    
    int grp = m_prGrpCnt.getIntValue();
    int agentsPerGrp = m_prAgentsPerGrpCnt.getIntValue();
   
    if (validate && 5000 < grp*agentsPerGrp)
        {
        return false;
        }
    
    m_groupCnt = grp;
    m_agentsPerGroupCnt = agentsPerGrp;
    m_agentCnt = m_groupCnt * m_agentsPerGroupCnt;
    m_prAgentCnt.setValueNoValidate(m_agentCnt);
    return true;
    }    

@Override
public void enableProperties (boolean enable)
    {
    m_prGrpCnt.setEditable(enable);
    m_prAgentsPerGrpCnt.setEditable(enable);
    }
}
