/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opti2dapplet;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import opti2dapplet.renderers.GaussianEditorRenderer;

/**
 *
 * @author Dalius
 */
public class Opti2DJApplet extends javax.swing.JApplet {

    /**
     * Initializes the applet HelloJApplet
     */
    @Override
    public void init() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Opti2DJApplet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Opti2DJApplet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Opti2DJApplet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Opti2DJApplet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the applet */
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    initComponents();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        try
            {
            BufferedImage imgPlay =  ImageIO.read(getClass().getResourceAsStream("/res/play.png"));
            BufferedImage imgPause  = ImageIO.read(getClass().getResourceAsStream("/res/pause.png"));
            
            m_playIcon = new ImageIcon (imgPlay);
            m_pauseIcon = new ImageIcon (imgPause);
            }
         catch (Exception ex) 
            {
            ex.printStackTrace();
            }
        
        m_presetEngine = getParameter("engine");
        m_presetFunction = getParameter("function");
        String strCanChangeEngine = getParameter("canChangeEngine");
        boolean canChangeEng = (null == strCanChangeEngine || strCanChangeEngine.equalsIgnoreCase("true"));
        
        MultiLayerCanvas cnvs = (MultiLayerCanvas)m_cnvsExp;
        m_factory = new EngineFactory(cnvs, m_chart2D);
        
        cmbSelLossFn.removeAllItems();
        int selIdx = 0;
        for (int i = 0; i < EngineFactory.supportedFunctions.length; i ++)
            {
            cmbSelLossFn.addItem(EngineFactory.supportedFunctions[i]);
            if (null != m_presetFunction && m_presetFunction.equalsIgnoreCase(EngineFactory.supportedFunctions[i]))
                {
                selIdx = i;
                }
            }
        
        if (null != m_presetFunction)
            {
            cmbSelLossFn.setSelectedIndex(selIdx);
            }
        
        cmbSelLossFn.setBackground(Color.yellow);

        cmbEngine.removeAllItems();
        selIdx = 0;
        for (int i = 0; i < EngineFactory.supportedEngines.length; i ++)
            {
            boolean preset = (null != m_presetEngine && m_presetEngine.equalsIgnoreCase(EngineFactory.supportedEngines[i]));
            if (!preset && !canChangeEng)
                {
                continue;
                }
            else if (preset && canChangeEng)
                {
                selIdx = i;
                }
            
            cmbEngine.addItem(EngineFactory.supportedEngines[i]);
            }
        
        if (0 == cmbEngine.getItemCount())
            {
            cmbEngine.addItem(EngineFactory.supportedEngines[0]);
            }
        else if (0 != selIdx)
            {
            cmbEngine.setSelectedIndex(selIdx);
            }
        
        if (1 ==  cmbEngine.getItemCount())
            {
            cmbEngine.setVisible(false);
            txtEngineName.setText((String)cmbEngine.getSelectedItem());
            txtEngineName.setPreferredSize(cmbEngine.getPreferredSize());
            cmbEngine.setPreferredSize(new Dimension (0,0));
            txtEngineName.setBackground(Color.yellow.brighter());
            }
        else
            {
            txtEngineName.setVisible(false);
            cmbEngine.setBackground(Color.yellow);
            }
        
        m_factory.CreateEngine((String)cmbEngine.getSelectedItem(), (String)cmbSelLossFn.getSelectedItem());
        
        setEnginePanelProps ();
        setFunctionPanelProps ();
        
        m_factory.updateView(true, chkGrayScale.isSelected(), chkContour.isSelected(), chkInvertColors.isSelected());
        m_initialized = true;
        updateControlsByStatus();
    }
       
    
    /**
     * This method is called from within the init() method to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        m_cnvsExp = new MultiLayerCanvas();
        jPanel4 = new javax.swing.JPanel();
        jToolBar3 = new javax.swing.JToolBar();
        cmbEngine = new javax.swing.JComboBox();
        txtEngineName = new javax.swing.JTextField();
        btnDefaultProps = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        propertySheetPanelMain = new com.l2fprod.common.propertysheet.PropertySheetPanel();
        chkTracePaths = new javax.swing.JCheckBox();
        jPanelChart = new javax.swing.JPanel();
        m_chart2D = new info.monitorenter.gui.chart.Chart2D();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jToolBar2 = new javax.swing.JToolBar();
        cmbSelLossFn = new javax.swing.JComboBox();
        btnGenerate = new javax.swing.JButton();
        btnDesignFn = new javax.swing.JToggleButton();
        propertySheetPanelLoss = new com.l2fprod.common.propertysheet.PropertySheetPanel();
        chkGrayScale = new javax.swing.JCheckBox();
        chkInvertColors = new javax.swing.JCheckBox();
        chkContour = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        btnStart = new javax.swing.JButton();
        btnPlayStop = new javax.swing.JButton();
        btnPause = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        sliderSpeed = new javax.swing.JSlider();

        setBackground(new java.awt.Color(204, 255, 255));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        m_cnvsExp.setMaximumSize(new java.awt.Dimension(400, 400));
        m_cnvsExp.setMinimumSize(new java.awt.Dimension(400, 400));
        m_cnvsExp.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                m_cnvsExpMouseWheelMoved(evt);
            }
        });
        m_cnvsExp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                m_cnvsExpMouseReleased(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                m_cnvsExpMouseExited(evt);
            }
        });
        m_cnvsExp.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                m_cnvsExpMouseMoved(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(m_cnvsExp, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(m_cnvsExp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);
        jToolBar3.setBorderPainted(false);
        jToolBar3.setPreferredSize(new java.awt.Dimension(239, 25));

        cmbEngine.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Simple Swarm", "Simple Genetic" }));
        cmbEngine.setBorder(null);
        cmbEngine.setPreferredSize(new java.awt.Dimension(212, 20));
        cmbEngine.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbEngineItemStateChanged(evt);
            }
        });
        jToolBar3.add(cmbEngine);

        txtEngineName.setEditable(false);
        txtEngineName.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtEngineName.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEngineName.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        txtEngineName.setOpaque(false);
        txtEngineName.setPreferredSize(new java.awt.Dimension(0, 20));
        jToolBar3.add(txtEngineName);

        btnDefaultProps.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/undo.png"))); // NOI18N
        btnDefaultProps.setToolTipText("Reset");
        btnDefaultProps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDefaultPropsActionPerformed(evt);
            }
        });
        jToolBar3.add(btnDefaultProps);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Engine:");

        propertySheetPanelMain.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        propertySheetPanelMain.setDescriptionVisible(false);
        propertySheetPanelMain.setOpaque(false);
        propertySheetPanelMain.setToolBarVisible(false);

        chkTracePaths.setSelected(true);
        chkTracePaths.setText("Trace paths");
        chkTracePaths.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkTracePathsStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chkTracePaths)
                .addContainerGap())
            .addComponent(propertySheetPanelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 4, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(chkTracePaths))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(propertySheetPanelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanelChart.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanelChart.setDoubleBuffered(false);
        jPanelChart.setOpaque(false);
        jPanelChart.setPreferredSize(new java.awt.Dimension(691, 164));

        m_chart2D.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout m_chart2DLayout = new javax.swing.GroupLayout(m_chart2D);
        m_chart2D.setLayout(m_chart2DLayout);
        m_chart2DLayout.setHorizontalGroup(
            m_chart2DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 656, Short.MAX_VALUE)
        );
        m_chart2DLayout.setVerticalGroup(
            m_chart2DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanelChartLayout = new javax.swing.GroupLayout(jPanelChart);
        jPanelChart.setLayout(jPanelChartLayout);
        jPanelChartLayout.setHorizontalGroup(
            jPanelChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelChartLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(m_chart2D, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelChartLayout.setVerticalGroup(
            jPanelChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelChartLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(m_chart2D, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel6.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Loss function:");

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setBorderPainted(false);

        cmbSelLossFn.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Bivariate normal", "Mount Everest" }));
        cmbSelLossFn.setBorder(null);
        cmbSelLossFn.setPreferredSize(new java.awt.Dimension(187, 20));
        cmbSelLossFn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbSelLossFnItemStateChanged(evt);
            }
        });
        jToolBar2.add(cmbSelLossFn);

        btnGenerate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/reset.png"))); // NOI18N
        btnGenerate.setToolTipText("Generate");
        btnGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateActionPerformed(evt);
            }
        });
        jToolBar2.add(btnGenerate);

        btnDesignFn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/brush.png"))); // NOI18N
        btnDesignFn.setToolTipText("Edit");
        btnDesignFn.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                btnDesignFnStateChanged(evt);
            }
        });
        btnDesignFn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDesignFnActionPerformed(evt);
            }
        });
        jToolBar2.add(btnDesignFn);

        propertySheetPanelLoss.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        propertySheetPanelLoss.setToolBarVisible(false);

        chkGrayScale.setSelected(true);
        chkGrayScale.setText("Grayscale");
        chkGrayScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkGrayScaleActionPerformed(evt);
            }
        });

        chkInvertColors.setText("Invert");
        chkInvertColors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkInvertColorsActionPerformed(evt);
            }
        });

        chkContour.setSelected(true);
        chkContour.setText("Contour");
        chkContour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkContourActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(chkGrayScale)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chkContour)
                .addGap(18, 18, 18)
                .addComponent(chkInvertColors)
                .addGap(15, 15, 15))
            .addComponent(propertySheetPanelLoss, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkContour, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chkGrayScale, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chkInvertColors))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(propertySheetPanelLoss, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jToolBar1.setBorder(null);
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setBorderPainted(false);

        btnStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/play.png"))); // NOI18N
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });
        jToolBar1.add(btnStart);

        btnPlayStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/playstop.png"))); // NOI18N
        btnPlayStop.setPreferredSize(new java.awt.Dimension(27, 27));
        btnPlayStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlayStopActionPerformed(evt);
            }
        });
        jToolBar1.add(btnPlayStop);

        btnPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/stop.png"))); // NOI18N
        btnPause.setEnabled(false);
        btnPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPauseActionPerformed(evt);
            }
        });
        jToolBar1.add(btnPause);

        jLabel3.setText("Speed:");
        jToolBar1.add(jLabel3);

        sliderSpeed.setMaximum(200);
        sliderSpeed.setToolTipText("Speed");
        sliderSpeed.setValue(190);
        sliderSpeed.setPreferredSize(new java.awt.Dimension(286, 23));
        sliderSpeed.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderSpeedStateChanged(evt);
            }
        });
        jToolBar1.add(sliderSpeed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelChart, javax.swing.GroupLayout.PREFERRED_SIZE, 684, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelChart, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
      
    private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
        
        if (!m_factory.isRunning())
            {
            m_factory.updateView(true, chkGrayScale.isSelected(), chkContour.isSelected(), chkInvertColors.isSelected());
            updateControlsByStatus();
            }
    }//GEN-LAST:event_btnGenerateActionPerformed

    private long getRunSpeedValue ()
        {
        long x = (200 - sliderSpeed.getValue());
        if (x > 180) { x = x * 5;}
        return x;
        }
    
    private void sliderSpeedStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderSpeedStateChanged
     
            m_factory.setRunSpeed(getRunSpeedValue ());
    }//GEN-LAST:event_sliderSpeedStateChanged

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        
        if (btnDesignFn.isSelected()) { btnDesignFn.setSelected(false); exitEditorMode ();}
        
        if (!m_factory.isRunning())
            {
            m_factory.start(getRunSpeedValue (), chkTracePaths.isSelected(), (!chkGrayScale.isSelected() ? Color.DARK_GRAY : Color.ORANGE));
            }
        else if (!m_factory.isPaused())
            {
            m_factory.pause();
            }
        else
            {
            m_factory.resume();
            }
        updateControlsByStatus ();
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPauseActionPerformed
       
        if (!m_factory.isRunning())
            {
            return;
            }
        
         m_factory.stop();
         updateControlsByStatus ();
    }//GEN-LAST:event_btnPauseActionPerformed

    private void chkTracePathsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkTracePathsStateChanged
      m_factory.m_renderer.setTrackPaths( chkTracePaths.isSelected());
    }//GEN-LAST:event_chkTracePathsStateChanged

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
       
    }//GEN-LAST:event_formComponentShown

    private void btnDefaultPropsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDefaultPropsActionPerformed
        if (!m_factory.isRunning())
            {
            m_factory.m_engine.setdeDefaultProperties();
            }
    }//GEN-LAST:event_btnDefaultPropsActionPerformed

    private void chkGrayScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGrayScaleActionPerformed
        m_factory.updateView(false, chkGrayScale.isSelected(), chkContour.isSelected(), chkInvertColors.isSelected());
    }//GEN-LAST:event_chkGrayScaleActionPerformed

    private void chkContourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkContourActionPerformed
        m_factory.updateView(false, chkGrayScale.isSelected(), chkContour.isSelected(), chkInvertColors.isSelected());
    }//GEN-LAST:event_chkContourActionPerformed

    private void cmbEngineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbEngineItemStateChanged
        if (!m_initialized)
            {
            return;
            }
        m_factory.CreateEngine((String)cmbEngine.getSelectedItem(), (String)cmbSelLossFn.getSelectedItem());
        setEnginePanelProps ();
        updateControlsByStatus ();
  
    }//GEN-LAST:event_cmbEngineItemStateChanged

    private void cmbSelLossFnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbSelLossFnItemStateChanged
       if (!m_initialized || m_factory.m_fnName.equals((String)cmbSelLossFn.getSelectedItem()))
            {
            return;
            }        
        if (btnDesignFn.isSelected()) { btnDesignFn.setSelected(false); exitEditorMode ();}
        m_factory.CreateEngine((String)cmbEngine.getSelectedItem(), (String)cmbSelLossFn.getSelectedItem());
        setFunctionPanelProps ();
        m_factory.updateView(true, chkGrayScale.isSelected(), chkContour.isSelected(), chkInvertColors.isSelected());
        updateControlsByStatus ();
    }//GEN-LAST:event_cmbSelLossFnItemStateChanged

    private void setFunctionPanelProps ()
        {
        ParametricBase.BasicProperty[] props = m_factory.m_lossFn.getProperties();
        propertySheetPanelLoss.setProperties(props);
        
        PropertyEditorRegistry reg = (PropertyEditorRegistry)propertySheetPanelLoss.getEditorFactory();        
        
        if (null != props && 0 != props.length)
            {
            FnPropertyChangeListener listener = new FnPropertyChangeListener();
            for (int i = 0; i < props.length; i++)
                {
                props[i].addPropertyChangeListener(listener);  
                if  (null != props[i].getCustomEditor())
                    {
                    reg.registerEditor(props[i], props[i].getCustomEditor());
                    }                
                }
            }
        
        propertySheetPanelLoss.setMode(PropertySheetPanel.VIEW_AS_CATEGORIES); 
        }
    
    private void setEnginePanelProps ()
        {
        ParametricBase.BasicProperty[] props = m_factory.m_engine.getProperties();
        propertySheetPanelMain.setProperties(props);
        
        PropertyEditorRegistry reg = (PropertyEditorRegistry)propertySheetPanelMain.getEditorFactory();
        
        if (null != props && 0 != props.length)
            {
            for (int i = 0; i < props.length; i++)
                {
                if  (null != props[i].getCustomEditor())
                    {
                    reg.registerEditor(props[i], props[i].getCustomEditor());
                    }
                }
            }
        
        propertySheetPanelMain.setMode(PropertySheetPanel.VIEW_AS_CATEGORIES); 
        }    
    
    private void m_cnvsExpMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_cnvsExpMouseMoved
        
            if (null != editorThread)
                {
                editorThread.SetPos(evt.getX(), evt.getY());
                }
        
    }//GEN-LAST:event_m_cnvsExpMouseMoved

    private void btnDesignFnStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_btnDesignFnStateChanged
    }//GEN-LAST:event_btnDesignFnStateChanged

    private void btnDesignFnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDesignFnActionPerformed
        if (btnDesignFn.isSelected())
            {
            editorThread = m_factory.m_lossFn.CreateEditor((MultiLayerCanvas)m_cnvsExp);
            if (null != editorThread)
                {
                editorThread.start();
                }
            }
        else
            {
            exitEditorMode ();
            }
        
        updateControlsByStatus();
    }//GEN-LAST:event_btnDesignFnActionPerformed

    private void exitEditorMode ()
        {
        editorThread.exit();
        editorThread = null;
        ((MultiLayerCanvas)m_cnvsExp).ClearOfscreen();    
        }
    
    private void m_cnvsExpMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_cnvsExpMouseExited
           if (null != editorThread)
                {
                editorThread.SetPos(-100, -100);
                }
    }//GEN-LAST:event_m_cnvsExpMouseExited

    private void m_cnvsExpMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_m_cnvsExpMouseWheelMoved
         if (null != editorThread)
             {
             float s = editorThread.GetSigma();
             s += 10.0f * evt.getWheelRotation();
             if (s < 1.0f) { s = 1.0f;}
             if (s > 400.0f) { s = 400.0f;}
             
             editorThread.SetSigma(s);
             }
    }//GEN-LAST:event_m_cnvsExpMouseWheelMoved

    private void m_cnvsExpMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_cnvsExpMouseReleased
         if (null != editorThread)
             {
             boolean wasClean = !m_factory.m_lossFn.isDirty ();
             float s = editorThread.GetSigma();
             float f = (evt.getButton() == MouseEvent.BUTTON1) ? 1.0f : -1.0f;
             m_factory.m_lossFn.OnUpdateAction(evt.getX(), evt.getY(), s/3.333f, f);
             m_factory.updateView(false, chkGrayScale.isSelected(), chkContour.isSelected(), chkInvertColors.isSelected());
             editorThread.redraw();
             
             if (wasClean)
                {
                updateControlsByStatus(); 
                }
             }
    }//GEN-LAST:event_m_cnvsExpMouseReleased

    private void chkInvertColorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkInvertColorsActionPerformed
         m_factory.updateView(false, chkGrayScale.isSelected(), chkContour.isSelected(), chkInvertColors.isSelected());
    }//GEN-LAST:event_chkInvertColorsActionPerformed

    private void btnPlayStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlayStopActionPerformed
        if (btnDesignFn.isSelected()) { btnDesignFn.setSelected(false); exitEditorMode ();}
        
        m_factory.runOneStep (getRunSpeedValue (), chkTracePaths.isSelected(), (!chkGrayScale.isSelected() ? Color.DARK_GRAY : Color.ORANGE));

        updateControlsByStatus ();        
    
    }//GEN-LAST:event_btnPlayStopActionPerformed

    private void updateControlsByStatus ()
        {
        boolean r = m_factory.isRunning();
        boolean p = m_factory.isPaused();
        btnStart.setIcon ((p || !r) ? m_playIcon : m_pauseIcon);
        btnPause.setEnabled(r);
        btnGenerate.setEnabled(!r && m_factory.m_lossFn.canGenerate());
        btnDefaultProps.setEnabled(!r);
        cmbEngine.setEnabled(!r);  
        cmbSelLossFn.setEnabled(!r);
        btnDesignFn.setEnabled(!r && m_factory.m_lossFn.canDesign ());
        btnPlayStop.setEnabled(p || !r);
        
        propertySheetPanelLoss.repaint();
        propertySheetPanelMain.repaint();
        }
    
    private class FnPropertyChangeListener implements PropertyChangeListener 
        {
        @Override
        public void propertyChange(PropertyChangeEvent pce) 
            {
            updateControlsByStatus ();
            }

        }    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDefaultProps;
    private javax.swing.JToggleButton btnDesignFn;
    private javax.swing.JButton btnGenerate;
    private javax.swing.JButton btnPause;
    private javax.swing.JButton btnPlayStop;
    private javax.swing.JButton btnStart;
    private javax.swing.JCheckBox chkContour;
    private javax.swing.JCheckBox chkGrayScale;
    private javax.swing.JCheckBox chkInvertColors;
    private javax.swing.JCheckBox chkTracePaths;
    private javax.swing.JComboBox cmbEngine;
    private javax.swing.JComboBox cmbSelLossFn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanelChart;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private info.monitorenter.gui.chart.Chart2D m_chart2D;
    private java.awt.Canvas m_cnvsExp;
    private com.l2fprod.common.propertysheet.PropertySheetPanel propertySheetPanelLoss;
    private com.l2fprod.common.propertysheet.PropertySheetPanel propertySheetPanelMain;
    private javax.swing.JSlider sliderSpeed;
    private javax.swing.JTextField txtEngineName;
    // End of variables declaration//GEN-END:variables
    private EngineFactory m_factory;
    private boolean m_initialized = false;
    private GaussianEditorRenderer editorThread = null;
    
    ImageIcon m_playIcon;
    ImageIcon m_pauseIcon;
    
    private String m_presetEngine = null;
    private String m_presetFunction = null;
}
