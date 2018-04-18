/*
 *
 * Copyright 2015-2017 magiclen.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.magiclen.magiccommand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

/**
 * 命令類別，用來呼叫系統的CLI指令。
 *
 * @see CommandListener
 * @author Magic Len
 */
public class Command {

    //-----類別列舉-----
    /**
     * 能傳給命令行程的信號。
     */
    public static enum Signal {
        SIGHUP(1), SIGINT(2), SIGKILL(9), SIGTERM(15), SIGSTOP(17);

        public final int value;

        Signal(final int value) {
            this.value = value;
        }
    }

    //-----類別常數-----
    //-----類別方法-----
    /**
     * 傳送信號給指令行程。
     *
     * @param signal 傳入要傳送給指定行程的信號
     * @param pids 傳入行程ID
     * @return 傳回信號是否全部傳送成功
     */
    public static boolean sendSignal(final Signal signal, final int... pids) {
        if (signal == null || pids == null) {
            return false;
        }
        boolean success = true;

        for (final int pid : pids) {
            try {
                final Process killProcess = Runtime.getRuntime().exec("kill -".concat(String.valueOf(signal.value)).concat(" ").concat(String.valueOf(pid)));
                killProcess.waitFor();
                final int exit = killProcess.exitValue();
                success = success && exit == 0;
            } catch (final Exception ex) {
            }
        }
        return success;
    }

    /**
     * 將命令列字串切割成字串陣列。
     *
     * @param commandLine 傳入命令列字串
     * @return 傳回切割後的命令字串
     */
    private static String[] splitCommandLineIntoArray(String commandLine) {
        if (commandLine == null) {
            return new String[0];
        }
        commandLine = commandLine.trim() + " ";
        final ArrayList<String> al = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();
        final char[] commandLineChars = commandLine.toCharArray();
        boolean appendMode = false, quoteMode = false;
        char quoteChar = ' ';
        for (int i = 0; i < commandLineChars.length; ++i) {
            final char c = commandLineChars[i];
            switch (c) {
                case ' ':
                case '\t':
                    if (appendMode) {
                        if (quoteMode) {
                            sb.append(c);
                        } else {
                            appendMode = false;
                            al.add(sb.toString());
                            sb.delete(0, sb.length());
                        }
                    }
                    break;
                case '\"':
                case '\'':
                    if (appendMode) {
                        if (quoteMode) {
                            if (quoteChar == c) {
                                appendMode = false;
                                quoteMode = false;
                                al.add(sb.toString());
                                sb.delete(0, sb.length());
                            } else {
                                sb.append(c);
                            }
                        } else {
                            quoteMode = true;
                            quoteChar = c;
                            al.add(sb.toString());
                            sb.delete(0, sb.length());
                        }
                    } else {
                        appendMode = true;
                        quoteMode = true;
                        quoteChar = c;
                    }
                    break;
                default:
                    sb.append(c);
                    if (!appendMode) {
                        appendMode = true;
                    }
                    break;
            }
        }
        if (quoteMode) {
            al.add(sb.toString().trim());
        }
        final String[] outputArray = new String[al.size()];
        al.toArray(outputArray);
        return outputArray;
    }

    //-----物件類別-----
    /**
     * 用以取得Reader中的資料。
     */
    private class Gobbler extends Thread {

        private final BufferedReader reader;
        private final String id;
        private final boolean isError;

        Gobbler(final String id, final BufferedReader reader, final boolean isError) {
            this.id = id;
            this.reader = reader;
            this.isError = isError;
        }

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = reader.readLine()) != null) {
                    if (listener != null) {
                        listener.commandRunning(id, msg, isError);
                    }
                }
            } catch (final Exception ex) {
                if (listener != null) {
                    listener.commandException(id, ex);
                }
            }
        }
    }

    //-----物件常數-----
    private final ArrayList<String> cmdArgs = new ArrayList<>(); //儲存命令與參數
    private final HashMap<String, Process> processes = new HashMap<>(); //儲存正在執行的Process

    //-----物件變數-----
    private long idNumberCounter = 0; //ID計數器
    private CommandListener listener; //命令監聽者
    private String command; //命令字串
    private File defaultDirectory; //預設工作目錄
    private Charset charset; //字元集

    //-----建構子-----
    /**
     * 建構子，直接傳入命令字串，使用UTF-8字元集。
     *
     * @param cmd 傳入命令字串
     * @throws RuntimeException 如果建構命令物件的過程發生問題，將拋出此例外
     */
    public Command(final String cmd) throws RuntimeException {
        this(StandardCharsets.UTF_8, cmd);
    }

    /**
     * 建構子，直接傳入命令字串。
     *
     * @param charset 傳入字元集
     * @param cmd 傳入命令字串
     * @throws RuntimeException 如果建構命令物件的過程發生問題，將拋出此例外
     */
    public Command(final Charset charset, final String cmd) throws RuntimeException {
        if (cmd == null) {
            throw new RuntimeException("Command is empty!");
        }
        initial(charset, splitCommandLineIntoArray(cmd));
    }

    /**
     * 建構子，個別傳入命令參數，使用UTF-8字元集。
     *
     * @param args 個別傳入命令參數
     * @throws RuntimeException 如果建構命令物件的過程發生問題，將拋出此例外
     */
    public Command(final String... args) throws RuntimeException {
        initial(StandardCharsets.UTF_8, args);
    }

    public Command(final List<String> args) throws RuntimeException {
		final String[] s = new String[args.size()];
		args.toArray(s);
		initial(StandardCharsets.UTF_8, s);
    }

    /**
     * 建構子，和個別傳入命令參數和字元集。
     *
     * @param charset 傳入字元集
     * @param args 個別傳入命令參數
     * @throws RuntimeException 如果建構命令物件的過程發生問題，將拋出此例外
     */
    public Command(final Charset charset, final String... args) throws RuntimeException {
        initial(charset, args);
    }

    //-----物件方法-----
    /**
     * 初始化。
     *
     * @param charset 傳入字元集
     * @param args 個別傳入命令參數
     * @throws RuntimeException 如果初始化命令物件的過程發生問題，將拋出此例外
     */
    private void initial(final Charset charset, final String... args) throws RuntimeException {
        if (charset == null) {
            throw new RuntimeException("Charset is empty!");
        }
        this.charset = charset;

        if (args == null || args.length == 0) {
            throw new RuntimeException("Command is empty!");
        }

        for (final String arg : args) {
            if (arg != null) {
                final String trimedArg = arg.trim();
                if (trimedArg.length() > 0) {
                    this.cmdArgs.add(trimedArg);
                }
            }
        }

        if (this.cmdArgs.isEmpty()) {
            throw new RuntimeException("Command is empty!");
        }

        makeCommand();
    }

    /**
     * 設定命令監聽者。
     *
     * @param listener 傳入命令監聽者物件
     */
    public void setCommandListener(final CommandListener listener) {
        this.listener = listener;
    }

    /**
     * 取得命令監聽者。
     *
     * @return 傳回命令監聽者物件
     */
    public CommandListener getCommandListener() {
        return listener;
    }

    /**
     * 設定預設工作目錄。
     *
     * @param defaultDirectory 傳入預設工作目錄
     */
    public void setDefaultDirectory(final File defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
    }

    /**
     * 取得預設工作目錄。
     *
     * @return 傳回預設工作目錄
     */
    public File getDefaultDirectory() {
        return defaultDirectory;
    }

    /**
     * 取得字元集。
     *
     * @return 傳回字元集
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * 以非同步模式執行命令，命令ID和工作目錄將使用預設值。
     */
    public void runAsync() {
        runAsync(null, null);
    }

    /**
     * 以非同步模式執行命令，工作目錄將使用預設值。
     *
     * @param id 傳入命令的ID字串
     */
    public void runAsync(final String id) {
        runAsync(id, null);
    }

    /**
     * 以非同步模式執行命令，命令ID將使用預設值。
     *
     * @param directory 傳入工作目錄物件
     */
    public void runAsync(final File directory) {
        runAsync(null, directory);
    }

    /**
     * 以非同步模式執行命令。
     *
     * @param id 傳入命令的ID字串
     * @param directory 傳入工作目錄物件
     */
    public void runAsync(final String id, final File directory) {
        new Thread() {
            @Override
            public void run() {
                Command.this.run(id, directory);
            }
        }.start();
    }

    /**
     * 以同步模式執行命令，命令ID和工作目錄將使用預設值。
     */
    public void run() throws RuntimeException {
        run(null, null);
    }

    /**
     * 以同步模式執行命令，工作目錄將使用預設值。
     *
     * @param id 傳入命令的ID字串
     */
    public void run(final String id) throws RuntimeException {
        run(id, null);
    }

    /**
     * 以同步模式執行命令，命令ID將使用預設值。
     *
     * @param directory 傳入工作目錄物件
     */
    public void run(final File directory) throws RuntimeException {
        run(null, directory);
    }

    /**
     * 以同步模式執行命令。
     *
     * @param id 傳入命令的ID字串
     * @param directory 傳入工作目錄物件
     */
    public void run(String id, final File directory) throws RuntimeException {
        final ProcessBuilder pb = new ProcessBuilder(cmdArgs);
        if (directory != null) {
            pb.directory(directory);
        } else if (defaultDirectory != null) {
            pb.directory(defaultDirectory);
        }

        Process process = null;
        synchronized (this) {
            int counter = 0;
            if (id == null) {
                id = String.valueOf(idNumberCounter);
                ++counter;
                while (processes.containsKey(id)) {
                    id = String.valueOf(idNumberCounter);
                    ++counter;
                }
            } else if (processes.containsKey(id)) {
                throw new RuntimeException(String.format("ID \"%s\" is using.", id));
            }
            try {
                process = pb.start();
            } catch (final IOException ex) {
                throw new RuntimeException(ex.getMessage());
            }
            idNumberCounter += counter;
            processes.put(id, process);
        }
        if (listener != null) {
            listener.commandStart(id);
        }

        final BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
        final BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream(), charset));

        final Gobbler inputGobbler = new Gobbler(id, stdInput, false);
        final Gobbler errorGobbler = new Gobbler(id, stdError, true);
        inputGobbler.start();
        errorGobbler.start();
        try {
            final int rtnValue = process.waitFor();
            try {
                inputGobbler.join();
            } catch (final Exception ex) {

            }
            try {
                errorGobbler.join();
            } catch (final Exception ex) {

            }
            if (listener != null) {
                listener.commandEnd(id, rtnValue);
            }
        } catch (final Exception ex) {
            if (listener != null) {
                listener.commandException(id, ex);
            }
        }
        processes.remove(id);
    }

    /**
     * 傳入字串至正在執行的命令行程中。
     *
     * @param id 傳入命令ID
     * @param inputString 傳入字串
     * @return 傳回字串是否傳入成功
     */
    public boolean inputStringToRunningProcess(final String id, final String inputString) {
        if (inputString == null || id == null || !processes.containsKey(id)) {
            return false;
        }
        try {
            final Process process = processes.get(id);
            final BufferedWriter stdOut = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), charset));
            stdOut.write(inputString);
            stdOut.flush();
            return true;
        } catch (final Exception ex) {
            return false;
        }
    }

    /**
     * 停止指定ID的命令行程。
     *
     * @param id 傳入命令ID
     * @return 傳回命令是否停止成功
     */
    public boolean stop(final String id) {
        if (id == null || !processes.containsKey(id)) {
            return false;
        }
        try {
            final Process process = processes.get(id);
            processes.remove(id);
            process.destroy();
            return true;
        } catch (final Exception ex) {
            return false;
        }
    }

    /**
     * 停止此命令物件的所有行程。
     *
     */
    public void stopAll() {
        final Set<String> keys = processes.keySet();
        for (final String key : keys) {
            try {
                stop(key);
            } catch (final Exception ex) {
            }
        }
    }

    /**
     * 取得執行中的Process ID。
     *
     * @return 傳回執行中的Process ID
     */
    public Set<String> getProcessIDSet() {
        return processes.keySet();
    }

    /**
     * 產生命令字串
     */
    private void makeCommand() {
        final StringBuilder sb = new StringBuilder();
        final int decArgsLength = cmdArgs.size() - 1;
        for (int i = 0; i < decArgsLength; ++i) {
            sb.append(cmdArgs.get(i)).append(" ");
        }
        sb.append(cmdArgs.get(decArgsLength));
        command = sb.toString();
    }

    /**
     * 取得命令字串。
     *
     * @return 傳回命令字串
     */
    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("--------------------").append("\n");
        sb.append(command).append("\n");
        final Set<String> keys = getProcessIDSet();
        if (keys.isEmpty()) {
            sb.append("No running process.").append("\n");
        } else {
            sb.append("Process ID List:").append("\n");
            for (final String key : keys) {
                sb.append(key).append("\n");
            }
        }
        sb.append("--------------------");
        return sb.toString();
    }

}
