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

import java.util.Scanner;

/**
 * Linux上的PID搜尋類別。
 *
 * @see PIDFinder
 * @author Magic Len
 */
public class LinuxPIDFinder extends PIDFinder {

    // -----類別變數-----
    private static LinuxPIDFinder pidFinder;

    // -----類別方法-----
    /**
     * 取得物件實體。
     *
     * @return 傳回物件實體
     */
    public static LinuxPIDFinder getInstance() {
        if (pidFinder == null) {
            pidFinder = new LinuxPIDFinder();
        }
        return pidFinder;
    }

    // -----抽象方法-----
    /**
     * 分解每行文字訊息的內容。
     *
     * @param strict 傳入是否要判斷大小寫
     * @param message 傳入文字訊息
     * @return 傳回PID和COMMAND
     */
    @Override
    protected String[] split(final boolean strict, final String message) {
        final Scanner sc = new Scanner(message);
        final String PID = sc.next();
        final String TTY = sc.next();
        final String STAT = sc.next();
        final String TIME = sc.next();
        final String COMMAND = sc.nextLine().trim();
        return new String[]{PID, COMMAND};
    }

    /**
     * 取得行程表的指令。
     *
     * @return 傳回行程表的指令
     */
    @Override
    protected String getProcessTableCommand() {
        return "/bin/ps ax";
    }

    // -----建構子-----
    /**
     * 私有的建構子，將無法被實體化。
     */
    private LinuxPIDFinder() {

    }

}
