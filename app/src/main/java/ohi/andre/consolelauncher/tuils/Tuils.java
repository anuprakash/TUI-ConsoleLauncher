package ohi.andre.consolelauncher.tuils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import dalvik.system.DexFile;
import ohi.andre.consolelauncher.BuildConfig;
import ohi.andre.consolelauncher.managers.MusicManager;
import ohi.andre.consolelauncher.tuils.tutorial.TutorialIndexActivity;

public class Tuils {

    public static final String SPACE = " ";
    public static final String DOUBLE_SPACE = "  ";
    public static final String NEWLINE = "\n";
    public static final String TRIBLE_SPACE = "   ";
    public static final String DOT = ".";
    public static final String EMPTYSTRING = "";
    private static final String TUI_FOLDER = "t-ui";

    public static boolean arrayContains(int[] array, int value) {
        for(int i : array) {
            if(i == value) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsExtension(String[] array, String value) {
        value = value.toLowerCase().trim();
        for(String s : array) {
            if(value.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    public static List<File> getSongsInFolder(File folder) {
        List<File> songs = new ArrayList<>();

        File[] files = folder.listFiles();
        if(files == null || files.length == 0) {
            return null;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                List<File> s = getSongsInFolder(file);
                if(s != null) {
                    songs.addAll(s);
                }
            }
            else if (containsExtension(MusicManager.MUSIC_EXTENSIONS, file.getName())) {
                songs.add(file);
            }
        }

        return songs;
    }

    public static void showTutorial(Context context) {
        Intent intent = new Intent(context, TutorialIndexActivity.class);
        context.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void openSettingsPage(Context c, String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        c.startActivity(intent);
    }

    public static void requestAdmin(Activity a, ComponentName component, String label) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, label);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        a.startActivityForResult(intent, 0);
    }

    public static String removeExtension(String s) {
        return s.substring(0, s.lastIndexOf("."));
    }

    public static String ramDetails(ActivityManager mgr, MemoryInfo info) {
        mgr.getMemoryInfo(info);
        long availableMegs = info.availMem / 1048576L;

        return availableMegs + " MB";
    }

    public static List<String> getClassesInPackage(String packageName, Context c)
            throws IOException {
        List<String> classes = new ArrayList<>();
        String packageCodePath = c.getPackageCodePath();
        DexFile df = new DexFile(packageCodePath);
        for (Enumeration<String> iter = df.entries(); iter.hasMoreElements(); ) {
            String className = iter.nextElement();
            if (className.contains(packageName) && !className.contains("$"))
                classes.add(className.substring(className.lastIndexOf(".") + 1, className.length()));
        }

        return classes;
    }

    public static int findPrefix(List<String> list, String prefix) {
        for (int count = 0; count < list.size(); count++)
            if (list.get(count).startsWith(prefix))
                return count;
        return -1;
    }

    public static boolean verifyRoot() {
        Process p;
        try {
            p = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("echo \"root?\" >/system/sd/temporary.txt\n");

            os.writeBytes("exit\n");
            os.flush();
            try {
                p.waitFor();
                return p.exitValue() != 255;
            } catch (InterruptedException e) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    public static void insertHeaders(List<String> s, boolean newLine) {
        char current = 0;
        for (int count = 0; count < s.size(); count++) {
            char c = 0;

            String st = s.get(count);
            for (int count2 = 0; count2 < st.length(); count2++) {
                c = st.charAt(count2);
                if (c != ' ')
                    break;
            }

            if (current != c) {
                s.add(count, (newLine ? NEWLINE : EMPTYSTRING) + Character.toString(c).toUpperCase() + (newLine ? NEWLINE : EMPTYSTRING));
                current = c;
            }
        }
    }

    public static void addPrefix(List<String> list, String prefix) {
        for (int count = 0; count < list.size(); count++)
            list.set(count, prefix.concat(list.get(count)));
    }

    public static void addSeparator(List<String> list, String separator) {
        for (int count = 0; count < list.size(); count++)
            list.set(count, list.get(count).concat(separator));
    }

    public static String toPlanString(String[] strings, String separator) {
        if(strings == null) {
            return Tuils.EMPTYSTRING;
        }

        String output = "";
        for (int count = 0; count < strings.length; count++) {
            output = output.concat(strings[count]);
            if (count < strings.length - 1)
                output = output.concat(separator);
        }
        return output;
    }

    public static String toPlanString(String[] strings) {
        if (strings != null) {
            return Tuils.toPlanString(strings, Tuils.NEWLINE);
        }
        return Tuils.EMPTYSTRING;
    }

    public static String toPlanString(List<String> strings, String separator) {
        if(strings != null) {
            String[] object = new String[strings.size()];
            return Tuils.toPlanString(strings.toArray(object), separator);
        }
        return Tuils.EMPTYSTRING;
    }

    public static String filesToPlanString(List<File> files, String separator) {
        StringBuilder builder = new StringBuilder();
        int limit = files.size() - 1;
        for (int count = 0; count < files.size(); count++) {
            builder.append(files.get(count).getName());
            if (count < limit) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    public static String toPlanString(List<String> strings) {
        return Tuils.toPlanString(strings, NEWLINE);
    }

    public static String toPlanString(Object[] objs, String separator) {
        if(objs == null) {
            return Tuils.EMPTYSTRING;
        }

        StringBuilder output = new StringBuilder();
        for(int count = 0; count < objs.length; count++) {
            output.append(objs[count]);
            if(count < objs.length - 1) {
                output.append(separator);
            }
        }
        return output.toString();
    }

//    public static CharSequence toPlanSequence(List<CharSequence> sequences, CharSequence separator) {
//        if(sequences != null) {
//            return toPlanSequence(sequences.toArray(new CharSequence[sequences.size()]), separator);
//        }
//        return null;
//    }
//
//    public static CharSequence toPlanSequence(CharSequence[] sequences, CharSequence separator) {
//        if(sequences == null) {
//            return null;
//        }
//
//        if (sequences.length == 0)
//            return null;
//
//        CharSequence sequence = null;
//        int count;
//        for (count = 0; (sequence = sequences[count]) == null; count++) {
//        }
//
//        CharSequence output = sequences[count];
//        do {
//            count++;
//            CharSequence current = sequences[count];
//            if (current == null)
//                continue;
//
//            output = TextUtils.concat(output, current);
//            if (count < sequences.length - 1 && !current.toString().contains(separator))
//                output = TextUtils.concat(output, separator);
//        } while (count + 1 < sequences.length);
//        return output;
//    }

    public static String removeUnncesarySpaces(String string) {
        while (string.contains(DOUBLE_SPACE)) {
            string = string.replace(DOUBLE_SPACE, SPACE);
        }
        return string;
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static boolean isAlpha(String s) {
        if(s == null) {
            return false;
        }
        char[] chars = s.toCharArray();

        for (char c : chars)
            if (!Character.isLetter(c))
                return false;

        return true;
    }

    public static boolean isNumber(String s) {
        if(s == null) {
            return false;
        }
        char[] chars = s.toCharArray();

        for (char c : chars) {
            if (Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    public static CharSequence trimWhitespaces(CharSequence source) {

        if(source == null) {
            return Tuils.EMPTYSTRING;
        }

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {}

        return source.subSequence(0, i+1);
    }

    public static String getSDK() {
        return "android-sdk " + Build.VERSION.SDK_INT;
    }

    public static String getUsername(Context context) {
        try {
            Pattern email = Patterns.EMAIL_ADDRESS;
            Account[] accs = AccountManager.get(context).getAccounts();
            for (Account a : accs)
                if (email.matcher(a.name).matches())
                    return a.name;
        } catch (SecurityException e) {
            return null;
        }
        return null;
    }

    public static Intent openFile(Context context, File url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        Uri uri = FileProvider.getUriForFile(context, "ohi.andre.consolelauncher.provider", url);
        intent.setDataAndType(uri, context.getContentResolver().getType(uri));

        return intent;
    }

    public static Intent shareFile(Context c, File url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        Uri uri = FileProvider.getUriForFile(c, "ohi.andre.consolelauncher.provider", url);
        intent.setDataAndType(uri, c.getContentResolver().getType(uri));

        return intent;
    }

    public static String getInternalDirectoryPath() {
        File f = Environment.getExternalStorageDirectory();
        if(f != null) {
            return f.getAbsolutePath();
        }
        return null;
    }

    public static File getTuiFolder() {
        String internalDir = Tuils.getInternalDirectoryPath();
        if(internalDir == null) {
            return null;
        }
        return new File(internalDir, TUI_FOLDER);
    }

    public static List<File> getMediastoreSongs(Context activity) {
        ContentResolver cr = activity.getContentResolver();

        List<File> paths = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        int count = 0;

        if(cur != null) {
            count = cur.getCount();
            if(count > 0) {
                while(cur.moveToNext()) {
                    String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    paths.add(new File(data));
                }

            }

            cur.close();
        }

        return paths;
    }

}
