/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.surfaces;

/**
 *
 * @author Dalius.Krunglevicius
 */
public class NormalKHandedBandit extends LossFunctionBase
{
public float[] m_mu;
public float[] m_std;

    public NormalKHandedBandit ()
        {
        super ();
        m_contourCnt = 3;
        }

    @Override
    public boolean canDesign() 
        {
        return false;
        }

    @Override
    public boolean canGenerate() 
        {
        return false;
        }

    @Override
    public boolean canSupportGradient() 
        {
        return false;
        }

    @Override
    protected boolean ReadAndValidateProps() 
        {
        return true;
        }    
    
    @Override
    public void Generate() 
        {
        init();
        m_mu = new float[m_w];
        m_std = new float[m_w];
        
        double muX = m_w/2;
        double muY = m_h/2;
        double std = m_w/2;
        
        double scale = NormDist(muX, muX, std);
        
        for (int x = 0; x < m_w; x++)
            {
            double val = NormDist(x, muX, std)/scale;
            m_mu[x] = (float)(val*muY);
            
            val = NormDist(x, muX, std*0.6)/scale;
            m_std[x] = (float)(val*40.0f);
            }
        
        for (int x = 0; x < m_w; x++) 
           {
           for (int y = 0; y < m_h; y++)
               {
               float val = (float)NormDist (y, m_mu[x], m_std[x]);
               m_data[(m_w * y) + x] = val;
               if (val > m_max || (0 == x && 0 == y)) { m_max = val; }
               if (m_min > val || (0 == x && 0 == y)) { m_min = val; }
               }
           }         
        }
    
    private static double SQRT2PI = Math.sqrt(Math.PI*2.0);
    public static double NormDist (double x, double mu, double std)
        {
        return (1.0/(std*SQRT2PI))*Math.exp(-0.5*(x-mu)*(x-mu)/(std*std));
        }
    
}
