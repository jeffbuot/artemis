/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.classes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 *
 * @author Jefferson
 */
public class ArtemisStatistics {

    public static Scene getScene(XYChart.Series series1, XYChart.Series series2) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> bc
                = new BarChart<>(xAxis, yAxis);
        bc.setTitle("Teachers Summary Statistics");
        xAxis.setLabel("Institutes");
        yAxis.setLabel("Population");
        bc.getData().addAll(series1, series2);
        Scene scene = new Scene(bc, 800, 600);
        scene.getStylesheets().add(ArtemisStatistics.class.getResource("/artemis/other/chartStyle.css").toExternalForm());
        return scene;
    }

    public static XYChart.Series getSeries(Connection con, String gender) throws SQLException {
        XYChart.Series series = new XYChart.Series();
        series.setName(gender);
        ResultSet rs = con.createStatement().executeQuery("Call showTeachersPopulation('" + gender + "')");
        while (rs.next()) {
            series.getData().add(new XYChart.Data(rs.getString("institute_id"), rs.getInt("count")));
        }
        return series;
    }
}
