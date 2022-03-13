/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet;

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import java.beans.PropertyEditor;
import java.util.ArrayList;

/**
 *
 * @author Dalius
 */
public abstract class ParametricBase 
{
protected ArrayList<BasicProperty> m_props = new ArrayList<BasicProperty>();
    /**
     *
     * @return
     */
    public BasicProperty[] getProperties ()
        {
        BasicProperty[] retArray = new BasicProperty [m_props.size()];
        return m_props.toArray(retArray);
        }

    /**
     *
     */
    public void setdeDefaultProperties ()
    {
    Property[] props = getProperties ();
    if (null == props || 0 == props.length)
        {
        return;    
        }
    
    for (int i = 0; i < props.length ; i ++)
        {
        ((BasicProperty)props[i]).resetToDefault(); 
        }
    
    ReadAndValidateProps();
    }

public abstract  void enableProperties (boolean enable);
protected abstract boolean ReadAndValidateProps ();

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
public static abstract class BasicProperty extends DefaultProperty
    {
     protected ParametricBase     m_parent = null;
     protected PropertyEditor m_customEditor = null;
     public BasicProperty (ParametricBase parent)
        {
        if (null == parent)
            {
            throw new IllegalArgumentException();
            }
        
        m_parent = parent;
        parent.m_props.add(this);
        }
     
     public abstract void resetToDefault ();
     public PropertyEditor getCustomEditor ()
        {
        return m_customEditor;
        }
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
public static class BasicNumericProperty extends BasicProperty
    {
     private Object     m_min = null;
     private Object     m_max = null;
     private Object     m_default = null;
     
//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>     
     public BasicNumericProperty(ParametricBase parent, Class type, String name,  String category, Object value, boolean readonly, Object min, Object max)
          {
          super (parent);
          setName( name );
          setDisplayName( name  );
          setShortDescription( category + "." + name );
          setType(type);
          setCategory(category);
          super.setValue(value);
          setEditable (!readonly);
          m_min =  min;
          m_max =  max;
          m_default = value;
          }
     
//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>     
     public BasicNumericProperty(ParametricBase parent, Class type, String name,  String category, Object value, boolean readonly)
          {
          super (parent);
              
          setName( name );
          setDisplayName( name  );
          setShortDescription( category + "." + name );
          setType(type);
          setCategory(category);
          super.setValue(value);
          setEditable (!readonly);
          m_default = value;
          }     
    
//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>     
    public float getFloatValue ()
        {
        return ((Number)getValue()).floatValue();    
        }
    
//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>    
    public int getIntValue ()
        {
        return ((Number)getValue()).intValue();    
        }
    
//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>    
    public long getLongValue ()
        {
        return ((Number)getValue()).longValue();    
        }    
    
//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>    
    public boolean getBoolValue ()
        {
        return ((Boolean)getValue()).booleanValue();    
        }      
     
    @Override
     public void setValue(Object Value)
        {
         if (int.class == getType ())
            {
            if (m_min != null || m_max != null)
               {
               int v  = ((Number)Value).intValue(); //Comparing only numeric values...
               if (m_min != null && v < ((Number)m_min).intValue())
                  {
                  Value = m_min;
                  }
               
               if ( m_max != null && v > ((Number)m_max).intValue() )
                  {
                  Value = m_max;
                  }                   
               }
            }
         
         if (float.class == getType ())
            {
            if (m_min != null || m_max != null)
               {
               float v  = ((Number)Value).floatValue();
               if (m_min != null && v < ((Number)m_min).floatValue())
                  {
                  Value = m_min;
                  }
               
               if ( m_max != null && v > ((Number)m_max).floatValue() )
                  {
                  Value = m_max;
                  } 
               }
            }
         
         if (double.class == getType ())
            {
            if (m_min != null || m_max != null)
               {
               double v  = ((Number)Value).doubleValue(); 
                  if (m_min != null && v < ((Number)m_min).doubleValue())
                  {
                  Value = m_min;
                  }
               
               if ( m_max != null && v > ((Number)m_max).doubleValue() )
                  {
                  Value = m_max;
                  } 
               }
            }  
         
         if (long.class == getType ())
            {
            if (m_min != null || m_max != null)
               {
               long v  = ((Number)Value).longValue(); 
               if (m_min != null && v < ((Number)m_min).longValue())
                  {
                  Value = m_min;
                  }
               
               if ( m_max != null && v > ((Number)m_max).longValue() )
                  {
                  Value = m_max;
                  } 
               }
            }          
         
         Object oldValue = super.getValue();
         super.setValue(Value);
         if (null != m_parent)
            {
            if (!m_parent.ReadAndValidateProps ())
                {
                super.setValue(oldValue);
                }         
            }
        }
    
//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>    
    public void setValueNoValidate (Object value)
        {
        super.setValue (value);
        }
    
   @Override    
   public void resetToDefault ()
        {
        super.setValue(m_default);
        }    
    }

//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>
public static class PickListProperty extends BasicProperty
    {
     protected String[]  m_list = null;
     private int m_defaultIdx;
     
     public PickListProperty(ParametricBase parent, String name,  String category, String[] list, int defaultIdx, boolean readonly)
          {
          super (parent);
          setName( name );
          setDisplayName( name  );
          setShortDescription( category + "." + name );
          setType(String.class);
          setCategory(category);
          m_defaultIdx = defaultIdx;
          m_list = list;
          super.setValue(m_list[m_defaultIdx]);
          
          m_customEditor = new PickListEditor (m_list);
          setEditable (!readonly);
          }
    
//<editor-fold defaultstate="collapsed" desc="comment">
//</editor-fold>     
    public int getIntValue ()
        {
        String txt = (String)getValue();
        for (int i = 0; i < m_list.length; i++)
            {
            if (txt.equals (m_list[i]))
                {
                return i;
                }
            }
        return -1;    
        }
    
   @Override    
   public void resetToDefault ()
        {
        super.setValue(m_list[m_defaultIdx]);
        }
   

    //<editor-fold defaultstate="collapsed" desc="comment">
    //</editor-fold>
    public static final class PickListEditor extends ComboBoxPropertyEditor
        {
        public PickListEditor(String[] values ) 
            {
            setAvailableValues(values);
            }
        }   
    }
}
