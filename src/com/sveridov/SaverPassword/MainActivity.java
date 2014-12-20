package com.sveridov.SaverPassword;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.Formatter;

public class MainActivity extends Activity {

    public String site, login, password;
    private ViewFlipper flipper;
    private DBHelper helperDB;
    private byte numberDisplay = 1;
    private Dialog dialogLogins;
    private ListView listLogins;
    private String focusSite;
    private boolean change;
    private String changeSite, changeLogin;

    //List
    private Button btnAdd;
    private ListView listPassword;

    //ADD
    private EditText etSite;
    private EditText etLogin;
    private EditText etPassword;
    private CheckBox chbShowPassword;
    private Button btnSave;
    private Button btnDelete;


    //Show
    private TextView tvUserSite, tvUserLogin;
    private EditText etUserPassword;
    private CheckBox chbShowPassword2;
    private Button btnChange;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        helperDB = new DBHelper(this);

        flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.showNext();

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipper.showPrevious();
                numberDisplay = 0;
            }
        });

        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = helperDB.getWritableDatabase();
                db.execSQL("DELETE FROM Passwords WHERE site='"+tvUserSite.getText().toString()+"' AND login='"+tvUserLogin.getText().toString()+"';");
                ArrayList<String> sites = getSitesFromDB();
                setList(sites);
                onBackPressed();
            }
        });
        listPassword = (ListView) findViewById(R.id.listPassword);
        listPassword.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                focusSite = textView.getText().toString();
                selectItem(textView.getText().toString());
            }
        });

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String site = etSite.getText().toString();
                String login = etLogin.getText().toString();
                String password = etPassword.getText().toString();

                addPassword(site, login, password);
            }
        });
        etSite = (EditText) findViewById(R.id.etSite);
        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
        chbShowPassword = (CheckBox) findViewById(R.id.chbShowPassword);
        chbShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("SaverPassword", isChecked + "");
                if (isChecked) {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        tvUserSite = (TextView) findViewById(R.id.tvUserSite);
        tvUserLogin = (TextView) findViewById(R.id.tvUserLogin);
        etUserPassword = (EditText) findViewById(R.id.etUserPassword);
        chbShowPassword2 = (CheckBox) findViewById(R.id.chbShowPassword2);
        chbShowPassword2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("SaverPassword", isChecked + "");
                if (isChecked) {
                    etUserPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    etUserPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });


        dialogLogins = new Dialog(this);
        dialogLogins.setTitle("Please , select login!");
        dialogLogins.setCancelable(true);
        dialogLogins.setContentView(R.layout.layout_items);

        listLogins = (ListView) dialogLogins.findViewById(R.id.listLogins);
        listLogins.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getSiteFromDB(focusSite, position);
                flipper.showNext();
                numberDisplay = 2;
                dialogLogins.hide();
            }
        });

        btnChange = (Button) findViewById(R.id.btnChange);
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 site = tvUserSite.getText().toString();
                 login = tvUserLogin.getText().toString();
                 password = etUserPassword.getText().toString();

                etSite.setText(site);
                etLogin.setText(login);
                etPassword.setText(password);

                changeSite = site;
                changeLogin = login;

                change = true;
                flipper.showNext();
                numberDisplay = 0;
            }
        });

        etUserPassword.setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ArrayList<String> arraySites = getSitesFromDB();
        setList(arraySites);
    }

    private ArrayList<String> getSitesFromDB() {
        SQLiteDatabase db = helperDB.getReadableDatabase();
        Cursor c = db.query("Passwords", new String[]{"site"}, null, null, null, null, null);
        ArrayList<String> arraySites = new ArrayList<String>();
        if (c.moveToFirst()) {
            Log.d("SaverPassword", "Good");
            do {
                int idSite = c.getColumnIndex("site");
                String site = c.getString(idSite);
                if (hasSite(arraySites, site) == false)
                    arraySites.add(site);
            } while (c.moveToNext());
        }

        db.close();
        return arraySites;
    }

    public boolean hasSite(ArrayList<String> sites, String value) {
        for (String item : sites)
            if (item.equals(value))
                return true;
        return false;
    }

    private void addPassword(String site, String login, String password) {


        if (validatePassword(site, login, password) && change == false) {
            boolean hasSite =  hasSite(site , login);
            if(hasSite){
                Toast.makeText(this , "This Site and Login has in DB" , Toast.LENGTH_LONG).show();
                return;
            }
            SQLiteDatabase db = helperDB.getWritableDatabase();
            Formatter command = new Formatter().format("INSERT INTO Passwords (site,login,password) VALUES ('%s','%s','%s')", site, login, password);
            db.execSQL(command.toString());
            db.close();

            ArrayList<String> arraySites = getSitesFromDB();
            setList(arraySites);
            onBackPressed();
        } else {
            if(login.equals(changeLogin) == false){
                if(hasSite(site , login)){
                    Toast.makeText(this , "This Site and Login has in DB" , Toast.LENGTH_LONG).show();
                    return;
                }
            }
            SQLiteDatabase db = helperDB.getWritableDatabase();
            String command = "UPDATE Passwords SET site = '" + site + "' , login = '" +
                    login + "' , password = '" + password + "' WHERE site = '" +
                    changeSite + "' AND login = '"+changeLogin+"';";
            Log.d("SaverPassword", command);
            try{
            db.execSQL(command);
            }catch (Exception e){
                Log.d("SaverPassword", e.toString());
            }
            db.close();
            ArrayList<String> a = getSitesFromDB();
            setList(a);
            onBackPressed();
        }
    }

    private void cleanAddDisplay() {
        etPassword.setText(null);
        etLogin.setText(null);
        etSite.setText(null);
        chbShowPassword.setChecked(false);
    }

    private boolean validatePassword(String site, String login, String password) {
        if (site.length() != 0 && login.length() != 0 && password.length() != 0)
            return true;
        return false;
    }

    private void setList(ArrayList<String> arraySites) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  R.layout.item_list, arraySites);
        listPassword.setAdapter(adapter);
    }

    private void selectItem(String site) {
        Log.d("SaverPassword", site);

        int count = getCountOfSite(site);
        if (count == 1) {

            getSiteFromDB(site, 0);
            flipper.showNext();
            numberDisplay = 2;
            change = false;
        } else {
            setDialogLogins(site);
            dialogLogins.show();
        }
    }

    private int getCountOfSite(String site) {
        SQLiteDatabase db = helperDB.getReadableDatabase();
        Cursor c = db.query("Passwords", new String[]{"site"}, "site = ?", new String[]{site}, null, null, null);
        int count = 0;
        if (c != null && c.moveToFirst()) {
            do {
                ++count;
            } while (c.moveToNext());
        }
        db.close();
        return count;
    }

    private void getSiteFromDB(String site, int number) {
        SQLiteDatabase db = helperDB.getReadableDatabase();

        Cursor c = db.query("Passwords", new String[]{"site", "password", "login"}, "site = ?", new String[]{site}, null, null, null);

        int count = 0;

        if (c.moveToFirst()) {

            do {
                if (count == number) {
                    int idSite = c.getColumnIndex("site");
                    String stringSite = c.getString(idSite);
                    int idLogin = c.getColumnIndex("login");
                    String stringLogin = c.getString(idLogin);
                    int idPassword = c.getColumnIndex("password");
                    String stringPassword = c.getString(idPassword);


                    tvUserSite.setText(stringSite);
                    tvUserLogin.setText(stringLogin);
                    etUserPassword.setText(stringPassword);
                }
                ++count;
            } while (c.moveToNext());
        }

        db.close();
    }

    private void setDialogLogins(String site) {
        SQLiteDatabase db = helperDB.getReadableDatabase();

        Cursor c = db.query("Passwords", new String[]{"login"}, "site = ?", new String[]{site}, null, null, null);

        ArrayList<String> logins = new ArrayList<String>();

        if (c.moveToFirst()) {

            do {
                int idLogin = c.getColumnIndex("login");
                String stringLogin = c.getString(idLogin);

                logins.add(stringLogin);
            } while (c.moveToNext());

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.item_list, logins);
            listLogins.setAdapter(arrayAdapter);
        }

        db.close();
    }

    private boolean hasSite(String site , String login){
         SQLiteDatabase db = helperDB.getReadableDatabase();
         Cursor c =  db.query("Passwords", new String[]{"site" , "login"} , "site = ? AND login = ?" , new String[]{site , login} , null , null , null);
         if(c.moveToFirst())
             return true;
        return false;
    }
    @Override
    public void onBackPressed() {
        switch (numberDisplay) {
            case 0:
                flipper.showNext();
                numberDisplay = 1;
                change = false;
                cleanAddDisplay();
                break;
            case 1:
                change = false;
                cleanAddDisplay();
                super.onBackPressed();
                break;
            case 2:
                flipper.showPrevious();
                numberDisplay = 1;
                change = false;
                cleanAddDisplay();
                break;
        }
    }

    class DBHelper extends SQLiteOpenHelper {
        public static final String dbName = "SPDB";
        public static final byte version = 1;

        public DBHelper(Context context) {
            super(context, dbName, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE Passwords (id  INTEGER PRIMARY KEY AUTOINCREMENT , site TEXT , login TEXT ,password TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
