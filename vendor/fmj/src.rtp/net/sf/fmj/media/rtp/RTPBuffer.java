package net.sf.fmj.media.rtp;

import javax.media.Buffer;

/**
 * Buffer for incoming RTP packets
 * 
 * instance variables have default values in java, so classes are gettings smaller 
 *
 * @author mgodehardt
 * @version 1-1-alpha3
 */
public class RTPBuffer
{
    static private int MIN_SEQUENTIAL = 2;
    static private int MAX_MISORDER = 100;
    static private int MAX_DROPOUT = 3000;
    
    // for the jitter buffer ( using a double linked list )
    private RTPBuffer         firstItem;
    private RTPBuffer         lastItem;
    private int               itemCount;
    private int               maxItems;
    private long              duration;
    private long              lastSequenceNumber = -1;
    private long              firstSequenceNumber = -1;
    private RTPReceptionStats receptionStats;
    private int               probation = RTPBuffer.MIN_SEQUENTIAL;
    private long              received;
    private int               cycles;
    
    // for single data buffer ( wrapping a Buffer )
    private RTPBuffer previousBuffer;
    private RTPBuffer nextBuffer;
    private Buffer    buffer;

    // default ctor
    private RTPBuffer()
    {
        // disallow
    }

    // ctor for the jitter buffer
    public RTPBuffer(int maxItems, RTPReceptionStats receptionStats)
    {
        this.maxItems = maxItems;
        this.receptionStats = receptionStats;
    }

    // ctor for single data buffers
    public RTPBuffer(Buffer buffer)
    {
        this.buffer = buffer;
    }

    // how many items are in the jitter buffer
    synchronized public void resize(int maxItems)
    {
        this.maxItems = maxItems;
    }

    // remove all items
    synchronized public void clear() throws InterruptedException
    {
        while ( null != firstItem )
        {
            remove();
        }
    }

    // how many items do fit in the jitter buffer
    synchronized public int getMaxItems()
    {
        return maxItems;
    }

    // how many items are in the jitter buffer
    synchronized public int getItemCount()
    {
        return itemCount;
    }

    // returns duration in nanoseconds of the hole jitter buffer
    synchronized public long getDuration()
    {
        return duration;
    }
    
    synchronized public long getExtendedHighestSequenceNumber()
    {
        if ( lastSequenceNumber != -1 )
        {
            return (cycles << 16) + (lastSequenceNumber & 0xffff);
        }
        
        return -1;
    }
    
    synchronized public int getCumulativePacketLoss()
    {
        long extendedHighestSequenceNumber = getExtendedHighestSequenceNumber();
        if ( (extendedHighestSequenceNumber != -1) && (firstSequenceNumber != -1) )
        {
            long packetsExpected = extendedHighestSequenceNumber - firstSequenceNumber + 1;
            long cumulativePacketLoss = (packetsExpected - received);
            
            // clamp cumulativePacketLoss RFC 3550 Page 82
            if ( cumulativePacketLoss > 0x7fffff )
            {
                cumulativePacketLoss = 0x7fffff;
            }
            else if ( cumulativePacketLoss < -0x800000 )
            {
                cumulativePacketLoss = -0x800000;
            }
            
            return (int)cumulativePacketLoss;
        }
        
        return -1;
    }
    
    // add a item to the end of the jitter buffer
    synchronized public void addWait(Buffer buffer) throws InterruptedException
    {
        if ( null != firstItem )
        {
            wait();
        }
        
        firstItem = lastItem = new RTPBuffer(buffer);
        itemCount = 1;
        duration = buffer.getDuration();
        
        long seq = (buffer.getSequenceNumber() & 0xffff);
        
        if ( lastSequenceNumber != -1 )
        {
            long udelta = ((short)seq - (short)lastSequenceNumber) & 0xffff;
            
            if ( seq < lastSequenceNumber )
            {
                cycles++;
                if ( null != receptionStats )
                {
                    receptionStats.addSequenceWrap();
                }
            }
        }
        
        if ( -1 == firstSequenceNumber )
        {
            firstSequenceNumber = seq;
        }
        
        if ( null != receptionStats )
        {
            receptionStats.addPDUProcessed();
        }
        received++;
        
        lastSequenceNumber = seq;
        
        notifyAll();
    }

    // add a item to the end of the jitter buffer
    synchronized public Buffer removeWait() throws InterruptedException
    {
        if ( null == firstItem )
        {
            wait();
        }
        
        Buffer aBuffer = null;
        
        RTPBuffer aRTPBuffer = firstItem;
        
        firstItem = lastItem = null;
        itemCount = 0;
        duration = 0;
        
        aBuffer = aRTPBuffer.buffer;
        
        notifyAll();
        
        return aBuffer;
    }

    private void addImpl(Buffer buffer)
    {
        RTPBuffer aRTPBuffer = new RTPBuffer(buffer);
        
        if ( null == firstItem )
        {
            firstItem = lastItem = aRTPBuffer;
            
            itemCount = 1;
            duration = buffer.getDuration();
        }
        else
        {
            lastItem.nextBuffer = aRTPBuffer;
            aRTPBuffer.previousBuffer = lastItem;
            lastItem = aRTPBuffer;
            
            itemCount++;
            duration += buffer.getDuration();
        }
        
        if ( null != receptionStats )
        {
            receptionStats.addPDUProcessed();
        }
    }

    // add a item to the end of the jitter buffer
    synchronized public boolean add(Buffer buffer)
    {
        boolean fBufferOverrun = false;
        
        long seq = (buffer.getSequenceNumber() & 0xffff);
        
        if ( -1 == lastSequenceNumber )
        {
            lastSequenceNumber = seq;
        }
        
        if ( probation > 0 ) // source is not valid until MIN_SEQUENTIAL packets have arrived (RFC 3550 Page 80)
        {
            if ( seq == ((lastSequenceNumber + 1) & 0xffff) )
            {
                probation--;
                if ( 0 == probation )
                {
                    firstSequenceNumber = seq;
                    addImpl(buffer);
                    received++;
                }
            }
            else
            {
                probation = RTPBuffer.MIN_SEQUENTIAL - 1;
            }
            lastSequenceNumber = seq;
        }
        else 
        {
            long udelta = ((short)seq - (short)lastSequenceNumber) & 0xffff;
            
            if ( udelta < MAX_DROPOUT )
            {
                if ( seq < lastSequenceNumber )
                {
                    cycles++;
                    if ( null != receptionStats )
                    {
                        receptionStats.addSequenceWrap();
                    }
                }
                addImpl(buffer);
                received++;
                lastSequenceNumber = seq;
            }
            else
            {
                ///System.out.println("### RTPBuffer DUPLICATE OR MISORDERED PACKET seq=" + seq + " last=" + lastSequenceNumber);
                
                // TODO: handle duplicate or misordered packets
                received++;
            }
        }
        
        // buffer overrun
        while ( itemCount > maxItems )
        {
            ///System.out.println("### RTPBuffer BUFFER OVERRUN " + itemCount);
            
            try
            {
                remove();
                fBufferOverrun = true;
            }
            catch ( Exception dontcare )
            {
            }
        }
        
        notifyAll();
        
        return fBufferOverrun;
    }

    // remove first item from the jitter buffer ( may block )
    synchronized public Buffer remove() throws InterruptedException
    {
        if ( null == firstItem )
        {
            wait();
        }
        
        RTPBuffer aRTPBuffer = firstItem;
        if ( null != aRTPBuffer )
        {
            if ( lastItem == firstItem )
            {
                lastItem = null;
            }
            
            firstItem = aRTPBuffer.nextBuffer;
            if ( null != firstItem )
            {
                firstItem.previousBuffer = null;
            }
            
            Buffer aBuffer = aRTPBuffer.buffer;
            
            aRTPBuffer.nextBuffer = null;
            aRTPBuffer.previousBuffer = null;
            aRTPBuffer.buffer = null;
            
            itemCount--;
            duration -= aBuffer.getDuration();
            
            return aBuffer;
        }
        
        return null;
    }
}
