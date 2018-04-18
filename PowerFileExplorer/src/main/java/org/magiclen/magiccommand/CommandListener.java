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

/**
 * 命令監聽者。
 *
 * @see Command
 * @author Magic Len
 */
public interface CommandListener {

    /**
     * 命令工作開始。
     *
     * @param id 命令ID
     */
    public void commandStart(final String id);

    /**
     * 命令工作中。
     *
     * @param id 命令ID
     * @param message 訊息
     * @param isError 是否為錯誤訊息
     */
    public void commandRunning(final String id, final String message, final boolean isError);

    /**
     * 命令發生例外。
     *
     * @param id 命令ID
     * @param exception 例外物件
     */
    public void commandException(final String id, final Exception exception);

    /**
     * 命令工作結束。
     *
     * @param id 命令ID
     * @param returnValue 工作結束回傳值
     */
    public void commandEnd(final String id, final int returnValue);
}
