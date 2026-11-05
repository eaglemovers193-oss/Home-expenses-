package com.homeexpenses.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class MainActivity extends Activity {

    LinearLayout main, list;
    TextView totalText, paidText, unpaidText;
    EditText nameInput, amountInput;
    ArrayList<JSONObject> expenses = new ArrayList<>();
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("expenses", MODE_PRIVATE);
        loadData();

        ScrollView scroll = new ScrollView(this);
        main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(20, 25, 20, 20);
        main.setBackgroundColor(Color.rgb(245,247,250));
        scroll.addView(main);
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText("HOME EXPENSES");
        title.setTextSize(25);
        title.setTextColor(Color.rgb(30,60,100));
        title.setGravity(Gravity.CENTER);
        main.addView(title);

        totalText = addText("Total: Rs 0", 21);
        paidText = addText("Paid: Rs 0", 17);
        unpaidText = addText("Unpaid: Rs 0", 17);

        nameInput = new EditText(this);
        nameInput.setHint("Expense name");
        main.addView(nameInput);

        amountInput = new EditText(this);
        amountInput.setHint("Amount in Rs");
        amountInput.setInputType(2);
        main.addView(amountInput);

        Button add = new Button(this);
        add.setText("ADD EXPENSE");
        main.addView(add);

        add.setOnClickListener(v -> addExpense());

        TextView heading = addText("EXPENSE LIST", 20);
        heading.setPadding(0,25,0,10);

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        main.addView(list);

        refresh();
    }

    TextView addText(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.DKGRAY);
        t.setPadding(5,10,5,10);
        main.addView(t);
        return t;
    }

    void addExpense() {
        String name = nameInput.getText().toString().trim();
        String amount = amountInput.getText().toString().trim();

        if (name.isEmpty() || amount.isEmpty()) {
            Toast.makeText(this, "Enter name and amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject item = new JSONObject();
            item.put("name", name);
            item.put("amount", Double.parseDouble(amount));
            item.put("paid", false);

            expenses.add(item);
            nameInput.setText("");
            amountInput.setText("");
            saveData();
            refresh();

        } catch (Exception e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }

    void refresh() {
        list.removeAllViews();

        double total = 0, paid = 0;

        for (int i = 0; i < expenses.size(); i++) {
            final int index = i;
            JSONObject item = expenses.get(i);

            double amount = item.optDouble("amount", 0);
            boolean isPaid = item.optBoolean("paid", false);

            total += amount;
            if (isPaid) paid += amount;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(12,12,12,12);
            row.setBackgroundColor(Color.WHITE);

            CheckBox check = new CheckBox(this);
            check.setText(item.optString("name") + " - Rs " + amount);
            check.setTextSize(16);
            check.setChecked(isPaid);

            check.setOnCheckedChangeListener((button, checked) -> {
                try {
                    expenses.get(index).put("paid", checked);
                    saveData();
                    refresh();
                } catch (Exception e) {}
            });

            Button delete = new Button(this);
            delete.setText("DELETE");
            delete.setOnClickListener(v -> {
                expenses.remove(index);
                saveData();
                refresh();
            });

            row.addView(check);
            row.addView(delete);
            list.addView(row);
        }

        totalText.setText("Total: Rs " + total);
        paidText.setText("Paid: Rs " + paid);
        unpaidText.setText("Unpaid: Rs " + (total - paid));
    }

    void saveData() {
        JSONArray array = new JSONArray();
        for (JSONObject item : expenses) {
            array.put(item);
        }
        prefs.edit().putString("items", array.toString()).apply();
    }

    void loadData() {
        try {
            JSONArray array = new JSONArray(
                prefs.getString("items", "[]")
            );
            for (int i = 0; i < array.length(); i++) {
                expenses.add(array.getJSONObject(i));
            }
        } catch (Exception e) {}
    }
          }
