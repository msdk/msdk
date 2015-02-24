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

import javax.annotation.Nullable;

/**
 * 
 *
 * @param <ResultType>
 */
public interface MSDKMethod<ResultType> {

    /**
     * Returns a number in the interval 0.0 to 1.0, representing the portion of
     * the algorithm that has completed.
     */
    double getFinishedPercentage();

    /**
     * Performs the algorithm.
     * 
     * @throws Exception
     *             On any error
     */
    @Nullable
    ResultType execute() throws MSDKException;

    /**
     * Returns the result of this algorithm, if it exists.
     */
    @Nullable
    ResultType getResult();

    /**
     * Cancel a running algorithm. This method can be called from any thread.
     */
    void cancel();

}
