package mandelbrot;

import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;



public class Mandelbrot extends javax.swing.JFrame {

    private int numWorkers = 1;
    private ExecutorService executor;
    private int[][] resultados;
    private double x1 = -2;
    private double y1 = 1;
    private double x2 = 1;
    private double y2 = -1;
    private int cx1, cy1, cx2, cy2;

    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel panel;
    private javax.swing.JSpinner spinner;

    private final Object lock = new Object();
    // End of variables declaration

    public Mandelbrot() {
        initComponents();
        executor = Executors.newFixedThreadPool(numWorkers);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        jButton1 = new javax.swing.JButton();
        panel = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Conjunto de Mandelbrot");

        jButton1.setText("Dibujar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panelMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                panelMouseReleased(evt);
            }
        });
        panel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                panelMouseDragged(evt);
            }
        });

        jButton2.setText("Reset");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));
        panelLayout.setVerticalGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 260, Short.MAX_VALUE));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jButton1).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jButton2).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(192, Short.MAX_VALUE)).addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jButton1).addComponent(jButton2).addComponent(spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap()));

        pack();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        // Apagar el ExecutorService anterior si está en ejecución
        if (executor != null && !executor.isTerminated()) {
            executor.shutdownNow();
        }

        // Vuelve a calcular el conjunto de Mandelbrot con el nuevo número de workers.
        calcularConjuntoMandelbrot();

        // Pinta los resultados en el panel
        pintaMandelbrot();
    }


    private void spinnerStateChanged(javax.swing.event.ChangeEvent evt) {
        // Este método se llama cuando cambia el valor del Spinner.
        // Vuelve a calcular el conjunto de Mandelbrot con el nuevo número de workers.
        calcularConjuntoMandelbrot();

        // Pinta los resultados en el panel
        pintaMandelbrot();
    }



    private void calcularConjuntoMandelbrot() {
        int maxIterations = 300;

        numWorkers = (int) spinner.getValue();
        executor = Executors.newFixedThreadPool(numWorkers);
        resultados = new int[panel.getWidth()][panel.getHeight()];

        int chunkHeight = panel.getHeight() / numWorkers;

        for (int i = 0; i < numWorkers; i++) {
            int startY = i * chunkHeight;
            int endY = (i + 1) * chunkHeight - 1;
            MandelbrotTask task = new MandelbrotTask(0, startY, panel.getWidth() - 1, endY);
            executor.execute(task);
        }

    }


    private void panelMousePressed(java.awt.event.MouseEvent evt) {
        cx1 = cx2 = evt.getX();
        cy1 = cy2 = evt.getY();
    }

    private void panelMouseReleased(java.awt.event.MouseEvent evt) {
        cy2 = evt.getY();

        x1 = cx1 * (x2 - x1) / panel.getWidth() + x1;
        y1 = y1 - cy1 * (y1 - y2) / panel.getHeight();

        x2 = cx2 * (x2 - x1) / panel.getWidth() + x1;
        y2 = y1 - cy2 * (y1 - y2) / panel.getHeight();

        pintaMandelbrot();
    }

    private void panelMouseDragged(java.awt.event.MouseEvent evt) {
        Graphics g = panel.getGraphics();

        g.setXORMode(Color.RED);

        g.drawRect(cx1, cy1, Math.abs(cx2 - cx1), Math.abs(cy2 - cy1));

        cx2 = evt.getX();
        cy2 = evt.getY();

        g.drawRect(cx1, cy1, Math.abs(cx2 - cx1), Math.abs(cy2 - cy1));
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        // Restaurar la vista inicial del conjunto de Mandelbrot
        resetearVista();

        // Vuelve a calcular el conjunto de Mandelbrot con el nuevo número de workers.
        calcularConjuntoMandelbrot();

        // Pinta los resultados en el panel
        pintaMandelbrot();
    }

    private void resetearVista() {
        x1 = -2;
        y1 = 1;
        x2 = 1;
        y2 = -1;
        //calcularConjuntoMandelbrot();
    }


    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Mandelbrot.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        java.awt.EventQueue.invokeLater(() -> {
            new Mandelbrot().setVisible(true);
        });

    }

    private class MandelbrotTask implements Runnable {
        private int startX, endX, startY, endY;

        public MandelbrotTask(int startX, int startY, int endX, int endY) {
            this.startX = startX;
            this.endX = endX;
            this.startY = startY;
            this.endY = endY;
        }

        @Override
        public void run() {
            // Implementa el cálculo del conjunto de Mandelbrot para la porción definida por startX, endX, startY y endY
            double x, y;
            for (int i = startX; i <= endX; i++) {
                for (int j = startY; j <= endY; j++) {
                    x = i * (x2 - x1) / panel.getWidth() + x1;
                    y = y1 - j * (y1 - y2) / panel.getHeight();
                    int velocidad = mandelbrot(x, y);
                    resultados[i][j] = velocidad;
                }
            }
        }
    }

    private int mandelbrot(double x, double y) {
        double zn1r = 0, zn1i = 0;
        double zn2r = 0, zn2i = 0;
        int contador = 0;

        while (contador < 300 && (zn2r * zn2r + zn2i * zn2i) < 10000) {
            zn2r = zn1r * zn1r - zn1i * zn1i + x;
            zn2i = 2 * zn1r * zn1i + y;

            zn1r = zn2r;
            zn1i = zn2i;

            contador++;
        }

        return contador;
    }



    private void pintaMandelbrot() {
        // Pintar los resultados en el panel
        Graphics g = panel.getGraphics();
        g.clearRect(0, 0, panel.getWidth(), panel.getHeight());

        for (int i = 0; i < panel.getWidth(); i++) {
            for (int j = 0; j < panel.getHeight(); j++) {
                int velocidad = resultados[i][j];
                g.setColor(Color.getHSBColor((float) velocidad / 300, 1, 1));
                g.drawRect(i, j, 1, 1);
            }
        }
    }





}




