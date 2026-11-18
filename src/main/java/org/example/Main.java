package org.example;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Main extends JFrame {

    static final double[] X = {4.415, 4.420, 4.425, 4.430, 4.435, 4.440, 4.445, 4.450, 4.455, 4.460, 4.465};
    static final double[] DEFAULT_Y = {0.888551, 0.889599, 0.890637, 0.891667, 0.892687, 0.893698, 0.894700, 0.895693, 0.896677, 0.897653, 0.898119};

    double[] currentY;

    JTextField inputX;
    JComboBox<Integer> pointSelector;
    JTextField newYField;
    JLabel resultLabel;
    JTable table;
    ChartPanel chartPanel;
    JTextPane formulaPane;

    public Main() {
        setTitle("Лабораторная работа №1: Метод Эйткена (Модификация точки)");
        setSize(1400, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        currentY = DEFAULT_Y.clone();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String formulaHtml = "<html><body style='font-family: Arial; font-size: 14px;'>" +
                "<h3>Метод Эйткена (итерационная интерполяция)</h3>" +
                "<p style='margin-bottom: 2px;'><b>1. Базовая линейная интерполяция:</b><br>" +
                "y<sub>1i</sub>(x) = [ y<sub>0</sub>(x - x<sub>i</sub>) - y<sub>i</sub>(x - x<sub>0</sub>) ] / ( x<sub>i</sub> - x<sub>0</sub> )</p>" +
                "<p><b>2. Итерационная формула (для шагов k > 1):</b><br>" +
                "y<sub>ki</sub>(x) = [ y<sub>k-1,k-1</sub>(x)(x - x<sub>i</sub>) - y<sub>i,k-1</sub>(x)(x - x<sub>k-1</sub>) ] / ( x<sub>i</sub> - x<sub>k-1</sub> )</p>" +
                "</body></html>";
        topPanel.add(new JLabel(formulaHtml), BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Управление"));
        leftPanel.setPreferredSize(new Dimension(300, 0));

        JPanel xPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        xPanel.add(new JLabel("Введите x:"));
        inputX = new JTextField("4.4175", 8);
        xPanel.add(inputX);
        leftPanel.add(xPanel);

        JButton calcBtn = new JButton("Вычислить значение");
        calcBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        calcBtn.setMaximumSize(new Dimension(200, 35));
        leftPanel.add(calcBtn);

        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        resultLabel = new JLabel("<html>Результат: </html>");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        resultLabel.setForeground(new Color(0, 100, 0));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(resultLabel);

        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        leftPanel.add(new JSeparator());
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel editPanel = new JPanel();
        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));
        editPanel.setBorder(BorderFactory.createTitledBorder("Изменить одну точку"));

        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.add(new JLabel("Точка i:"));
        pointSelector = new JComboBox<>();
        for (int i = 0; i < X.length; i++) pointSelector.addItem(i);
        selectorPanel.add(pointSelector);
        editPanel.add(selectorPanel);

        JPanel yInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        yInputPanel.add(new JLabel("Новое Y:"));
        newYField = new JTextField(8);
        newYField.setText(String.valueOf(DEFAULT_Y[0]));
        yInputPanel.add(newYField);
        editPanel.add(yInputPanel);

        pointSelector.addActionListener(e -> {
            int idx = (int) pointSelector.getSelectedItem();
            newYField.setText(String.valueOf(currentY[idx]));
        });

        JButton applyBtn = new JButton("Применить Y");
        applyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        editPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        editPanel.add(applyBtn);

        JButton resetBtn = new JButton("Сбросить всё");
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        editPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        editPanel.add(resetBtn);

        leftPanel.add(editPanel);
        add(leftPanel, BorderLayout.WEST);

        table = new JTable();
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Схема Эйткена"));

        chartPanel = new ChartPanel(null);
        chartPanel.setBorder(BorderFactory.createTitledBorder("График интерполяции"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, chartPanel);
        splitPane.setResizeWeight(0.45);
        add(splitPane, BorderLayout.CENTER);

        formulaPane = new JTextPane();
        formulaPane.setEditorKit(new HTMLEditorKit());
        formulaPane.setEditable(false);
        formulaPane.setBackground(new Color(245, 245, 245));
        JScrollPane formulaScroll = new JScrollPane(formulaPane);
        formulaScroll.setBorder(BorderFactory.createTitledBorder("Формула интерполяционного полинома (точка)"));
        formulaScroll.setPreferredSize(new Dimension(1400, 120));
        add(formulaScroll, BorderLayout.SOUTH);

        calcBtn.addActionListener(e -> {
            try {
                double x = Double.parseDouble(inputX.getText().replace(',', '.'));
                calculateAndDraw(x);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка ввода x");
            }
        });

        applyBtn.addActionListener(e -> {
            try {
                int idx = (int) pointSelector.getSelectedItem();
                double newY = Double.parseDouble(newYField.getText().replace(',', '.'));
                currentY[idx] = newY;
                double x = Double.parseDouble(inputX.getText().replace(',', '.'));
                calculateAndDraw(x);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка ввода нового Y");
            }
        });

        resetBtn.addActionListener(e -> {
            currentY = DEFAULT_Y.clone();
            int idx = (int) pointSelector.getSelectedItem();
            newYField.setText(String.valueOf(currentY[idx]));
            double x = Double.parseDouble(inputX.getText().replace(',', '.'));
            calculateAndDraw(x);
        });

        calculateAndDraw(4.4175);
    }

    private void calculateAndDraw(double x) {
        int n = X.length;
        double[][] P = new double[n][n];
        Object[][] tableData = new Object[n][n + 2];
        String[] colNames = new String[n + 2];

        colNames[0] = "i";
        colNames[1] = "x_i";
        for (int i = 0; i < n; i++) colNames[i + 2] = "y" + (i == 0 ? "0" : i);

        for (int i = 0; i < n; i++) {
            P[i][0] = currentY[i];
            tableData[i][0] = i;
            tableData[i][1] = X[i];
            tableData[i][2] = String.format("%.5f", currentY[i]);
        }

        for (int j = 1; j < n; j++) {
            for (int i = j; i < n; i++) {
                P[i][j] = (P[j - 1][j - 1] * (x - X[i]) - P[i][j - 1] * (x - X[j - 1])) / (X[i] - X[j - 1]);
                tableData[i][j + 2] = String.format("%.4f", P[i][j]);
            }
        }

        double resultY = P[n - 1][n - 1];

        if (x < X[0] || x > X[n - 1]) {
            resultLabel.setText(String.format("<html>y(%.4f) = %.4f<br><font color='red'>ЭКСТРАПОЛЯЦИЯ!</font></html>", x, resultY));
        } else {
            resultLabel.setText(String.format("<html>y(%.4f) = %.6f</html>", x, resultY));
        }

        table.setModel(new DefaultTableModel(tableData, colNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(60);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries originalCurve = new XYSeries("Исходная кривая (эталон)");
        buildCurve(originalCurve, DEFAULT_Y);

        XYSeries currentCurve = new XYSeries("Измененная кривая");
        buildCurve(currentCurve, currentY);

        XYSeries originalPoints = new XYSeries("Исходные точки");
        for (int i = 0; i < n; i++) originalPoints.add(X[i], DEFAULT_Y[i]);

        XYSeries currentPoints = new XYSeries("Текущие точки");
        for (int i = 0; i < n; i++) currentPoints.add(X[i], currentY[i]);

        dataset.addSeries(originalCurve);
        dataset.addSeries(currentCurve);
        dataset.addSeries(originalPoints);
        dataset.addSeries(currentPoints);

        if (x >= X[0] && x <= X[n - 1]) {
            XYSeries targetSeries = new XYSeries("Искомое значение");
            targetSeries.add(x, resultY);
            dataset.addSeries(targetSeries);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Интерполяция методом Эйткена", "x", "y", dataset, PlotOrientation.VERTICAL, true, true, false);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();

        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, new float[]{6.0f, 6.0f}, 0.0f));

        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesPaint(1, new Color(200, 0, 0));
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));

        renderer.setSeriesLinesVisible(2, false);
        renderer.setSeriesShapesVisible(2, true);
        renderer.setSeriesPaint(2, Color.BLUE);
        renderer.setSeriesShape(2, new Ellipse2D.Double(-3, -3, 6, 6));

        renderer.setSeriesLinesVisible(3, false);
        renderer.setSeriesShapesVisible(3, true);
        renderer.setSeriesPaint(3, Color.RED);
        renderer.setSeriesShape(3, new Ellipse2D.Double(-4, -4, 8, 8));

        if (x >= X[0] && x <= X[n - 1]) {
            renderer.setSeriesLinesVisible(4, false);
            renderer.setSeriesShapesVisible(4, true);
            renderer.setSeriesPaint(4, Color.GREEN);
            renderer.setSeriesShape(4, new Ellipse2D.Double(-5, -5, 10, 10));
        }

        chart.getXYPlot().setBackgroundPaint(Color.WHITE);
        chartPanel.setChart(chart);

        updateFormula();
    }

    private void updateFormula() {
        double[] poly = getPolynomial(X, currentY);
        StringBuilder sb = new StringBuilder("<html><body style='font-family: Arial; font-size: 14px; padding: 5px;'><b>y(x) = </b>");
        boolean first = true;
        for (int i = poly.length - 1; i >= 0; i--) {
            if (Math.abs(poly[i]) < 1e-10) continue;
            if (!first) {
                if (poly[i] > 0) sb.append(" + ");
                else sb.append(" - ");
            } else {
                if (poly[i] < 0) sb.append("-");
            }
            double val = Math.abs(poly[i]);
            if (i == 0 || Math.abs(val - 1.0) > 1e-9) {
                sb.append(String.format("%.4g", val));
            }
            if (i > 0) {
                sb.append("x");
                if (i > 1) sb.append("<sup>").append(i).append("</sup>");
            }
            first = false;
        }
        sb.append("</body></html>");
        formulaPane.setText(sb.toString());
    }

    private double[] getPolynomial(double[] x, double[] y) {
        int n = x.length;
        double[] c = y.clone();
        for (int j = 1; j < n; j++) {
            for (int i = n - 1; i >= j; i--) {
                c[i] = (c[i] - c[i - 1]) / (x[i] - x[i - j]);
            }
        }

        double[] poly = new double[n];
        double[] q = new double[n];
        q[0] = 1.0;
        poly[0] = c[0];

        for (int j = 1; j < n; j++) {
            for (int i = n - 1; i > 0; i--) {
                q[i] = q[i - 1] - x[j - 1] * q[i];
            }
            q[0] = -x[j - 1] * q[0];

            for (int i = 0; i < n; i++) {
                poly[i] += c[j] * q[i];
            }
        }
        return poly;
    }

    private void buildCurve(XYSeries series, double[] Y) {
        int n = X.length;
        double minX = X[0], maxX = X[n - 1];
        double step = (maxX - minX) / 100.0;
        for (double cx = minX; cx <= maxX; cx += step) {
            double[][] tempP = new double[n][n];
            for (int i = 0; i < n; i++) tempP[i][0] = Y[i];
            for (int j = 1; j < n; j++) {
                for (int i = j; i < n; i++) {
                    tempP[i][j] = (tempP[j - 1][j - 1] * (cx - X[i]) - tempP[i][j - 1] * (cx - X[j - 1])) / (X[i] - X[j - 1]);
                }
            }
            series.add(cx, tempP[n - 1][n - 1]);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new Main().setVisible(true);
        });
    }
}
