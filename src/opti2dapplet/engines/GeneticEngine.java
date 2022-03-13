/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.engines;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import opti2dapplet.ParametricBase;
import opti2dapplet.surfaces.LossFunctionBase;

/**
 *
 * @author Dalius
 */
public class GeneticEngine extends EngineBase
{

private ParametricBase.BasicNumericProperty m_prAgentCnt  = new ParametricBase.BasicNumericProperty (this, int.class,     "Agents",  "Settings", 40,    false, 2,    5000);
private ParametricBase.BasicNumericProperty m_prSigma     = new ParametricBase.BasicNumericProperty (this, float.class,   "Sigma",   "Settings", 15.0f, false, 0.0f, 1000.0f);
private DeathRatePercentProperty            m_prDeathRate = new DeathRatePercentProperty(this, "Survival (%)", "Settings", 50, false);

private int m_dataHeight;
private int m_dataWidth;

private float   m_sigma;
private int     m_surviversCnt;

private List<ErrIdPair> m_errs;
private ErrIdPairComparator m_comparator = new ErrIdPairComparator();
    
public GeneticEngine (LossFunctionBase lossFunction) 
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

    m_errs   = new ArrayList<ErrIdPair>();    
    
    m_x      = new float[m_agentCnt];
    m_y      = new float[m_agentCnt];
    
    m_xPrev  = new float[m_agentCnt];
    m_yPrev  = new float[m_agentCnt];

    m_imageId = new int[m_agentCnt];

    float sum_error = 0.0f;
    
    for (int i = 0; i < m_agentCnt; i++)
        {
        m_xPrev[i] = m_x[i] = m_dataWidth * rnd.nextFloat();
        m_yPrev[i] = m_y[i] = m_dataHeight * rnd.nextFloat();
        
        float err  = m_lossFunction.getErrorValue(m_x[i], m_y[i]);
      
        m_errs.add (new ErrIdPair(err, i));
        sum_error += err;
        }
    
    Collections.sort(m_errs, m_comparator);
    
    m_currAvverageError = sum_error/m_agentCnt;
    m_currGlobalError = m_errs.get(0).m_error;
    }

@Override
public void tick ()
    {
    float sum_error = 0.0f;
    
    int parentIdx = 0;
    
    for (int n = 0; n < m_agentCnt; n++)
        {
        ErrIdPair p = m_errs.get(n);
        int id = p.m_id;
        if (n < m_surviversCnt)
            {
            m_imageId[id] = 0;
            sum_error += p.m_error;
            continue;
            }
        int mumId = m_errs.get(parentIdx).m_id;
        
        float rndX = (float)rnd.nextGaussian()*m_sigma;
        float rndY = (float)rnd.nextGaussian()*m_sigma;

        m_imageId[id] = 1;
        
        m_x[id] = m_x[mumId] + rndX;
        m_y[id] = m_y[mumId] + rndY;
        
        m_xPrev[id] = m_x[mumId];
        m_yPrev[id] = m_y[mumId];
       
        p.m_error = m_lossFunction.getErrorValue(m_x[id], m_y[id]);;
         
        parentIdx ++;
        if (m_surviversCnt == parentIdx) {parentIdx = 0;}
        }
    
    Collections.sort(m_errs, m_comparator);
    m_currAvverageError = sum_error/m_agentCnt;
    m_currGlobalError = m_errs.get(0).m_error;
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
        m_sigma     =  m_prSigma.getFloatValue();
        
        int deathrate = m_prDeathRate.getIntValue();
        int cnt = (int)((deathrate/100.f)*m_agentCnt + 0.5f);
        if (cnt < 1) {cnt = 1;}
        if (cnt >= m_agentCnt) {cnt = m_agentCnt - 1;}
        m_surviversCnt = cnt;
        
        return true;
        }

    @Override
    public void enableProperties (boolean enable)
        {
        m_prAgentCnt.setEditable(enable);
        }
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    private class ErrIdPair
    //</editor-fold>
        {
        public ErrIdPair (float error, int id)
            {
            m_error = error;
            m_id = id;
            }
        float m_error;
        int m_id;
        }
    
    private class ErrIdPairComparator implements Comparator<ErrIdPair>
        {
        @Override
        public int compare(ErrIdPair pair1, ErrIdPair pair2) 
            {
            if (pair1.m_error > pair2.m_error) { return  1;}
            if (pair1.m_error < pair2.m_error) { return -1;}
            return 0;
            }
        }
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>
    private static class DeathRatePercentProperty extends BasicNumericProperty
    {
     
     public DeathRatePercentProperty(GeneticEngine parent, String name,  String category, Object value, boolean readonly)
          {
          super (parent, int.class, name, category, value, readonly);
          }
    
     
    @Override
     public void setValue(Object Value)
        {
        int num = ((Number) Value).intValue();
        if (num > 99)  { num = 99;}
        if (num < 1)  { num = 1;}
 
        GeneticEngine parent = (GeneticEngine) m_parent;
        
        int cnt = (int)((num/100.f)*parent.m_agentCnt + 0.5f);
        if (cnt < 1)
            {
            num = (int)((100.0f/parent.m_agentCnt) + 0.5f);
            }
        else if (cnt >= parent.m_agentCnt)
            {
            num = (int)((100.0f-(100.0f/parent.m_agentCnt)) + 0.5f);
            }
        
        Object oldValue = super.getValue();
        super.setValue(num);
        if (!parent.ReadAndValidateProps ())
            {
               super.setValue(oldValue);
            }         
        }
    } // eof class DeathRatePercentProperty
} //eof class GeneticEngine
