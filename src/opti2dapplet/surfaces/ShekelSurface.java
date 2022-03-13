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
public class ShekelSurface extends LossFunctionBase
{
    long m_seed = 1;
    boolean m_setSeed = false;
    boolean m_firstGen = true;
    
    private BasicNumericProperty m_prPeaks = new BasicNumericProperty (this, int.class, "peaks", "Generator", 10, false, 1, 1000);
    private BasicNumericProperty m_prSetSeed = new BasicNumericProperty (this, boolean.class, "set seed", "Generator", false, false);
    private BasicNumericProperty m_prSeed = new BasicNumericProperty (this, long.class, "seed", "Generator", null, true);
    
    private float[] m_c;
    private float[] m_ax;
    private float[] m_ay;
    
    /*
    private float[] m_c  = {0.1f, 0.2f, 0.2f, 0.4f, 0.4f, 0.6f, 0.3f, 0.7f, 0.5f, 0.5f};
    private float[] m_ax = {4.0f, 1.0f, 8.0f, 6.0f, 3.0f, 2.0f, 5.0f, 8.0f, 6.0f, 7.0f};
    private float[] m_ay = {4.0f, 1.0f, 8.0f, 6.0f, 7.0f, 9.0f, 5.0f, 1.0f, 2.0f, 3.6f};     
      */
    
    public ShekelSurface ()
        {
        super ();
        }

    @Override
    public boolean canDesign() 
        {
        return false;
        }

    @Override
    public boolean canSupportGradient() 
        {
        return false;
        }
    
    @Override
    public boolean canGenerate ()
        {
        if (!m_isDirty && m_prSetSeed.getBoolValue() && m_prSeed.getLongValue() == m_seed && m_c.length == m_prPeaks.getIntValue())
            {
            return false;
            }
        
        return true;
        }    

    @Override
    public void Generate() 
        {
        init ();
        Random generator = new Random();
        
        int m = m_prPeaks.getIntValue();
        m_setSeed = m_prSetSeed.getBoolValue();
        
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
        
        generator.setSeed(m_seed);
        m_firstGen = false;
        
        m_c = new float[m];
        m_ax = new float[m];
        m_ay = new float[m];
        
        for (int i = 0; i <m; i++)
            {
            m_c[i]  = 0.1f + 0.9f*generator.nextFloat();
            m_ax[i] = 10.0f*generator.nextFloat();
            m_ay[i] = 10.0f*generator.nextFloat();
            }
        
        m_scale = 10.0f / m_w;
        
        for (int x = 0; x < m_w; x++) 
           {
           for (int y = 0; y < m_h; y++)
               {
               float x0 = x*m_scale;
               float y0 = y*m_scale;
               float val = 0.0f;
               for (int i=0; i < m; i ++)
                    {
                    float dx = (x0-m_ax[i]);
                    float dy = (y0-m_ay[i]);
                    val += 1.0f/(m_c[i]+(dx*dx)+(dy*dy));
                    }
               
               m_data[(m_w * y) + x] = val;
               if (val > m_max || (0 == x && 0 == y)) { m_max = val; }
               if (m_min > val || (0 == x && 0 == y)) { m_min = val; }               
               }
           }
        
        m_isDirty = false;
        }

    @Override
    protected boolean ReadAndValidateProps() 
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
    public float getValue (float x0, float y0)
        {
        if (LIMIT_PEAKS_DRV < (m_c.length + m_insertedGaussians.size()))
            {
            return super.getValue(x0, y0);
            }
        
        float x = x0*m_scale;
        float y = y0*m_scale;
               
        float retVal = 0.0f;
        for (int i = 0; i  < m_c.length; i++)
            {
            float dx = (x-m_ax[i]);
            float dy = (y-m_ay[i]);
            retVal += 1.0f/(m_c[i]+(dx*dx)+(dy*dy));
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
    public Vector2d getGradient (float x0, float y0)
        {
        if (LIMIT_PEAKS_DRV < (m_c.length + m_insertedGaussians.size()))
            {
            return super.getGradient(x0, y0);
            }
        
        Vector2d ptGradient;
        if (0 != m_insertedGaussians.size())
            {
            ptGradient = getInsertedGradient(x0, y0);
            ptGradient.x = ptGradient.x / m_scale;
            ptGradient.y = ptGradient.y / m_scale;
            }
        else
            {
            ptGradient = new Vector2d ();
            }   
        
        float x = x0*m_scale;
        float y = y0*m_scale;        

        for (int i = 0; i  < m_c.length; i++)
            {
            float dx = (x-m_ax[i]);
            float dy = (y-m_ay[i]);  
            float fn = m_c[i]+(dx*dx)+(dy*dy);
                
            ptGradient.x += -2.0f*dx/(fn*fn);
            ptGradient.y += -2.0f*dy/(fn*fn);
            }
        
        return ptGradient;
        }    
    
        //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>    
    @Override
    public Matrix2x2 getHessian (float x0, float y0)
        {
        if (LIMIT_PEAKS_DRV < (m_c.length + m_insertedGaussians.size()))
            {
            return super.getHessian(x0, y0);
            }            
      
        float fxx = 0;
        float fyy = 0;
        float fxy = 0;
        
        if (0 != m_insertedGaussians.size())
            {
            Matrix2x2 hi = getInsertedHessian (x0, y0);
            fxx = hi.c11 / (m_scale*m_scale);
            fyy = hi.c22 / (m_scale*m_scale);
            fxy = hi.c12 / (m_scale*m_scale); 
            }                  
        
        float x = x0*m_scale;
        float y = y0*m_scale;         
        
        for (int i = 0; i  < m_c.length; i++)
            {        
            float dx = (x-m_ax[i]);
            float dy = (y-m_ay[i]);  
            float fn = m_c[i]+(dx*dx)+(dy*dy);

            fxx += (8*dx*dx/(fn*fn*fn)) - (2/(fn*fn));
            fyy += (8*dy*dy/(fn*fn*fn)) - (2/(fn*fn));
            fxy += (8*dx*dy/(fn*fn*fn));
            }
        
        return new Matrix2x2(fxx, fyy, fxy);
        }
     
}
