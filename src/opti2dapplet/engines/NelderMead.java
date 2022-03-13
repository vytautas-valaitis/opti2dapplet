package opti2dapplet.engines;
import java.util.ArrayList;
import java.util.List;
import opti2dapplet.ParametricBase;
import opti2dapplet.surfaces.LossFunctionBase;

/**
 *
 * @author vytautas
 */
public class NelderMead extends EngineBase
{
    private ParametricBase.BasicNumericProperty m_prAgentCnt  = new ParametricBase.BasicNumericProperty (this, int.class,     "Agents",  "Settings", 40,    false, 1,    5000);
    private ParametricBase.BasicNumericProperty m_prReflection  = new ParametricBase.BasicNumericProperty (this, float.class,     "Reflection",  "Settings", 1.0f,    false, 0.8f,    1.2f);
    private ParametricBase.BasicNumericProperty m_prExpansion  = new ParametricBase.BasicNumericProperty (this, float.class,     "Expansion",  "Settings", 2.0f,    false, 1.3f,    50.0f);
    private ParametricBase.BasicNumericProperty m_prContraction  = new ParametricBase.BasicNumericProperty (this, float.class,     "Contraction",  "Settings", -0.5f,    false, -50.0f,    -0.1f);
    private ParametricBase.BasicNumericProperty m_prReduction  = new ParametricBase.BasicNumericProperty (this, float.class,     "Reduction",  "Settings", 0.5f,    false, 0.1f,    50.0f);

    private int m_dataHeight;
    private int m_dataWidth;
    private float m_reflection;
    private float m_expansion;
    private float m_contraction;
    private float m_reduction;
    
    protected Triangle tri;
    protected List<Triangle> trlst = new ArrayList<Triangle>();
   
    
public NelderMead (LossFunctionBase lossFunction) 
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
        
        trlst.clear();
        
        for (int it = 0; it < m_agentCnt/3; it++){
            DataPoint dp1 = new DataPoint(m_dataWidth * rnd.nextFloat(), m_dataHeight * rnd.nextFloat());
            dp1.calcLoss();
            DataPoint dp2 = new DataPoint(m_dataWidth * rnd.nextFloat(), m_dataHeight * rnd.nextFloat());
            dp2.calcLoss();
            DataPoint dp3 = new DataPoint(m_dataWidth * rnd.nextFloat(), m_dataHeight * rnd.nextFloat());
            dp3.calcLoss();
            tri = new Triangle(dp1, dp2, dp3);
            tri.order();
            trlst.add(tri);

            m_x[it * 3] = m_xPrev[it * 3 + 2] = (int)(tri.getDp1().x() + 0.5f);
            m_y[it * 3] = m_yPrev[it * 3 + 2] = (int)(tri.getDp1().y() + 0.5f);
            m_x[it * 3 + 1] = m_xPrev[it * 3] = (int)(tri.getDp2().x() + 0.5f);
            m_y[it * 3 + 1] = m_yPrev[it * 3] = (int)(tri.getDp2().y() + 0.5f);
            m_x[it * 3 + 2] = m_xPrev[it * 3 + 1] = (int)(tri.getDp3().x() + 0.5f);
            m_y[it * 3 + 2] = m_yPrev[it * 3 + 1] = (int)(tri.getDp3().y() + 0.5f);
        }
        m_currAvverageError = tri.getDp1().loss();
        m_currGlobalError = tri.getDp1().loss();
        
    }

@Override
public void tick ()
    {
        float sum_error = 0.0f;
        for(int it = 0; it < m_agentCnt/3; it++) {
            sum_error = 0.0f;
            tri = trlst.get(it);
            tri.order();


            if (tri.transform(m_reflection).loss() <= tri.getDp2().loss() && tri.transform(m_reflection).loss() > tri.getDp1().loss()) {
            // If the reflected point is better than the second worst, but not better than the best

                tri.setDp3(tri.transform(m_reflection));
                //then obtain a new simplex by replacing the worst point with the reflected point and go to step 1.

            } else if (tri.transform(m_reflection).loss() < tri.getDp1().loss()) {
                   // If the reflected point is the best point so far
                
                if(tri.transform(m_expansion).loss() < tri.transform(m_reflection).loss()) {
                //  If the expanded point is better than the reflected point
                    
                    tri.setDp3(tri.transform(m_expansion));
                    // then obtain a new simplex by replacing the worst point with the expanded point, and go to step 1.
                     
                } else {
                  // Else obtain a new simplex by replacing the worst point with the reflected point, and go to step 1.
                    tri.setDp3(tri.transform(m_reflection));
                }
                
            } else {
              // Else (i.e. reflected point is not better than second worst) continue at step 5.
                
                if (tri.transform(m_contraction).loss() < tri.getDp3().loss()) {
                // If the contracted point is better than the worst point
                
                    tri.setDp3(tri.transform(m_contraction));
                    // then obtain a new simplex by replacing the worst point  with the contracted point
                } else {
                    tri.reduction();
                    // For all but the best point, replace the point with <reduction>
                }
            }

            m_x[it * 3] = m_xPrev[it * 3 + 2] = (int)(tri.getDp1().x() + 0.5f);
            m_y[it * 3] = m_yPrev[it * 3 + 2] = (int)(tri.getDp1().y() + 0.5f);
            m_x[it * 3 + 1] = m_xPrev[it * 3] = (int)(tri.getDp2().x() + 0.5f);
            m_y[it * 3 + 1] = m_yPrev[it * 3] = (int)(tri.getDp2().y() + 0.5f);
            m_x[it * 3 + 2] = m_xPrev[it * 3 + 1] = (int)(tri.getDp3().x() + 0.5f);
            m_y[it * 3 + 2] = m_yPrev[it * 3 + 1] = (int)(tri.getDp3().y() + 0.5f);
            sum_error += tri.getDp1().loss();

        }
        
    m_currAvverageError = sum_error / m_agentCnt / 3;
    m_currGlobalError = m_currAvverageError;
    m_tickCount ++;        
    }
    
    public class DataPoint {
        float c_x, c_y;
        float c_loss;
        
        private DataPoint(float x, float y) {
            c_x = x;
            c_y = y;
            c_loss = 0;
        }
        
        public float x() {
            return c_x;
        }
        
        public float y() {
            return c_y;
        }
        
        public float loss() {
            return c_loss;
        }
        
        public void setX(float x){
            c_x = x;
        }
        
        public void setY(float y){
            c_y = y;
        }
        
        public void calcLoss(){
            c_loss = m_lossFunction.getErrorValue(c_x, c_y);
        }
    }
    
    public class Triangle {
        DataPoint dp;
        DataPoint c_dp1, c_dp2, c_dp3;
        DataPoint centroid;

        private Triangle(DataPoint dp1, DataPoint dp2, DataPoint dp3) {
            c_dp1 = dp1;
            c_dp2 = dp2;
            c_dp3 = dp3;
            centroid = new DataPoint(0, 0);
            calcCentroid();
        }
        
        private void calcCentroid() {
            centroid.setX((c_dp1.x() + c_dp2.x()) / 2);
            centroid.setY((c_dp1.y() + c_dp2.y()) / 2);
            centroid.calcLoss();
        }
        
        public void order() {
            DataPoint temp_dp = new DataPoint(0,0);
            
            //if (el1 > el2) Swap(el1,el2)
            //if (el2 > el3) Swap(el2,el3)
            //if (el1 > el2) Swap(el1,el2)
        
            if (c_dp1.loss() > c_dp2.loss()) {
                temp_dp = c_dp1;
                c_dp1 = c_dp2;
                c_dp2 = temp_dp;
            }
            if (c_dp2.loss() > c_dp3.loss()) {
                temp_dp = c_dp2;
                c_dp2 = c_dp3;
                c_dp3 = temp_dp;
            }
            if (c_dp1.loss() > c_dp2.loss()) {
                temp_dp = c_dp1;
                c_dp1 = c_dp2;
                c_dp2 = temp_dp;
            }
            calcCentroid();
        }
        
        public DataPoint transform(float coef) {
            float x = centroid.x();
            float y = centroid.y();
            x += coef * (centroid.x() - c_dp3.x());
            y += coef * (centroid.y() - c_dp3.y());
            DataPoint dpp = new DataPoint(x, y);
            dpp.calcLoss();
            return dpp;
        }
        
        public void reduction() {
            float x = c_dp1.x();
            float y = c_dp1.y();
            x += m_reduction * (c_dp2.x() - c_dp1.x());
            y += m_reduction * (c_dp2.y() - c_dp1.y());
            DataPoint dpp = new DataPoint(x, y);
            dpp.calcLoss();
            c_dp2 = dpp; 
            x = c_dp1.x();
            y = c_dp1.y();           
            x += m_reduction * (c_dp3.x() - c_dp1.x());
            y += m_reduction * (c_dp3.y() - c_dp1.y());
            dpp = new DataPoint(x, y);
            dpp.calcLoss();
            c_dp3 = dpp;             
        }
        
        public DataPoint getDp1() {
            return c_dp1;
        }
        
        public DataPoint getDp2() {
            return c_dp2;
        }
        
        public DataPoint getDp3() {
            return c_dp3;
        }
        
        public void setDp1(DataPoint new_dp) {
            c_dp1 = new_dp;
            order();
        }
        
        public void setDp2(DataPoint new_dp) {
            c_dp2 = new_dp;
            order();
        }
        
        public void setDp3(DataPoint new_dp) {
            c_dp3 = new_dp;
            order();
        }
        
    }
    
    @Override
    protected final boolean ReadAndValidateProps ()
    {
        m_agentCnt = m_prAgentCnt.getIntValue() * 3;
        m_reflection = m_prReflection.getFloatValue();
        m_expansion = m_prExpansion.getFloatValue();
        m_contraction = m_prContraction.getFloatValue();
        m_reduction = m_prReduction.getFloatValue();
        
        return true;
    }

    @Override
    public void enableProperties(boolean enable)
    {
        m_prAgentCnt.setEditable(enable);
    }
    
} //eof class NelderMead
