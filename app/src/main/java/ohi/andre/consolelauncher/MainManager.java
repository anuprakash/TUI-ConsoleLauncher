package ohi.andre.consolelauncher;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ohi.andre.consolelauncher.commands.Command;
import ohi.andre.consolelauncher.commands.CommandGroup;
import ohi.andre.consolelauncher.commands.CommandTuils;
import ohi.andre.consolelauncher.commands.ExecInfo;
import ohi.andre.consolelauncher.managers.AliasManager;
import ohi.andre.consolelauncher.managers.AppsManager;
import ohi.andre.consolelauncher.managers.ContactManager;
import ohi.andre.consolelauncher.managers.MusicManager;
import ohi.andre.consolelauncher.managers.PreferencesManager;
import ohi.andre.consolelauncher.tuils.ShellUtils;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.CommandExecuter;
import ohi.andre.consolelauncher.tuils.interfaces.Inputable;
import ohi.andre.consolelauncher.tuils.interfaces.Outputable;

/*Copyright Francesco Andreuzzi

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

public class MainManager {

    private final String COMMANDS_PKG = "ohi.andre.consolelauncher.commands.raw";
    private final int LAST_COMMANDS_SIZE = 20;

    private CmdTrigger[] triggers = new CmdTrigger[]{
            new AliasTrigger(),
            new TuiCommandTrigger(),
            new AppTrigger(),
//            keep this as last trigger
            new SystemCommandTrigger()
    };
    private ExecInfo info;

    private Context mContext;

    private Inputable in;
    private Outputable out;

    private List<String> lastCommands;
    private int lastCommandIndex;

    protected MainManager(LauncherActivity c, Inputable i, Outputable o, PreferencesManager prefsMgr,
                          DevicePolicyManager devicePolicyManager, ComponentName componentName, Runnable clearer) {
        mContext = c;

        in = i;
        out = o;

        lastCommands = new ArrayList<>(LAST_COMMANDS_SIZE);

        CommandGroup group = new CommandGroup(mContext, COMMANDS_PKG);

        ContactManager cont = null;
        try {
            cont = new ContactManager(mContext);
        } catch (NullPointerException e) {
        }

        CommandExecuter executer = new CommandExecuter() {
            @Override
            public String exec(String input) {
                onCommand(input);
                return null;
            }
        };

        MusicManager music = new MusicManager(mContext, prefsMgr, out);

        AppsManager appsMgr = new AppsManager(c, Boolean.parseBoolean(prefsMgr.getValue(PreferencesManager.COMPARESTRING_APPS)), out);
        AliasManager aliasManager = new AliasManager(prefsMgr);

        info = new ExecInfo(mContext, prefsMgr, group, aliasManager, appsMgr, music, cont, devicePolicyManager, componentName,
                c, executer);
    }

    //    command manager
    public void onCommand(String input) {
        if (lastCommands.size() == LAST_COMMANDS_SIZE)
            lastCommands.remove(0);

        lastCommands.add(input);
        lastCommandIndex = lastCommands.size() - 1;

        input = input.trim();
        input = Tuils.removeUnncesarySpaces(input);

        for (CmdTrigger trigger : triggers) {
            boolean r;
            try {
                r = trigger.trigger(info, out, input);
            } catch (Exception e) {
                out.onOutput(Tuils.getStackTrace(e));
                return;
            }
            if (r) {
                return;
            } else {}
        }
    }

    //    back managers
    public void onBackPressed() {
        String s;
        if (lastCommands.size() > 0 && lastCommandIndex < lastCommands.size() && lastCommandIndex >= 0)
            s = lastCommands.get(lastCommandIndex--);
        else
            s = Tuils.EMPTYSTRING;

        in.in(s);
    }

    public void onLongBack() {
        in.in(Tuils.EMPTYSTRING);
    }

    //    dispose
    public void dispose() {
        info.dispose();
    }

    public void destroy() {
        info.destroy();
    }

    public ExecInfo getInfo() {
        return info;
    }

    interface CmdTrigger {
        boolean trigger(ExecInfo info, Outputable out, String input) throws Exception;
    }

    class AliasTrigger implements CmdTrigger {
        @Override
        public boolean trigger(ExecInfo info, Outputable out, String input) {
            String alias = info.aliasManager.getAlias(input);
            if (alias == null) {
                return false;
            }
            Log.e("andre", alias);

            info.executer.exec(alias);

            return true;
        }
    }

    //    this must be the last trigger
    class SystemCommandTrigger implements CmdTrigger {

        final int COMMAND_NOTFOUND = 127;

        @Override
        public boolean trigger(final ExecInfo info, final Outputable out, String input) throws Exception {

            if (CommandTuils.isSuRequest(input)) {
                boolean su = Tuils.verifyRoot();
                out.onOutput(info.res.getString(su ? R.string.su : R.string.nosu));
            } else {
                boolean su = false;
                if (CommandTuils.isSuCommand(input)) {
                    su = true;
                    input = input.substring(3);
                    if (input.startsWith(ShellUtils.COMMAND_SU_ADD + Tuils.SPACE))
                        input = input.substring(3);
                }

                final String cmd = input;
                final boolean useSU = su;

                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        ShellUtils.CommandResult result = ShellUtils.execCommand(cmd, useSU, info.currentDirectory.getAbsolutePath());
                        if (result.result == COMMAND_NOTFOUND) {
                            out.onOutput(mContext.getString(R.string.output_commandnotfound));
                        } else {
                            out.onOutput(result.toString());
                        }
                    }
                }.start();
            }

            return true;
        }
    }

    class AppTrigger implements CmdTrigger {

        @Override
        public boolean trigger(ExecInfo info, Outputable out, String input) {
            String packageName = info.appsManager.findPackage(input, AppsManager.SHOWN_APPS);
            if (packageName == null) {
                return false;
            }

            Intent intent = info.appsManager.getIntent(packageName);
            if (intent == null) {
                return false;
            }

            out.onOutput(info.res.getString(R.string.starting_app) + Tuils.SPACE + intent.getComponent().getClassName());

            mContext.startActivity(intent);

            return true;
        }
    }

    class TuiCommandTrigger implements CmdTrigger {

        @Override
        public boolean trigger(final ExecInfo info, final Outputable out, final String input) throws Exception {

            final boolean[] returnValue = new boolean[1];
            Thread t = new Thread() {
                @Override
                public void run() {
                    super.run();

                    info.calledCommand = input;

                    try {
                        Command command = CommandTuils.parse(input, info, false);

                        synchronized (returnValue) {
                            returnValue[0] = command != null;
                            returnValue.notify();
                        }

                        if (returnValue[0]) {
                            String output = command.exec(info);
                            if(output != null) {
                                out.onOutput(output);
                            }
                        }
                    } catch (Exception e) {
                        out.onOutput(e.toString());
                    }
                }
            };

            t.start();

            synchronized (returnValue) {
                returnValue.wait();
                return returnValue[0];
            }
        }
    }
}
