/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.surfaces;
import java.util.Random;

/**
 *
 * @author Dalius
 */
public class GaussianSurface extends LossFunctionBase
    {
    protected int m_peaks = 0;
    protected float m_sigma = 0;
    
    private long m_seed;
    private boolean m_setSeed = false;
    private boolean m_firstGen = true;
    
    private float[] m_mux = new float[m_peaks];
    private float[] m_muy = new float[m_peaks];
    private float[] m_a = new float[m_peaks];    

    private BasicNumericProperty m_prCnt = new BasicNumericProperty (this, int.class, "peaks", "Generator", 150, false, 0, 1000);
    private BasicNumericProperty m_prSigma = new BasicNumericProperty (this, float.class, "sigma", "Generator", 15.0f, false, 1.0f, 500.0f);

    private BasicNumericProperty m_prSetSeed = new BasicNumericProperty (this, boolean.class, "set seed", "Generator", false, false);
    private BasicNumericProperty m_prSeed = new BasicNumericProperty (this, long.class, "seed", "Generator", null, true);
    
    /**
     *
     */
    public GaussianSurface ()
        {
        super ();
        m_seed = 1;
        m_prSeed.setValue(m_seed);
       
        m_peaks = m_prCnt.getIntValue();
        m_sigma = m_prSigma.getFloatValue();        
        }

    @Override
    public float getScale ()
        {
        return 1.0f/m_sigma;    
        }
    
    @Override
    public boolean canGenerate ()
        {
        if (!m_isDirty && m_prSetSeed.getBoolValue() && m_prSeed.getLongValue() == m_seed && 
             m_peaks == m_prCnt.getIntValue() && m_sigma == m_prSigma.getFloatValue())
            {
            return false;
            }
        
        return true;
        }
    
    @Override
    public void Generate ()
        {
        init ();
        Random rnd = new Random();
        
        m_peaks = m_prCnt.getIntValue();
        m_sigma = m_prSigma.getFloatValue();
        m_setSeed =  m_prSetSeed.getBoolValue();        
        
        if (m_setSeed)
            {
            m_seed = m_prSeed.getLongValue();
            }
        else
            {
            if (!m_firstGen) //let the first gen be from some default seed..
                {
                m_seed = System.currentTimeMillis(); 
                }
            
            m_prSeed.setValueNoValidate(m_seed);
            }
        
        m_firstGen = false;
        rnd.setSeed(m_seed);

        m_mux = new float[m_peaks];
        m_muy = new float[m_peaks];
        m_a = new float[m_peaks];
        
        for (int i = 0; i < m_peaks; i++)
            {
            m_a[i] = 1.0f;
            if (1 == m_peaks)
                {
                m_mux[i] =  m_w / 2.0f;
                m_muy[i] =  m_h / 2.0f;    
                }
            else
                {
                m_mux[i] = rnd.nextFloat() * m_w;
                m_muy[i] = rnd.nextFloat() * m_h;
                }
            }


        int lBiv = (int)(6*m_sigma+0.5f);
        float m0 = lBiv/2.0f;
        float min = 100.0f;
        float max = -100.0f;
        float [] pBivNormal = new float[lBiv*lBiv];
        
        for (int x= 0; x < lBiv; x++)
            {
            for (int y = 0; y < lBiv; y++)
                {
                int pos; 
                pos = (y*lBiv) + x;
                pBivNormal[pos] = Gaussian2d (x, y, m0, m0, m_sigma, 1.0f);
                }
            }

        for (int x = 0; x < m_w; x++) 
            {
            for (int y = 0; y < m_h; y++)
                {
                float val = 0;
                for (int i = 0; i < m_peaks; i++)
                    {
                    int px = (int)((float)x-m_mux[i] + m0 +0.5f);
                    int py = (int)((float)y-m_muy[i] + m0 +0.5f);
                    if (!(px < 0 || py < 0 || px >= lBiv || py >= lBiv))
                        {
                        val += m_a[i]*pBivNormal[(py*lBiv) + px];// * ((0 == i) ? 5.0f : 1.0f);
                        }
                    }

                m_data[(m_w * y) + x] = val;
                if (val > max) { max = val; }
                if (min > val) { min = val; }
                }
            }    

        m_min = min;
        m_max = max;
        m_isDirty = false;
        }
    
    @Override
    protected boolean ReadAndValidateProps ()
        {
        boolean setSeed = m_prSetSeed.getBoolValue();
        m_prSeed.setEditable(setSeed);
        return true;
        }

    @Override
    public void enableProperties (boolean enable)
        {
        super.enableProperties(enable);
        boolean setSeed = m_prSetSeed.getBoolValue();
        m_prSeed.setEditable(setSeed && enable);
        }     
    
    @Override
    public boolean canDesign ()
        {
        return true;
        }
    
    @Override
    public boolean canSupportGradient ()
        {
        return true;
        }
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>
    @Override
    public float getErrorValue (float x, float y)
        {
        return (m_max-getValue (x,y))/(m_max-m_min);
        }
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>
    @Override
    public float getValue (float x, float y)
        {
        if (LIMIT_PEAKS_DRV < (m_peaks + m_insertedGaussians.size()))
            {
            return super.getValue(x, y);
            }
        
        float retVal = 0.0f;
        for (int i = 0; i  < m_peaks; i++)
            {
            retVal += Gaussian2d(x, y, m_mux[i], m_muy[i], m_sigma, m_a[i]);
            }
        
        if (0 != m_insertedGaussians.size())
            {
            retVal += getInsertedValue(x, y);
            }          
        
        return retVal;
        }    
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>    
    @Override
    public Vector2d getGradient (float x, float y)
        {
        if (LIMIT_PEAKS_DRV < (m_peaks + m_insertedGaussians.size()))
            {
            return super.getGradient(x, y);
            }

        Vector2d ptGradient;
        if (0 != m_insertedGaussians.size())
            {
            ptGradient = getInsertedGradient(x, y);
            }
        else
            {
            ptGradient = new Vector2d ();
            }        

        for (int i = 0; i  < m_peaks; i++)
            {        
            ptGradient.x += -((x-m_mux[i])/(m_sigma*m_sigma))*Gaussian2d(x, y, m_mux[i], m_muy[i], m_sigma, m_a[i]);
            ptGradient.y += -((y-m_muy[i])/(m_sigma*m_sigma))*Gaussian2d(x, y, m_mux[i], m_muy[i], m_sigma, m_a[i]);
            }
        
        return ptGradient;
        }    
    
        //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>    
    @Override
    public Matrix2x2 getHessian (float x, float y)
        {
        if (LIMIT_PEAKS_DRV < (m_peaks + m_insertedGaussians.size()))
            {
            return super.getHessian(x, y);
            }            
      
        float fxx = 0;
        float fyy = 0;
        float fxy = 0;
        
        if (0 != m_insertedGaussians.size())
            {
            Matrix2x2 hi = getInsertedHessian (x, y);
            fxx = hi.c11;
            fyy = hi.c22;
            fxy = hi.c12; 
            }        
        
        for (int i = 0; i  < m_peaks; i++)
            {        
            float val = Gaussian2d(x, y, m_mux[i], m_muy[i], m_sigma, m_a[i]);

            fxx += (-val/(m_sigma*m_sigma)) + ((val*(x-m_mux[i])*(x-m_mux[i]))/(m_sigma*m_sigma*m_sigma*m_sigma));
            fyy += (-val/(m_sigma*m_sigma)) + ((val*(y-m_muy[i])*(y-m_muy[i]))/(m_sigma*m_sigma*m_sigma*m_sigma));
            fxy += (x-m_mux[i])*(y-m_muy[i])*val/(m_sigma*m_sigma*m_sigma*m_sigma);
            }
        
        return new Matrix2x2(fxx, fyy, fxy);
        }
    
    }
