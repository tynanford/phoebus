/*******************************************************************************
 * Copyright (c) 2019 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.apputil.formula.node;

import static org.csstudio.apputil.formula.Formula.logger;

import java.util.logging.Level;

import org.csstudio.apputil.formula.Node;
import org.csstudio.apputil.formula.spi.FormulaFunction;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListNumber;

/** Node for evaluating an SPI-provided function
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SPIFuncNode implements Node
{
    final private FormulaFunction function;
    final private Node args[];

    /** Construct node for SPI function.
     *
     *  @param function {@link FormulaFunction}
     *  @param args Argument nodes
     */
    public SPIFuncNode(final FormulaFunction function, final Node[] args)
    {
        this.function = function;
        this.args = args;
        // Should be called with correct number
        if (args.length != function.getArgumentCount())
            throw new IllegalStateException("Wrong number of arguments");
    }

    @Override
    public ListNumber eval()
    {
        // Evaluate all arguments, get common element count
        final ListNumber[] v = new ListNumber[args.length];
        for (int i = 0; i < args.length; i++)
            v[i] = args[i].eval();
        try
        {
            return function.eval(v);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Formula math function error for " + this, ex);
        }
        return ArrayDouble.of(Double.NaN);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasSubnode(final Node node)
    {
        for (Node arg : args)
            if (arg == node  ||  arg.hasSubnode(node))
                return true;
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasSubnode(final String name)
    {
        for (Node arg : args)
            if (arg.hasSubnode(name))
                return true;
        return false;
    }

    @Override
    public String toString()
    {
        final StringBuffer b = new StringBuffer(function.getName());
        b.append("(");
        for (int i = 0; i < args.length; i++)
        {
            if (i>0)
                b.append(", ");
            b.append(args[i].toString());
        }
        b.append(")");
        return b.toString();
    }
}
