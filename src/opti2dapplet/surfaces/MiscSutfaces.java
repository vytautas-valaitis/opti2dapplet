/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet.surfaces;

/**
 *
 * @author Dalius
 */
public class MiscSutfaces extends LossFunctionBase
    {
    public enum SurfType {SINEX, SINEXY, JORISGILLIS, HIMMELBLAU, SCHAFFER, ACKLEY, ROSENBROCK}
    private BasicNumericProperty m_prScale = new BasicNumericProperty (this, int.class, "scale", "Generator", 40, false, 1, 400);
    
    private SurfType m_type;

    private float m_muX = 0.0f;
    private float m_muY = 0.0f; 

    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>        
    public MiscSutfaces (SurfType type)
        {
        super ();
        m_type = type;
       
        switch (m_type)
            {
            case HIMMELBLAU:
                m_prScale.setValue(40);
                m_contourCnt = 20;
                m_powContourVal = 8;
                break;
            case ACKLEY:
                m_prScale.setValue(60);
                break;
            case SCHAFFER:
                m_prScale.setValue(30);
                break;
            case ROSENBROCK:
                m_prScale.setValue(110);
                m_contourCnt = 20;
                m_powContourVal = 8;
                break;
            case SINEX:
            case SINEXY:
                m_prScale.setValue(10);
                break;
            case JORISGILLIS:
                m_prScale.setValue(50);
                m_contourCnt = 20;
                break;     
            }
        }

    @Override
    public void Generate() 
        {
        init ();

        m_muX = m_w/2.0f;
        m_muY = m_h/2.0f;
        m_scale = m_scale = 1.0f/m_prScale.getIntValue();
        
        switch (m_type)
            {
            case HIMMELBLAU:
                GenerateHimmelblau();
                break;
            case ACKLEY:
                GenerateAckley();
                break;
            case SCHAFFER:
                GenerateSchaffer();
                break;
            case ROSENBROCK:
                GenerateRosenbrock();
                break;
            case SINEX:
                GenerateSinX();
                break;  
            case SINEXY:
                GenerateSinXY();
                break;
            case JORISGILLIS:
                GenerateJorisGillis();
                break;                  
            }
        
        m_isDirty = false;
        }
   
    
    @Override
    public float getErrorValue (float x0, float y0)
        {
        return  (m_max-getValue (x0, y0))/(m_max-m_min);
        }       
    
    @Override
    public float getValue (float x0, float y0)
        {
        if (LIMIT_PEAKS_DRV < m_insertedGaussians.size())
            {
            return super.getValue (x0, y0);
            }
        
        float x = (x0-m_muX)*m_scale;
        float y = (y0-m_muY)*m_scale;
        float val = 0;
        switch (m_type)
            {
            case HIMMELBLAU:
                val = -Himmelblau(x, y);
                break;
            case ACKLEY:
                val = -Ackley(x, y);
                break;
            case SCHAFFER:
                val = -Schaffer(x, y);
                break;
            case ROSENBROCK:
                val = -Rosenbrock(x, y);
                break;
            case SINEX:
                val = SinX(x, y);
                break;
            case SINEXY:
                val = SinXY(x, y);
                break;
            case JORISGILLIS:
                val = JorisGillis(x, y);
                break;
            default:
                return super.getValue (x0, y0);
            }        
        
        if (0 != m_insertedGaussians.size())
            {
            val = getInsertedValue(x0, y0);
            }          
        
        return val;
        }       
    
    @Override
    public Vector2d getGradient (float x0, float y0)
        {
        if (LIMIT_PEAKS_DRV < m_insertedGaussians.size())
            {
            return super.getGradient (x0, y0);
            }

        Vector2d ptGradient = new Vector2d ();;
        
        float x = (x0-m_muX)*m_scale;
        float y = (y0-m_muY)*m_scale;        
        
        switch (m_type)
            {
            case HIMMELBLAU:
                ptGradient.x = -((4*x*((x*x)+y-11)) + (2*x) + (2*y*y) - 14);
                ptGradient.y = -((2*x*x) + (2*y) + (4*y*(x+(y*y)-7)) - 22);
                break;
            case ROSENBROCK:
                ptGradient.x = -((2*x) - (400*x*(y-(x*x)))-2);
                ptGradient.y = -((200*y) - (200*x*x));
                break;                
            case ACKLEY:
                {
                double a = Math.sqrt(0.5*((x*x) + (y*y)));
                double be = Math.exp(0.5 * (Math.cos(2*Math.PI*x)+Math.cos(2*Math.PI*y)));
                double ce = Math.exp(-0.2*a);
                ptGradient.x = - (float) (0.125*((4*x*ce/(2*a))+(0.5*Math.sin (2*Math.PI*x)*2*Math.PI*be)));
                ptGradient.y = - (float) (0.125*((4*y*ce/(2*a))+(0.5*Math.sin (2*Math.PI*y)*2*Math.PI*be)));
                }
                break;
            case SCHAFFER:
                {
                double a = ((x*x)+(y*y));
                double b = Math.sqrt(a);
                double c = (1+(0.001*a));
                double sn= Math.sin(b);
                double cs= Math.cos(b);
                
                double dx = ((2*sn*cs*x)/(b*c*c)) - (0.004*x*((sn*sn)-0.5)/(c*c*c));
                double dy = ((2*sn*cs*y)/(b*c*c)) - (0.004*y*((sn*sn)-0.5)/(c*c*c));
                ptGradient.x = - (float) dx;
                ptGradient.y = - (float) dy;
                }
                break;
            case SINEX:
                ptGradient.x = -(float) Math.sin(x);
                ptGradient.y = 0;
                break;
            case SINEXY:
                ptGradient.x = -(float) (Math.sin(x)*Math.cos(y));
                ptGradient.y = -(float) (Math.sin(y)*Math.cos(x));
                break;
            case JORISGILLIS:
                {
                double ey = Math.exp(y);
                double a = (0.5*x*x)-(0.25*y*y)+3;
                double b = (2*x)+1-ey;
                double csa = Math.cos(a);
                double sna = Math.sin(a);
                double csb = Math.cos(b);
                double snb = Math.sin(b);
                
                double dx = (csa*csb*x)  - (2.0*sna*snb);
                double dy = (sna*snb*ey) - (0.5*csa*csb*y);
                ptGradient.x = (float) dx;
                ptGradient.y = (float) dy;                
                }
                break;
            default:
                return super.getGradient (x0, y0);
            }        
        
        if (0 != m_insertedGaussians.size())
            {
            Vector2d gi = getInsertedGradient(x0, y0);
            ptGradient.x += gi.x/m_scale;
            ptGradient.y += gi.y/m_scale;
            }        
        
        return ptGradient;
        }        
   
    @Override
    public Matrix2x2 getHessian (float x0, float y0)
        {
        if (LIMIT_PEAKS_DRV < m_insertedGaussians.size())
            {
            return super.getHessian(x0, y0);
            }            

        float x = (x0-m_muX)*m_scale;
        float y = (y0-m_muY)*m_scale;
        
        float fxx = 0;
        float fyy = 0;
        float fxy = 0;        
        
        switch (m_type)
            {
            case HIMMELBLAU:
                fxx = -((12.0f*x*x) + (4.0f*y) - 42.0f);
                fyy = -((12.0f*y*y) + (4.0f*x) - 26.0f);
                fxy = -((4.0f*x) + (4.0f*y));
                break;
            case ROSENBROCK:
                fxx = -((1200.0f*x*x) - (400.0f*y) + 2.0f);
                fyy = -(200.0f);
                fxy = -(-400.0f*x);
                break;
            case ACKLEY:
                {
                //TODO: move to consts
                double a = 20;
                double b = 0.2;
                double c = 2*Math.PI;
                double d = 5.7;
                double f = 0.8;
                double n = 2;
                //---------------
                
                double f1 = Math.sqrt (((x*x)+(y*y))/n);
                double f2 = Math.exp(-b*f1);
                double f3 =a*b*b/(n*((x*x)+(y*y)));
                double f4 = c*c*Math.exp((Math.cos(c*x)+Math.cos(c*y))/n);
                
                fxx = -(float)((1/f)*(-(a*b*x*x*f2/(f1*f1*f1*n*n)) + (a*b*f2/(f1*n)) - (f3*x*x*f2) + (Math.cos(c*x)*f4/n) - (Math.sin(c*x)*Math.sin(c*x)*f4/(n*n))));
                fyy = -(float)((1/f)*(-(a*b*y*y*f2/(f1*f1*f1*n*n)) + (a*b*f2/(f1*n)) - (f3*y*y*f2) + (Math.cos(c*y)*f4/n) - (Math.sin(c*y)*Math.sin(c*y)*f4/(n*n))));
                fxy = -(float)((1/f)*(-(a*b*y*x*f2/(f1*f1*f1*n*n)) - (f3*y*x*f2) - (Math.sin(c*x)*Math.sin(c*y)*f4/(n*n))));
                }
                break;
            case SCHAFFER:
                {
                double fd= ((x*x) + (y*y));
                double fsqrt = Math.sqrt (fd);
                double fcos= Math.cos(fsqrt);
                double fsin= Math.sin(fsqrt);
                double fdv = (1+(0.001*fd));
                
                double fx = (2*x*x*fcos*fcos/(fd*fdv*fdv))-(2*x*x*fsin*fsin/(fd*fdv*fdv))-(2*x*x*fsin*fcos/(fsqrt*fsqrt*fsqrt*fdv*fdv))+(2*fsin*fcos/(fsqrt*fdv*fdv))-(0.016*x*x*fsin*fcos/(fsqrt*fdv*fdv*fdv))+(0.000024*x*x*((fsin*fsin)-0.5)/(fdv*fdv*fdv*fdv))-(0.004*((fsin*fsin)-0.5)/(fdv*fdv*fdv));
                double fy = (2*y*y*fcos*fcos/(fd*fdv*fdv))-(2*y*y*fsin*fsin/(fd*fdv*fdv))-(2*y*y*fsin*fcos/(fsqrt*fsqrt*fsqrt*fdv*fdv))+(2*fsin*fcos/(fsqrt*fdv*fdv))-(0.016*y*y*fsin*fcos/(fsqrt*fdv*fdv*fdv))+(0.000024*y*y*((fsin*fsin)-0.5)/(fdv*fdv*fdv*fdv))-(0.004*((fsin*fsin)-0.5)/(fdv*fdv*fdv));
                double fo = (2*x*y*fcos*fcos/(fd*fdv*fdv))-(2*x*y*fsin*fsin/(fd*fdv*fdv))-(2*x*y*fsin*fcos/(fsqrt*fsqrt*fsqrt*fdv*fdv))                              -(0.016*x*y*fsin*fcos/(fsqrt*fdv*fdv*fdv))+(0.000024*x*y*((fsin*fsin)-0.5)/(fdv*fdv*fdv*fdv));
                
                fxx = - (float) fx;
                fyy = - (float) fy;
                fxy = - (float) fo;
                }
                break;
            case SINEX:
                fxx = - (float) Math.cos(x);
                fyy = 0.0f;
                fxy = 0.0f;
                break;
            case SINEXY:
                fxx = - (float) (Math.cos(x)*Math.cos(y));
                fyy = fxx;
                fxy = (float) (Math.sin(x)*Math.sin(y));
                break;
            case JORISGILLIS:
                {
                double fa = (0.5*x*x)-(0.25*y*y)+3;
                double fe = Math.exp(y);
                double fb = fe-(2*x)-1;
                double fsina = Math.sin (fa);
                double fcosa = Math.cos (fa);
                double fsinb = Math.sin (fb);
                double fcosb = Math.cos (fb);
                
                double fx =      -(x*x*fsina*fcosb) +     (fcosa*fcosb) + (4*x*fcosa*fsinb) - (4*fsina*fcosb);
                double fy = -(0.25*y*y*fsina*fcosb) + (0.5*fcosa*fcosb) + (fe*y*fcosa*fsinb) - (fsina*fcosb*fe*fe) - (fe*fsina*fcosb);
                double fo =  (0.5*x*y*fsina*fcosb) -(y*fcosa*fsinb) -(x*fe*fcosa*fsinb) +(2*fe*fsina*fcosb);
                
                fxx = (float) fx;
                fyy = (float) fy;
                fxy = (float) fo;                
                }
                break;
            default:
                return super.getHessian(x0, y0);
            } 
        
        if (0 != m_insertedGaussians.size())
            {
            Matrix2x2 hi = getInsertedHessian(x0, y0);
            fxx += hi.c11/(m_scale*m_scale);
            fyy += hi.c22/(m_scale*m_scale);
            fxy += hi.c12/(m_scale*m_scale);
            }   
        
        return new Matrix2x2(fxx, fyy, fxy);
        }    
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>        
    private void GenerateHimmelblau ()
        {
        for (int x = 0; x < m_w; x++) 
           {
           for (int y = 0; y < m_h; y++)
               {
               float x0 = (x-m_muX)*m_scale;
               float y0 = (y-m_muY)*m_scale;
               float val = -Himmelblau (x0, y0);
               m_data[(m_w * y) + x] = val;
               if (val > m_max || (0 == x && 0 == y)) { m_max = val; }
               if (m_min > val || (0 == x && 0 == y)) { m_min = val; }
               }
           }        
       }
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>    
    private void GenerateAckley ()
        {
        for (int x = 0; x < m_w; x++) 
           {
           for (int y = 0; y < m_h; y++)
               {
               float x0 = (x-m_muX)*m_scale;
               float y0 = (y-m_muY)*m_scale;
               float val = -Ackley (x0, y0);
               m_data[(m_w * y) + x] = val;
               if (val > m_max || (0 == x && 0 == y)) { m_max = val; }
               if (m_min > val || (0 == x && 0 == y)) { m_min = val; }
               }
           }         
       }    
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>        
    private void GenerateSchaffer ()
        {
        for (int x = 0; x < m_w; x++) 
           {
           for (int y = 0; y < m_h; y++)
               {
               float x0 = (x-m_muX)*m_scale;
               float y0 = (y-m_muY)*m_scale;
               float val = -Schaffer (x0, y0);
               m_data[(m_w * y) + x] = val;
               if (val > m_max || (0 == x && 0 == y)) { m_max = val; }
               if (m_min > val || (0 == x && 0 == y)) { m_min = val; }
               }
           }         
       }
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>        
    private void GenerateRosenbrock ()
        {
        //m_scale = 8.0f*m_scaleX;
        
        for (int x = 0; x < m_w; x++) 
           {
           for (int y = 0; y < m_h; y++)
               {
               float x0 = (x-m_muX)*m_scale;
               float y0 = (y-m_muY)*m_scale;
               float val = -Rosenbrock(x0, y0);
               m_data[(m_w * y) + x] = val;
               if (val > m_max || (0 == x && 0 == y)) { m_max = val; }
               if (m_min > val || (0 == x && 0 == y)) { m_min = val; }
               }
           }         
       }
    
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>        
    private void GenerateSinX ()
        {
        for (int x = 0; x < m_w; x++) 
           {
           for (int y = 0; y < m_h; y++)
               {
               float x0 = (x-m_muX)*m_scale;
               float val = SinX (x0, 0);
               m_data[(m_w * y) + x] = val;
               if (val > m_max || (0 == x && 0 == y)) { m_max = val; }
               if (m_min > val || (0 == x && 0 == y)) { m_min = val; }
               }
           }         
       }   
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>        
    private void GenerateSinXY ()
        {
        for (int x = 0; x < m_w; x++) 
           {
           for (int y = 0; y < m_h; y++)
               {
               float x0 = (x-m_muX)*m_scale;
               float y0 = (y-m_muY)*m_scale;
               float val = SinXY (x0, y0);
               m_data[(m_w * y) + x] = val;
               if (val > m_max || (0 == x && 0 == y)) { m_max = val; }
               if (m_min > val || (0 == x && 0 == y)) { m_min = val; }
               }
           }         
       }      
    
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>        
    private void GenerateJorisGillis ()
        {
        for (int x = 0; x < m_w; x++) 
           {
           for (int y = 0; y < m_h; y++)
               {
               float x0 = (x-m_muX)*m_scale;
               float y0 = (y-m_muY)*m_scale;
               float val = JorisGillis (x0, y0);
               m_data[(m_w * y) + x] = val;
               if (val > m_max || (0 == x && 0 == y)) { m_max = val; }
               if (m_min > val || (0 == x && 0 == y)) { m_min = val; }
               }
           }         
       } 

    @Override
    protected boolean ReadAndValidateProps() 
        {
        return true;
        }
  
    @Override
    public boolean canDesign ()
        {
        return false;
        }
    
    @Override
    public boolean canSupportGradient ()
        {
        return false;
        }
    
    @Override
    public boolean canGenerate ()
        {
        return (m_isDirty || (m_scale != 1.0f/m_prScale.getIntValue()));
        }       
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>        
    public static float Himmelblau (float x, float y)
        {
        return ((x*x)+y-11)*((x*x)+y-11) + ((x+(y*y)-7)*(x+(y*y)-7));
        }
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>        
    private static float SQRT05 = (float)Math.sqrt(0.5);
    private static float EXP1 = (float)Math.E;
    public static float Ackley (float x, float y)
        {
        double edsq = (x*x)+(y*y);
        double ed = Math.sqrt (edsq);
        return (float)((1f/0.8)*(-20.0*Math.exp(-0.2*SQRT05*ed) - Math.exp(0.5*(Math.cos(2*Math.PI*x) + Math.cos(2*Math.PI*y))) + 20 + EXP1 + 5.7));
        }   
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>        
    public static float Schaffer (float x, float y)
        {
        double edsq = (x*x)+(y*y);
        double ed = Math.sqrt (edsq);
        double md = Math.sin(ed);
        return (float)( 0.5 + ((md*md) - 0.5)/((1 + 0.001*edsq)*(1 + 0.001*edsq)));
        }  
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>      
    public static float Rosenbrock  (float x, float y)
        {
        float a = 1.0f-x;
        float b = (y-(x*x));
        return (a*a) + (100*b*b);
        }       
        
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>      
    public static float JorisGillis  (float x, float y)
        {
        return (float)(Math.sin(0.5*x*x-0.25*y*y+3)*Math.cos(2*x+1-Math.exp(y)));
        }       
        
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>      
    public static float SinXY  (float x, float y)
        {
        return (float)(Math.cos(x)*Math.cos(y));
        }       
        
    
    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>      
    public static float SinX  (float x, float y)
        {
        return (float)(Math.cos(x));
        }       
            
    }
