package com.example.budgetmanager.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.budgetmanager.R;
import com.example.budgetmanager.adapters.LegendAdapter;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.DatabaseHelper.CategoryType;
import com.example.budgetmanager.database.dao.ChartDao;
import com.example.budgetmanager.dto.CategoryTotal;
import com.example.budgetmanager.dto.MonthlyTotal;
import com.example.budgetmanager.utils.SharedPreferencesHelper;
import com.example.budgetmanager.utils.NavigationHelper;
import com.example.budgetmanager.utils.EdgeToEdgeHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
/**
 * STATSACTIVITY - DEVELOPER GUIDE
 *
 * HIGH-LEVEL EXPLANATION:
 * StatsActivity is responsible for displaying the user's financial statistics.
 * It uses two types of charts: Pie Chart and Line Chart.
 * The Pie Chart displays the distribution of expenses or income by category.
 * The Line Chart displays the trend of expenses or income over time.
 * The user can switch between expense and income charts using the toggle buttons.
 * 
 * KEY DESIGN DECISIONS:
 * - Use toggle buttons to switch between expense and income
 * - Use RecyclerView for category legend display
 * 
 */
public class StatsActivity extends AppCompatActivity {
    
    // UI Components
    private ToggleButton chartToggleIncome, chartToggleExpense;
    private PieChart pieChart;
    private LineChart lineChart;
    private TextView pieChartTitle, lineChartTitle;
    private RecyclerView legendRecyclerView;

    // Data access
    private DatabaseHelper dbHelper;
    private ChartDao chartDao;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private SharedPreferencesHelper.ThemeManager themeManager;
    private SharedPreferencesHelper.LanguageManager languageManager;
    private SharedPreferencesHelper.UserManager userManager;
    private LegendAdapter legendAdapter;
    private int currentUserId;
    private String currentChartType = "expense"; // Default to expense

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable Edge-to-Edge
        EdgeToEdge.enable(this);
        
        // Initialize dependencies first
        initializeDependencies();
        
        // Apply language and theme
        languageManager.applyLanguageOnStartup(this);
        themeManager.applyTheme(this);
        
        setContentView(R.layout.activity_stats);
        
        // Setup edge-to-edge helper
        EdgeToEdgeHelper.handleWindowInsets(findViewById(R.id.main));
        
        // Setup views and load data
        initializeViews();
        setupViews();
        loadInitialData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Reapply theme in case it was changed
        themeManager.applyTheme(this);
        
        // Reload data in case transactions were added/updated
        loadInitialData();
    }

    private void initializeDependencies() {
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        themeManager = new SharedPreferencesHelper.ThemeManager(sharedPreferencesHelper);
        languageManager = new SharedPreferencesHelper.LanguageManager(sharedPreferencesHelper);
        userManager = new SharedPreferencesHelper.UserManager(sharedPreferencesHelper);
        
        dbHelper = DatabaseHelper.getInstance(this);
        chartDao = new ChartDao(dbHelper);
        currentUserId = userManager.getUserId();
    }

    private void initializeViews() {
        // Chart toggles (removed Both toggle)
        chartToggleIncome = findViewById(R.id.chart_toggle_income);
        chartToggleExpense = findViewById(R.id.chart_toggle_expense);

        // Charts
        pieChart = findViewById(R.id.pie_chart);
        lineChart = findViewById(R.id.line_chart);

        // Titles
        pieChartTitle = findViewById(R.id.pie_chart_title);
        lineChartTitle = findViewById(R.id.line_chart_title);

        // Legend RecyclerView
        legendRecyclerView = findViewById(R.id.legend_recycler_view);
    }
        
    private void setupViews() {
        // Setup navigation
        NavigationHelper.setupNavigation(this, NavigationHelper.NavigationPage.STATS);
        
        // Setup legend RecyclerView
        legendRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Setup chart toggle listeners
        setupChartToggleListeners();
        
        // Set initial toggle states - expense selected by default
        updateToggleButtonStates();
    }

    private void setupChartToggleListeners() {
        chartToggleIncome.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                currentChartType = "income";
                updateToggleButtonStates();
                loadChartData();
            }
        });

        chartToggleExpense.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                currentChartType = "expense";
                updateToggleButtonStates();
                loadChartData();
            }
        });
    }

    private void updateToggleButtonStates() {
        // Update checked states
        chartToggleIncome.setChecked("income".equals(currentChartType));
        chartToggleExpense.setChecked("expense".equals(currentChartType));
        
        // Update button colors dynamically
        updateToggleButtonColors();
    }

    private void updateToggleButtonColors() {
        if ("income".equals(currentChartType)) {
            // Income selected - income green, expense neutral
            chartToggleIncome.setBackgroundResource(R.drawable.bg_toggle_selected_income);
            chartToggleIncome.setTextColor(getResources().getColor(R.color.income_text, getTheme()));
            
            chartToggleExpense.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            chartToggleExpense.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        } else {
            // Expense selected - expense red, income neutral  
            chartToggleExpense.setBackgroundResource(R.drawable.bg_toggle_selected_expense);
            chartToggleExpense.setTextColor(getResources().getColor(R.color.expense_text, getTheme()));
            
            chartToggleIncome.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            chartToggleIncome.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        }
    }

    private void loadInitialData() {
        loadChartData();
        loadGraphData();
    }

    private void loadChartData() {
        try {
            List<CategoryTotal> categoryTotals = chartDao.getCategoryTotals(currentUserId, currentChartType);
            
            // Update pie chart title
            if ("income".equals(currentChartType)) {
                pieChartTitle.setText(R.string.chart_title_income);
            } else {
                pieChartTitle.setText(R.string.chart_title_expense);
            }
            
            if (categoryTotals.isEmpty()) {
                pieChart.setNoDataText(getString(R.string.no_data_available));
                pieChart.clear();
                legendRecyclerView.setVisibility(View.GONE);
            } else {
                setupPieChart(categoryTotals);
                setupLegend(categoryTotals);
                legendRecyclerView.setVisibility(View.VISIBLE);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            pieChart.setNoDataText(getString(R.string.no_data_available));
            pieChart.clear();
            legendRecyclerView.setVisibility(View.GONE);
        }
    }

    private void setupPieChart(List<CategoryTotal> categoryTotals) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        
        for (CategoryTotal categoryTotal : categoryTotals) {
            entries.add(new PieEntry((float) categoryTotal.getTotal(), categoryTotal.getCategoryName()));
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "");
        
        // Use Material Design colors properly
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        
        // Styling
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        
        // Force US locale for pie chart value labels to avoid Arabic numerals
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.US, "$%.0f", value);
            }
        });
        
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        
        // Chart styling
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        
        // Hide legend (we use custom RecyclerView)
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);
        
        pieChart.invalidate();
    }

    private void setupLegend(List<CategoryTotal> categoryTotals) {
        if (legendAdapter == null) {
            legendAdapter = new LegendAdapter(this, categoryTotals, currentChartType);
            legendRecyclerView.setAdapter(legendAdapter);
        } else {
            legendAdapter.updateData(categoryTotals, currentChartType);
        }
    }

    private void loadGraphData() {
        try {
            List<MonthlyTotal> dailyTotals = chartDao.getDailyTotalsCurrentMonth(currentUserId);
            
            if (dailyTotals.isEmpty()) {
                lineChart.setNoDataText(getString(R.string.no_data_available));
                lineChart.clear();
            } else {
                setupLineChart(dailyTotals);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            lineChart.setNoDataText(getString(R.string.no_data_available));
            lineChart.clear();
        }
    }

    private void setupLineChart(List<MonthlyTotal> dailyTotals) {
        ArrayList<Entry> incomeEntries = new ArrayList<>();
        ArrayList<Entry> expenseEntries = new ArrayList<>();
        
        // Create entries with day of month as X value
        for (int i = 0; i < dailyTotals.size(); i++) {
            MonthlyTotal dailyTotal = dailyTotals.get(i);
            
            // Extract day from date (YYYY-MM-DD format)
            String date = dailyTotal.getDate();
            int dayOfMonth = Integer.parseInt(date.substring(8)); // Get DD from YYYY-MM-DD
            
            incomeEntries.add(new Entry(dayOfMonth, (float) dailyTotal.getIncome()));
            expenseEntries.add(new Entry(dayOfMonth, (float) dailyTotal.getExpense()));
        }
        
        // Income line
        LineDataSet incomeDataSet = new LineDataSet(incomeEntries, getString(R.string.toggle_income));
        incomeDataSet.setColor(getResources().getColor(R.color.income_text, getTheme()));
        incomeDataSet.setCircleColor(getResources().getColor(R.color.income_text, getTheme()));
        incomeDataSet.setLineWidth(3f);
        incomeDataSet.setCircleRadius(5f);
        incomeDataSet.setValueTextSize(10f);
        incomeDataSet.setDrawValues(false); // Hide value labels on points
        
        // Expense line
        LineDataSet expenseDataSet = new LineDataSet(expenseEntries, getString(R.string.toggle_expense));
        expenseDataSet.setColor(getResources().getColor(R.color.expense_text, getTheme()));
        expenseDataSet.setCircleColor(getResources().getColor(R.color.expense_text, getTheme()));
        expenseDataSet.setLineWidth(3f);
        expenseDataSet.setCircleRadius(5f);
        expenseDataSet.setValueTextSize(10f);
        expenseDataSet.setDrawValues(false); // Hide value labels on points
        
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(incomeDataSet);
        dataSets.add(expenseDataSet);
        
        LineData data = new LineData(dataSets);
        lineChart.setData(data);
        
        // Configure chart styling and axes
        setupChartAxes();
        
        lineChart.invalidate();
    }
    
    private void setupChartAxes() {
        // Chart styling
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        
        // X-Axis configuration (Days)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(getResources().getColor(R.color.divider_color, getTheme()));
        xAxis.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        xAxis.setTextSize(12f);
        xAxis.setGranularity(5f);  // Show every 5th day
        xAxis.setGranularityEnabled(true);
        xAxis.setAxisMinimum(1f);   // Start from day 1
        xAxis.setAxisMaximum(30f);  // End at day 30
        xAxis.setLabelCount(6, true); // Force exactly 6 labels: 1, 5, 10, 15, 20, 25, 30
        
        // Simple formatter that just shows the day number - force US locale
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.US, "%.0f", value);
            }
        });
        
        // Y-Axis configuration (Amount with currency)
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(getResources().getColor(R.color.divider_color, getTheme()));
        leftAxis.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        leftAxis.setTextSize(12f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Force US locale to avoid Arabic numerals in currency formatting
                return "$" + String.format(Locale.US, "%.0f", value);
            }
        });
        
        // Disable right Y-axis
        lineChart.getAxisRight().setEnabled(false);
        
        // Configure legend
        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        legend.setTextSize(14f);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        languageManager.applyLanguageOnStartup(this);
    }
}