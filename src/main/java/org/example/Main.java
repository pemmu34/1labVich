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

    // Исходные данные (Таблица 1 из Задачи 2)
    static final double[] X = {4.415, 4.420, 4.425, 4.430, 4.435, 4.440, 4.445, 4.450, 4.455, 4.460, 4.465};
    static final double[] Y = {0.888551, 0.889599, 0.890637, 0.891667, 0.892687, 0.893698, 0.894700, 0.895693, 0.896677, 0.897653, 0.898119};

    JTextField inputX;
    JLabel resultLabel;
    JTable table;
    ChartPanel chartPanel;

    public Main() {
        setTitle("Лабораторная работа №1: Метод Эйткена");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ВЕРХНЯЯ ПАНЕЛЬ (Ввод и формулы)
        JPanel topPanel = new JPanel(new BorderLayout());

        String formula = "<html><b>Метод Эйткена:</b><br>" +
                "y<sub>ki</sub>(x) = ( y<sub>k-1,k-1</sub>(x) * (x - x<sub>i</sub>) - y<sub>i,k-1</sub>(x) * (x - x<sub>k-1</sub>) ) / ( x<sub>i</sub> - x<sub>k-1</sub> )</html>";
        topPanel.add(new JLabel(formula), BorderLayout.WEST);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Введите x (от 4.415 до 4.465):"));
        inputX = new JTextField("4.4175", 8);
        inputPanel.add(inputX);

        JButton calcBtn = new JButton("Вычислить");
        inputPanel.add(calcBtn);

        resultLabel = new JLabel("Результат: ");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(resultLabel);

        topPanel.add(inputPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ЦЕНТРАЛЬНАЯ ПАНЕЛЬ
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Схема Эйткена (промежуточные значения)"));
        centerPanel.add(scrollPane);

        chartPanel = new ChartPanel(null);
        chartPanel.setBorder(BorderFactory.createTitledBorder("График интерполяции"));
        centerPanel.add(chartPanel);

        add(centerPanel, BorderLayout.CENTER);

        calcBtn.addActionListener(e -> {
            try {
                double x = Double.parseDouble(inputX.getText().replace(',', '.'));
                calculateAndDraw(x);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректное число (например, 4.4175)");
            }
        });
    }

    private void calculateAndDraw(double x) {
        int n = X.length;
        double[][] P = new double[n][n];
        Object[][] tableData = new Object[n][n + 2];
        String[] colNames = new String[n + 2];

        colNames[0] = "i"; colNames[1] = "x_i";
        for (int i = 0; i < n; i++) colNames[i + 2] = "y_" + (i == 0 ? "0" : i);

        for (int i = 0; i < n; i++) {
            P[i][0] = Y[i];
            tableData[i][0] = i;
            tableData[i][1] = X[i];
            tableData[i][2] = String.format("%.6f", Y[i]);
        }

        // Итерационный процесс
        for (int j = 1; j < n; j++) {
            for (int i = j; i < n; i++) {
                P[i][j] = (P[j-1][j-1] * (x - X[i]) - P[i][j-1] * (x - X[j-1])) / (X[i] - X[j-1]);
                tableData[i][j + 2] = String.format("%.4f", P[i][j]); // Округляем для красоты
            }
        }

        double resultY = P[n-1][n-1];

        // Проверка на экстраполяцию
        if (x < X[0] || x > X[n-1]) {
            resultLabel.setText(String.format("y(%.4f) = %.4f (ЭКСТРАПОЛЯЦИЯ! Погрешность огромна)", x, resultY));
            resultLabel.setForeground(Color.RED);
        } else {
            resultLabel.setText(String.format("Результат: y(%.4f) = %.6f", x, resultY));
            resultLabel.setForeground(new Color(0, 128, 0)); // Зеленый
        }

        table.setModel(new DefaultTableModel(tableData, colNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(60);
        }

        // ГРАФИК
        XYSeries pointsSeries = new XYSeries("Табличные точки");
        for (int i = 0; i < n; i++) pointsSeries.add(X[i], Y[i]);

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

            // Ищем минимум и максимум для правильного масштаба
            if (cy < minY) minY = cy;
            if (cy > maxY) maxY = cy;
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(curveSeries);
        dataset.addSeries(pointsSeries);

        // Добавляем искомую точку, только если она в пределах графика
        if (x >= minX && x <= maxX) {
            XYSeries targetSeries = new XYSeries("Искомое значение");
            targetSeries.add(x, resultY);
            dataset.addSeries(targetSeries);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Интерполяция методом Эйткена", "x", "y", dataset, PlotOrientation.VERTICAL, true, true, false);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesLinesVisible(1, false);
        renderer.setSeriesShapesVisible(1, true);

        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.BLACK);

        if (x >= minX && x <= maxX) {
            renderer.setSeriesLinesVisible(2, false);
            renderer.setSeriesShapesVisible(2, true);
            renderer.setSeriesPaint(2, Color.RED);
        }

        // ОГРАНИЧИВАЕМ ОСЬ Y ДЛЯ КОРРЕКТНОГО ОТОБРАЖЕНИЯ
        chart.getXYPlot().getRangeAxis().setRange(minY - 0.001, maxY + 0.001);

        chartPanel.setChart(chart);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new Main().setVisible(true);
        });
    }
}