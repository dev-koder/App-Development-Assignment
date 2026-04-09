package com.example.curencyconverterappnew;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Currency Converter Application
 *
 * Features:
 * - Convert between 4 currencies (INR, USD, EUR, JPY)
 * - Real-time conversion calculations
 * - Dark/Light theme toggle
 * - Theme persistence using SharedPreferences
 * - Input validation and error handling
 */
public class MainActivity extends AppCompatActivity {

    // UI Components
    private Spinner fromCurrency, toCurrency;
    private EditText amountInput;
    private Button convertBtn, themeBtn;
    private TextView resultText;

    // SharedPreferences for storing preferences
    private SharedPreferences preferences;
    private static final String PREF_THEME = "theme_preference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load saved theme preference
        loadThemePreference();

        setContentView(R.layout.activity_main);

        // Initialize UI components
        initializeViews();

        // Setup spinners with currencies
        setupSpinners();

        // Setup click listeners
        setupClickListeners();
    }

    /**
     * Load theme preference from SharedPreferences
     */
    private void loadThemePreference() {
        preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean(PREF_THEME, false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Find all UI elements by ID
     */
    private void initializeViews() {
        fromCurrency = findViewById(R.id.fromCurrency);
        toCurrency = findViewById(R.id.toCurrency);
        amountInput = findViewById(R.id.amountInput);
        convertBtn = findViewById(R.id.convertBtn);
        themeBtn = findViewById(R.id.themeBtn);
        resultText = findViewById(R.id.resultText);
    }

    /**
     * Setup spinners with currency data
     */
    private void setupSpinners() {
        String[] currencies = {"INR", "USD", "EUR", "JPY"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fromCurrency.setAdapter(adapter);
        toCurrency.setAdapter(adapter);

        // Set default values
        fromCurrency.setSelection(0); // INR
        toCurrency.setSelection(1);   // USD
    }

    /**
     * Setup button click listeners
     */
    private void setupClickListeners() {
        convertBtn.setOnClickListener(v -> performConversion());
        themeBtn.setOnClickListener(v -> toggleTheme());
    }

    /**
     * Perform currency conversion
     */
    private void performConversion() {
        // Get input values
        String amountStr = amountInput.getText().toString().trim();
        String from = fromCurrency.getSelectedItem().toString();
        String to = toCurrency.getSelectedItem().toString();

        // Validate input
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            amountInput.requestFocus();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            // Validate amount
            if (amount < 0) {
                Toast.makeText(this, "Amount cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert currency
            double result = convertCurrency(amount, from, to);

            // Display result
            String resultMsg = String.format("%.2f %s = %.2f %s", amount, from, result, to);
            resultText.setText(resultMsg);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Convert currency amount
     *
     * @param amount Amount to convert
     * @param from Source currency
     * @param to Destination currency
     * @return Converted amount
     */
    private double convertCurrency(double amount, String from, String to) {
        // Exchange rates (base: INR)
        double fromRate = getINRRate(from);
        double toRate = getINRRate(to);

        // Convert: (amount in from currency) → INR → (to currency)
        return (amount / fromRate) * toRate;
    }

    /**
     * Get exchange rate from INR to given currency
     *
     * @param currency Currency code
     * @return Exchange rate (1 INR = ? currency)
     */
    private double getINRRate(String currency) {
        switch (currency) {
            case "INR":
                return 1.0;
            case "USD":
                return 0.012; // 1 INR = 0.012 USD
            case "EUR":
                return 0.011; // 1 INR = 0.011 EUR
            case "JPY":
                return 1.85;  // 1 INR = 1.85 JPY
            default:
                return 1.0;
        }
    }

    /**
     * Toggle between dark and light theme
     */
    private void toggleTheme() {
        // Get current theme preference
        boolean isDarkMode = preferences.getBoolean(PREF_THEME, false);

        // Toggle
        isDarkMode = !isDarkMode;

        // Save preference
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_THEME, isDarkMode);
        editor.apply();

        // Apply theme
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Recreate activity to apply theme
        recreate();
        Toast.makeText(this, "Theme changed", Toast.LENGTH_SHORT).show();
    }
}
