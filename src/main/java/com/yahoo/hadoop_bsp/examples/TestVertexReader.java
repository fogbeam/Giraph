package com.yahoo.hadoop_bsp.examples;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.log4j.Logger;

import com.yahoo.hadoop_bsp.BspInputSplit;
import com.yahoo.hadoop_bsp.VertexReader;
import com.yahoo.hadoop_bsp.MutableVertex;

/**
 * Used by TestVertexInputFormat to read some generated data
 * @author aching
 *
 * @param <I>
 * @param <V>
 * @param <E>
 */
public class TestVertexReader implements
    VertexReader<LongWritable, IntWritable, FloatWritable> {
    /** Logger */
    private static final Logger LOG = Logger.getLogger(TestVertexReader.class);
    /** Records read so far */
    long m_recordsRead = 0;
    /** Total records to read */
    long m_totalRecords = 0;
    /** The input split from initialize(). */
    BspInputSplit m_inputSplit = null;

    public static final String READER_VERTICES =
        "TestVertexReader.reader_vertices";
    public static final long DEFAULT_READER_VERTICES = 10;

    public void initialize(
        InputSplit inputSplit, TaskAttemptContext context)
        throws IOException {
        Configuration configuration = context.getConfiguration();
            m_totalRecords = configuration.getLong(
                TestVertexReader.READER_VERTICES,
                TestVertexReader.DEFAULT_READER_VERTICES);
            m_inputSplit = (BspInputSplit) inputSplit;
    }

    public boolean next(
        MutableVertex<LongWritable, IntWritable, FloatWritable, ?> vertex)
        throws IOException {
        if (m_totalRecords <= m_recordsRead) {
            return false;
        }
        vertex.setVertexId(new LongWritable(
            (m_inputSplit.getNumSplits() * m_totalRecords) + m_recordsRead));
        vertex.setVertexValue(
            new IntWritable(((int) (vertex.getVertexId().get() * 10))));
        // Adds an edge to the neighbor vertex
        vertex.addEdge(
            new LongWritable((vertex.getVertexId().get() + 1) % m_totalRecords),
            new FloatWritable((float) vertex.getVertexId().get() * 100));
        ++m_recordsRead;
        LOG.info("next: Return vertexId=" + vertex.getVertexId().get() +
            ", vertexValue=" + vertex.getVertexValue() + ", destinationId=" +
            (vertex.getVertexId().get() + 1) % m_totalRecords +
            ", edgeValue=" + ((float) vertex.getVertexId().get() * 100));
        return true;
    }

    public long getPos() throws IOException {
        return m_recordsRead;
    }

    public void close() throws IOException {
    }

    public float getProgress() throws IOException {
        return m_recordsRead * 100.0f / m_totalRecords;
    }

    public LongWritable createVertexId() {
        return new LongWritable(-1);
    }

    public IntWritable createVertexValue() {
        return new IntWritable(-1);
    }

    public FloatWritable createEdgeValue() {
        return new FloatWritable(0.0f);
    }
}
