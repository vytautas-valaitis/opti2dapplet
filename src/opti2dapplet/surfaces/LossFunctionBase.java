/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.surfaces;

import com.l2fprod.common.propertysheet.DefaultProperty;
import java.util.ArrayList;
import opti2dapplet.MultiLayerCanvas;
import opti2dapplet.ParametricBase;
import opti2dapplet.renderers.GaussianEditorRenderer;

/**
 *
 * @author Dalius
 */
public abstract class LossFunctionBase extends ParametricBase
    {
    public static final int LIMIT_PEAKS_DRV = 200;
        
    public static float [] m_data; // Static for the sake of performance
    protected int m_w = 400;
    protected int m_h = 400;
    protected float m_min = 0;
    protected float m_max = 0;
    
    protected float m_scale = 1.0f;
    
    protected int m_contourCnt = 10;
    protected int m_powContourVal = 1;
    
    protected boolean m_isDirty = false;
    protected ArrayList<InsertedGaussian> m_insertedGaussians = new ArrayList<InsertedGaussian>();
      
    protected  BasicNumericProperty m_prW = new BasicNumericProperty (this, int.class, "width", "Size", 400, true);
    protected  BasicNumericProperty m_prH = new BasicNumericProperty (this, int.class, "height", "Size", 400, true); 
     
    public LossFunctionBase ()
        {
        super ();
        }
    
    public int getWidth ()
        {
        return m_w;   
        }
    
    public int getHeight ()
        {
        return m_h;    
        }
    
    public float getScale ()
        {
        return m_scale;    
        }    
    
    
    public abstract boolean canDesign ();
    public abstract boolean canGenerate ();
    public abstract boolean canSupportGradient ();
    
    public boolean isDirty ()
        {
        return m_isDirty;
        }
        
    //<editor-fold defaultstate="collapsed" desc="comment">
    public abstract void Generate ();
    
    @Override
    public void enableProperties (boolean enable)
        {
        for (int i = 2; i < m_props.size() ; i++)
            {
            ((DefaultProperty) m_props.get(i)).setEditable(enable);
            }
        }    
    
         
    //<editor-fold defaultstate="collapsed" desc="comment">
    /**
     *
     * @param x
     * @param y
     * @param m1
     * @param m2
     * @param sigma
     * @return
     */
    //</editor-fold>
    public static float Gaussian2d (float x, float y, float m1, float m2, float sigma, float a)
        {
        if (0 == sigma) {return 1.0f;}
        
        float var = sigma*sigma;
        return a*(float) (Math.exp (-0.5*( (((x-m1)*(x-m1))/var) + (((y-m2)*(y-m2))/var))));
        }
    
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    /**
     *
     */
    //</editor-fold>    
    public static float BivariateNormal (float x, float y, float m1, float m2, float sigma)
        {
        if (0 == sigma) {return 0.0f;}
        float var = sigma*sigma;
        return (float) ((1.0/(2.0*Math.PI*var)) * Math.exp (-0.5*( (((x-m1)*(x-m1))/var) + (((y-m2)*(y-m2))/var))));
        }    
    
 
    //<editor-fold defaultstate="collapsed" desc="comment">
    /**
     *
     * @param isolines
     * @return
     */
    //</editor-fold>
    public int[] getRGBBits (boolean greyscale, boolean contours, boolean invertColors)
        {
        if (null == m_data)
            {
            return null;
            }

        byte [][] isoBits = null;
        float [] isoLineHeights = null;
        if (contours)
            {
            isoBits = new byte[m_contourCnt][m_data.length];
            isoLineHeights = new float [m_contourCnt];
            float step = 1.0f/m_contourCnt;
            float pos = 1.0f - (0.5f*step);
            for (int i = 0; i < m_contourCnt; i ++)
                {
                isoLineHeights[i] = (1== m_powContourVal) ? pos : (float)Math.sqrt(pos);
                pos -= step;
                }
            }

        int [] pixels = new int[m_w*m_h*3];
        for (int x = 0; x < m_w; x++)
            {
            for (int y=0; y < m_h; y++)
                {
                int pos = (y*m_w*3)+x*3;
                float span = m_max-m_min;
                span = (0==span) ? 1.0f : span;
                float normVal = (m_data[((m_h-y-1)*m_w)+x]-m_min)/span; //(m_h-y-1) -> invert Y axis
                float powerNormVal = 1;
                
                for (int p = 0; p < m_powContourVal; p ++ )
                    {
                    powerNormVal *= normVal;
                    }
                
                float colorVal = (invertColors) ? 1.0f - powerNormVal : powerNormVal;
                
                int r, g, b;
                
                if (greyscale)
                    {
                    r = g = b = (int)(255.0f*colorVal + 0.5f);
                    }
                else
                    {
                    RGB c = jetColor(colorVal);
                    r = (int)((255.0f*c.r));
                    g = (int)((255.0f*c.g));
                    b = (int)((255.0f*c.b));
                    }

                pixels[pos]=r;
                pixels[pos+1]=g;
                pixels[pos+2]=b;
                
                if (contours)
                    {
                    for (int i = 0; i < m_contourCnt; i ++)
                        {
                        if (powerNormVal < isoLineHeights[i]) {isoBits[i][(y*m_w)+x]=1;}
                        }
                    }
                
                }

            }
        
        if (!contours)
            {
            return pixels;
            }
        
       for (int x = 1; x < m_w-1; x++)
            {
            for (int y=1; y < m_h-1; y++)
                {
                for (int i=0; i < m_contourCnt; i++)
                    {
                    if (0 != isoBits[i][(y*m_w)+x] && 
                        0 != isoBits[i][((y+1)*m_w)+x] &&
                        0 != isoBits[i][((y-1)*m_w)+x] &&
                        0 != isoBits[i][((y)*m_w)+x+1] &&
                        0 != isoBits[i][((y)*m_w)+x-1])
                        {
                        isoBits[i][(y*m_w)+x] = 2;
                        }
                    }
                }
            }

        for (int x = 1; x < m_w-1; x++)
            {
            for (int y=1; y < m_h-1; y++)
                {
                int pos = (y*m_w*3)+x*3;
                RGB c = null;
                for (int i=0; i < m_contourCnt; i++)
                    {
                    if (1 == isoBits[i][(y*m_w)+x])
                        {
                        c = jetColor(invertColors ? (1.0f - isoLineHeights[i]) : isoLineHeights[i]);
                        }                    
                    }
               
                if (null == c)
                    {
                    continue;    
                    }
                
                int r, g, b;
                float factor = (!greyscale) ? 200.0f : 255.0f;
                r = (int)(factor*c.r);
                g = (int)(factor*c.g);
                b = (int)(factor*c.b);
                
                pixels[pos]=r;
                pixels[pos+1]=g;
                pixels[pos+2]=b;                
                }        
            }

        return pixels;
        }
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>
    public float getErrorValue (float x0, float y0)
        {
        int x = (int) (x0 + 0.5f);
        int y = (int) (y0 + 0.5f);

        if (x >= 0 && y >= 0 && x < m_w && y < m_h && m_max > m_min)
            {
            return  (m_max-m_data[(m_w * y) + x])/(m_max-m_min);
            } 
        
        return 1.0f;
        }
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>
    public float getValue (float x0, float y0)
        {
        int x = (int) (x0 + 0.5f);
        int y = (int) (y0 + 0.5f);

        if (x >= 0 && y >= 0 && x < m_w && y < m_h )
            {
            return  m_data[(m_w * y) + x];
            } 
        
        return m_min;
        }    
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>    
    public Vector2d getGradient (float x0, float y0)
        {
        int x = (int) (x0 + 0.5f);
        int y = (int) (y0 + 0.5f);
        
        Vector2d ptGradient = new Vector2d ();
        //calc gradient, currenmtly from data. TODO: cal directly from difirential?
        ptGradient.x = 0.0f;
        ptGradient.y = 0.0f;
        if (x >= m_w - 1) 
            {
            ptGradient.x = -0.1f;
            }
        else if (x < 0)
            {
            ptGradient.x = 0.1f;
            }
        else if (y >= 0 && y < m_h)
            {
            ptGradient.x = m_data[(m_w * y) + (x+1)] - m_data[(m_w * y) + x];
            }
        else 
            {
            ptGradient.x = 0.0f;
            }
        
        if (y >= m_h - 1) 
            {
            ptGradient.y = -0.1f;
            }
        else if (y < 0)
            {
            ptGradient.y = 0.1f;
            }
        else if (x >= 0 && x < m_h)
            {
            ptGradient.y = m_data[(m_w * (y+1)) + x] - m_data[(m_w * y) + x];
            }
        else 
            {
            ptGradient.y = 0.0f;
            }
        
        return ptGradient;
        }    
    
        //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>    
    public Matrix2x2 getHessian (float x0, float y0)
        {
        Vector2d g00 = getGradient(x0, y0);
        Vector2d g10 = getGradient(x0 + 1.0f, y0);
        Vector2d g01 = getGradient(x0, y0 + 1.0f);

        float fxx = g10.x - g00.x;
        float fyy = g01.y - g00.y;
        float fxy = g01.x - g00.x;

        return new Matrix2x2(fxx, fyy, fxy);
        }
    
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    /**
     *
     * @param x
     * @param y
     * @param m1
     * @param m2
     * @param sigma
     * @return
     */
    //</editor-fold>
    public float getInsertedValue (float x, float y)
        {
        float val = 0;
        for (int i = 0; i  < m_insertedGaussians.size(); i++)
            {        
            InsertedGaussian ig = m_insertedGaussians.get(i);
            val += Gaussian2d(x, y, ig.m_muX, ig.m_muY, ig.m_sigma, ig.m_a);
            }
        
        return  val;
        }  
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    /**
     *
     * @param x
     * @param y
     * @param m1
     * @param m2
     * @param sigma
     * @return
     */
    //</editor-fold>
    public Vector2d getInsertedGradient (float x, float y)
        {
        Vector2d ptGradient = new Vector2d ();

        for (int i = 0; i  < m_insertedGaussians.size(); i++)
            {
            InsertedGaussian ig = m_insertedGaussians.get(i);
            float val = Gaussian2d(x, y, ig.m_muX, ig.m_muY, ig.m_sigma, ig.m_a);
            ptGradient.x += -((x-ig.m_muX)/(ig.m_sigma*ig.m_sigma))*val;
            ptGradient.y += -((y-ig.m_muY)/(ig.m_sigma*ig.m_sigma))*val;
            }
        
        return ptGradient;
        }      
   
        
    //<editor-fold defaultstate="collapsed" desc="comment">
    /**
     *
     * @param x
     * @param y
     * @param m1
     * @param m2
     * @param sigma
     * @return
     */
    //</editor-fold>
    public Matrix2x2 getInsertedHessian (float x, float y)
        {
        float fxx = 0;
        float fyy = 0;
        float fxy = 0;
        
        for (int i = 0; i  < m_insertedGaussians.size(); i++)
            {        
            InsertedGaussian ig = m_insertedGaussians.get(i);
            float val = Gaussian2d(x, y, ig.m_muX, ig.m_muY, ig.m_sigma, ig.m_a);
            float var = ig.m_sigma*ig.m_sigma;

            fxx += (-val/(var)) + ((val*(x-ig.m_muX)*(x-ig.m_muX))/(var*var));
            fyy += (-val/(var)) + ((val*(y-ig.m_muY)*(y-ig.m_muY))/(var*var));
            fxy += (x-ig.m_muX)*(y-ig.m_muY)*val/(var*var);
            }
        
        return new Matrix2x2(fxx, fyy, fxy);
        }  
    
    
   /**
     *
     */
    public static class Vector2d
        {
        public float x = 0;
        public float y = 0;
        public Vector2d ()
            {
            }
        
        public Vector2d (float x0, float y0)
            {
            x = x0;
            y = y0;
            }
        
         public float dotProduct (Vector2d v)
            {
            return (v.x*x) + (v.y*y);
            }
         
         public float norm ()
            {
            return (float)Math.sqrt ((x*x) + (y*y));
            }         
         
         public Matrix2x2 multiply (Vector2d v)
            {
            return new Matrix2x2 (x*v.x,
                                  x*v.y,
                                  y*v.x,
                                  y*v.y);
            }
         
         public Vector2d multiply (Matrix2x2 m)
            {
            return new Vector2d ((m.c11*x)+(m.c21*y),(m.c12*x)+(m.c22*y));
            }
         
         public void multiplySelf (Matrix2x2 m)
            {
            float x0 = (m.c11*x)+(m.c21*y);
            float y0 = (m.c12*x)+(m.c22*y);
            x = x0;
            y = y0;
            }
         
         public void multiplySelf (float scalar)
            {
            x = x*scalar;
            y = y*scalar;
            }
         
         public void set (Vector2d v)
            {
            x = v.x;
            y = v.y;
            }           
         
        }   
    
   /**
     *
     */
    public static class Matrix2x2
        {
        public Matrix2x2 (float p11, float p12, float p21, float p22)
            {
            c11 = p11;
            c22 = p22;
            c12 = p12;
            c21 = p21;
            
            det = ((c11*c22)-(c12*c21));
            }
        
        public Matrix2x2 (Matrix2x2 m)
            {
            c11 = m.c11;
            c22 = m.c22;
            c12 = m.c12;
            c21 = m.c21;
            
            det = ((c11*c22)-(c12*c21));
            }        
        
        //Hessian constructor
        public Matrix2x2 (float fxx, float fyy, float fxy)
            {
            c11 = fxx;
            c22 = fyy;
            c12 = c21 = fxy;
            
            det = ((c11*c22)-(c12*c21));
            }
        
        public float c11 = 0;
        public float c21 = 0;
        public float c12 = 0;
        public float c22 = 0;
        public float det = 0;
        
        public void invert ()
            {
            //then the second derivative test is inconclusive. We have complete platoe or 1D dimension surface, like sin(x). 
            //Attemt to invert as 1D
            if (0 == det)
                {
                c11 = (0 == c11) ? 0 : 1.0f/c11;
                c22 = (0 == c22) ? 0 : 1.0f/c22;
                c12 = (0 == c12) ? 0 : 1.0f/c12;
                c21 = (0 == c21) ? 0 : 1.0f/c21;
                return;
                }
                
            float det_inv = 1.0f/det;
            float a = c11;
            c11 = det_inv*c22;
            c22 = det_inv*a;
            c12 = -det_inv*c12;
            c21 = -det_inv*c21;
            
            det = ((c11*c22)-(c12*c21));
            }
        
        public void transpose ()
            {
            float a = c12;
            c12 = c21;
            c21 = a;
            det = ((c11*c22)-(c12*c21));
            }
        
         public void multiplySelf (Matrix2x2 m)
            {
            float p11 = (c11*m.c11) + (c12*m.c21);
            float p12 = (c11*m.c12) + (c12*m.c22);
            
            float p21 = (c21*m.c11) + (c22*m.c21);
            float p22 = (c21*m.c12) + (c22*m.c22);
            
            c11 = p11;
            c22 = p22;
            c12 = p12;
            c21 = p21;
            
            det = ((c11*c22)-(c12*c21));            
            }
         
         public void multiplySelf (float scalar)
            {
            c11 = c11*scalar;
            c22 = c22*scalar;
            c12 = c12*scalar;
            c21 = c21*scalar;
            
            det = ((c11*c22)-(c12*c21));            
            }         
         
         public Matrix2x2 multiply (Matrix2x2 m)
            {
            float p11 = (c11*m.c11) + (c12*m.c21);
            float p12 = (c11*m.c12) + (c12*m.c22);
            
            float p21 = (c21*m.c11) + (c22*m.c21);
            float p22 = (c21*m.c12) + (c22*m.c22);
            
            return new Matrix2x2 (p11, p12, p21, p22);           
            }         
         
         public Vector2d multiply (Vector2d v)
            {
            return new Vector2d ((c11*v.x)+(c12*v.y),(c21*v.x)+(c22*v.y));
            }
         
         public void addSelf (Matrix2x2 m)
            {
            c11 += m.c11;
            c22 += m.c22;
            c12 += m.c12;
            c21 += m.c21;
            
            det = ((c11*c22)-(c12*c21));         
            }
         
         public void set (Matrix2x2 m)
            {
            c11 = m.c11;
            c22 = m.c22;
            c12 = m.c12;
            c21 = m.c21;
            
            det = ((c11*c22)-(c12*c21));         
            }          
        
        }      
    
    /**
     *
     */
    public class RGB 
        {
        public RGB () {}
        public float r = 1.0f;
        public float g = 1.0f;
        public float b = 1.0f;
        }
    
    /**
     *
     * @param v
     * @return
     */
    public RGB jetColor(float v)
        {
        RGB c = new RGB ();

        if (v < 0.0f)  {v = 0.0f;}
        if (v > 1.0f)  {v = 1.0f;}

        if (v < (0.25f)) 
            {
            c.r = 0;
            c.g = 4.0f * v;
            } 
        else if (v < 0.5f) 
            {
            c.r = 0;
            c.b = 1 + 4.0f * (0.25f - v);
            } 
        else if (v < 0.75f) 
            {
            c.r = 4.0f * (v - 0.5f);
            c.b = 0;
            } 
        else 
            {
            c.g = 1 + (4.0f * (0.75f - v));
            c.b = 0;
            }

        return(c);
        }
    
   //<editor-fold defaultstate="collapsed" desc="comment">
   //</editor-fold>    
   public GaussianEditorRenderer CreateEditor (MultiLayerCanvas canvas)
       {
       return new GaussianEditorRenderer(canvas);
       } 
   
    protected class InsertedGaussian
        {
        public InsertedGaussian (float muX, float muY, float sigma, float a)
            {
            m_muX = muX;
            m_muY = muY;
            m_sigma = sigma;
            m_a = a;
            }
        
        public float m_muX = 0;
        public float m_muY = 0;
        public float m_sigma = 0;
        public float m_a = 0;
        }

   //<editor-fold defaultstate="collapsed" desc="comment">
   //</editor-fold>    
    public void OnUpdateAction (int mx, int my, float sigma, float factor)
        {
        m_isDirty = true;
        float span = m_max-m_min;
        span = (0==span) ? 0.3f : 0.3f*span; 
        my = m_h - 1 - my; //invert Y axis
        float scale = (factor*span)/Gaussian2d (mx, my, mx, my, sigma, 1.0f);

        for (int x = 0; x < m_w; x++) 
            {
            for (int y = 0; y < m_h; y++)
                {
                float val = m_data[(m_w * y) + x] + (Gaussian2d (x, y, mx, my, sigma, scale));
                m_data[(m_w * y) + x] = val;
                }
            }
     
        //Optimization for clicking at the same place more than once
        int prew = m_insertedGaussians.size() - 1;
        if (0 <= prew && 
            mx == m_insertedGaussians.get(prew).m_muX && 
            my == m_insertedGaussians.get(prew).m_muY &&
            sigma == m_insertedGaussians.get(prew).m_sigma )
            {
            m_insertedGaussians.get(prew).m_a += scale;  
            }
        else
            {
            m_insertedGaussians.add(new InsertedGaussian(mx, my, sigma, scale));
            }
        
        m_max = m_min = m_data[0];
        for (int i = 1; i < m_data.length; i ++)
            {
            if (m_data[i] > m_max) {m_max = m_data[i];}
            if (m_data[i] < m_min) {m_min = m_data[i];}
            }
        }
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>
    protected void allocData()
        {
        if (null != m_data && m_data.length != m_w*m_h)
            {
            m_data = null;
            System.gc();
            }
        
        if (null == m_data)
            {
            m_data = new float [m_w*m_h];
            }
        }
 
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>
    public void init ()
        {
        m_insertedGaussians.clear();
        allocData ();
        }    
    
    }//end of class
