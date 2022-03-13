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
import opti2dapplet.surfaces.LossFunctionBase.Matrix2x2;
import opti2dapplet.surfaces.LossFunctionBase.Vector2d;

/**
 *
 * @author Dalius
 */
public class CMA_ES extends EngineBase
{
private ParametricBase.BasicNumericProperty m_prGroupCnt       = new ParametricBase.BasicNumericProperty (this, int.class,      "Groups",          "Settings", 1,    false, 1,  50);
private ParametricBase.BasicNumericProperty m_prAgentInGrpCnt  = new ParametricBase.BasicNumericProperty (this, int.class,      "Agents in group", "Settings", 40,   false, 4,  100);
private ParametricBase.BasicNumericProperty m_prSurvRate       = new ParametricBase.BasicNumericProperty (this, int.class,      "Survival (%)",    "Settings", 50,   false, 1,  100);
private ParametricBase.BasicNumericProperty m_prShowContour    = new ParametricBase.BasicNumericProperty (this, boolean.class,  "Show contour","Settings", true, false);

private ParametricBase.BasicNumericProperty m_prAdaptVar      = new ParametricBase.BasicNumericProperty (this, boolean.class,  "Adapt variance",     "Strategy", true, false);
private ParametricBase.BasicNumericProperty m_prAdaptCovar    = new ParametricBase.BasicNumericProperty (this, boolean.class,  "Adapt covariation",  "Strategy", true, false);

private int m_dataHeight;
private int m_dataWidth;

private boolean m_sigmaOn = true;
private boolean m_covarOn = true;
private boolean m_showContour;

public  Vector2d[] m_circle;
public  Vector2d[] m_contours;
public  final static int CONTOUR_SZ = 100;

public  int m_grpCnt;
private int m_popCnt;
private int m_parentCnt;

private float[] m_sigmas;
private float[] m_w;

public boolean[] m_skipContour;

//strategy paremeter settings: Adaptation --->
private float m_mueff;  // variance-effectiveness of sum w_i x_i
private float m_cc; // time constant for cumulation for C
private float m_cc1; // sqrt(cc*(2-cc)*mueff)
private float m_cs; // t-const for cumulation for sigma control
private float m_cs1; // sqrt(cs*(2-cs)*mueff)
private float m_c1; // learning rate for rank-one update of C
private float m_cmu; // and for rank-mu update
private float m_damps; // damping for sigma 
private float m_chiN; //expectation of ||N(0,I)|| == norm(randn(N,1))
//<------------          

private Vector2d[] m_ps;
private Vector2d[] m_pc;

private Matrix2x2[] m_C;
private Matrix2x2[] m_invsqrtC;

private List<FitnessIdPair>[] m_fitness;

private FitnessIdPairComparator m_comparator = new FitnessIdPairComparator();
    
public CMA_ES (LossFunctionBase lossFunction) 
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
    
    m_sigmaOn = m_prAdaptVar.getBoolValue();
    m_covarOn = m_prAdaptCovar.getBoolValue();
    m_showContour = m_prShowContour.getBoolValue();    

    m_grpCnt    = m_prGroupCnt.getIntValue();
    m_popCnt    = m_prAgentInGrpCnt.getIntValue();
    int surv    = m_prSurvRate.getIntValue();
    m_parentCnt = (int) (m_popCnt * (surv/100.0f));
    m_agentCnt  = m_grpCnt*(m_popCnt+1);
    
    //init circle
    m_circle = new Vector2d[CONTOUR_SZ];
    m_contours = new Vector2d[CONTOUR_SZ*m_grpCnt];
    for (int i = 0; i < CONTOUR_SZ; i++)
        {
        m_circle[i] = new Vector2d ((float)Math.sin(i*3.6*0.0174532925)*2, (float)Math.cos(i*3.6*0.0174532925)*2);
        }

    for (int j = 0; j < CONTOUR_SZ*m_grpCnt; j++)
        {
        m_contours[j] = new Vector2d ();
        }        

    //init weights
    m_w = new float[m_parentCnt];
    float sumW = 0;
    for (int i = 0; i < m_parentCnt; i ++)
        {
        m_w[i] = (float)(Math.log(m_parentCnt+0.5) - Math.log(i+1));
        sumW += m_w[i];
        }
    //normalize weights
    float sum_w = 0;
    float sum_sqw = 0;
    for (int i = 0; i < m_parentCnt; i ++)
        {
        m_w[i] = m_w[i]/sumW;
        sum_w += m_w[i];
        sum_sqw += m_w[i]*m_w[i];
        }
    
    //strategy paremeter settings: Adaptation
    float N = 2; //dimensions
    m_mueff = (sum_w*sum_w)/sum_sqw;
    m_cc = (4+(m_mueff/N)) / (N+4 + (2*m_mueff/N)); // time constant for cumulation for C
    m_cc1 = (float)Math.sqrt(m_cc*(2-m_cc)*m_mueff);
    m_cs = (m_mueff+2) / (N+m_mueff+5);  // t-const for cumulation for sigma control
    m_cs1 = (float)Math.sqrt(m_cs*(2-m_cs)*m_mueff);
    m_c1 = 2 / (((N+1.3f)*(N+1.3f))+m_mueff);  // learning rate for rank-one update of C
    m_cmu = 2 * (m_mueff-2+(1/m_mueff)) / (((N+2)*(N+2))+m_mueff);  // and for rank-mu update
    float sq = (float)Math.sqrt((m_mueff-1)/(N+1));
    m_damps = 1 + m_cs + 2*((sq>1) ? sq-1 : 0); // damping for sigma 
    m_chiN = (float)Math.sqrt (N)*(1-(1/(4*N))+(1/(21*N*N))); //expectation of ||N(0,I)|| == norm(randn(N,1))
    
    //init groups and members
    m_x       = new float[m_agentCnt];
    m_y       = new float[m_agentCnt];
    m_imageId = new int[m_agentCnt];
    
    m_xPrev  = new float[m_grpCnt];
    m_yPrev  = new float[m_grpCnt];
    m_sigmas = new float[m_grpCnt];
    m_fitness = new List[m_grpCnt];
    
    m_skipContour = new boolean[m_grpCnt];
    
    m_ps = new Vector2d[m_grpCnt];
    m_pc = new Vector2d[m_grpCnt];
    m_C = new Matrix2x2[m_grpCnt];
    m_invsqrtC = new Matrix2x2[m_grpCnt];
    
    float sum_error = 0.0f;
    for (int n = 0; n < m_grpCnt; n++)
        {
        m_xPrev[n] = m_x[n] = m_dataWidth  * rnd.nextFloat();
        m_yPrev[n] = m_y[n] = m_dataHeight * rnd.nextFloat();
        m_sigmas[n] = 10.5f;
        m_fitness[n] = new ArrayList<FitnessIdPair>();
        m_ps[n] = new Vector2d(0, 0);
        m_pc[n] = new Vector2d(0, 0);
        m_C[n] = new Matrix2x2(1, 0, 0, 1);
        m_invsqrtC[n] = new Matrix2x2(1, 0, 0, 1);
        m_skipContour[n] = false;
        
        for (int k = 0; k < m_popCnt; k ++)
            {
            int j = m_grpCnt + (n*m_popCnt) + k;
            m_imageId[j] = 2; //blue
            m_x[j] = m_x[n] + (float)(rnd.nextGaussian() * m_sigmas[n]);
            m_y[j] = m_y[n] + (float)(rnd.nextGaussian() * m_sigmas[n]);
            
            float err  = m_lossFunction.getErrorValue(m_x[j], m_y[j]);
            m_fitness[n].add(new FitnessIdPair(err, j));
            
            sum_error += err;
            m_currGlobalError = (0 == n && 0 == k) ? err : (m_currGlobalError > err) ? err : m_currGlobalError;            
            }
        
        for (int k = 0; k < CONTOUR_SZ; k++)
            {
            m_contours[(n*CONTOUR_SZ)+k].x = (m_sigmas[n]*m_circle[k].x) + m_x[n];
            m_contours[(n*CONTOUR_SZ)+k].y = (m_sigmas[n]*m_circle[k].y) + m_y[n];
            }
        }
   
    m_currAvverageError = sum_error/m_agentCnt;
    }

@Override
public void tick ()
    {
    float sum_error = 0.0f;
    Vector2d dmu = new Vector2d();
    Matrix2x2 dx = new Matrix2x2(0, 0, 0, 0);
    Matrix2x2 B = new Matrix2x2(0, 0, 0, 0);
    Matrix2x2 D = new Matrix2x2(0, 0, 0, 0);
    Matrix2x2 tmp = new Matrix2x2(0, 0, 0, 0);
    
    for (int n = 0; n < m_grpCnt; n++)
        {
        m_xPrev[n] = m_x[n];
        m_yPrev[n] = m_y[n];
        Collections.sort(m_fitness[n], m_comparator);
        
        m_x[n] = m_y[n] = 0;
        dx.c11=dx.c12=dx.c21=dx.c22=dx.det=0;
        //calc new mean from fitest
        for (int i = 0; i < m_parentCnt; i ++)
            {
            FitnessIdPair p = m_fitness[n].get(i);
            int id = p.m_id;
            m_x[n] += m_x[id]*m_w[i];
            m_y[n] += m_y[id]*m_w[i];
            
            float x = (m_x[id]-m_xPrev[n])/m_sigmas[n];
            float y = (m_y[id]-m_yPrev[n])/m_sigmas[n];
            
            dx.c11 += x*x*m_w[i];
            dx.c22 += y*y*m_w[i];
            dx.c12 += x*y*m_w[i];
            }
        
       if (m_x[n]+1 != m_x[n]+1 || m_y[n]+1 != m_y[n]+1)//NaN, Inf test
           {
           m_x[n] = m_xPrev[n];
           m_y[n] = m_yPrev[n];
           continue;
           }          
        
        dx.c21 = dx.c12;// ensure symmetry
        dx.multiplySelf(m_cmu);
        
        //Update evolution paths
        dmu.x = m_x[n] - m_xPrev[n];
        dmu.y = m_y[n] - m_yPrev[n];
        
        if (0 == dmu.x && 0 == dmu.y)
            {
            continue;
            }        
        
        Vector2d v0 = m_invsqrtC[n].multiply(dmu);
        v0.multiplySelf(m_cs1/m_sigmas[n]);
        m_ps[n].x = ((1-m_cs)*m_ps[n].x) + v0.x;
        m_ps[n].y = ((1-m_cs)*m_ps[n].y) + v0.y;
        
        float hsig = (m_ps[n].norm()/Math.sqrt(1-Math.pow(1-m_cs, 2*m_tickCount))/m_chiN < 1.4 + 2/3) ? 1.0f: 0.0f;
        v0.set (dmu);
        v0.multiplySelf(hsig*m_cc1/m_sigmas[n]);
        m_pc[n].x = ((1-m_cc)*m_pc[n].x) + v0.x;
        m_pc[n].y = ((1-m_cc)*m_pc[n].y) + v0.y;
        
        //Adapt covariance matrix C
        Matrix2x2 pcpc = m_pc[n].multiply(m_pc[n]);
        Matrix2x2 c0 = new Matrix2x2 (m_C[n]);
        c0.multiplySelf((1-hsig)*m_cc*(2-m_cc));
        pcpc.addSelf(c0);
        pcpc.multiplySelf(m_c1);
        m_C[n].multiplySelf(1-m_c1-m_cmu);
        m_C[n].addSelf(pcpc);
        m_C[n].addSelf(dx);
        m_C[n].c21 = m_C[n].c12; //enforce symetry
        
        //Adapt step size sigma
        if (m_sigmaOn)
            {
            m_sigmas[n] = (float)(m_sigmas[n]*Math.exp((m_cs/m_damps)*((m_ps[n].norm()/m_chiN)-1)));
            }

        if (m_covarOn)
            {
            //Calc eigen vector of C
            float t = (m_C[n].c11+m_C[n].c22);
            float d = (m_C[n].c11*m_C[n].c22)-(m_C[n].c12*m_C[n].c21);
            float sqrtDiscr = (float)Math.sqrt((t*t/4)-d);
            D.c11 = (t/2) + sqrtDiscr;
            D.c22 = (t/2) - sqrtDiscr;

            if (0 != m_C[n].c12)
                {
                B.c11 = D.c11 - m_C[n].c22;
                B.c12 = D.c22- m_C[n].c22;
                B.c21 = B.c22 = m_C[n].c21;
                float l1 = (float)Math.sqrt (1/((B.c11*B.c11)+(B.c21*B.c21)));
                float l2 = (float)Math.sqrt (1/((B.c12*B.c12)+(B.c22*B.c22)));
                B.c11 = B.c11 * l1;
                B.c21 = B.c21 * l1;
                B.c12 = B.c12 * l2;
                B.c22 = B.c22 * l2;
                }
            else
                {
                B.c11 = B.c22 = 1.0f; 
                B.c21 = B.c12 = 0.0f;
                }

            D.c11 = (D.c11 < 0 ? -1 : 1)*(float)Math.sqrt(D.c11 * (D.c11 < 0 ? -1 : 1));
            D.c22 = (D.c22 < 0 ? -1 : 1)*(float)Math.sqrt(D.c22 * (D.c22 < 0 ? -1 : 1));

            tmp.set(D);
            tmp.c11 = 1.0f/tmp.c11;
            tmp.c22 = 1.0f/tmp.c22;

            m_invsqrtC[n].set(B);
            m_invsqrtC[n].multiplySelf(tmp);
            tmp.set(B);
            tmp.transpose();
            m_invsqrtC[n].multiplySelf(tmp);
            }
        else //user has disabled Covariance Matrix Adaptation 
            {
            B.c11 = B.c22 = 1.0f; 
            B.c21 = B.c12 = 0.0f;
            D.c11 = D.c22 = 1.0f; 
            D.c21 = D.c12 = 0.0f;            
            }
      
        //spawn new children
        for (int k = 0; k < m_popCnt; k ++)
            {
            FitnessIdPair p = m_fitness[n].get(k);
            int j = p.m_id;

            float x = (float)(rnd.nextGaussian() * m_sigmas[n]*D.c11);
            float y = (float)(rnd.nextGaussian() * m_sigmas[n]*D.c22);
            
            m_x[j] = m_x[n] + (B.c11*x) + (B.c12*y);
            m_y[j] = m_y[n] + (B.c21*x) + (B.c22*y);
             
            float err = m_lossFunction.getErrorValue(m_x[j], m_y[j]);
            
            p.m_error = err;
            
            sum_error += err;
            m_currGlobalError = (0 == n && 0 == k) ? err : (m_currGlobalError > err) ? err : m_currGlobalError;            
            } 
        
        //just a contour for drawing
         for (int k = 0; k < CONTOUR_SZ; k++)
            {
            float x = (float)(m_circle[k].x * m_sigmas[n]*D.c11);
            float y = (float)(m_circle[k].y * m_sigmas[n]*D.c22);
           
            float x1 = m_x[n] + (B.c11*x) + (B.c12*y);
            float y1 = m_y[n] + (B.c21*x) + (B.c22*y);
            
            if (x1 == x1 +1 || y1 == y1 +1 )
                {
                m_skipContour[n] = true;
                break;
                }
            
           m_contours[(n*CONTOUR_SZ)+k].x = x1;
           m_contours[(n*CONTOUR_SZ)+k].y = y1;            
           }
        }
    
    m_currAvverageError = sum_error/m_agentCnt;
    m_tickCount ++;
    }

public boolean getShowContour ()
    {
    return m_showContour;
    }

public int getGroupCount ()
    {
    return m_grpCnt;
    }

/**
 *
 * @return
 */
@Override
protected final boolean ReadAndValidateProps ()
    {
    boolean sigmaOn = m_prAdaptVar.getBoolValue();
    boolean covarOn = m_prAdaptCovar.getBoolValue();
    
    if (!sigmaOn && sigmaOn != m_sigmaOn && covarOn)
        {
        m_prAdaptCovar.setValue(false);
        }
    else if (covarOn && covarOn != m_covarOn && !sigmaOn)
        {
        m_prAdaptVar.setValue(true);
        }    
    
    m_sigmaOn = m_prAdaptVar.getBoolValue();
    m_covarOn = m_prAdaptCovar.getBoolValue();
    m_showContour = m_prShowContour.getBoolValue();
    
    return true;
    }

@Override
public void enableProperties (boolean enable)
    {
    m_prGroupCnt.setEditable(enable);
    m_prAgentInGrpCnt.setEditable(enable);
    m_prSurvRate.setEditable(enable);
    m_prAdaptVar.setEditable(enable);
    m_prAdaptCovar.setEditable(enable);
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
private class FitnessIdPair
    {
    public FitnessIdPair (float error, int id)
        {
        m_error = error;
        m_id = id;
        }
    float m_error;
    int m_id;
    }
    
private class FitnessIdPairComparator implements Comparator<FitnessIdPair>
    {
    @Override
    public int compare(FitnessIdPair pair1, FitnessIdPair pair2) 
        {
        if (pair1.m_error > pair2.m_error) { return  1;}
        if (pair1.m_error < pair2.m_error) { return -1;}
        return 0;
        }
    }
    

} //eof class GeneticEngine
