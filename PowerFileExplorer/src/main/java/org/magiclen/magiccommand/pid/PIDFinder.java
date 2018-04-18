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
package org.magiclen.magiccommand.pid;

import java.util.ArrayList;
import org.magiclen.magiccommand.Command;
import org.magiclen.magiccommand.CommandListener;

/**
 * PID搜尋類別。
 *
 * @see Command
 * @author Magic Len
 */
public abstract class PIDFinder {

    // -----抽象方法-----
    /**
     * 分解每行文字訊息的內容。
     *
     * @param strict 傳入是否要判斷大小寫
     * @param message 傳入文字訊息
     * @return 傳回PID和COMMAND
     */
    protected abstract String[] split(final boolean strict, final String message);

    /**
     * 取得行程表的指令。
     *
     * @return 傳回行程表的指令
     */
    protected abstract String getProcessTableCommand();

    // -----物件方法-----
    /**
     * 搜尋行程(process)的ID(Process ID)。
     *
     * @param commandKeywords 傳入要搜尋的指令關鍵字
     * @return 傳回所有搜尋到的行程ID
     */
    public int[] findPID(final String... commandKeywords) {
        return findPID(true, commandKeywords);
    }

    /**
     * 搜尋行程(process)的ID(Process ID)。
     *
     * @param strict 傳入是否要判斷大小寫
     * @param commandKeywords 傳入要搜尋的指令關鍵字
     *
     * @return 傳回所有搜尋到的行程ID
     */
    public int[] findPID(final boolean strict, final String... commandKeywords) {
        if (commandKeywords == null) {
            return null;
        }
        try {
            final Command command = new Command(getProcessTableCommand());
            final int[] lineCounter = new int[]{0};
            final int[][] result = new int[1][];
            final ArrayList<Integer> pids = new ArrayList<>();
            command.setCommandListener(new CommandListener() {
                @Override
                public void commandStart(final String id) {
                }

                @Override
                public void commandRunning(final String id, final String message, final boolean isError) {
                    if (!isError) {
                        if (lineCounter[0] > 0) {
                            final String[] split = split(strict, message);
                            final String PID = split[0];
                            final String COMMAND = split[1];
                            boolean found = true;
                            for (final String commandKeyword : commandKeywords) {
                                if (commandKeyword == null) {
                                    continue;
                                }
                                found = strict ? COMMAND.contains(commandKeyword) : COMMAND.toLowerCase().contains(commandKeyword.toLowerCase());
                                if (!found) {
                                    break;
                                }
                            }
                            if (found) {
                                pids.add(Integer.parseInt(PID));
                            }
                        }
                        ++lineCounter[0];
                    }
                }

                @Override
                public void commandException(final String id, final Exception exception) {
                }

                @Override
                public void commandEnd(final String id, final int returnValue) {
                    if (returnValue == 0) {
                        final int pidCount = pids.size();
                        result[0] = new int[pidCount];
                        for (int i = 0; i < pidCount; ++i) {
                            result[0][i] = pids.get(i);
                        }
                    }
                }
            });
            command.run();
            return result[0];
        } catch (final Exception ex) {
            return null;
        }
    }

    // -----建構子-----
    /**
     * 私有的建構子，將無法被實體化。
     */
    protected PIDFinder() {

    }
}
