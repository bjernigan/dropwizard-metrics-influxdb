package com.izettle.metrics.influxdb;

import com.izettle.metrics.influxdb.data.InfluxDbPoint;
import com.izettle.metrics.influxdb.data.InfluxDbWriteObject;
import com.izettle.metrics.influxdb.utils.InfluxDbWriteObjectSerializer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A base implementation of InfluxDbSender
 */
abstract class InfluxDbBaseSender implements InfluxDbSender {
    static final Charset UTF_8 = StandardCharsets.UTF_8;
    private final InfluxDbWriteObject influxDbWriteObject;
    private final InfluxDbWriteObjectSerializer influxDbWriteObjectSerializer;
    private final boolean groupedFields;

    InfluxDbBaseSender(final String database, final TimeUnit timePrecision, final String measurementPrefix) {
        this.influxDbWriteObject = new InfluxDbWriteObject(database, timePrecision);
        this.influxDbWriteObjectSerializer = new InfluxDbWriteObjectSerializer(measurementPrefix);
        this.groupedFields = false;
    }

    InfluxDbBaseSender(final String database, final TimeUnit timePrecision, final String measurementPrefix, final boolean groupedFields) {
        this.influxDbWriteObject = new InfluxDbWriteObject(database, timePrecision);
        this.influxDbWriteObjectSerializer = new InfluxDbWriteObjectSerializer(measurementPrefix);
        this.groupedFields = groupedFields;
    }

    @Override
    public void flush() {
        influxDbWriteObject.setPoints(new HashSet<InfluxDbPoint>());
    }

    @Override
    public boolean hasSeriesData() {
        return influxDbWriteObject.getPoints() != null && !influxDbWriteObject.getPoints().isEmpty();
    }

    @Override
    public void appendPoints(InfluxDbPoint point) {
        if (point != null) {
            influxDbWriteObject.getPoints().add(point);
        }
    }

    @Override
    public int writeData() throws Exception {
        String linestr = this.groupedFields
            ? influxDbWriteObjectSerializer.getGroupedLineProtocolString(influxDbWriteObject)
            : influxDbWriteObjectSerializer.getLineProtocolString(influxDbWriteObject);
        final byte[] line = linestr.getBytes(UTF_8);

        return writeData(line);
    }

    protected abstract int writeData(byte[] line) throws Exception;

    @Override
    public void setTags(Map<String, String> tags) {
        if (tags != null) {
            influxDbWriteObject.setTags(tags);
        }
    }

    @Override
    public Map<String, String> getTags() {
        return influxDbWriteObject.getTags();
    }
}
