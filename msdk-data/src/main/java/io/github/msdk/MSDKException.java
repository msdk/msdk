/* 
 * (C) Copyright 2015 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk;

public class MSDKException extends Exception {

    private static final long serialVersionUID = 1L;

    public MSDKException(String msg) {
        super(msg);
    }

    public MSDKException(Throwable exception) {
        super(exception);
    }

}
