
package com.homeexpenses.app;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.print.*;
import android.webkit.WebView;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity {

    LinearLayout main, list;
    TextView totalText, paidText, unpaidText, countText;
    ArrayList<JSONObject> expenses = new ArrayList<>();
    SharedPreferences prefs;

    int blue = Color.rgb(15, 91, 220);
    int darkBlue = Color.rgb(8, 48, 150);
    int green = Color.rgb(0, 165, 65);
    int red = Color.rgb(235, 35, 65);
    int ink = Color.rgb(20, 35, 75);
    int pale = Color.rgb(244, 247, 255);

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("expenses", MODE_PRIVATE);
        loadData();
        showHome();
    }

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density);
    }

    GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    GradientDrawable gradient(int a, int b, int radius) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{a,b}
        );
        d.setCornerRadius(dp(radius));
        return d;
    }

    TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    LinearLayout.LayoutParams params(int w, int h) {
        return new LinearLayout.LayoutParams(w < 0 ? w : dp(w),
            h < 0 ? h : dp(h));
    }

    void gap(LinearLayout parent, int height) {
        View v = new View(this);
        parent.addView(v, params(1,height));
    }

    TextView button(String label, int color) {
        TextView b = text(label, 16, Color.WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(12),dp(14),dp(12),dp(14));
        b.setBackground(gradient(color, darkBlue, 18));
        return b;
    }

    LinearLayout card() {
        LinearLayout c = column();
        c.setPadding(dp(15),dp(14),dp(15),dp(14));
        c.setBackground(shape(Color.WHITE,20));
        return c;
    }

    void showHome() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        main = column();
        main.setPadding(dp(16),dp(18),dp(16),dp(18));
        main.setBackgroundColor(pale);
        scroll.addView(main);
        setContentView(scroll);

        LinearLayout header = column();
        header.setPadding(dp(18),dp(20),dp(18),dp(20));
        header.setBackground(gradient(0xffd8ecff,0xffffffff,24));

        TextView brand = text("⌂  HOME EXPENSES",25,darkBlue,true);
        brand.setGravity(Gravity.CENTER);
        header.addView(brand);

        TextView tagline = text("Manage Your Home Budget Easily",14,ink,false);
        tagline.setGravity(Gravity.CENTER);
        header.addView(tagline);
        ImageView homePicture = new ImageView(this);
homePicture.setImageResource(R.drawable.ic_home_banner);
homePicture.setScaleType(ImageView.ScaleType.FIT_CENTER);

LinearLayout.LayoutParams imageParams =
    new LinearLayout.LayoutParams(-1, dp(125));

imageParams.topMargin = dp(10);
header.addView(homePicture, imageParams);
        main.addView(header);

        gap(main,15);

        LinearLayout summaries = new LinearLayout(this);
        summaries.setOrientation(LinearLayout.HORIZONTAL);

        totalText = summaryCard(summaries,"TOTAL","Rs 0",blue);
        paidText = summaryCard(summaries,"PAID","Rs 0",green);
        unpaidText = summaryCard(summaries,"UNPAID","Rs 0",red);
        main.addView(summaries);

        gap(main,16);

        LinearLayout overview = card();
        overview.addView(text("📊  Expenses Overview",19,ink,true));
        overview.addView(text("Your household budget at a glance",13,Color.GRAY,false));
        main.addView(overview);

        gap(main,15);

        TextView add = button("＋   ADD EXPENSE",blue);
        main.addView(add);
        add.setOnClickListener(v -> addDialog());

        gap(main,18);

        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.addView(text("☷  Expense List",21,ink,true),
            new LinearLayout.LayoutParams(0,dp(42),1));
        countText = text("0 Items",13,blue,true);
        heading.addView(countText);
        main.addView(heading);

        list = column();
        main.addView(list);

        gap(main,16);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        TextView print = button("🖨  PRINT",green);
        TextView pdf = button("▣  SAVE AS PDF",0xff8128d8);

        LinearLayout.LayoutParams half =
            new LinearLayout.LayoutParams(0,dp(55),1);
        half.setMargins(dp(3),0,dp(3),0);
        actions.addView(print,half);
        actions.addView(pdf,half);
        main.addView(actions);

        print.setOnClickListener(v -> printPDF());
        pdf.setOnClickListener(v -> printPDF());

        gap(main,18);

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setBackground(shape(Color.WHITE,18));

        String[] tabs = {"Home","Reports","History","About"};

int[] icons = {
    R.drawable.ic_home,
    R.drawable.ic_reports,
    R.drawable.ic_history,
    R.drawable.ic_about
};

for (int i=0; i<tabs.length; i++) {
    final int index=i;

    LinearLayout item = column();
    item.setGravity(Gravity.CENTER);
    item.setPadding(dp(4),dp(10),dp(4),dp(10));

    ImageView icon = new ImageView(this);
    icon.setImageResource(icons[i]);

    LinearLayout.LayoutParams ip = params(24,24);
    item.addView(icon,ip);

    TextView label = text(tabs[i],11,blue,true);
    item.addView(label);

    nav.addView(item,
        new LinearLayout.LayoutParams(0,-2,1));

    item.setOnClickListener(v -> openTab(index));
}
        main.addView(nav);

        refresh();
    }

    TextView summaryCard(LinearLayout parent,String title,
                         String amount,int color) {
        LinearLayout c = column();
        c.setPadding(dp(8),dp(13),dp(8),dp(13));
        c.setGravity(Gravity.CENTER);
        c.setBackground(gradient(color,darkBlue,17));

        TextView name = text(title,12,Color.WHITE,true);
        name.setGravity(Gravity.CENTER);
        c.addView(name);

        TextView value = text(amount,14,Color.WHITE,true);
        value.setGravity(Gravity.CENTER);
        c.addView(value);

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(0,dp(88),1);
        p.setMargins(dp(3),0,dp(3),0);
        parent.addView(c,p);
        return value;
    }

    void addDialog() {
        LinearLayout box = column();
        box.setPadding(dp(20),dp(8),dp(20),dp(8));

        EditText name = new EditText(this);
        name.setHint("Expense name");
        box.addView(name);

        EditText amount = new EditText(this);
        amount.setHint("Amount in Rs");
        amount.setInputType(8194);
        box.addView(amount);

        CheckBox paid = new CheckBox(this);
        paid.setText("Already Paid");
        box.addView(paid);

        new AlertDialog.Builder(this)
            .setTitle("Add New Expense")
            .setView(box)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("SAVE",(d,w)->{
                String n=name.getText().toString().trim();
                String a=amount.getText().toString().trim();

                if(n.isEmpty() || a.isEmpty()) {
                    Toast.makeText(this,"Enter name and amount",
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double value=Double.parseDouble(a);
                    if(value<=0) throw new Exception();

                    JSONObject item=new JSONObject();
                    item.put("name",n);
                    item.put("amount",value);
                    item.put("paid",paid.isChecked());
                    item.put("date",new SimpleDateFormat(
                        "dd MMM yyyy",Locale.getDefault()).format(new Date()));

                    expenses.add(item);
                    saveData();
                    showHome();

                } catch(Exception e) {
                    Toast.makeText(this,"Enter a valid amount",
                        Toast.LENGTH_SHORT).show();
                }
            }).show();
    }

    void refresh() {
        if(list==null) return;
        list.removeAllViews();

        double total=0,paidAmount=0;

        for(int i=0;i<expenses.size();i++) {
            JSONObject item=expenses.get(i);
            total+=item.optDouble("amount",0);
            if(item.optBoolean("paid",false))
                paidAmount+=item.optDouble("amount",0);
        }

        totalText.setText("Rs "+String.format(Locale.US,"%,.0f",total));
        paidText.setText("Rs "+String.format(Locale.US,"%,.0f",paidAmount));
        unpaidText.setText("Rs "+String.format(Locale.US,"%,.0f",
            total-paidAmount));
        countText.setText(expenses.size()+" Items");

        for(int i=0;i<expenses.size();i++) {
            final int index=i;
            JSONObject item=expenses.get(i);

            LinearLayout row=card();
            LinearLayout.LayoutParams rp=params(-1,-2);
            rp.setMargins(0,dp(5),0,dp(5));
            list.addView(row,rp);

            TextView name=text("●  "+item.optString("name"),17,ink,true);
            row.addView(name);

            TextView amount=text("Rs "+String.format(Locale.US,"%,.0f",
                item.optDouble("amount",0)),16,blue,true);
            row.addView(amount);

            TextView date=text(item.optString("date",""),12,
                Color.GRAY,false);
            row.addView(date);

            CheckBox check=new CheckBox(this);
            check.setText(item.optBoolean("paid",false) ?
                "Paid" : "Unpaid");
            check.setChecked(item.optBoolean("paid",false));
            row.addView(check);

            check.setOnCheckedChangeListener((b,checked)->{
                try {
                    expenses.get(index).put("paid",checked);
                    saveData();
                    refresh();
                } catch(Exception e) {}
            });

            TextView delete=button("DELETE",red);
            LinearLayout.LayoutParams dp1=params(-1,42);
            dp1.topMargin=dp(5);
            row.addView(delete,dp1);
            delete.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                    .setTitle("Delete Expense?")
                    .setMessage("Do you want to delete this expense?")
                    .setNegativeButton("Cancel",null)
                    .setPositiveButton("Delete",(d,w)->{
                        expenses.remove(index);
                        saveData();
                        refresh();
                    }).show());
        }

        if(expenses.isEmpty()) {
            TextView empty=text("No expenses added yet.",15,
                Color.GRAY,false);
            empty.setGravity(Gravity.CENTER);
            list.addView(empty,params(-1,75));
        }
    }

    void openTab(int tab) {
        if(tab==0) {
            showHome();
        } else if(tab==1) {
            double total=0,paid=0;
            for(JSONObject x:expenses) {
                total+=x.optDouble("amount",0);
                if(x.optBoolean("paid",false))
                    paid+=x.optDouble("amount",0);
            }
            new AlertDialog.Builder(this)
                .setTitle("Expenses Report")
                .setMessage("Total: Rs "+total+
                    "\nPaid: Rs "+paid+
                    "\nUnpaid: Rs "+(total-paid))
                .setPositiveButton("OK",null).show();
        } else if(tab==2) {
            StringBuilder s=new StringBuilder();
            for(JSONObject x:expenses)
                s.append(x.optString("date","")).append("  ")
                 .append(x.optString("name")).append("  Rs ")
                 .append(x.optDouble("amount",0)).append("\n");
            new AlertDialog.Builder(this)
                .setTitle("Expense History")
                .setMessage(s.length()==0?"No history":s.toString())
                .setPositiveButton("OK",null).show();
        } else {
            new AlertDialog.Builder(this)
                .setTitle("Home Expenses")
                .setMessage("Manage your household expenses easily.\nVersion 2.0")
                .setPositiveButton("OK",null).show();
        }
    }

    void printPDF() {
        StringBuilder html=new StringBuilder();
        double total=0,paid=0;

        html.append("<html><body style='font-family:sans-serif;padding:20px'>");
        html.append("<h1 style='color:#095bdc'>HOME EXPENSES</h1>");
        html.append("<h3>Household Expense Report</h3>");
        html.append("<table border='1' cellpadding='9' cellspacing='0' width='100%'>");
        html.append("<tr><th>Expense</th><th>Amount</th><th>Status</th><th>Date</th></tr>");

        for(JSONObject x:expenses) {
            double a=x.optDouble("amount",0);
            boolean p=x.optBoolean("paid",false);
            total+=a;
            if(p) paid+=a;

            html.append("<tr><td>").append(x.optString("name"))
                .append("</td><td>Rs ").append(a)
                .append("</td><td>").append(p?"Paid":"Unpaid")
                .append("</td><td>").append(x.optString("date",""))
                .append("</td></tr>");
        }

        html.append("</table><h3>Total: Rs ").append(total)
            .append("</h3><h3>Paid: Rs ").append(paid)
            .append("</h3><h3>Unpaid: Rs ").append(total-paid)
            .append("</h3></body></html>");

        WebView web=new WebView(this);
        web.loadDataWithBaseURL(null,html.toString(),
            "text/HTML","UTF-8",null);

        web.setWebViewClient(new android.webkit.WebViewClient() {
            public void onPageFinished(WebView view,String url) {
                PrintManager manager=(PrintManager)getSystemService(
                    Context.PRINT_SERVICE);
                PrintDocumentAdapter adapter=view.createPrintDocumentAdapter(
                    "HomeExpenses");
                manager.print("Home Expenses Report",adapter,
                    new PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(new PrintAttributes.Resolution(
                        "pdf","PDF",300,300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build());
            }
        });
    }

    void saveData() {
        JSONArray array=new JSONArray();
        for(JSONObject x:expenses) array.put(x);
        prefs.edit().putString("items",array.toString()).apply();
    }

    void loadData() {
        try {
            JSONArray array=new JSONArray(prefs.getString("items","[]"));
            for(int i=0;i<array.length();i++)
                expenses.add(array.getJSONObject(i));
        } catch(Exception e) {}
    }
}
