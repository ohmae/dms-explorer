/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter.property;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.view.adapter.PropertyAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class DescriptionFormatter implements PropertyFormatter {
    private static final Pattern URL_PATTERN =
            Pattern.compile("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+");

    @NonNull
    @Override
    public CharSequence format(@NonNull final Context context, @NonNull final String string) {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        final String[] lines = string.split("\n");
        for (final String line : lines) {
            if (line.startsWith(PropertyAdapter.TITLE_PREFIX)) {
                appendTitle(context, builder, line);
                continue;
            }
            appendDescription(context, builder, line);
        }
        return builder;
    }

    private void appendTitle(@NonNull final Context context,
                             @NonNull final SpannableStringBuilder builder,
                             @NonNull final String line) {
        final int start = builder.length();
        builder.append(line.substring(2));
        final Object style = new TextAppearanceSpan(
                context, R.style.PropertyTitleTextAppearance);
        builder.setSpan(style, start, builder.length(), Spanned.SPAN_POINT_MARK);
        builder.append('\n');
    }

    private void appendDescription(@NonNull final Context context,
                                   @NonNull final SpannableStringBuilder builder,
                                   @NonNull final String line) {
        final int base = builder.length();
        builder.append(line);
        builder.append('\n');
        final Matcher matcher = URL_PATTERN.matcher(line);
        while (matcher.find()) {
            final int start = matcher.start();
            final int end = matcher.end();
            builder.setSpan(new LinkSpan(context, line.substring(start, end)),
                    start + base, end + base, Spanned.SPAN_POINT_MARK);
        }
    }
}
