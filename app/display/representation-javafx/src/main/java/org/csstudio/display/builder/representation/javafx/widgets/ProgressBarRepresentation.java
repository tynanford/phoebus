/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.ProgressBarWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.phoebus.vtype.Display;
import org.phoebus.vtype.VType;
import org.phoebus.vtype.ValueUtil;

import javafx.scene.control.ProgressBar;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class ProgressBarRepresentation extends RegionBaseRepresentation<ProgressBar, ProgressBarWidget>
{
    private final DirtyFlag dirty_look = new DirtyFlag();
    private final DirtyFlag dirty_value = new DirtyFlag();
    private volatile double percentage = 0.0;

    @Override
    public ProgressBar createJFXNode() throws Exception
    {
        final ProgressBar bar = new ProgressBar();
        return bar;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propFillColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::lookChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::lookChanged);
        model_widget.propLimitsFromPV().addUntypedPropertyListener(this::valueChanged);
        model_widget.propMinimum().addUntypedPropertyListener(this::valueChanged);
        model_widget.propMaximum().addUntypedPropertyListener(this::valueChanged);
        model_widget.runtimePropValue().addUntypedPropertyListener(this::valueChanged);
        model_widget.propHorizontal().addUntypedPropertyListener(this::lookChanged);
        valueChanged(null, null, null);
    }

    private void lookChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_look.mark();
        toolkit.scheduleUpdate(this);
    }

    private void valueChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        final VType vtype = model_widget.runtimePropValue().getValue();

        final boolean limits_from_pv = model_widget.propLimitsFromPV().getValue();
        double min_val = model_widget.propMinimum().getValue();
        double max_val = model_widget.propMaximum().getValue();
        if (limits_from_pv)
        {
            // Try display range from PV
            final Display display_info = ValueUtil.displayOf(vtype);
            if (display_info != null)
            {
                min_val = display_info.getLowerDisplayLimit();
                max_val = display_info.getUpperDisplayLimit();
            }
        }
        // Fall back to 0..100 range
        if (min_val >= max_val)
        {
            min_val = 0.0;
            max_val = 100.0;
        }

        // Determine percentage of value within the min..max range
        final double value = VTypeUtil.getValueNumber(vtype).doubleValue();
        final double percentage = (value - min_val) / (max_val - min_val);
        // Limit to 0.0 .. 1.0
        if (percentage < 0.0)
            this.percentage = 0.0;
        else if (percentage > 1.0)
            this.percentage = 1.0;
        else
            this.percentage = percentage;
        dirty_value.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_look.checkAndClear())
        {
            boolean horizontal = model_widget.propHorizontal().getValue();
            double width = model_widget.propWidth().getValue();
            double height = model_widget.propHeight().getValue();
            if (!horizontal)
            {
                jfx_node.setPrefSize(height, width);
                jfx_node.getTransforms().setAll(
                        new Translate(0, height),
                        new Rotate(-90, 0, 0));
            }
            else
            {
                jfx_node.setPrefSize(width, height);
                jfx_node.getTransforms().clear();
            }
            // Could clear style and use setBackground(),
            // but result is very plain.
            // Tweaking the color used by CSS keeps overall style.
            // See also http://stackoverflow.com/questions/13467259/javafx-how-to-change-progressbar-color-dynamically
            jfx_node.setStyle("-fx-accent: " + JFXUtil.webRGB(model_widget.propFillColor().getValue()));
        }
        if (dirty_value.checkAndClear())
            jfx_node.setProgress(percentage);
    }
}
