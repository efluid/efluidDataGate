package fr.uem.efluid.utils;

import com.github.rjeschke.txtmark.Processor;

/**
 * Common rendering utils for various content types. Adapted for rendering in html ui
 *
 * @author elecomte
 * @version 1
 * @since v3.1.11
 */
public class DisplayContentUtils {

    public static String renderDefault(byte[] raw) {
        return FormatUtils.toString(raw);
    }

    public static String renderSql(byte[] raw) {
        return renderDefault(raw)
                .replaceAll("--(.*)", "<span class=\"sql-comment\">--$1</span>")
                .replaceAll("\n", "<br/>");
    }

    public static String renderMarkdown(byte[] raw) {
        return Processor.process(renderDefault(raw));
    }

    public static String renderTxt(byte[] raw) {
        return renderDefault(raw).replaceAll("\n", "<br/>");
    }

    public static String renderJson(byte[] raw) {
        try {
            // Pretty printer
            return SharedOutputInputUtils.commonMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(SharedOutputInputUtils.commonMapper().readTree(raw))
                    .replaceAll("\n", "<br/>")
                    .replaceAll(" ", "&nbsp;") + "<br/>";
        } catch (Exception e) {
            throw new ApplicationException(ErrorType.JSON_READ_ERROR, "cannot render json content");
        }
    }

    public static boolean isJsonCompliant(byte[] raw) {

        // Try only on compliant start / end chars
        if (raw[0] == '[' || raw[0] == '{' && raw[raw.length - 1] == ']' || raw[raw.length - 1] == '}') {

            try {
                SharedOutputInputUtils.commonMapper().readTree(raw);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }
}
