/*******************************************************************************
 * Copyright (c) 2019 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.apputil.formula.spi;

import org.epics.util.array.ListNumber;

/** SPI for contributing a function to the formula evaluator
 *  @author Kay Kasemir
 */
public interface FormulaFunction
{
    /** @return Name of the function as used in formula */
    public String getName();

    /** @return Number of arguments */
    public int getArgumentCount();

    /** Evaluate the formula, i.e. compute its value.
     *  @param args Arguments
     *  @return Formula value.
     *  @throws Exception on error
     */
    public ListNumber eval(ListNumber... args) throws Exception;
}
