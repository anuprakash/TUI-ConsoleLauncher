package ohi.andre.consolelauncher;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import ohi.andre.consolelauncher.commands.ExecInfo;
import ohi.andre.consolelauncher.tuils.interfaces.CommandExecuter;
import ohi.andre.consolelauncher.tuils.interfaces.Inputable;
import ohi.andre.consolelauncher.tuils.interfaces.Outputable;
import ohi.andre.consolelauncher.tuils.interfaces.Reloadable;
import ohi.andre.consolelauncher.tuils.stuff.PolicyReceiver;
import ohi.andre.consolelauncher.managers.PreferencesManager;
import ohi.andre.consolelauncher.tuils.Tuils;

public class LauncherActivity extends Activity implements Reloadable {

    private final int FILEUPDATE_DELAY = 300;

    public static final int COMMAND_REQUEST_PERMISSION = 10;
    public static final int STARTING_PERMISSION = 11;
    public static final int COMMAND_SUGGESTION_REQUEST_PERMISSION = 12;

    private final String FIRSTACCESS_KEY = "firstAccess";

    private UIManager ui;
    private MainManager main;

    private boolean hideStatusBar;
    private boolean openKeyboardOnStart;

    private PreferencesManager preferencesManager;

    private Intent starterIntent;

    private CommandExecuter ex = new CommandExecuter() {

        @Override
        public String exec(String input) {
            try {
                main.onCommand(input);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    private Inputable in = new Inputable() {

        @Override
        public void in(String s) {
            try {
                ui.setInput(s);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };

    private Outputable out = new Outputable() {

        @Override
        public void onOutput(String output) {
            try {
                ui.setOutput(output);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };
    private Runnable clearer = new Runnable() {

        @Override
        public void run() {
            ui.clear();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED  &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        LauncherActivity.STARTING_PERMISSION);
                return;
            }
        }

        finishOnCreate();
    }

    private void finishOnCreate() {

        SharedPreferences preferences = getPreferences(0);
        boolean firstAccess = preferences.getBoolean(FIRSTACCESS_KEY, true);
        if (firstAccess) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(FIRSTACCESS_KEY, false);
            editor.commit();

            Tuils.showTutorial(this);
        }

        File tuiFolder = getFolder();
        Resources res = getResources();
        starterIntent = getIntent();

        DevicePolicyManager policy = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName component = new ComponentName(this, PolicyReceiver.class);

        try {
            preferencesManager = new PreferencesManager(res.openRawResource(R.raw.settings), res.openRawResource(R.raw.alias), tuiFolder);
        } catch (IOException e) {
            this.startActivity(new Intent(this, LauncherActivity.class));
            this.finish();
        }

        boolean showNotification = Boolean.parseBoolean(preferencesManager.getValue(PreferencesManager.NOTIFICATION));
        if (showNotification) {
            Intent service = new Intent(this, KeeperService.class);
            startService(service);
        }

        boolean useSystemWP = Boolean.parseBoolean(preferencesManager.getValue(PreferencesManager.USE_SYSTEMWP));
        if (useSystemWP)
            setTheme(R.style.SystemWallpaperStyle);

        hideStatusBar = Boolean.parseBoolean(preferencesManager.getValue(PreferencesManager.FULLSCREEN));

        openKeyboardOnStart = Boolean.parseBoolean(preferencesManager.getValue(PreferencesManager.OPEN_KEYBOARD));
        if (!openKeyboardOnStart) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        setContentView(R.layout.main_view);

        ViewGroup mainView = (ViewGroup) findViewById(R.id.mainview);
        main = new MainManager(this, in, out, preferencesManager, policy, component, clearer);
        ui = new UIManager(main.getInfo(), this, mainView, ex, policy, component, preferencesManager);

        in.in(Tuils.EMPTYSTRING);

        System.gc();
    }

    private File getFolder() {
        final File tuiFolder = Tuils.getTuiFolder();

        while (true) {
            if (tuiFolder.isDirectory() || tuiFolder.mkdir()) {
                break;
            }

            try {
                Thread.sleep(FILEUPDATE_DELAY);
            } catch (InterruptedException e) {}
        }

        return tuiFolder;
    }

    private void hideStatusBar() {
        if (!hideStatusBar)
            return;

        if (Build.VERSION.SDK_INT < 16)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onStart() {
        super.onStart();

        overridePendingTransition(0,0);

        if (ui != null && openKeyboardOnStart) {
            ui.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        hideStatusBar();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ui != null && main != null) {
            ui.pause();
            main.dispose();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(main != null) {
            main.destroy();
        }
    }

    @Override
    public void onBackPressed() {
        if (main != null) {
            main.onBackPressed();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_BACK)
            return super.onKeyLongPress(keyCode, event);

        if (main != null)
            main.onLongBack();
        return true;
    }

    @Override
    public void reload() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    reloadOver11();
                } else {
                    finish();
                    startActivity(starterIntent);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void reloadOver11() {
        recreate();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (ui != null) {
            ui.focusTerminal();
        }
        hideStatusBar();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case COMMAND_REQUEST_PERMISSION:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        ExecInfo info = main.getInfo();
                        main.onCommand(info.calledCommand);
                    } else {
                        ui.setOutput(getString(R.string.output_nopermissions));
                    }
                    break;
                case STARTING_PERMISSION:
                    int count = 0;
                    while(count < permissions.length && count < grantResults.length) {
                        if( (permissions[count].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) || permissions[count].equals(Manifest.permission.READ_EXTERNAL_STORAGE))
                                && grantResults[count] == PackageManager.PERMISSION_DENIED) {
                            Toast.makeText(this, R.string.permissions_toast, Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        count++;
                    }
                    finishOnCreate();
                    break;
                case COMMAND_SUGGESTION_REQUEST_PERMISSION:
                    if (grantResults.length == 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        ui.setOutput(getString(R.string.output_nopermissions));
                    }
                    break;
            }
        } catch (Exception e) {}
    }

}
