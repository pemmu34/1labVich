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
import java.awt.*;

public class Main extends JFrame {

    // Исходные данные X 
    static final double[] X = {4.415, 4.420, 4.425, 4.430, 4.435, 4.440, 4.445, 4.450, 4.455, 4.460, 4.465};
    // Исходные данные Y
    static final double[] DEFAULT_Y = {0.888551, 0.889599, 0.890637, 0.891667, 0.892687, 0.893698, 0.894700, 0.895693, 0.896677, 0.897653, 0.898119};

    JTextField inputX;
    JTextField[] yFields; // Массив полей для ввода Y
    JLabel resultLabel;
    JTable table;
    ChartPanel chartPanel;

    public Main() {
        setTitle("Лабораторная работа №1: Метод Эйткена");
        setSize(1400, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Вывод формул
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
        leftPanel.setBorder(BorderFactory.createTitledBorder("Управление и данные Y"));
        leftPanel.setPreferredSize(new Dimension(250, 0));

        // Ввод X
        JPanel xPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        xPanel.add(new JLabel("Введите x:"));
        inputX = new JTextField("4.4175", 8);
        xPanel.add(inputX);
        leftPanel.add(xPanel);

        // Кнопка
        JButton calcBtn = new JButton("Вычислить");
        calcBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        calcBtn.setMaximumSize(new Dimension(200, 35));
        leftPanel.add(calcBtn);

        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Результат
        resultLabel = new JLabel("<html>Результат: </html>");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        resultLabel.setForeground(new Color(0, 100, 0));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(resultLabel);

        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Табличка для ввода Y
        JPanel yPanel = new JPanel(new GridLayout(X.length, 2, 5, 5));
        yPanel.setBorder(BorderFactory.createTitledBorder("Значения Y (можно менять)"));
        yFields = new JTextField[X.length];
        for (int i = 0; i < X.length; i++) {
            yPanel.add(new JLabel("y" + i + " (x=" + X[i] + "):"));
            yFields[i] = new JTextField(String.valueOf(DEFAULT_Y[i]));
            yFields[i].setFont(new Font("Arial", Font.PLAIN, 12));
            yPanel.add(yFields[i]);
        }

        JScrollPane yScroll = new JScrollPane(yPanel);
        yScroll.setBorder(null);
        leftPanel.add(yScroll);

        add(leftPanel, BorderLayout.WEST);

        table = new JTable();
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Схема Эйткена"));

        chartPanel = new ChartPanel(null);
        chartPanel.setBorder(BorderFactory.createTitledBorder("График интерполяции"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, chartPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        calcBtn.addActionListener(e -> {
            try {
                double x = Double.parseDouble(inputX.getText().replace(',', '.'));
                // Считываем значения Y из текстовых полей
                double[] currentY = new double[X.length];
                for (int i = 0; i < X.length; i++) {
                    currentY[i] = Double.parseDouble(yFields[i].getText().replace(',', '.'));
                }
                calculateAndDraw(x, currentY);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Проверьте правильность ввода чисел (используйте точку или запятую).");
            }
        });

        // Первичная отрисовка (по умолчанию)
        calculateAndDraw(4.4175, DEFAULT_Y);
    }

    private void calculateAndDraw(double x, double[] Y) {
        int n = X.length;
        double[][] P = new double[n][n];
        Object[][] tableData = new Object[n][n + 2];
        String[] colNames = new String[n + 2];

        colNames[0] = "i"; colNames[1] = "x_i";
        for (int i = 0; i < n; i++) colNames[i + 2] = "y" + (i == 0 ? "0" : i);

        // Инициализация 0-го столбца
        for (int i = 0; i < n; i++) {
            P[i][0] = Y[i];
            tableData[i][0] = i;
            tableData[i][1] = X[i];
            tableData[i][2] = String.format("%.5f", Y[i]);
        }

        // Итерационный процесс (Формула Эйткена)
        for (int j = 1; j < n; j++) {
            for (int i = j; i < n; i++) {
                P[i][j] = (P[j-1][j-1] * (x - X[i]) - P[i][j-1] * (x - X[j-1])) / (X[i] - X[j-1]);
                tableData[i][j + 2] = String.format("%.4f", P[i][j]);
            }
        }

        double resultY = P[n-1][n-1];

        // Проверка на экстраполяцию
        if (x < X[0] || x > X[n-1]) {
            resultLabel.setText(String.format("<html>y(%.4f) = %.4f<br><font color='red'>ЭКСТРАПОЛЯЦИЯ!</font></html>", x, resultY));
        } else {
            resultLabel.setText(String.format("<html>y(%.4f) = %.6f</html>", x, resultY));
        }

        table.setModel(new DefaultTableModel(tableData, colNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(60);
        }

        // Отрисовка графика 
        XYSeries pointsSeries = new XYSeries("Табличные точки");
        for (int i = 0; i < n; i++) {
            pointsSeries.add(X[i], Y[i]);
        }

        XYSeries curveSeries = new XYSeries("Кривая Эйткена");
        double minX = X[0], maxX = X[n-1];
        double step = (maxX - minX) / 100.0;

        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (double cx = minX; cx <= maxX; cx += step) {
            double[][] tempP = new double[n][n];
            for (int i = 0; i < n; i++) tempP[i][0] = Y[i];
            for (int j = 1; j < n; j++) {
                for (int i = j; i < n; i++) {
                    tempP[i][j] = (tempP[j-1][j-1] * (cx - X[i]) - tempP[i][j-1] * (cx - X[j-1])) / (X[i] - X[j-1]);
                }
            }
            double cy = tempP[n-1][n-1];
            curveSeries.add(cx, cy);
            if (cy < minY) minY = cy;
            if (cy > maxY) maxY = cy;
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(curveSeries);
        dataset.addSeries(pointsSeries);

        if (x >= minX && x <= maxX) {
            XYSeries targetSeries = new XYSeries("Искомая точка");
            targetSeries.add(x, resultY);
            dataset.addSeries(targetSeries);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Интерполяция методом Эйткена", "x", "y", dataset, PlotOrientation.VERTICAL, true, true, false);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesPaint(0, Color.BLUE);

        renderer.setSeriesLinesVisible(1, false);
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesPaint(1, Color.RED);

        if (x >= minX && x <= maxX) {
            renderer.setSeriesLinesVisible(2, false);
            renderer.setSeriesShapesVisible(2, true);
            renderer.setSeriesPaint(2, Color.GREEN);
        }

        chart.getXYPlot().getRangeAxis().setRange(minY - 0.001, maxY + 0.001);
        chart.getXYPlot().setBackgroundPaint(Color.WHITE);
        chartPanel.setChart(chart);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new Main().setVisible(true);
        });
    }
}
