package com.farlo.mjpeg_test;

import java.io.IOException;
import java.io.OutputStream;

final class MemoryOutputStream extends OutputStream
{
    private final byte[] mBuffer;
    private int mLength = 0;

    MemoryOutputStream(final int size)
    {
        this(new byte[size]);
    }

    MemoryOutputStream(final byte[] buffer)
    {
        super();
        mBuffer = buffer;
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int count)
            throws IOException
    {
        checkSpace(count);
        System.arraycopy(buffer, offset, mBuffer, mLength, count);
        mLength += count;
    }

    @Override
    public void write(final byte[] buffer) throws IOException
    {
        checkSpace(buffer.length);
        System.arraycopy(buffer, 0, mBuffer, mLength, buffer.length);
        mLength += buffer.length;
    }

    @Override
    public void write(final int oneByte) throws IOException
    {
        checkSpace(1);
        mBuffer[mLength++] = (byte) oneByte;
    }

    private void checkSpace(final int length) throws IOException
    {
        if (mLength + length >= mBuffer.length)
        {
            throw new IOException("insufficient space in buffer");
        }
    }

    void seek(final int index)
    {
        mLength = index;
    }

    byte[] getBuffer()
    {
        return mBuffer;
    }

    int getLength()
    {
        return mLength;
    }
}

