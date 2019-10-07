package org.csstudio.apputil.formula.demo;

import org.csstudio.apputil.formula.spi.FormulaFunction;
import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ListNumber;

/** Example for SPI-provided formula function
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Factorial implements FormulaFunction
{
    @Override
    public String getName()
    {
        return "fac";
    }

    @Override
    public int getArgumentCount()
    {
        return 1;
    }

    @Override
    public ListNumber eval(final ListNumber... args) throws Exception
    {
        if (args[0].size() != 1)
            throw new Exception("fac(n) expects scalar, got " + args[0]);
        final int n = args[0].getInt(0);
        final int result = fac(n);
        return ArrayInteger.of(result);
    }

    private static int fac(final int n)
    {
        if (n <= 1)
            return 1;
        return n * fac(n-1);
    }
}
