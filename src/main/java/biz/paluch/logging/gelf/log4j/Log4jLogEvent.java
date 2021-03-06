package biz.paluch.logging.gelf.log4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import biz.paluch.logging.gelf.*;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:37
 */
class Log4jLogEvent implements LogEvent {

    private LoggingEvent loggingEvent;

    public Log4jLogEvent(LoggingEvent loggingEvent) {
        this.loggingEvent = loggingEvent;
    }

    @Override
    public String getMessage() {
        return loggingEvent.getRenderedMessage();
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public Throwable getThrowable() {
        ThrowableInformation ti = loggingEvent.getThrowableInformation();
        if (ti != null) {
            return ti.getThrowable();
        }

        return null;
    }

    @Override
    public long getLogTimestamp() {
        return Log4jVersionChecker.getTimeStamp(loggingEvent);
    }

    @Override
    public String getSyslogLevel() {
        return "" + levelToSyslogLevel(loggingEvent.getLevel());
    }

    private int levelToSyslogLevel(final Level level) {
        final int syslogLevel;

        switch (level.toInt()) {
            case Level.FATAL_INT:
                return 2;
            case Level.ERROR_INT:
                return 3;
            case Level.WARN_INT:
                return 4;
            case Level.INFO_INT:
                return 6;
            default:
                return GelfMessage.DEFAUL_LEVEL;

        }
    }

    @Override
    public Values getValues(MessageField field) {
        if (field instanceof LogMessageField) {
            return new Values(field.getName(), getValue((LogMessageField) field));
        }

        if (field instanceof MdcMessageField) {
            return new Values(field.getName(), getValue((MdcMessageField) field));
        }

        if (field instanceof DynamicMdcMessageField) {
            return getMdcValues((DynamicMdcMessageField) field);
        }

        throw new UnsupportedOperationException("Cannot provide value for " + field);
    }

    public String getValue(LogMessageField field) {

        switch (field.getNamedLogField()) {
            case Severity:
                return loggingEvent.getLevel().toString();
            case ThreadName:
                return loggingEvent.getThreadName();
            case SourceClassName:
                return loggingEvent.getLocationInformation().getClassName();
            case SourceLineNumber:
                return loggingEvent.getLocationInformation().getLineNumber();
            case SourceMethodName:
                return loggingEvent.getLocationInformation().getMethodName();
            case SourceSimpleClassName:
                return GelfUtil.getSimpleClassName(loggingEvent.getLocationInformation().getClassName());
            case LoggerName:
                return loggingEvent.getLoggerName();
            case NDC:
                String ndc = NDC.get();
                if (ndc != null && !"".equals(ndc)) {
                    return ndc;
                }
                return null;
        }

        throw new UnsupportedOperationException("Cannot provide value for " + field);
    }

    private String getValue(MdcMessageField field) {

        return getMdcValue(field.getMdcName());
    }

    @Override
    public String getMdcValue(String mdcName) {
        Object value = MDC.get(mdcName);
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    private Values getMdcValues(DynamicMdcMessageField field) {
        Values result = new Values();

        Set<String> mdcNames = getAllMdcNames();
        Set<String> matchingMdcNames = getMatchingMdcNames(field, mdcNames);

        for (String mdcName : matchingMdcNames) {
            String mdcValue = getMdcValue(mdcName);
            if (mdcName != null) {
                result.setValue(mdcName, mdcValue);
            }
        }

        return result;
    }

    private Set<String> getAllMdcNames() {
        Set<String> mdcNames = new HashSet<String>();
        Map context = MDC.getContext();
        if (context != null) {
            mdcNames.addAll(context.keySet());
        }
        return mdcNames;
    }

    private Set<String> getMatchingMdcNames(DynamicMdcMessageField field, Set<String> mdcNames) {
        Set<String> matchingMdcNames = new HashSet<String>();

        for (String mdcName : mdcNames) {
            if (field.getPattern().matcher(mdcName).matches()) {
                matchingMdcNames.add(mdcName);
            }
        }
        return matchingMdcNames;
    }

    @Override
    public Set<String> getMdcNames() {
        return getAllMdcNames();
    }
}
